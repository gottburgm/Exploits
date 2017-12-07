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
package org.jboss.test.jbossmx.implementation.util;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.jboss.test.jbossmx.implementation.TestCase;

import org.jboss.mx.server.ServerConstants;
import org.jboss.mx.util.AgentID;


public class AgentIDTestCase
   extends TestCase 
   implements ServerConstants
{
   public AgentIDTestCase(String s)
   {
      super(s);
   }

   public void testCreate()
   {
      String id1 = AgentID.create();
      String id2 = AgentID.create();
      
      assertTrue(!id1.equals(id2));
   }
   
   public void testGet()
   {
      try
      {
         MBeanServer server = MBeanServerFactory.createMBeanServer();
         String id1 = (String)server.getAttribute(new ObjectName(MBEAN_SERVER_DELEGATE), "MBeanServerId");
         String id2 = AgentID.get(server);
         
         assertTrue(id1.equals(id2));
      }
      catch (Throwable t)
      {
         log.debug("failed", t);
         fail("Unexpected error: " + t.toString());
      }  
   }
   
}
