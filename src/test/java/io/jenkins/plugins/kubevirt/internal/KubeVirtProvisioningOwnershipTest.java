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

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.client.server.mock.KubernetesMockServer;
import io.jenkins.plugins.kubevirt.KubeVirtConfiguration;
import io.jenkins.plugins.kubevirt.ProvisioningCallback;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.util.Map;

import static io.jenkins.plugins.kubevirt.internal.KubeVirtOwnershipTestSupport.CLOUD_NAME;
import static io.jenkins.plugins.kubevirt.internal.KubeVirtOwnershipTestSupport.TEMPLATE_NAME;
import static io.jenkins.plugins.kubevirt.internal.KubeVirtOwnershipTestSupport.VM_NAME;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests ownership validation when provisioning reuses an existing VM (HTTP 409).
 */
@WithJenkins
class KubeVirtProvisioningOwnershipTest {

    private JenkinsRule jenkins;
    private KubernetesMockServer server;

    @BeforeEach
    void setUp(JenkinsRule jenkins) {
        this.jenkins = jenkins;
        server = KubeVirtOwnershipTestSupport.startMockServer();
        KubeVirtOwnershipTestSupport.configureJenkinsRootUrl(jenkins.jenkins);
    }

    @AfterEach
    void tearDown() {
        KubeVirtOwnershipTestSupport.stopMockServer(server);
    }

    @Test
    void createVM_409_existingVmWrongController_throws() {
        Map<String, String> labels = KubeVirtOwnershipTestSupport.matchingOwnershipLabels(jenkins.jenkins);
        labels.put(KubeVirtConfiguration.CONTROLLER_LABEL_KEY, "other-controller");
        GenericKubernetesResource existingVm = KubeVirtOwnershipTestSupport.virtualMachine(VM_NAME, labels);
        KubeVirtOwnershipTestSupport.expectVmGet(server, VM_NAME, existingVm);
        KubeVirtOwnershipTestSupport.expectCloudInitSecretApply(server, VM_NAME);
        KubeVirtOwnershipTestSupport.expectVmCreateConflict(server);

        try (KubeVirt virt = KubeVirtOwnershipTestSupport.createKubeVirt(server)) {
            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    virt.createVM(
                            VM_NAME,
                            CLOUD_NAME,
                            TEMPLATE_NAME,
                            "quay.io/containerdisks/fedora:40",
                            "30Gi",
                            "ReadWriteOnce",
                            "1",
                            "2Gi",
                            null,
                            null,
                            "#cloud-config\n",
                            false,
                            ProvisioningCallback.NOOP));
            assertTrue(ex.getMessage().contains("different"));
            assertTrue(ex.getMessage().contains("controller"));
        }
    }
}
