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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

class KubernetesClientExceptionsTest {

    private static final String K8S_RESOURCE_QUOTA_CONFLICT =
            "Operation cannot be fulfilled on resourcequotas \"compute-resources\": the object has been modified; please apply your changes to the latest version and try again";

    private static final String OPENSHIFT_CLUSTER_RESOURCE_QUOTA_CONFLICT =
            "Operation cannot be fulfilled on clusterresourcequotas.quota.openshift.io \"dno--notterminating\": the object has been modified; please apply your changes to the latest version and try again";

    @Test
    void isResourceQuotaUpdateConflict_kubernetesResourceQuota() {
        assertThat(KubernetesClientExceptions.isResourceQuotaUpdateConflict(409, K8S_RESOURCE_QUOTA_CONFLICT), is(true));
    }

    @Test
    void isResourceQuotaUpdateConflict_openshiftClusterResourceQuota() {
        assertThat(
                KubernetesClientExceptions.isResourceQuotaUpdateConflict(409, OPENSHIFT_CLUSTER_RESOURCE_QUOTA_CONFLICT),
                is(true));
    }

    @Test
    void isResourceQuotaUpdateConflict_not409() {
        assertThat(KubernetesClientExceptions.isResourceQuotaUpdateConflict(403, K8S_RESOURCE_QUOTA_CONFLICT), is(false));
    }

    @Test
    void isResourceQuotaUpdateConflict_nullMessage() {
        assertThat(KubernetesClientExceptions.isResourceQuotaUpdateConflict(409, null), is(false));
    }

    @Test
    void isResourceQuotaUpdateConflict_unrelated409() {
        assertThat(
                KubernetesClientExceptions.isResourceQuotaUpdateConflict(409, "virtualmachines.kubevirt.io \"my-vm\" already exists"),
                is(false));
    }
}
