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
import io.fabric8.kubernetes.api.model.GenericKubernetesResourceBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.server.mock.KubernetesMockServer;
import io.jenkins.plugins.kubevirt.KubeVirtConfiguration;
import jenkins.model.Jenkins;
import jenkins.model.JenkinsLocationConfiguration;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * Shared helpers for KubeVirt ownership validation tests.
 */
public final class KubeVirtOwnershipTestSupport {

    public static final String NAMESPACE = "test-ns";
    public static final String CLOUD_NAME = "my-cloud";
    public static final String TEMPLATE_NAME = "fedora-agent";
    public static final String VM_NAME = "test-vm";
    public static final String JENKINS_ROOT_URL = "http://jenkins.test/";
    public static final String TOKEN = "test-token";

    public static final CustomResourceDefinitionContext VM_CONTEXT = new CustomResourceDefinitionContext.Builder()
            .withGroup(KubeVirtConfiguration.KUBEVIRT_API_GROUP)
            .withVersion(KubeVirtConfiguration.KUBEVIRT_API_VERSION)
            .withPlural("virtualmachines")
            .withScope("Namespaced")
            .build();

    private KubeVirtOwnershipTestSupport() {
    }

    public static KubernetesMockServer startMockServer() {
        KubernetesMockServer server = new KubernetesMockServer(false);
        server.init();
        server.expectCustomResource(VM_CONTEXT);
        return server;
    }

    public static void stopMockServer(KubernetesMockServer server) {
        if (server != null) {
            server.destroy();
        }
    }

    public static KubeVirt createKubeVirt(KubernetesMockServer server) {
        String masterUrl = server.createClient().getConfiguration().getMasterUrl();
        return new KubeVirt(masterUrl, TOKEN, NAMESPACE, true);
    }

    public static String vmPath(String vmName) {
        return "/apis/kubevirt.io/v1/namespaces/" + NAMESPACE + "/virtualmachines/" + vmName;
    }

    public static String cloudInitSecretPath(String vmName) {
        return "/api/v1/namespaces/" + NAMESPACE + "/secrets/" + vmName + "-cloudinit";
    }

    public static String controllerLabel(Jenkins jenkins) {
        return KubeVirt.sanitizeLabelValue(jenkins.getRootUrl());
    }

    public static Map<String, String> matchingOwnershipLabels(Jenkins jenkins) {
        Map<String, String> labels = new HashMap<>();
        labels.put(KubeVirtConfiguration.CONTROLLER_LABEL_KEY, controllerLabel(jenkins));
        labels.put(KubeVirtConfiguration.CLOUD_LABEL_KEY, KubeVirt.sanitizeLabelValue(CLOUD_NAME));
        labels.put(KubeVirtConfiguration.TEMPLATE_LABEL_KEY, KubeVirt.sanitizeLabelValue(TEMPLATE_NAME));
        return labels;
    }

    public static GenericKubernetesResource virtualMachine(String vmName, Map<String, String> labels) {
        return new GenericKubernetesResourceBuilder()
                .withApiVersion(KubeVirtConfiguration.KUBEVIRT_API_GROUP + "/"
                        + KubeVirtConfiguration.KUBEVIRT_API_VERSION)
                .withKind("VirtualMachine")
                .withNewMetadata()
                    .withName(vmName)
                    .withNamespace(NAMESPACE)
                    .withLabels(labels)
                    .withUid("vm-uid-123")
                .endMetadata()
                .build();
    }

    public static void expectVmGet(KubernetesMockServer server, String vmName, GenericKubernetesResource vm) {
        server.expect()
                .get()
                .withPath(vmPath(vmName))
                .andReturn(HttpURLConnection.HTTP_OK, vm)
                .always();
    }

    public static void expectCloudInitSecretApply(KubernetesMockServer server, String vmName) {
        Secret secret = new SecretBuilder()
                .withNewMetadata()
                    .withName(vmName + "-cloudinit")
                    .withNamespace(NAMESPACE)
                .endMetadata()
                .build();
        server.expect()
                .patch()
                .withPath(cloudInitSecretPath(vmName) + "?fieldManager=fabric8")
                .andReturn(HttpURLConnection.HTTP_OK, secret)
                .once();
    }

    public static void expectVmCreateConflict(KubernetesMockServer server) {
        server.expect()
                .post()
                .withPath("/apis/kubevirt.io/v1/namespaces/" + NAMESPACE + "/virtualmachines")
                .andReturn(HttpURLConnection.HTTP_CONFLICT,
                        new io.fabric8.kubernetes.api.model.StatusBuilder()
                                .withCode(409)
                                .withMessage("Already exists")
                                .build())
                .once();
    }

    public static void expectSuccessfulDelete(KubernetesMockServer server, String vmName) {
        server.expect()
                .delete()
                .withPath(cloudInitSecretPath(vmName))
                .andReturn(HttpURLConnection.HTTP_OK, "{}")
                .once();
        server.expect()
                .delete()
                .withPath(vmPath(vmName))
                .andReturn(HttpURLConnection.HTTP_OK, "{}")
                .once();
    }

    public static void configureJenkinsRootUrl(Jenkins jenkins) {
        JenkinsLocationConfiguration.get().setUrl(JENKINS_ROOT_URL);
    }
}
