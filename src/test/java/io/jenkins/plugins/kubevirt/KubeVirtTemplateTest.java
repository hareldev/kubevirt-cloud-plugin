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

import hudson.util.FormValidation;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;


/**
 * Unit tests for KubeVirtTemplate class.
 */
@WithJenkins
public class KubeVirtTemplateTest {

    @SuppressWarnings("unused")
    private JenkinsRule jenkins;

    @BeforeEach
    public void setUp(JenkinsRule jenkins) {
        this.jenkins = jenkins;
    }

    /**
     * Tests SSH port parsing with valid values.
     */
    @Test
    public void testSshPortParsing_validPort() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "2222", "/home/jenkins", null, null, "10", null, null, null, false, null, null
        );

        assertEquals(2222, template.getSshPort());
    }

    @Test
    public void testSshPortParsing_defaultPort() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                null, "/home/jenkins", null, null, "10", null, null, null, false, null, null
        );

        assertEquals(KubeVirtConfiguration.DEFAULT_SSH_PORT, template.getSshPort());
    }

    @Test
    public void testSshPortParsing_emptyString() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "", "/home/jenkins", null, null, "10", null, null, null, false, null, null
        );

        assertEquals(KubeVirtConfiguration.DEFAULT_SSH_PORT, template.getSshPort());
    }

    @Test
    public void testSshPortParsing_invalidFormat() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "not-a-number", "/home/jenkins", null, null, "10", null, null, null, false, null, null
        );

        assertEquals(KubeVirtConfiguration.DEFAULT_SSH_PORT, template.getSshPort());
    }

    @Test
    public void testSshPortParsing_outOfRangeLow() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "0", "/home/jenkins", null, null, "10", null, null, null, false, null, null
        );

        assertEquals(KubeVirtConfiguration.DEFAULT_SSH_PORT, template.getSshPort());
    }

    @Test
    public void testSshPortParsing_outOfRangeHigh() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "70000", "/home/jenkins", null, null, "10", null, null, null, false, null, null
        );

        assertEquals(KubeVirtConfiguration.DEFAULT_SSH_PORT, template.getSshPort());
    }

    @Test
    public void testSshPortParsing_maxValidPort() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "65535", "/home/jenkins", null, null, "10", null, null, null, false, null, null
        );

        assertEquals(65535, template.getSshPort());
    }

    /**
     * Tests idle minutes parsing with valid values.
     */
    @Test
    public void testIdleMinutesParsing_validValue() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "30", null, null, null, false, null, null
        );

        assertEquals(30, template.getIdleMinutes());
    }

    @Test
    public void testIdleMinutesParsing_zeroValue() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "0", null, null, null, false, null, null
        );

        assertEquals(0, template.getIdleMinutes());
    }

    @Test
    public void testIdleMinutesParsing_defaultValue() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, null, null, null, null, false, null, null
        );

        assertEquals(KubeVirtConfiguration.DEFAULT_IDLE_MINUTES, template.getIdleMinutes());
    }

    @Test
    public void testIdleMinutesParsing_emptyString() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "", null, null, null, false, null, null
        );

        assertEquals(KubeVirtConfiguration.DEFAULT_IDLE_MINUTES, template.getIdleMinutes());
    }

    @Test
    public void testIdleMinutesParsing_invalidFormat() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "not-a-number", null, null, null, false, null, null
        );

        assertEquals(KubeVirtConfiguration.DEFAULT_IDLE_MINUTES, template.getIdleMinutes());
    }

    @Test
    public void testIdleMinutesParsing_negativeValue() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "-10", null, null, null, false, null, null
        );

        assertEquals(KubeVirtConfiguration.DEFAULT_IDLE_MINUTES, template.getIdleMinutes());
    }

    /**
     * Tests disk source type detection.
     */
    @Test
    public void testDiskSourceType_containerDisk() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "10", null, null, null, false, null, null
        );

        assertTrue(template.isContainerDisk());
        assertFalse(template.isDataSource());
    }

    @Test
    public void testDiskSourceType_dataSource() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_DATA_SOURCE,
                null, "default", "my-datasource", "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "10", null, null, null, false, null, null
        );

        assertFalse(template.isContainerDisk());
        assertTrue(template.isDataSource());
    }

    /**
     * Tests connection type detection.
     */
    @Test
    public void testConnectionType_directSsh() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "10", null, null, null, false, null, null
        );

        assertTrue(template.isDirectSsh());
        assertFalse(template.isVirtctlSsh());
    }

    @Test
    public void testConnectionType_virtctlSsh() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_VIRTCTL_SSH, "creds",
                "22", "/home/jenkins", null, null, "10", null, null, null, false, null, null
        );

        assertFalse(template.isDirectSsh());
        assertTrue(template.isVirtctlSsh());
    }

    @Test
    public void testConnectionType_nullDefaultsToDirectSsh() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                null, "creds",
                "22", "/home/jenkins", null, null, "10", null, null, null, false, null, null
        );

        assertTrue(template.isDirectSsh());
        assertFalse(template.isVirtctlSsh());
    }

    /**
     * Tests default values.
     */
    @Test
    public void testDefaults_remoteFS() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", null, null, null, "10", null, null, null, false, null, null
        );

        assertEquals(KubeVirtConfiguration.DEFAULT_REMOTE_FS, template.getRemoteFS());
    }

    @Test
    public void testDefaults_javaPath() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "10", null, null, null, false, null, null
        );

        assertEquals(KubeVirtConfiguration.DEFAULT_JAVA_PATH, template.getJavaPath());
    }

    @Test
    public void testDefaults_diskSize() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, null,
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "10", null, null, null, false, null, null
        );

        assertEquals(KubeVirtConfiguration.DEFAULT_DISK_SIZE, template.getDiskSize());
    }

    @Test
    public void testDefaults_accessMode() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                null,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "10", null, null, null, false, null, null
        );

        assertEquals(KubeVirtConfiguration.ACCESS_MODE_RWX, template.getAccessMode());
    }

    /**
     * Tests provisioning timeout minutes parsing.
     */
    @Test
    public void testProvisioningTimeoutMinutes_validValue() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "10", null, "30", null, false, null, null
        );

        assertEquals(30, template.getProvisioningTimeoutMinutes());
    }

    @Test
    public void testProvisioningTimeoutMinutes_defaultValue() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "10", null, null, null, false, null, null
        );

        assertEquals(KubeVirtConfiguration.DEFAULT_PROVISIONING_TIMEOUT_MINUTES, template.getProvisioningTimeoutMinutes());
    }

    @Test
    public void testProvisioningTimeoutMinutes_emptyString() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "10", null, "", null, false, null, null
        );

        assertEquals(KubeVirtConfiguration.DEFAULT_PROVISIONING_TIMEOUT_MINUTES, template.getProvisioningTimeoutMinutes());
    }

    @Test
    public void testProvisioningTimeoutMinutes_invalidValue() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "10", null, "not-a-number", null, false, null, null
        );

        assertEquals(KubeVirtConfiguration.DEFAULT_PROVISIONING_TIMEOUT_MINUTES, template.getProvisioningTimeoutMinutes());
    }

    @Test
    public void testProvisioningTimeoutMinutes_zeroValue() {
        // Zero is not valid for timeout (min is 1), should default
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "10", null, "0", null, false, null, null
        );

        assertEquals(KubeVirtConfiguration.DEFAULT_PROVISIONING_TIMEOUT_MINUTES, template.getProvisioningTimeoutMinutes());
    }

    /**
     * Tests inject /tmp disk config option.
     */
    @Test
    public void testInjectTmpDiskConfig_true() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "10", null, null, null, true, null, null
        );

        assertTrue(template.isInjectTmpDiskConfig());
    }

    @Test
    public void testInjectTmpDiskConfig_false() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "10", null, null, null, false, null, null
        );

        assertFalse(template.isInjectTmpDiskConfig());
    }

    /**
     * Tests CPU and Memory limits.
     */
    @Test
    public void testResourceLimits_withLimits() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", "2", "4Gi",
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "10", null, null, null, false, null, null
        );

        assertEquals("1", template.getCpu());
        assertEquals("2Gi", template.getMemory());
        assertEquals("2", template.getCpuLimit());
        assertEquals("4Gi", template.getMemoryLimit());
    }

    @Test
    public void testResourceLimits_millicores() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "500m", "1Gi", "1500m", "2Gi",
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "10", null, null, null, false, null, null
        );

        assertEquals("500m", template.getCpu());
        assertEquals("1500m", template.getCpuLimit());
    }

    @Test
    public void testResourceLimits_noLimits() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "10", null, null, null, false, null, null
        );

        assertEquals("1", template.getCpu());
        assertEquals("2Gi", template.getMemory());
        assertNull(template.getCpuLimit());
        assertNull(template.getMemoryLimit());
    }

    /**
     * Tests orphan grace period minutes parsing.
     */
    @Test
    public void testOrphanGracePeriodMinutes_validValue() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "10", null, null, null, false, "45", null
        );

        assertEquals(45, template.getOrphanGracePeriodMinutes());
    }

    @Test
    public void testOrphanGracePeriodMinutes_defaultValue() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "10", null, null, null, false, null, null
        );

        assertEquals(KubeVirtConfiguration.DEFAULT_ORPHAN_GRACE_PERIOD_MINUTES, template.getOrphanGracePeriodMinutes());
    }

    @Test
    public void testOrphanGracePeriodMinutes_emptyString() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "10", null, null, null, false, "", null
        );

        assertEquals(KubeVirtConfiguration.DEFAULT_ORPHAN_GRACE_PERIOD_MINUTES, template.getOrphanGracePeriodMinutes());
    }

    @Test
    public void testOrphanGracePeriodMinutes_invalidValue() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "10", null, null, null, false, "not-a-number", null
        );

        assertEquals(KubeVirtConfiguration.DEFAULT_ORPHAN_GRACE_PERIOD_MINUTES, template.getOrphanGracePeriodMinutes());
    }

    @Test
    public void testOrphanGracePeriodMinutes_zeroValue() {
        // Zero is not valid for grace period (min is 1), should default
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "10", null, null, null, false, "0", null
        );

        assertEquals(KubeVirtConfiguration.DEFAULT_ORPHAN_GRACE_PERIOD_MINUTES, template.getOrphanGracePeriodMinutes());
    }

    /**
     * Tests cloud-init wait seconds parsing.
     */
    @Test
    public void testCloudInitWaitSeconds_validValue() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "10", null, null, null, false, null, "360"
        );

        assertEquals(360, template.getCloudInitWaitSeconds());
    }

    @Test
    public void testCloudInitWaitSeconds_defaultValue() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "10", null, null, null, false, null, null
        );

        assertEquals(KubeVirtConfiguration.DEFAULT_CLOUD_INIT_WAIT_SECONDS, template.getCloudInitWaitSeconds());
    }

    @Test
    public void testCloudInitWaitSeconds_emptyString() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "10", null, null, null, false, null, ""
        );

        assertEquals(KubeVirtConfiguration.DEFAULT_CLOUD_INIT_WAIT_SECONDS, template.getCloudInitWaitSeconds());
    }

    @Test
    public void testCloudInitWaitSeconds_invalidValue() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "10", null, null, null, false, null, "not-a-number"
        );

        assertEquals(KubeVirtConfiguration.DEFAULT_CLOUD_INIT_WAIT_SECONDS, template.getCloudInitWaitSeconds());
    }

    @Test
    public void testCloudInitWaitSeconds_zeroValue() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "10", null, null, null, false, null, "0"
        );

        assertEquals(KubeVirtConfiguration.DEFAULT_CLOUD_INIT_WAIT_SECONDS, template.getCloudInitWaitSeconds());
    }

    /**
     * Tests template ID generation.
     */
    @Test
    public void testTemplateId_generatedWhenNull() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "10", null, null, null, false, null, null
        );

        assertNotNull(template.getId());
        assertFalse(template.getId().isEmpty());
    }

    @Test
    public void testTemplateId_preservedWhenProvided() {
        String expectedId = "my-custom-id-12345";
        KubeVirtTemplate template = new KubeVirtTemplate(
                expectedId, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "10", null, null, null, false, null, null
        );

        assertEquals(expectedId, template.getId());
    }

    @Test
    public void testMaxProvisionAttempts_defaultValue() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "10", null, null, null, false, null, null
        );

        assertEquals(KubeVirtConfiguration.DEFAULT_MAX_PROVISION_ATTEMPTS, template.getMaxProvisionAttempts());
        assertEquals(1, template.getMaxProvisionAttempts());
        assertEquals(2, template.getAllowedProvisionFailures());
        assertFalse(template.hasUnlimitedProvisionRetries());
    }

    @Test
    public void testMaxProvisionAttempts_customValue() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "10", null, null, "5", false, null, null
        );

        assertEquals(5, template.getMaxProvisionAttempts());
        assertEquals(6, template.getAllowedProvisionFailures());
        assertFalse(template.hasUnlimitedProvisionRetries());
    }

    @Test
    public void testMaxProvisionAttempts_zeroMeansNoRetries() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "10", null, null, "0", false, null, null
        );

        assertEquals(0, template.getMaxProvisionAttempts());
        assertEquals(1, template.getAllowedProvisionFailures());
        assertFalse(template.hasUnlimitedProvisionRetries());
    }

    @Test
    public void testMaxProvisionAttempts_minusOneMeansUnlimited() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "10", null, null, "-1", false, null, null
        );

        assertEquals(KubeVirtConfiguration.UNLIMITED_PROVISION_RETRIES, template.getMaxProvisionAttempts());
        assertTrue(template.hasUnlimitedProvisionRetries());
    }

    @Test
    public void testMaxProvisionAttempts_invalidNegativeFallsBackToDefault() {
        KubeVirtTemplate template = new KubeVirtTemplate(
                null, "test", "label1",
                KubeVirtConfiguration.DISK_SOURCE_CONTAINER_DISK,
                "image:latest", null, null, "30Gi",
                KubeVirtConfiguration.ACCESS_MODE_RWX,
                "1", "2Gi", null, null,
                KubeVirtConfiguration.CONNECTION_DIRECT_SSH, "creds",
                "22", "/home/jenkins", null, null, "10", null, null, "-2", false, null, null
        );

        assertEquals(KubeVirtConfiguration.DEFAULT_MAX_PROVISION_ATTEMPTS, template.getMaxProvisionAttempts());
    }

    @Test
    public void testMaxProvisionAttempts_formValidation() {
        KubeVirtTemplate.DescriptorImpl descriptor = new KubeVirtTemplate.DescriptorImpl();

        assertEquals(FormValidation.Kind.OK, descriptor.doCheckMaxProvisionAttempts(null).kind);
        assertEquals(FormValidation.Kind.OK, descriptor.doCheckMaxProvisionAttempts("").kind);
        assertEquals(FormValidation.Kind.OK, descriptor.doCheckMaxProvisionAttempts("0").kind);
        assertEquals(FormValidation.Kind.OK, descriptor.doCheckMaxProvisionAttempts("1").kind);
        assertEquals(FormValidation.Kind.OK, descriptor.doCheckMaxProvisionAttempts("3").kind);
        assertEquals(FormValidation.Kind.WARNING, descriptor.doCheckMaxProvisionAttempts("-1").kind);
        assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckMaxProvisionAttempts("-2").kind);
        assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckMaxProvisionAttempts("abc").kind);
    }
}
