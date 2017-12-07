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

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Ian Springer
 */
public abstract class AbstractProfileServiceConnectionProvider implements ProfileServiceConnectionProvider
{
    private final Log log = LogFactory.getLog(this.getClass());

    private ProfileServiceConnectionImpl existingConnection;
    private boolean connected;

    public final ProfileServiceConnection connect()
    {
        ProfileServiceConnectionImpl connection = doConnect();
        this.connected = true;
        if (this.existingConnection == null)
            this.existingConnection = connection;
        return this.existingConnection;
    }

    protected abstract ProfileServiceConnectionImpl doConnect();

    public boolean isConnected()
    {
        return this.connected;
    }

    public final void disconnect()
    {
        this.connected = false;
        doDisconnect();
    }

    protected abstract void doDisconnect();

    public ProfileServiceConnection getExistingConnection()
    {
        return this.existingConnection;
    }

    protected InitialContext createInitialContext(Properties env)
    {
        InitialContext initialContext;
        this.log.debug("Creating JNDI InitialContext with env [" + env + "]...");
        try
        {
            initialContext = new InitialContext(env);
        }
        catch (NamingException e)
        {
            throw new RuntimeException("Failed to create JNDI InitialContext.", e);
        }
        this.log.debug("Created JNDI InitialContext [" + initialContext + "].");
        return initialContext;
    }

    protected Object lookup(InitialContext initialContext, String name)
    {
        try
        {
            return initialContext.lookup(name);
        }
        catch (NamingException e)
        {
            throw new RuntimeException("Failed to lookup JNDI name '" + name + "' from InitialContext.", e);
        }
    }
}
