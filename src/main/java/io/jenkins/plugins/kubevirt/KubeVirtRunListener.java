/*
 * The MIT License
 *
 * Copyright (c) Red Hat, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package io.jenkins.plugins.kubevirt;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.Executor;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import jenkins.model.Jenkins;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Listens for builds on KubeVirt agents and handles:
 * - Writing VM provisioning log to build console output on start
 * - Triggering VM cleanup when builds complete (success, failure, or abort)
 * 
 * This ensures VMs are deleted after builds finish, preventing orphaned resources.
 */
@Extension
public class KubeVirtRunListener extends RunListener<Run<?, ?>> {
    
    private static final Logger LOGGER = Logger.getLogger(KubeVirtRunListener.class.getName());
    
    @Override
    public void onStarted(Run<?, ?> run, TaskListener listener) {
        try {
            // Get the executor running this build
            Executor executor = run.getExecutor();
            if (executor == null) {
                return;
            }
            
            // Get the computer (node) where this build is running
            Computer computer = executor.getOwner();
            if (!(computer instanceof KubeVirtComputer)) {
                return;
            }
            
            KubeVirtComputer kvc = (KubeVirtComputer) computer;
            KubeVirtAgent agent = kvc.getNode();
            
            if (agent == null) {
                return;
            }
            
            String provisioningInfo = agent.getProvisioningInfo();
            if (provisioningInfo == null || provisioningInfo.isEmpty()) {
                return;
            }
            
            // Write provisioning log to build console.
            // These lines intentionally use raw println() instead of KubeVirtLog.log()
            // because they draw a fixed-width decorative box. Prepending a timestamp
            // would break the column alignment of the box-drawing characters.
            listener.getLogger().println();
            listener.getLogger().println("╔════════════════════════════════════════════════════════════════════════════╗");
            listener.getLogger().println("║                    KubeVirt - VM Provisioning Log                          ║");
            listener.getLogger().println("╠════════════════════════════════════════════════════════════════════════════╣");
            listener.getLogger().println("║ VM Name: " + padRight(agent.getNodeName(), 66) + "║");
            listener.getLogger().println("║ Cloud: " + padRight(agent.getCloudName(), 68) + "║");
            listener.getLogger().println("╠════════════════════════════════════════════════════════════════════════════╣");
            
            for (String line : provisioningInfo.split("\n")) {
                if (!line.trim().isEmpty()) {
                    listener.getLogger().println("║ " + padRight(line, 75) + "║");
                }
            }
            
            listener.getLogger().println("╚════════════════════════════════════════════════════════════════════════════╝");
            listener.getLogger().println();
            
            LOGGER.log(Level.FINE, "Wrote provisioning log to build {0}", run.getFullDisplayName());
            
        } catch (Exception e) {
            // Don't let failures here affect the build
            LOGGER.log(Level.WARNING, "Failed to write provisioning log to build console", e);
        }
    }
    
    @Override
    public void onCompleted(Run<?, ?> run, TaskListener listener) {
        scheduleAgentTermination(run, listener, "completed");
    }
    
    @Override
    public void onFinalized(Run<?, ?> run) {
        // This is called after the build is fully done, including any post-build actions
        // Use this as a backup to ensure cleanup happens
        scheduleAgentTermination(run, TaskListener.NULL, "finalized");
    }
    
    /**
     * Handles agent termination after a build ends.
     * For single-use agents (idleMinutes = 0), the VM is deleted immediately.
     * For reusable agents (idleMinutes > 0), the CloudRetentionStrategy handles termination
     * after the idle timeout expires.
     */
    private void scheduleAgentTermination(Run<?, ?> run, TaskListener listener, String phase) {
        try {
            Executor executor = run.getExecutor();
            if (executor == null) {
                return;
            }
            
            Computer computer = executor.getOwner();
            if (!(computer instanceof KubeVirtComputer)) {
                return;
            }
            
            KubeVirtComputer kvc = (KubeVirtComputer) computer;
            KubeVirtAgent agent = kvc.getNode();
            
            if (agent == null) {
                return;
            }
            
            String nodeName = agent.getNodeName();
            Result result = run.getResult();
            String resultStr = result != null ? result.toString() : "UNKNOWN";
            int idleMinutes = agent.getIdleMinutes();
            boolean isSingleUse = agent.isSingleUse();
            
            LOGGER.log(Level.INFO, "Build {0} on agent {1}: result={2}, singleUse={3}, idleMinutes={4}", 
                    new Object[]{phase, nodeName, resultStr, isSingleUse, idleMinutes});
            
            if (listener != TaskListener.NULL) {
                listener.getLogger().println();
                KubeVirtLog.log(listener.getLogger(), "Build " + phase + 
                        " with result: " + resultStr);
                
                if (isSingleUse) {
                    KubeVirtLog.log(listener.getLogger(), "Single-use agent - VM '" + 
                            nodeName + "' will be terminated immediately.");
                } else {
                    KubeVirtLog.log(listener.getLogger(), "VM '" + nodeName + 
                            "' will remain available for " + idleMinutes + " minutes before termination.");
                }
            }
            
            // Only terminate immediately for single-use agents
            // For reusable agents, let CloudRetentionStrategy handle the idle timeout
            if (!isSingleUse) {
                LOGGER.log(Level.FINE, "Agent {0} is reusable (idleMinutes={1}), " +
                        "CloudRetentionStrategy will handle termination", 
                        new Object[]{nodeName, idleMinutes});
                return;
            }
            
            // Schedule async termination for single-use agents
            Computer.threadPoolForRemoting.submit(() -> {
                try {
                    // Small delay to ensure build log is flushed
                    Thread.sleep(2000);
                    
                    // Check if agent still exists
                    Jenkins jenkins = Jenkins.get();
                    KubeVirtAgent currentAgent = 
                            (KubeVirtAgent) jenkins.getNode(nodeName);
                    
                    if (currentAgent != null) {
                        LOGGER.log(Level.INFO, "Terminating single-use agent {0} and VM after build {1}", 
                                new Object[]{nodeName, phase});
                        // terminate() calls _terminate() which deletes the VM,
                        // then removes the node from Jenkins
                        currentAgent.terminate();
                        LOGGER.log(Level.INFO, "Agent {0} terminated and VM deleted", nodeName);
                    } else {
                        LOGGER.log(Level.FINE, "Agent {0} already removed", nodeName);
                    }
                } catch (IOException | InterruptedException e) {
                    LOGGER.log(Level.WARNING, "Error terminating agent " + nodeName, e);
                }
            });
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error scheduling agent termination", e);
        }
    }
    
    /**
     * Pads a string to the right with spaces to a specified length.
     */
    private String padRight(String s, int length) {
        if (s == null) {
            s = "";
        }
        if (s.length() >= length) {
            return s.substring(0, length);
        }
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < length) {
            sb.append(' ');
        }
        return sb.toString();
    }
}
