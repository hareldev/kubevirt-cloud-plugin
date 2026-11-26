/*
 * The MIT License — see source repository for full text.
 */

package io.jenkins.plugins.kubevirt.disksource;

import io.jenkins.plugins.kubevirt.ProvisioningCallback;
import io.jenkins.plugins.kubevirt.internal.KubeVirt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DiskSourceStorageClassTest {

    private KubeVirt virt;
    private ProvisioningCallback callback;
    private KubeVirtDiskSource.VmProvisioningContext context;

    @BeforeEach
    void setUp() {
        virt = mock(KubeVirt.class);
        callback = mock(ProvisioningCallback.class);
        context = new KubeVirtDiskSource.VmProvisioningContext(
                "agent-1", "cloud-1", "template-1", "40Gi", "ReadWriteMany", "fast-storage",
                "2", "4Gi", "4", "8Gi", "#cloud-config", true);
    }

    @Test
    void legacyProvisioningContextUsesDefaultStorageClass() {
        KubeVirtDiskSource.VmProvisioningContext legacyContext = new KubeVirtDiskSource.VmProvisioningContext(
                "agent-1", "cloud-1", "template-1", "40Gi", "ReadWriteMany",
                "2", "4Gi", "4", "8Gi", "#cloud-config", true);

        assertNull(legacyContext.getStorageClass());
    }

    @Test
    void containerDiskForwardsStorageClass() {
        new ContainerDiskSource("quay.io/example/disk:latest").createVm(virt, context, callback);

        verify(virt).createVM(
                "agent-1", "cloud-1", "template-1", "quay.io/example/disk:latest",
                "40Gi", "ReadWriteMany", "fast-storage", "2", "4Gi", "4", "8Gi",
                "#cloud-config", true, callback);
    }

    @Test
    void dataSourceCloneForwardsStorageClass() {
        new DataSourceCloneDiskSource("images", "rhel9").createVm(virt, context, callback);

        verify(virt).createVMFromDataSource(
                "agent-1", "cloud-1", "template-1", "images", "rhel9",
                "40Gi", "ReadWriteMany", "fast-storage", "2", "4Gi", "4", "8Gi",
                "#cloud-config", true, callback);
    }

    @Test
    void dataSourceImportForwardsStorageClass() {
        new DataSourceImportDiskSource("images", "rhel9", "quay.io/example/disk:latest")
                .createVm(virt, context, callback);

        verify(virt).createVMFromDataSourceImport(
                "agent-1", "cloud-1", "template-1", "images", "rhel9",
                "quay.io/example/disk:latest", "40Gi", "ReadWriteMany", "fast-storage",
                "2", "4Gi", "4", "8Gi", "#cloud-config", true, callback);
    }
}
