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

import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.deployers.spi.management.deploy.DeploymentManager;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.ProfileService;

/**
 * @author Ian Springer
 */
public class ProfileServiceConnectionImpl implements ProfileServiceConnection
{
    private static final ProfileKey DEFAULT_PROFILE_KEY = new ProfileKey(ProfileKey.DEFAULT);

    private AbstractProfileServiceConnectionProvider connectionProvider;
    private ProfileService profileService;
    private ManagementView managementView;
    private DeploymentManager deploymentManager;

    protected ProfileServiceConnectionImpl(AbstractProfileServiceConnectionProvider connectionProvider,
                                           ProfileService profileService, ManagementView managementView, DeploymentManager deploymentManager)
    {
        this.connectionProvider = connectionProvider;
        this.profileService = profileService;
        this.managementView = managementView;
        this.managementView.load();
        this.deploymentManager = deploymentManager;
        // Load and associate the given profile with the DeploymentManager for future operations. This is mandatory
        // in order for us to be able to successfully invoke the various DeploymentManager methods.
        try
        {
            this.deploymentManager.loadProfile(DEFAULT_PROFILE_KEY);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public ProfileServiceConnectionProvider getConnectionProvider()
    {
        return this.connectionProvider;
    }

    public ProfileService getProfileService()
    {
        return this.profileService;
    }

    public ManagementView getManagementView()
    {
        return this.managementView;
    }

    public DeploymentManager getDeploymentManager()
    {
        return this.deploymentManager;
    }
}
