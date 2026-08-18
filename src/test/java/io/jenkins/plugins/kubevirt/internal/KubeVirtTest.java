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

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for KubeVirt class.
 */
public class KubeVirtTest {

    /**
     * Tests the sanitizeLabelValue method which is critical for security.
     * Kubernetes label values must comply with RFC 1123:
     * - Max 63 characters
     * - Only alphanumerics, dashes, underscores, and dots
     * - Must start and end with alphanumeric
     */
    @Test
    public void testSanitizeLabelValue_basicUrl() {
        String result = KubeVirt.sanitizeLabelValue("https://jenkins.example.com");
        assertEquals("jenkins.example.com", result);
    }

    @Test
    public void testSanitizeLabelValue_httpUrl() {
        String result = KubeVirt.sanitizeLabelValue("http://jenkins.test.org");
        assertEquals("jenkins.test.org", result);
    }

    @Test
    public void testSanitizeLabelValue_withPort() {
        String result = KubeVirt.sanitizeLabelValue("https://jenkins.example.com:8080");
        assertEquals("jenkins.example.com-8080", result);
    }

    @Test
    public void testSanitizeLabelValue_withPath() {
        String result = KubeVirt.sanitizeLabelValue("https://jenkins.example.com/foo/bar");
        assertEquals("jenkins.example.com-foo-bar", result);
    }

    @Test
    public void testSanitizeLabelValue_withTrailingSlash() {
        String result = KubeVirt.sanitizeLabelValue("https://jenkins.example.com/");
        assertEquals("jenkins.example.com", result);
    }

    @Test
    public void testSanitizeLabelValue_withMultipleSlashes() {
        String result = KubeVirt.sanitizeLabelValue("https://jenkins.example.com///");
        assertEquals("jenkins.example.com", result);
    }

    @Test
    public void testSanitizeLabelValue_invalidCharacters() {
        String result = KubeVirt.sanitizeLabelValue("https://jenkins@example.com#fragment?query=1");
        assertEquals("jenkins-example.com-fragment-query-1", result);
    }

    @Test
    public void testSanitizeLabelValue_multipleDashes() {
        String result = KubeVirt.sanitizeLabelValue("https://jenkins---example.com");
        assertEquals("jenkins-example.com", result);
    }

    @Test
    public void testSanitizeLabelValue_leadingDashes() {
        String result = KubeVirt.sanitizeLabelValue("https://---jenkins.example.com");
        assertEquals("jenkins.example.com", result);
    }

    @Test
    public void testSanitizeLabelValue_trailingDashes() {
        String result = KubeVirt.sanitizeLabelValue("https://jenkins.example.com---");
        assertEquals("jenkins.example.com", result);
    }

    @Test
    public void testSanitizeLabelValue_maxLength() {
        // Create a URL that will result in a string longer than 63 characters
        String longUrl = "https://jenkins.very-long-subdomain.example.com/with/very/long/path/segments/here";
        String result = KubeVirt.sanitizeLabelValue(longUrl);

        // Result should be truncated to 63 characters
        assertTrue(result.length() <= 63, "Result should be <= 63 characters");

        // Result should not end with a non-alphanumeric character
        char lastChar = result.charAt(result.length() - 1);
        assertTrue(Character.isLetterOrDigit(lastChar),
            "Last character should be alphanumeric");
    }

    @Test
    public void testSanitizeLabelValue_exactlyMaxLength() {
        // Create a string that's exactly 63 alphanumeric characters when sanitized
        // "jenkins" (7) + "." (1) + "a" repeated 55 times = 63
        String url = "https://jenkins." + "a".repeat(55);
        String result = KubeVirt.sanitizeLabelValue(url);

        assertEquals(63, result.length());
        assertTrue(Character.isLetterOrDigit(result.charAt(62)), "Should end with alphanumeric");
    }

    @Test
    public void testSanitizeLabelValue_emptyString() {
        String result = KubeVirt.sanitizeLabelValue("");
        assertEquals("", result);
    }

    @Test
    public void testSanitizeLabelValue_nullInput() {
        String result = KubeVirt.sanitizeLabelValue(null);
        assertEquals("", result);
    }

    @Test
    public void testSanitizeLabelValue_onlyProtocol() {
        String result = KubeVirt.sanitizeLabelValue("https://");
        assertEquals("", result);
    }

    @Test
    public void testSanitizeLabelValue_localhost() {
        String result = KubeVirt.sanitizeLabelValue("http://localhost:8080");
        assertEquals("localhost-8080", result);
    }

    @Test
    public void testSanitizeLabelValue_ipAddress() {
        String result = KubeVirt.sanitizeLabelValue("http://192.168.1.100:8080");
        assertEquals("192.168.1.100-8080", result);
    }

    @Test
    public void testSanitizeLabelValue_underscoresAndDots() {
        String result = KubeVirt.sanitizeLabelValue("https://my_jenkins.test_server.com");
        assertEquals("my_jenkins.test_server.com", result);
    }

    @Test
    public void testSanitizeLabelValue_startsWithNumber() {
        String result = KubeVirt.sanitizeLabelValue("https://123jenkins.example.com");
        assertEquals("123jenkins.example.com", result);
    }

    @Test
    public void testSanitizeLabelValue_endsWithNumber() {
        String result = KubeVirt.sanitizeLabelValue("https://jenkins.example123.com");
        assertEquals("jenkins.example123.com", result);
    }

    @Test
    public void testSanitizeLabelValue_whitespace() {
        String result = KubeVirt.sanitizeLabelValue("https://jenkins.example.com   ");
        assertEquals("jenkins.example.com", result);
    }

    @Test
    public void testSanitizeLabelValue_truncationWithDashAtEnd() {
        // Create a URL that will be truncated exactly at a position where there's a dash
        // This tests that we properly remove trailing dashes after truncation
        String url = "https://very-long-jenkins-hostname-that-will-exceed-the-max-length-limit.example.com";
        String result = KubeVirt.sanitizeLabelValue(url);

        assertTrue(result.length() <= 63, "Result should be <= 63 characters");
        char lastChar = result.charAt(result.length() - 1);
        assertTrue(Character.isLetterOrDigit(lastChar), 
            "Last character should be alphanumeric after truncation");
    }

    @Test
    public void testSanitizeLabelValue_allSpecialCharacters() {
        String result = KubeVirt.sanitizeLabelValue("https://@#$%^&*()+={}[]|\\:;\"'<>?,/");
        // Should be empty since there are no alphanumeric characters
        assertEquals("", result);
    }

    @Test
    public void testSanitizeLabelValue_mixedCase() {
        // URLs are typically lowercase but test mixed case to ensure it's preserved
        String result = KubeVirt.sanitizeLabelValue("https://Jenkins.Example.COM");
        assertEquals("Jenkins.Example.COM", result);
    }
}
