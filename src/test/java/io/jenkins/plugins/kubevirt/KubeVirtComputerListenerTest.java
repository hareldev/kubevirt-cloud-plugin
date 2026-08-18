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

import hudson.model.TaskListener;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.SlaveComputer;
import org.jenkinsci.plugins.cloudstats.ProvisioningActivity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@WithJenkins
class KubeVirtComputerListenerTest {

    private static final String CLOUD_NAME = "kubevirt-test-cloud";

    private JenkinsRule jenkins;
    private KubeVirtTemplate template;

    @BeforeEach
    void setUp(JenkinsRule jenkins) throws Exception {
        this.jenkins = jenkins;
        template = newTemplate("5");
        KubeVirtCloud cloud = new KubeVirtCloud(
                CLOUD_NAME,
                "https://kubernetes.example",
                "creds",
                "default",
                true,
                "0",
                false,
                List.of(template));
        jenkins.jenkins.clouds.add(cloud);
    }

    @AfterEach
    void tearDown() {
        ProvisioningFailureTracker.clearAll();
    }

    @Test
    void handleProvisioningFailure_isIdempotentForTheSameLaunch() throws Exception {
        KubeVirtAgent agent = createAgent("vm-once", new KubeVirtProvisioningLauncher(CLOUD_NAME, template.getName()));

        KubeVirtComputerListener.handleProvisioningFailure(agent, TaskListener.NULL);
        KubeVirtComputerListener.handleProvisioningFailure(agent, TaskListener.NULL);

        assertEquals(1, ProvisioningFailureTracker.getFailureCount(template.getId()));
    }

    @Test
    void interruptIsNotCountedAsProvisioningFailure() throws Exception {
        KubeVirtAgent agent = createAgent("vm-interrupt", new KubeVirtProvisioningLauncher(CLOUD_NAME, template.getName()));

        assertTrue(agent.tryMarkProvisioningFailureHandled());
        KubeVirtComputerListener.handleProvisioningFailure(agent, TaskListener.NULL);

        assertEquals(0, ProvisioningFailureTracker.getFailureCount(template.getId()));
    }

    @Test
    void reconnectingAgentIsNotConsideredProvisioning() throws Exception {
        KubeVirtAgent reconnecting = createAgent("vm-reconnect", new NonProvisioningLauncher());
        jenkins.jenkins.addNode(reconnecting);

        assertFalse(((KubeVirtComputer) reconnecting.toComputer()).isProvisioning());
    }

    private KubeVirtAgent createAgent(String nodeName, ComputerLauncher launcher) throws Exception {
        return new KubeVirtAgent(
                nodeName,
                CLOUD_NAME,
                template.getName(),
                "/home/jenkins",
                launcher,
                template.getLabels(),
                null,
                10,
                new ProvisioningActivity.Id(CLOUD_NAME, template.getName(), nodeName));
    }

    private static KubeVirtTemplate newTemplate(String maxProvisionAttempts) {
        return new KubeVirtTemplate(
                null, "test-template", "linux",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "10", null, null, maxProvisionAttempts, false, null, null
        );
    }

    private static final class NonProvisioningLauncher extends ComputerLauncher {
        @Override
        public void launch(SlaveComputer computer, TaskListener listener) {
            // Reconnect path: launcher has already been swapped off provisioning.
        }
    }
}
