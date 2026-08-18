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

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.TaskListener;

import java.io.PrintStream;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Centralised helper for writing timestamped {@code [KubeVirt …]} messages to
 * agent console logs.
 *
 * <p>System-level logs (via {@code java.util.logging}) already carry timestamps,
 * but agent console output written through {@link TaskListener} does not.  This
 * utility adds a UTC timestamp to every line so that operators can correlate
 * agent activity with system events and measure provisioning durations.</p>
 *
 * <p>The format mirrors the Jenkins SSH launcher convention so that all
 * agent log entries share a consistent layout:</p>
 * <pre>
 * [03/05/26 12:20:07] [KubeVirt] VM provisioning complete, launching SSH connection...
 * </pre>
 */
public final class KubeVirtLog {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("MM/dd/yy HH:mm:ss").withZone(ZoneOffset.UTC);

    private KubeVirtLog() {
        // utility class — not instantiable
    }

    /**
     * Prints a timestamped {@code [KubeVirt …]} line to the agent log.
     *
     * @param out     the agent console {@link PrintStream}
     * @param message the message to print (without prefix)
     */
    public static void log(@NonNull PrintStream out, @NonNull String message) {
        out.println("[" + FMT.format(Instant.now()) + "] [KubeVirt] " + message);
    }

    /**
     * Prints a timestamped {@code [KubeVirt] ERROR: …} line to the agent log.
     *
     * <p>We intentionally write through {@link PrintStream#println} rather than
     * {@link TaskListener#error(String, Object...)} because the latter is a
     * {@link java.util.Formatter}-based API.  Any {@code %} character that
     * appears in an exception message (e.g. {@code "CPU at 100% load"}) would
     * be misinterpreted as a format specifier and throw
     * {@link java.util.MissingFormatArgumentException} at runtime.</p>
     *
     * @param listener the task listener
     * @param message  the error message (without prefix)
     */
    public static void error(@NonNull TaskListener listener, @NonNull String message) {
        listener.getLogger().println("[" + FMT.format(Instant.now()) + "] [KubeVirt] ERROR: " + message);
    }

    /**
     * Returns a non-null, human-readable description of a throwable.
     *
     * <p>If the exception carries a message, that message is returned.
     * Otherwise the simple class name is used (e.g. {@code "NullPointerException"})
     * so that log lines never contain a bare {@code "null"} string.</p>
     *
     * @param t the throwable
     * @return a description suitable for log messages
     */
    @NonNull
    public static String messageOf(@NonNull Throwable t) {
        String msg = t.getMessage();
        return msg != null ? msg : t.getClass().getSimpleName();
    }
}
