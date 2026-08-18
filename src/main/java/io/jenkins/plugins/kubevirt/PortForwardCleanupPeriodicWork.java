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
import hudson.model.AsyncPeriodicWork;
import hudson.model.TaskListener;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Periodic background task that checks for and cleans up orphaned port-forward resources.
 *
 * <p>This provides an additional safety net for detecting port-forward resources
 * whose launcher was garbage collected without proper cleanup (e.g., due to
 * serialization during Jenkins restart, or abnormal connection termination).</p>
 *
 * <p>The task runs periodically (default: every 15 minutes) and checks if any
 * tracked port-forward resources have become orphaned (their launcher WeakReference
 * has been cleared by the garbage collector).</p>
 */
@Extension
public class PortForwardCleanupPeriodicWork extends AsyncPeriodicWork {

    private static final Logger LOGGER = Logger.getLogger(PortForwardCleanupPeriodicWork.class.getName());

    /**
     * How often to run the cleanup check (in milliseconds).
     * Default: 15 minutes
     */
    private static final long RECURRENCE_PERIOD_MS = TimeUnit.MINUTES.toMillis(15);

    public PortForwardCleanupPeriodicWork() {
        super("KubeVirt Port-Forward Cleanup");
    }

    @Override
    public long getRecurrencePeriod() {
        return RECURRENCE_PERIOD_MS;
    }

    @Override
    protected void execute(TaskListener listener) {
        int activeCount = PortForwardCleanupTracker.getActiveCount();

        if (activeCount == 0) {
            LOGGER.log(Level.FINE, "No active port-forward resources to check");
            return;
        }

        LOGGER.log(Level.FINE, "Checking {0} active port-forward resource(s) for orphans", activeCount);
        KubeVirtLog.log(listener.getLogger(), "Checking " + activeCount +
                " active port-forward resource(s) for orphans");

        int cleanedCount = PortForwardCleanupTracker.cleanupOrphaned();

        if (cleanedCount > 0) {
            KubeVirtLog.log(listener.getLogger(), "Cleaned up " + cleanedCount +
                    " orphaned port-forward resource(s)");
        } else {
            LOGGER.log(Level.FINE, "No orphaned port-forward resources found");
        }
    }
}
