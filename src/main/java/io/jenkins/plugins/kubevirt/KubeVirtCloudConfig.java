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

import java.util.Objects;

/**
 * Immutable value object containing KubeVirt cloud configuration.
 * Used to pass cloud configuration to services without exposing the full KubeVirtCloud object.
 */
public final class KubeVirtCloudConfig {

    private final String cloudName;
    private final String serverUrl;
    private final String credentialsId;
    private final String namespace;
    private final boolean ignoreSsl;

    /**
     * Creates a new cloud configuration.
     *
     * @param cloudName     The name of the cloud in Jenkins
     * @param serverUrl     The Kubernetes API server URL
     * @param credentialsId The ID of the credentials for authentication
     * @param namespace     The Kubernetes namespace for VMs
     * @param ignoreSsl     Whether to ignore SSL certificate errors
     */
    public KubeVirtCloudConfig(String cloudName, String serverUrl, String credentialsId,
                                String namespace, boolean ignoreSsl) {
        this.cloudName = Objects.requireNonNull(cloudName, "cloudName cannot be null");
        this.serverUrl = Objects.requireNonNull(serverUrl, "serverUrl cannot be null");
        this.credentialsId = Objects.requireNonNull(credentialsId, "credentialsId cannot be null");
        this.namespace = Objects.requireNonNull(namespace, "namespace cannot be null");
        this.ignoreSsl = ignoreSsl;
    }

    public String getCloudName() {
        return cloudName;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public String getNamespace() {
        return namespace;
    }

    public boolean isIgnoreSsl() {
        return ignoreSsl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KubeVirtCloudConfig that = (KubeVirtCloudConfig) o;
        return ignoreSsl == that.ignoreSsl &&
                Objects.equals(cloudName, that.cloudName) &&
                Objects.equals(serverUrl, that.serverUrl) &&
                Objects.equals(credentialsId, that.credentialsId) &&
                Objects.equals(namespace, that.namespace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cloudName, serverUrl, credentialsId, namespace, ignoreSsl);
    }

    @Override
    public String toString() {
        return "KubeVirtCloudConfig{" +
                "cloudName='" + cloudName + '\'' +
                ", serverUrl='" + serverUrl + '\'' +
                ", namespace='" + namespace + '\'' +
                ", ignoreSsl=" + ignoreSsl +
                '}';
    }
}
