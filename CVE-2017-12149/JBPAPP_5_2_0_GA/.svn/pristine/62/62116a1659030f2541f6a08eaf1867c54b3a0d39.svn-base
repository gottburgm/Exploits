/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.iiop.test;

import java.util.Properties;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.jmx.adaptor.rmi.RMIAdaptor;
import org.jboss.mx.util.MBeanProxy;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;
import org.jboss.iiop.CorbaNamingServiceMBean;
import org.jboss.test.JBossTestCase;

import junit.framework.Test;

public class Jbpapp6469TestCase extends JBossTestCase
{
   public Jbpapp6469TestCase(String name) 
   {
      super(name);
      SecurityAssociation.setPrincipal(new SimplePrincipal("admin"));
      SecurityAssociation.setCredential("admin");
   }

   public void testIiopWorksOnStandard() throws Exception
   {
      try
      {
         deployFailOnError("jbpapp6469.jar");
         String objectNameString = "jboss:service=CorbaNaming";
         MBeanServerConnection mbeanServerConnection = getRMIServer(null);
         ObjectName objectName = new ObjectName(objectNameString);
         CorbaNamingServiceMBean mbean = JMX.newMBeanProxy(mbeanServerConnection, objectName, CorbaNamingServiceMBean.class);
         String listing = mbean.list();
         String[] deployments = listing.split("\n");
         for(String deployment : deployments)
         {
            if(deployment.equals("HelloSessionBean/"))
               return;
         }
         fail("Did not find IIOP EJB");
      }
      finally
      {
         undeployFailOnError("jbpapp6469.jar");
      }
   }

   private static MBeanServerConnection getRMIServer(String host) throws Exception
   {
      String connectorName = "jmx/rmi/RMIAdaptor";
      RMIAdaptor server = (RMIAdaptor) new InitialContext().lookup(connectorName);
      return server;
   }

   private void deployFailOnError(String file)
   {
      try
      {
         log.info("Deploying : " + file);
         super.deploy(file);
      }
      catch ( Exception e )
      {
         e.printStackTrace();
         fail("Failed to deploy: " + file);
      }
   }

   private void undeployFailOnError(String file)
   {
      try
      {
         log.info("Undeploying : " + file);
         super.undeploy(file);
      }
      catch ( Exception e )
      {
         fail("Failed to undeploy: " + file);
      }
   }
}
