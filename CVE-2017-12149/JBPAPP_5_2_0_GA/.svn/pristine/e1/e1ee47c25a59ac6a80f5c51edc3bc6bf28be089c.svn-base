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

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.jboss.adminclient.AdminClientMain;

/**
 * @author Ian Springer
 */
public class DisconnectCommand extends AbstractClientCommand
{
    public String getName()
    {
        return "disconnect";
    }

    public OptionParser getOptionParser()
    {
        return new OptionParser();
    }

    public boolean execute(AdminClientMain client, OptionSet options)
    {
        if (!options.nonOptionArguments().isEmpty())
            throw new IllegalArgumentException("Usage: " + getName());
        if (client.getConnection() != null)
            client.getConnection().getConnectionProvider().disconnect();
        client.getPrintWriter().println("Disconnected.");
        return true;
    }

    public String getHelp()
    {
        return "Disconnect from the JBoss AS Profile Service.";
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