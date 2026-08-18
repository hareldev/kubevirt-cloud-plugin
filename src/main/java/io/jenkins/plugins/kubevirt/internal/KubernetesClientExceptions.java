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

package io.jenkins.plugins.kubevirt.internal;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * Helpers for interpreting {@link io.fabric8.kubernetes.client.KubernetesClientException} responses.
 */
public final class KubernetesClientExceptions {

    private KubernetesClientExceptions() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Detects transient quota update conflicts during resource creation.
     * These occur when concurrent creations race to update ResourceQuota or OpenShift ClusterResourceQuota.
     *
     * @see <a href="https://github.com/jenkinsci/kubernetes-plugin/issues/2843">kubernetes-plugin#2843</a>
     * @see <a href="https://github.com/kubernetes/kubernetes/issues/67761">kubernetes#67761</a>
     */
    public static boolean isResourceQuotaUpdateConflict(int httpCode, @CheckForNull String message) {
        return httpCode == 409
                && message != null
                && message.contains("Operation cannot be fulfilled on")
                && message.contains("resourcequotas");
    }
}
