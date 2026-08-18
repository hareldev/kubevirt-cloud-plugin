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

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.client.server.mock.KubernetesMockServer;
import io.jenkins.plugins.kubevirt.exception.VMDeletionException;
import io.jenkins.plugins.kubevirt.internal.KubeVirtOwnershipTestSupport;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import hudson.util.Secret;

import java.util.Collections;
import java.util.Map;

import static io.jenkins.plugins.kubevirt.internal.KubeVirtOwnershipTestSupport.CLOUD_NAME;
import static io.jenkins.plugins.kubevirt.internal.KubeVirtOwnershipTestSupport.TEMPLATE_NAME;
import static io.jenkins.plugins.kubevirt.internal.KubeVirtOwnershipTestSupport.TOKEN;
import static io.jenkins.plugins.kubevirt.internal.KubeVirtOwnershipTestSupport.VM_NAME;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link KubeVirtCloud#deleteVM(String, String)}.
 */
@WithJenkins
class KubeVirtCloudDeleteVMTest {

    private static final String CREDENTIALS_ID = "kube-creds";

    private JenkinsRule jenkins;
    private KubernetesMockServer server;
    private KubeVirtCloud cloud;

    @BeforeEach
    void setUp(JenkinsRule jenkins) throws Exception {
        this.jenkins = jenkins;
        server = KubeVirtOwnershipTestSupport.startMockServer();
        KubeVirtOwnershipTestSupport.configureJenkinsRootUrl(jenkins.jenkins);

        StringCredentialsImpl credentials = new StringCredentialsImpl(
                CredentialsScope.GLOBAL, CREDENTIALS_ID, "test", Secret.fromString(TOKEN));
        CredentialsProvider.lookupStores(jenkins.jenkins).iterator().next()
                .addCredentials(Domain.global(), credentials);

        String masterUrl = server.createClient().getConfiguration().getMasterUrl();
        cloud = new KubeVirtCloud(
                CLOUD_NAME,
                masterUrl,
                CREDENTIALS_ID,
                KubeVirtOwnershipTestSupport.NAMESPACE,
                true,
                "0",
                true,
                Collections.emptyList());
    }

    @AfterEach
    void tearDown() {
        KubeVirtOwnershipTestSupport.stopMockServer(server);
    }

    @Test
    void deleteVM_ownershipFailure_wrapsAsVMDeletionException() {
        Map<String, String> labels = KubeVirtOwnershipTestSupport.matchingOwnershipLabels(jenkins.jenkins);
        labels.put(KubeVirtConfiguration.CLOUD_LABEL_KEY, "other-cloud");
        GenericKubernetesResource vm = KubeVirtOwnershipTestSupport.virtualMachine(VM_NAME, labels);
        KubeVirtOwnershipTestSupport.expectVmGet(server, VM_NAME, vm);

        VMDeletionException ex = assertThrows(VMDeletionException.class,
                () -> cloud.deleteVM(VM_NAME, TEMPLATE_NAME));
        assertTrue(ex.getMessage().contains("Cannot delete VM"));
        assertTrue(ex.getCause() instanceof IllegalStateException);
    }

    @Test
    void deleteVM_matchingOwnership_deletesVm() {
        GenericKubernetesResource vm = KubeVirtOwnershipTestSupport.virtualMachine(
                VM_NAME, KubeVirtOwnershipTestSupport.matchingOwnershipLabels(jenkins.jenkins));
        KubeVirtOwnershipTestSupport.expectVmGet(server, VM_NAME, vm);
        KubeVirtOwnershipTestSupport.expectSuccessfulDelete(server, VM_NAME);

        assertDoesNotThrow(() -> cloud.deleteVM(VM_NAME, TEMPLATE_NAME));
    }
}
