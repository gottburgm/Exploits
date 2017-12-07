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
package org.jboss.adminclient.command;

import java.util.Set;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.jboss.adminclient.AdminClientMain;
import org.jboss.deployers.spi.management.ManagementView;

/**
 * @author Ian Springer
 */
public class ListDeploymentsCommand extends AbstractClientCommand
{
    public String getName()
    {
        return "listdeployments";
    }

    public OptionParser getOptionParser()
    {
        return new OptionParser()
        {
            {
                acceptsAll(asList("t", "type")).withRequiredArg().ofType(String.class)
                        .describedAs("an optional type (e.g. war) to filter by");
            }
        };
    }

    public boolean execute(AdminClientMain client, OptionSet options)
    {
        if (!options.nonOptionArguments().isEmpty())
            throw new IllegalArgumentException("Usage: " + getName() + " [--type <type>]");
        String type = (String)options.valueOf("type");
        ManagementView managementView = client.getConnection().getManagementView();
        Set<String> deploymentNames;
        if (type != null)
            deploymentNames = managementView.getDeploymentNamesForType(type);
        else
            deploymentNames = managementView.getDeploymentNames();
        for (String deploymentName : deploymentNames)
            client.getPrintWriter().println(deploymentName);
        return true;
    }

    public String getHelp()
    {
        return "List all deployments, or, if a type is specified, all deployments of that type.";
    }

    public String getDetailedHelp()
    {
        return null;
    }

    public boolean isConnectionRequired()
    {
        return true;
    }
}