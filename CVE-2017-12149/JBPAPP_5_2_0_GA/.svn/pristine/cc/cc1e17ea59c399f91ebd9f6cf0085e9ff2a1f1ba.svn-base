/*
 * RHQ Management Platform
 * Copyright (C) 2005-2008 Red Hat, Inc.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.jboss.adminclient.command;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.jboss.adminclient.AdminClientMain;
import org.jboss.deployers.spi.management.ManagementView;

/**
 * @author Ian Springer
 */
public class ReloadCommand extends AbstractClientCommand
{
    public String getName()
    {
        return "reload";
    }

    public OptionParser getOptionParser()
    {
        return new OptionParser();
    }

    public boolean execute(AdminClientMain client, OptionSet options)
    {
        if (!options.nonOptionArguments().isEmpty())
            throw new IllegalArgumentException("Usage: " + getName());
        ManagementView managementView = client.getConnection().getManagementView();
        managementView.reload();
        client.getPrintWriter().println("Reloaded management view.");
        return true;
    }

    public String getHelp()
    {
        return "Reload the Profile Service management view.";
    }

    public String getDetailedHelp()
    {
        return null;
    }

    public boolean isConnectionRequired()
    {
        return true;
    }

    @Override
    public boolean isUndocumented()
    {
        return true;
    }
}