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

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link KubeVirtLog}.
 */
public class KubeVirtLogTest {

    // ── log() ──────────────────────────────────────────────────────────

    @Test
    public void testLog_outputMatchesExpectedFormat() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        KubeVirtLog.log(ps, "hello world");

        String output = baos.toString().trim();
        // Expected: [MM/dd/yy HH:mm:ss] [KubeVirt] hello world
        assertTrue(output.matches(
                "\\[\\d{2}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2}\\] \\[KubeVirt\\] hello world"),
                "Unexpected format: " + output);
    }

    @Test
    public void testLog_messagePreservedVerbatim() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        String msg = "VM provisioning complete, launching SSH connection...";
        KubeVirtLog.log(ps, msg);

        String output = baos.toString().trim();
        assertTrue(output.endsWith("[KubeVirt] " + msg),
                "Message should appear verbatim at end: " + output);
    }

    @Test
    public void testLog_percentCharactersAreNotInterpreted() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        // % characters must pass through literally (no format-string processing)
        KubeVirtLog.log(ps, "CPU at 100% load");

        String output = baos.toString().trim();
        assertTrue(output.contains("CPU at 100% load"),
                "Percent characters must be preserved: " + output);
    }

    // ── error() ────────────────────────────────────────────────────────

    @Test
    public void testError_includesErrorPrefix() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        hudson.model.TaskListener listener = new hudson.model.StreamBuildListener(ps, StandardCharsets.UTF_8);

        KubeVirtLog.error(listener, "something broke");

        String output = baos.toString().trim();
        assertTrue(output.contains("[KubeVirt] ERROR: something broke"),
                "Should contain ERROR prefix: " + output);
    }

    @Test
    public void testError_percentCharactersDoNotThrow() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        hudson.model.TaskListener listener = new hudson.model.StreamBuildListener(ps, StandardCharsets.UTF_8);

        // This would throw MissingFormatArgumentException if routed through
        // TaskListener.error(String format, Object... args)
        assertDoesNotThrow(() ->
                KubeVirtLog.error(listener, "disk 95% full, %s remaining"));

        String output = baos.toString().trim();
        assertTrue(output.contains("disk 95% full, %s remaining"),
                "Percent characters must be preserved: " + output);
    }

    @Test
    public void testError_outputMatchesExpectedFormat() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        hudson.model.TaskListener listener = new hudson.model.StreamBuildListener(ps, StandardCharsets.UTF_8);

        KubeVirtLog.error(listener, "connection refused");

        String output = baos.toString().trim();
        assertTrue(output.matches(
                "\\[\\d{2}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2}\\] \\[KubeVirt\\] ERROR: connection refused"),
                "Unexpected format: " + output);
    }

    // ── messageOf() ────────────────────────────────────────────────────

    @Test
    public void testMessageOf_returnsMessageWhenPresent() {
        Exception e = new IOException("disk full");
        assertEquals("disk full", KubeVirtLog.messageOf(e));
    }

    @Test
    public void testMessageOf_returnsClassNameWhenMessageIsNull() {
        Exception e = new NullPointerException();
        assertEquals("NullPointerException", KubeVirtLog.messageOf(e));
    }

    @Test
    public void testMessageOf_returnsClassNameForCustomException() {
        Exception e = new IllegalStateException((String) null);
        assertEquals("IllegalStateException", KubeVirtLog.messageOf(e));
    }

    @Test
    public void testMessageOf_preservesEmptyMessage() {
        // An empty message is still a non-null message — preserve it
        Exception e = new RuntimeException("");
        assertEquals("", KubeVirtLog.messageOf(e));
    }
}
