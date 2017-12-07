/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.test.ws;

import java.net.URL;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * A JBossWS test helper that deals with test deployment/undeployment, etc.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 14-Oct-2004
 */
public class JBossTestDeployer implements JBossWSTestDeployer
{
   private static final String MAIN_DEPLOYER = "jboss.system:service=MainDeployer";

   public void deploy(URL url) throws Exception
   {
      MBeanServerConnection server = getServer();
      server.invoke(new ObjectName(MAIN_DEPLOYER), "deploy", new Object[] { url }, new String[] { "java.net.URL" });
   }

   public void undeploy(URL url) throws Exception
   {
      MBeanServerConnection server = getServer();
      server.invoke(new ObjectName(MAIN_DEPLOYER), "undeploy", new Object[] { url }, new String[] { "java.net.URL" });
   }

   private MBeanServerConnection getServer() throws NamingException
   {
      InitialContext iniCtx = new InitialContext();
      MBeanServerConnection server = (MBeanServerConnection)iniCtx.lookup("jmx/invoker/RMIAdaptor");
      return server;
   }
}
