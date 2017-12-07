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
import org.jboss.adminclient.connection.ProfileServiceConnection;
import org.jboss.adminclient.connection.ProfileServiceConnectionProvider;
import org.jboss.adminclient.connection.RemoteProfileServiceConnectionProvider;

/**
 * @author Ian Springer
 */
public class ConnectCommand extends AbstractClientCommand
{
    public static final String COMMAND_NAME = "connect";

    public String getName()
    {
        return COMMAND_NAME;
    }

    public OptionParser getOptionParser()
    {
        return new OptionParser()
        {
            {
                acceptsAll(asList("H", "host")).withRequiredArg().ofType(String.class)
                        .describedAs("the JBoss AS instance's JNP host (may be a hostname or an IP address)");
                acceptsAll(asList("P", "port")).withRequiredArg().ofType(String.class)
                        .describedAs("the JBoss AS instance's the JNP port");
                acceptsAll(asList("u", "username")).withRequiredArg().ofType(String.class)
                        .describedAs("the username used to authenticate against the JBoss AS Profile Service");
                acceptsAll(asList("p", "password")).withRequiredArg().ofType(String.class)
                        .describedAs("the password used to authenticate against the JBoss AS Profile Service");
            }
        };
    }

    public boolean execute(AdminClientMain client, OptionSet options)
    {
        if (!options.nonOptionArguments().isEmpty())
            throw new IllegalArgumentException("Usage: " + getName() + " ...");
        String host = options.has("host") ? (String)options.valueOf("host") : "127.0.0.1";
        int port = options.has("port") ? (Integer)options.valueOf("port") : 1099;
        String username = (String)options.valueOf("username");
        String password = (String)options.valueOf("password");
        String jnpURL = "jnp://" + host + ":" + port;

        ProfileServiceConnectionProvider connectionProvider =
                new RemoteProfileServiceConnectionProvider(jnpURL, username, password);
        ProfileServiceConnection connection = connectionProvider.connect();
        client.setConnection(connection);
        client.setHost(host);
        client.setPort(port);
        client.getPrintWriter().println("Connected.");
        return true;
    }

    public String getHelp()
    {
        return "Connect to the JBoss AS Profile Service.";
    }

    public String getDetailedHelp()
    {
        return null;
    }

    public boolean isConnectionRequired()
    {
        return false;
    }
}
