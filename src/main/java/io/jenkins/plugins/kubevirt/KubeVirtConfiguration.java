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

/**
 * Central configuration class containing all constants for the KubeVirt plugin.
 * This class provides named constants for timeouts, defaults, and magic numbers
 * to improve code maintainability and readability.
 */
public final class KubeVirtConfiguration {

    // Prevent instantiation
    private KubeVirtConfiguration() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // ==================== Kubernetes API Constants ====================

    /** KubeVirt API group for VirtualMachine and VirtualMachineInstance resources */
    public static final String KUBEVIRT_API_GROUP = "kubevirt.io";

    /** KubeVirt API version */
    public static final String KUBEVIRT_API_VERSION = "v1";

    /** CDI (Containerized Data Importer) API group for DataVolume resources */
    public static final String CDI_API_GROUP = "cdi.kubevirt.io";

    /** CDI API version */
    public static final String CDI_API_VERSION = "v1beta1";

    /** Label key for identifying the Jenkins controller that created the VM */
    public static final String CONTROLLER_LABEL_KEY = "kubevirt.jenkins.io/controller";

    /** Label key for identifying the cloud name that created the VM */
    public static final String CLOUD_LABEL_KEY = "kubevirt.jenkins.io/cloud";

    /** Label key for identifying the template that created the VM */
    public static final String TEMPLATE_LABEL_KEY = "kubevirt.jenkins.io/template";

    /** Maximum length for Kubernetes label values (RFC 1123) */
    public static final int MAX_LABEL_VALUE_LENGTH = 63;

    // ==================== VM Provisioning Timeouts ====================

    /** Default VM provisioning timeout in minutes */
    public static final int DEFAULT_PROVISIONING_TIMEOUT_MINUTES = 15;

    /**
     * Sentinel for unlimited VM provisioning retries
     * ({@code maxProvisionAttempts = -1}).
     */
    public static final int UNLIMITED_PROVISION_RETRIES = -1;

    /**
     * Default number of extra VM provisioning tries after the first failure.
     * {@code 0} means fail after the first failure; {@link #UNLIMITED_PROVISION_RETRIES}
     * disables the limit.
     */
    public static final int DEFAULT_MAX_PROVISION_ATTEMPTS = 1;

    /** Interval between VM status checks during provisioning (5 seconds) */
    public static final int VM_PROVISIONING_RETRY_INTERVAL_SECONDS = 5;

    /**
     * Maximum attempts when resource creation hits transient ResourceQuota or
     * OpenShift ClusterResourceQuota update conflicts (HTTP 409).
     *
     * @see <a href="https://github.com/jenkinsci/kubernetes-plugin/issues/2843">kubernetes-plugin#2843</a>
     */
    public static final int RESOURCE_QUOTA_CONFLICT_MAX_ATTEMPTS = 60;

    // ==================== Cloud-Init and SSH Readiness Timeouts ====================

    /** Default maximum wait time for cloud-init to complete SSH setup (3 minutes) */
    public static final int DEFAULT_CLOUD_INIT_WAIT_SECONDS = 180;

    /** Interval between SSH readiness checks (10 seconds) */
    public static final int SSH_READINESS_CHECK_INTERVAL_SECONDS = 10;

    /** Socket timeout for SSH port checks (5 seconds) */
    public static final int SSH_SOCKET_TIMEOUT_MS = 5000;

    /** Buffer size for reading SSH banner */
    public static final int SSH_BANNER_BUFFER_SIZE = 256;

    // ==================== SSH Connection Configuration ====================

    /** Default SSH port */
    public static final int DEFAULT_SSH_PORT = 22;

    /** Timeout for SSH launch attempts via port-forward (3 minutes per attempt) */
    public static final int SSH_LAUNCH_TIMEOUT_SECONDS_PORTFORWARD = 180;

    /** Maximum number of SSH connection retries via port-forward */
    public static final int SSH_MAX_RETRIES_PORTFORWARD = 10;

    /** Wait time between SSH connection retries via port-forward (15 seconds) */
    public static final int SSH_RETRY_WAIT_TIME_PORTFORWARD = 15;

    /** Timeout for SSH launch attempts via direct connection (1 minute per attempt) */
    public static final int SSH_LAUNCH_TIMEOUT_SECONDS_DIRECT = 60;

    /** Maximum number of SSH connection retries via direct connection */
    public static final int SSH_MAX_RETRIES_DIRECT = 3;

    /** Wait time between SSH connection retries via direct connection (15 seconds) */
    public static final int SSH_RETRY_WAIT_TIME_DIRECT = 15;

    // ==================== SSH Authentication Retry ====================
    // Cloud-init may still be injecting authorized_keys when sshd is already running.
    // SSHLauncher treats auth failure as permanent, so we add our own retry layer.

    /** Maximum number of SSH authentication retries (cloud-init may delay key injection) */
    public static final int SSH_AUTH_MAX_RETRIES = 5;

    /** Wait time between SSH authentication retries (seconds) */
    public static final int SSH_AUTH_RETRY_INTERVAL_SECONDS = 15;

    // ==================== VM Resource Defaults ====================

    /** Default CPU allocation (Kubernetes CPU units) */
    public static final String DEFAULT_CPU = "1";

    /** Default memory allocation */
    public static final String DEFAULT_MEMORY = "2Gi";

    /** Default DataSource namespace (OpenShift Virtualization golden images) */
    public static final String DEFAULT_DATASOURCE_NAMESPACE = "openshift-virtualization-os-images";

    /** Whether to inject /tmp disk mount by default */
    public static final boolean DEFAULT_INJECT_TMP_DISK = true;

    // ==================== Agent Configuration Defaults ====================

    /** Default remote filesystem path for Jenkins agents */
    public static final String DEFAULT_REMOTE_FS = "/home/jenkins";

    /** Default Java path (empty means use PATH) */
    public static final String DEFAULT_JAVA_PATH = "";

    /** Default idle timeout before agent termination (10 minutes) */
    public static final int DEFAULT_IDLE_MINUTES = 10;

    /** Default orphan grace period before VM is considered orphaned (30 minutes) */
    public static final int DEFAULT_ORPHAN_GRACE_PERIOD_MINUTES = 30;

    /** Graceful disconnect wait time before VM deletion (2 seconds) */
    public static final int DISCONNECT_WAIT_MS = 2000;

    // ==================== Disk Configuration ====================

    /** Default size for the root disk */
    public static final String DEFAULT_DISK_SIZE = "30Gi";

    /** Size of the ephemeral /tmp disk (emptyDisk) */
    public static final String TMP_DISK_SIZE = "10Gi";

    // ==================== Disk Source Types ====================

    /** Disk source type: Container disk image */
    public static final String DISK_SOURCE_CONTAINER_DISK = "containerDisk";

    /** Disk source type: DataSource (clone from existing volume) */
    public static final String DISK_SOURCE_DATA_SOURCE = "dataSource";

    /** Disk source type: DataSource Import (import image into DataSource, then clone) */
    public static final String DISK_SOURCE_DATA_SOURCE_IMPORT = "dataSourceImport";

    // ==================== Connection Types ====================

    /** Connection type: Direct SSH to VM IP */
    public static final String CONNECTION_DIRECT_SSH = "directSsh";

    /** Connection type: virtctl SSH via Kubernetes API port-forward */
    public static final String CONNECTION_VIRTCTL_SSH = "virtctlSsh";

    // ==================== KubeVirt Subresource API ====================

    /** KubeVirt subresource API group for portforward/console/vnc endpoints */
    public static final String KUBEVIRT_SUBRESOURCE_API_GROUP = "subresources.kubevirt.io";

    /** WebSocket subprotocol used by KubeVirt port-forward */
    public static final String KUBEVIRT_WS_SUBPROTOCOL = "plain.kubevirt.io";

    /** Buffer size for WebSocket tunnel data transfer (8 KB) */
    public static final int WS_TUNNEL_BUFFER_SIZE = 8192;

    /** Timeout for WebSocket connection establishment (seconds) */
    public static final int WS_CONNECT_TIMEOUT_SECONDS = 30;

    // ==================== Disk Access Modes ====================

    /** Disk access mode: ReadWriteOnce (single node only) */
    public static final String ACCESS_MODE_RWO = "ReadWriteOnce";

    /** Disk access mode: ReadWriteMany (supports live migration) */
    public static final String ACCESS_MODE_RWX = "ReadWriteMany";

    // ==================== Cloud Connectivity ====================

    /** Timeout in milliseconds for the pre-provisioning / pre-cleanup connectivity check */
    public static final int CONNECTIVITY_CHECK_TIMEOUT_MS = 5000;

    // ==================== Capacity Limits ====================

    /** Default global instance cap - maximum VMs across all templates */
    public static final int DEFAULT_GLOBAL_INSTANCE_CAP = 10;

    /** Default template instance cap - maximum VMs per template */
    public static final int DEFAULT_TEMPLATE_INSTANCE_CAP = 10;

    /** Value representing unlimited capacity */
    public static final int UNLIMITED_CAPACITY = Integer.MAX_VALUE;
}
