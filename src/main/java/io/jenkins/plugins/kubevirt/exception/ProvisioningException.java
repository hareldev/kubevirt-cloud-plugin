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

package io.jenkins.plugins.kubevirt.exception;

import java.io.IOException;

/**
 * Exception thrown when VM provisioning fails.
 * This is a checked exception that extends IOException for compatibility with Jenkins APIs.
 */
public class ProvisioningException extends IOException {

    private final ErrorCode errorCode;
    private final String vmName;

    /**
     * Creates a new ProvisioningException.
     *
     * @param vmName  The name of the VM that failed to provision
     * @param message The detailed error message
     */
    public ProvisioningException(String vmName, String message) {
        super(message);
        this.errorCode = ErrorCode.PROVISIONING_FAILED;
        this.vmName = vmName;
    }

    /**
     * Creates a new ProvisioningException with a specific error code.
     *
     * @param vmName    The name of the VM that failed to provision
     * @param message   The detailed error message
     * @param errorCode The specific error code
     */
    public ProvisioningException(String vmName, String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.vmName = vmName;
    }

    /**
     * Creates a new ProvisioningException with a cause.
     *
     * @param vmName  The name of the VM that failed to provision
     * @param message The detailed error message
     * @param cause   The underlying cause
     */
    public ProvisioningException(String vmName, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.PROVISIONING_FAILED;
        this.vmName = vmName;
    }

    /**
     * Gets the error code for this exception.
     *
     * @return The error code
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return String.format("[%s] VM '%s': %s", errorCode.getCode(), vmName, getMessage());
    }
}
