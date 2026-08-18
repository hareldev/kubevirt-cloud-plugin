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
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.server.mock.KubernetesMockServer;
import io.jenkins.plugins.kubevirt.KubeVirtConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.util.Map;

import static io.jenkins.plugins.kubevirt.internal.KubeVirtOwnershipTestSupport.CLOUD_NAME;
import static io.jenkins.plugins.kubevirt.internal.KubeVirtOwnershipTestSupport.NAMESPACE;
import static io.jenkins.plugins.kubevirt.internal.KubeVirtOwnershipTestSupport.TEMPLATE_NAME;
import static io.jenkins.plugins.kubevirt.internal.KubeVirtOwnershipTestSupport.VM_NAME;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link KubeVirt#deleteVM(String, String, String)} ownership validation.
 */
@WithJenkins
class KubeVirtDeleteVMTest {

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
    void deleteVM_matchingOwnership_deletesVm() {
        GenericKubernetesResource vm = KubeVirtOwnershipTestSupport.virtualMachine(
                VM_NAME, KubeVirtOwnershipTestSupport.matchingOwnershipLabels(jenkins.jenkins));
        KubeVirtOwnershipTestSupport.expectVmGet(server, VM_NAME, vm);
        KubeVirtOwnershipTestSupport.expectSuccessfulDelete(server, VM_NAME);

        try (KubeVirt virt = KubeVirtOwnershipTestSupport.createKubeVirt(server)) {
            assertDoesNotThrow(() -> virt.deleteVM(VM_NAME, CLOUD_NAME, TEMPLATE_NAME));
        }
    }

    @Test
    void deleteVM_wrongController_throws() {
        Map<String, String> labels = KubeVirtOwnershipTestSupport.matchingOwnershipLabels(jenkins.jenkins);
        labels.put(KubeVirtConfiguration.CONTROLLER_LABEL_KEY, "other-controller");
        KubeVirtOwnershipTestSupport.expectVmGet(server, VM_NAME,
                KubeVirtOwnershipTestSupport.virtualMachine(VM_NAME, labels));

        try (KubeVirt virt = KubeVirtOwnershipTestSupport.createKubeVirt(server)) {
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> virt.deleteVM(VM_NAME, CLOUD_NAME, TEMPLATE_NAME));
            assertTrue(ex.getMessage().contains("different"));
            assertTrue(ex.getMessage().contains("controller"));
        }
    }

    @Test
    void deleteVM_wrongCloud_throws() {
        Map<String, String> labels = KubeVirtOwnershipTestSupport.matchingOwnershipLabels(jenkins.jenkins);
        labels.put(KubeVirtConfiguration.CLOUD_LABEL_KEY, "other-cloud");
        KubeVirtOwnershipTestSupport.expectVmGet(server, VM_NAME,
                KubeVirtOwnershipTestSupport.virtualMachine(VM_NAME, labels));

        try (KubeVirt virt = KubeVirtOwnershipTestSupport.createKubeVirt(server)) {
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> virt.deleteVM(VM_NAME, CLOUD_NAME, TEMPLATE_NAME));
            assertTrue(ex.getMessage().contains("different"));
            assertTrue(ex.getMessage().contains("cloud"));
        }
    }

    @Test
    void deleteVM_wrongTemplate_throws() {
        Map<String, String> labels = KubeVirtOwnershipTestSupport.matchingOwnershipLabels(jenkins.jenkins);
        labels.put(KubeVirtConfiguration.TEMPLATE_LABEL_KEY, "other-template");
        KubeVirtOwnershipTestSupport.expectVmGet(server, VM_NAME,
                KubeVirtOwnershipTestSupport.virtualMachine(VM_NAME, labels));

        try (KubeVirt virt = KubeVirtOwnershipTestSupport.createKubeVirt(server)) {
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> virt.deleteVM(VM_NAME, CLOUD_NAME, TEMPLATE_NAME));
            assertTrue(ex.getMessage().contains("different"));
            assertTrue(ex.getMessage().contains("template"));
        }
    }

    @Test
    void deleteVM_notFound_throws() {
        server.expect()
                .get()
                .withPath(KubeVirtOwnershipTestSupport.vmPath(VM_NAME))
                .andReturn(404, "{}")
                .once();

        try (KubeVirt virt = KubeVirtOwnershipTestSupport.createKubeVirt(server)) {
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> virt.deleteVM(VM_NAME, CLOUD_NAME, TEMPLATE_NAME));
            assertTrue(ex.getMessage().contains("not found"));
            assertTrue(ex.getMessage().contains(NAMESPACE));
        }
    }

    @Test
    void deleteVM_unlabeledVm_throws() {
        GenericKubernetesResource vm = new GenericKubernetesResource();
        vm.setApiVersion(KubeVirtConfiguration.KUBEVIRT_API_GROUP + "/"
                + KubeVirtConfiguration.KUBEVIRT_API_VERSION);
        vm.setKind("VirtualMachine");
        ObjectMeta metadata = new ObjectMeta();
        metadata.setName(VM_NAME);
        metadata.setNamespace(NAMESPACE);
        metadata.setUid("vm-uid-123");
        vm.setMetadata(metadata);

        KubeVirtOwnershipTestSupport.expectVmGet(server, VM_NAME, vm);

        try (KubeVirt virt = KubeVirtOwnershipTestSupport.createKubeVirt(server)) {
            assertThrows(IllegalStateException.class,
                    () -> virt.deleteVM(VM_NAME, CLOUD_NAME, TEMPLATE_NAME));
        }
    }
}
