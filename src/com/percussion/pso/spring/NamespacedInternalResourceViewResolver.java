/*
 * com.percussion.soln.linkback.servlet NamespacedInternalResourceViewResolver
 * 
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 */

package com.percussion.pso.spring;

import java.util.Locale;

import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

public class NamespacedInternalResourceViewResolver extends InternalResourceViewResolver {

    private String m_namespace;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.web.servlet.view.UrlBasedViewResolver#loadView(java
     * .lang.String, java.util.Locale)
     */
    @Override
    protected View loadView(String viewName, Locale locale) throws Exception {
        if (m_namespace == null)
            throw new IllegalStateException("namespace must be assigned");

        // only handle requests whose view name is prefixed with a specific
        // namespace
        if (viewName.startsWith(m_namespace)) {
            return super.loadView(viewName.substring(m_namespace.length()), locale);
        }
        return null;
    }

    /**
     * @return the namespace
     */
    public String getNamespace() {
        return m_namespace;
    }

    /**
     * @param namespace
     *            the namespace to set
     */
    public void setNamespace(String namespace) {
        m_namespace = namespace;
    }
}
