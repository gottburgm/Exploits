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
package org.jboss.test.jmx.compliance.server;

import java.util.List;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;


public class MBeanServerFactoryTEST extends TestCase
{
   public MBeanServerFactoryTEST(String s)
   {
      super(s);
   }

   public void testFindNonCreated()
   {
      MBeanServerFactory.newMBeanServer();
      List mbsList = MBeanServerFactory.findMBeanServer(null);
      assertEquals(0, mbsList.size());
   }

   public void testCreateFindAndRelease()
   {
      MBeanServer server = null;
      List mbsList = null;

      try
      {
         server = MBeanServerFactory.createMBeanServer();
         mbsList = MBeanServerFactory.findMBeanServer(null);
         assertEquals(1, mbsList.size());
      }
      finally
      {
         if (null != server)
         {
            MBeanServerFactory.releaseMBeanServer(server);
         }
      }

      mbsList = MBeanServerFactory.findMBeanServer(null);
      assertEquals(0, mbsList.size());
   }

   public void testRemoveNonCreated()
   {
      try
      {
         MBeanServer server = MBeanServerFactory.newMBeanServer();
         MBeanServerFactory.releaseMBeanServer(server);
         fail("expected an IllegalArgumentException");
      }
      catch (IllegalArgumentException e)
      {
      }
      catch (Exception e)
      {
         fail("expected an IllegalArgumentException but got: " + e.getMessage());
      }
   }

   public void testDomainCreated()
   {
      String domain = "dOmAiN";
      MBeanServer server = null;
      try
      {
         server = MBeanServerFactory.createMBeanServer(domain);
         assertEquals(domain, server.getDefaultDomain());
         List mbsList = MBeanServerFactory.findMBeanServer(null);
         assertEquals(server, mbsList.get(0));
         assertTrue("expected server reference equality", mbsList.get(0) == server);
      }
      finally
      {
         if (null != server)
         {
            MBeanServerFactory.releaseMBeanServer(server);
         }
      }
   }

   public void testDomainNonCreated()
   {
      String domain = "dOmAiN";
      MBeanServer server = MBeanServerFactory.newMBeanServer(domain);
      assertEquals(domain, server.getDefaultDomain());
   }

   public void testFindByAgentID()
   {
      try
      {
         MBeanServer server1 = MBeanServerFactory.createMBeanServer();
         MBeanServer server2 = MBeanServerFactory.createMBeanServer();
         MBeanServer server3 = MBeanServerFactory.newMBeanServer();
         ObjectName delegateName = new ObjectName("JMImplementation:type=MBeanServerDelegate");
         
         String agentID1 = (String)server1.getAttribute(delegateName, "MBeanServerId");
         String agentID2 = (String)server2.getAttribute(delegateName, "MBeanServerId");
         String agentID3 = (String)server3.getAttribute(delegateName, "MBeanServerId");
         
         assertTrue((MBeanServer)MBeanServerFactory.findMBeanServer(agentID1).get(0) == server1);
         assertTrue((MBeanServer)MBeanServerFactory.findMBeanServer(agentID2).get(0) == server2);
         assertTrue(MBeanServerFactory.findMBeanServer(agentID3).size() == 0);
      }
      catch (AssertionFailedError e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         fail("Unexpected error: " + t.toString());
      }
   }

}
