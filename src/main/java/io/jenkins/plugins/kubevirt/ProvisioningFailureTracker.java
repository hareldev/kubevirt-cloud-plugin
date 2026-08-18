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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks consecutive VM provisioning failures per template.
 *
 * <p>When a launch fails, the counter increments. A successful agent connection
 * resets the counter. If consecutive failures exceed the template's retry
 * count ({@link KubeVirtTemplate#getMaxProvisionAttempts()}), waiting queue
 * items are cancelled so Jenkins does not provision VMs indefinitely.
 * {@code 0} fails after the first launch; {@code -1} disables the limit.</p>
 */
public final class ProvisioningFailureTracker {

    private static final ConcurrentHashMap<String, AtomicInteger> FAILURES = new ConcurrentHashMap<>();

    private ProvisioningFailureTracker() {
    }

    /**
     * Records a provisioning failure and returns the new failure count.
     *
     * @param templateId the {@link KubeVirtTemplate#getId()} of the failed template
     * @return the updated consecutive failure count, or {@code 0} if {@code templateId} is blank
     */
    public static int recordFailure(String templateId) {
        if (templateId == null || templateId.isBlank()) {
            return 0;
        }
        return FAILURES.computeIfAbsent(templateId, ignored -> new AtomicInteger(0))
                .incrementAndGet();
    }

    /**
     * Returns the current failure count without modifying it.
     */
    public static int getFailureCount(String templateId) {
        if (templateId == null || templateId.isBlank()) {
            return 0;
        }
        AtomicInteger count = FAILURES.get(templateId);
        return count == null ? 0 : count.get();
    }

    /**
     * Clears the failure counter after a successful launch or after giving up.
     */
    public static void reset(String templateId) {
        if (templateId == null || templateId.isBlank()) {
            return;
        }
        FAILURES.remove(templateId);
    }

    /**
     * Clears all counters. Intended for tests.
     */
    static void clearAll() {
        FAILURES.clear();
    }
}
