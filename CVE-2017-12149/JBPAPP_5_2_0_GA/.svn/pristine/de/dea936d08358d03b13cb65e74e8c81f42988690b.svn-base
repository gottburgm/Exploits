/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.jaxr.scout.basic;

import junit.framework.TestCase;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.registry.ConnectionFactory;
import java.util.Properties;

/**
 * Tests the JAXR connection factory bound
 * to JNDI
 *
 * @author <mailto:Anil.Saldhana@jboss.org>Anil Saldhana
 * @since Apr 12, 2005
 */
public class JaxrJNDIConnectionTestCase extends TestCase
{
    public JaxrJNDIConnectionTestCase(String name)
    {
        super(name);
    }

    public void testJaxrJNDIConnection() throws Exception
    {
        String bindname = System.getProperty("jndi.bind.name");
        InitialContext ctx = getClientContext();
        ConnectionFactory factory = (ConnectionFactory) ctx.lookup(bindname);
        assertNotNull("Connection Factory from JNDI:", factory);
    }

    protected InitialContext getClientContext() throws NamingException
    {
        String hostname = System.getProperty("host.name");
        Properties env = new Properties();
        env.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
        env.setProperty(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
        env.setProperty(Context.PROVIDER_URL, "jnp://" + hostname + ":1099");
        return new InitialContext(env);
    }
}
