/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.adminclient.connection;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.deployers.spi.management.deploy.DeploymentManager;
import org.jboss.profileservice.spi.ProfileService;

/**
 * @author Ian Springer
 */
public class RemoteProfileServiceConnectionProvider extends AbstractProfileServiceConnectionProvider
{
    private static final String NAMING_CONTEXT_FACTORY = "org.jnp.interfaces.NamingContextFactory";
    private static final String JNDI_LOGIN_INITIAL_CONTEXT_FACTORY = "org.jboss.security.jndi.JndiLoginInitialContextFactory";

    private static final String PROFILE_SERVICE_JNDI_NAME = "ProfileService";
    private static final String SECURE_PROFILE_SERVICE_JNDI_NAME = "SecureProfileService/remote";
    private static final String SECURE_MANAGEMENT_VIEW_JNDI_NAME = "SecureManagementView/remote";
    private static final String SECURE_DEPLOYMENT_MANAGER_JNDI_NAME = "SecureDeploymentManager/remote";

    private static final String JNP_DISABLE_DISCOVERY_JNP_INIT_PROP = "jnp.disableDiscovery";

    /**
     * This is the timeout for the initial attempt to establish the remote connection.
     */
    private static final int JNP_TIMEOUT = 60 * 1000; // 60 seconds
    /**
     * This is the timeout for methods invoked on the remote ProfileService. NOTE: This timeout comes into play if the
     * JBossAS instance has gone down since the original JNP connection was made.
     */
    private static final int JNP_SO_TIMEOUT = 60 * 1000; // 60 seconds

    private final Log log = LogFactory.getLog(this.getClass());

    private String providerURL;
    private String principal;
    private String credentials;

    public RemoteProfileServiceConnectionProvider(String providerURL, String principal, String credentials)
    {
        this.providerURL = providerURL;
        this.principal = principal;
        this.credentials = credentials;
    }

    protected ProfileServiceConnectionImpl doConnect()
    {
        Properties env = new Properties();
        env.setProperty(Context.PROVIDER_URL, this.providerURL);
        ProfileService profileService;
        ManagementView managementView;
        DeploymentManager deploymentManager;
        ClassLoader originalContextClassLoader = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            if (this.principal != null)
            {
                env.setProperty(Context.INITIAL_CONTEXT_FACTORY, JNDI_LOGIN_INITIAL_CONTEXT_FACTORY);
                env.setProperty(Context.SECURITY_PRINCIPAL, this.principal);
                env.setProperty(Context.SECURITY_CREDENTIALS, this.credentials);
                log.debug("Connecting to Profile Service via remote JNDI using env [" + env + "]...");
                InitialContext initialContext = createInitialContext(env);
                profileService = (ProfileService)lookup(initialContext, SECURE_PROFILE_SERVICE_JNDI_NAME);
                managementView = (ManagementView)lookup(initialContext, SECURE_MANAGEMENT_VIEW_JNDI_NAME);
                deploymentManager = (DeploymentManager)lookup(initialContext, SECURE_DEPLOYMENT_MANAGER_JNDI_NAME);
            }
            else
            {
                env.setProperty(Context.INITIAL_CONTEXT_FACTORY, NAMING_CONTEXT_FACTORY);
                env.setProperty(JNP_DISABLE_DISCOVERY_JNP_INIT_PROP, "true");
                // Make sure the timeout always happens, even if the JBoss server is hung.
                env.setProperty("jnp.timeout", String.valueOf(JNP_TIMEOUT));
                env.setProperty("jnp.sotimeout", String.valueOf(JNP_SO_TIMEOUT));
                log.debug("Connecting to Profile Service via remote JNDI using env [" + env + "]...");
                InitialContext initialContext = createInitialContext(env);
                profileService = (ProfileService)lookup(initialContext, PROFILE_SERVICE_JNDI_NAME);
                managementView = profileService.getViewManager();
                deploymentManager = profileService.getDeploymentManager();
            }
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(originalContextClassLoader);
        }
        return new ProfileServiceConnectionImpl(this, profileService, managementView, deploymentManager);
    }

    protected void doDisconnect()
    {
        return;
    }
}
