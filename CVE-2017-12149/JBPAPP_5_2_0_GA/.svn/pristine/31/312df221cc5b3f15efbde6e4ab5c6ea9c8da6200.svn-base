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
public interface ClientCommand
{
    /**
     * All implementations must indicate what the prompt command name is that will trigger its execution. This method
     * returns that name.
     *
     * @return the prompt command name - if the first prompt argument is this value, then this command will be
     *         executed.
     */
    String getName();

    /**
     * Executes the agent prompt command with the given arguments.
     *
     * @param client the ClientMain class itself
     * @param args   the arguments passed to the agent on the agent prompt
     * @return <code>true</code> if the agent can continue accepting prompt commands; <code>false</code> if the agent
     *         should die
     */
    boolean execute(AdminClientMain client, OptionSet options);

    /**
     * Returns a help summary to describe to the user what the prompt command does and its purpose. It is usually a
     * short one line help summary.
     *
     * @return help string
     */
    String getHelp();

    /**
     * Returns a detailed help message to describe to the user what the command's syntax is and any detailed information
     * that is useful to the user that wants to use this command.
     *
     * @return detailed help string
     */
    String getDetailedHelp();

    /**
     * TODO
     *
     * @return
     */
    OptionParser getOptionParser();

    /**
     * TODO
     *
     * @return
     */
    boolean isConnectionRequired();

    /**
     * TODO
     *
     * @return
     */
    boolean isUndocumented();
}