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
package org.jboss.test.aop.test;

import javax.management.Attribute;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.jboss.test.JBossTestCase;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 85945 $
 */
public class ScopedDependencyTest extends JBossTestCase
{

   public ScopedDependencyTest(String name)
   {
      super(name);
   }

   public void testBeanWithDependencyFromAspect() throws Exception
   {
      //Do this twice since there was a problem with redeployment
      doTestDeployDependencies();
   }

   public void testRedeployedBeanWithDependencyFromAspect() throws Exception
   {
      //Do this twice since there was a problem with redeployment
      doTestDeployDependencies();
   }

   private void doTestDeployDependencies() throws Exception
   {
      try
      {
         deploy("aop-scopeddependency-scoped.sar");
      }
      catch(Exception expected)
      {
         //Since the dependencies are not there, we get an exception...
      }
      
      try
      {
         MBeanServerConnection server = getServer();
         ObjectName testerName = new ObjectName("jboss.aop:name=ScopedTester");
         try
         {
            server.getMBeanInfo(testerName);
            fail(testerName + " should not have been found");
         }
         catch (InstanceNotFoundException expected)
         {
         }
   
         deploy("aop-scopeddependency-global.jar");
         try
         {
            server.setAttribute(testerName, new Attribute("Property", 42));
            assertEquals(42, server.getAttribute(testerName, "Property"));
            String ret = (String)server.invoke(testerName, "someAction", new Object[0], new String[0]);
            assertNotNull(ret);
            assertEquals("true", ret);
         }
         finally
         {
            undeploy("aop-scopeddependency-global.jar");
         }
   
         try
         {
            server.getMBeanInfo(testerName);
            fail(testerName + " should not have been found");
         }
         catch (InstanceNotFoundException expected)
         {
         }
         
      }
      finally
      {
         undeploy("aop-scopeddependency-scoped.sar");
      }
   }

}