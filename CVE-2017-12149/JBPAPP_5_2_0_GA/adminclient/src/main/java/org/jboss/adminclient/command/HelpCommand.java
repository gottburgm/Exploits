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

import java.util.List;
import java.io.IOException;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.jboss.adminclient.AdminClientMain;

/**
 * @author Ian Springer
 */
public class HelpCommand extends AbstractClientCommand
{
    public String getName()
    {
        return "help";
    }

    public OptionParser getOptionParser()
    {
        return new OptionParser();
    }

    public boolean execute(AdminClientMain client, OptionSet options)
    {
        List<String> nonOptionArgs = options.nonOptionArguments();
        if (nonOptionArgs.size() > 1)
            throw new IllegalArgumentException("Usage: " + getName() + " [command]");
        if (nonOptionArgs.isEmpty()) {
            client.getPrintWriter().println("The following commands are available:\n");
            for (ClientCommand command : client.getCommands().values()) {
                if (!command.isUndocumented())
                    client.getPrintWriter().printf("  %-16s  %s\n", command.getName(), command.getHelp());
            }
            client.getPrintWriter().println("\nEnter 'help <command>' to display detailed help for a command.");
        } else {
            String commandName = nonOptionArgs.get(0);
            ClientCommand command = client.getCommands().get(commandName);
            if (command != null) {
                client.getPrintWriter().println("Command: " + command.getName());
                String detailedHelp = (command.getDetailedHelp() != null) ? command.getDetailedHelp() :
                        command.getHelp();
                client.getPrintWriter().println("\n" + detailedHelp);
                String syntax = "TODO";
                client.getPrintWriter().println("Usage: " + command.getName() + "\t" + syntax);
                try
                {
                    command.getOptionParser().printHelpOn(client.getPrintWriter());
                }
                catch (IOException e)
                {
                    throw new IllegalStateException(e);
                }
            } else {
                client.getPrintWriter().println("Unknown command: " + commandName);
            }
        }
        return true;
    }

    public String getHelp()
    {
        return "Display a list of available commands or the syntax for a specific command.";
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