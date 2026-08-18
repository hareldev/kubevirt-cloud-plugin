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

import hudson.security.Permission;
import jenkins.model.Jenkins;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

/**
 * A group of VM templates that can be saved together.
 * This interface is implemented by KubeVirtCloud to provide template management.
 */
public interface VMTemplateGroup {

    /**
     * Add the template to the group.
     * @param template the template to add
     */
    void addTemplate(KubeVirtTemplate template);

    /**
     * Replaces the old template with the new template.
     * @param oldTemplate the old template to replace
     * @param newTemplate the new template to replace with
     */
    void replaceTemplate(KubeVirtTemplate oldTemplate, KubeVirtTemplate newTemplate);

    /**
     * Removes the template from the group.
     * @param template the template to remove
     */
    void removeTemplate(KubeVirtTemplate template);

    /**
     * @return the URL to redirect to after the template is saved.
     */
    String getVMTemplateGroupUrl();

    /**
     * @return The permission required to manage the templates in this group.
     */
    Permission getManagePermission();

    /**
     * @return {@code true} if the current {@link Authentication} has permissions to add / replace / remove templates.
     */
    default boolean hasManagePermission() {
        return Jenkins.get().hasPermission(getManagePermission());
    }

    /**
     * Checks whether the current {@link Authentication} has sufficient permissions to manage the templates in this group.
     * @throws AccessDeniedException if access is denied for the current {@link Authentication}.
     */
    default void checkManagePermission() throws AccessDeniedException {
        Jenkins.get().checkPermission(getManagePermission());
    }
}
