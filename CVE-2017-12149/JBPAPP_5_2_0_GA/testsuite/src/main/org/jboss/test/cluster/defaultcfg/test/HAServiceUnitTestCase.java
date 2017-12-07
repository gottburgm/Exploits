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
package org.jboss.test.cluster.defaultcfg.test;

import java.util.List;

import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.ObjectName;

import junit.framework.Test;

import org.jboss.test.JBossClusteredTestCase;

/**
 * Verifies that public methods of an HAServiceMBeanSupport subclass are accessible via the HAService's rpc mechanism.
 * 
 * @author Paul Ferraro
 */
public class HAServiceUnitTestCase extends JBossClusteredTestCase
{
   public HAServiceUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      return JBossClusteredTestCase.getDeploySetup(HAServiceUnitTestCase.class, "ha-service.sar");
   }

   public void testEcho() throws Exception
   {
      Boolean result = this.testMethod("echo", new Object[] { false }, new Class[] { Boolean.TYPE }, Boolean.class);
      
      assertFalse(result.booleanValue());
      
      result = this.testMethod("echo", new Object[] { true }, new Class[] { Boolean.TYPE }, Boolean.class);
      
      assertTrue(result.booleanValue());
   }

   public void testHandleEvent() throws Exception
   {
      Void result = this.testMethod("handleEvent", new Object[] { new Notification("test", "source", 1) }, new Class[] { Notification.class }, Void.class);
      
      assertNull(result);
   }
   
   private <T> T testMethod(String method, Object[] parameters, Class<?>[] types, Class<T> resultType) throws Exception
   {
      MBeanServerConnection[] adaptors = this.getAdaptors();
      ObjectName name = ObjectName.getInstance("jboss.ha:service=EchoHAService");
      Object[] args = new Object[] { method, parameters, types };
      String[] signature = new String[] { String.class.getName(), Object[].class.getName(), Class[].class.getName() };
      List<?> responses = (List<?>) adaptors[0].invoke(name, "callMethodOnPartition", args, signature);
      
      this.log.debug("Response list: " + responses);
      
      assertEquals(1, responses.size());
      
      Object response = responses.get(0);
      
      if (response instanceof Exception)
      {
         throw (Exception) response;
      }

      return resultType.cast(response);
   }
   
   public void testSendNotification() throws Exception
   {
      MBeanServerConnection[] adaptors = this.getAdaptors();
      ObjectName name = new ObjectName("jboss.ha:service=EchoHAService");
      adaptors[0].invoke(name, "sendNotification", new Object[] { new Notification("test", "source", 1) }, new String[] { Notification.class.getName() });
   }
}
