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
package org.jboss.test.jmx.test;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.jboss.test.JBossTestCase;
import org.jboss.test.jmx.conf.SimpleBean;

/**
 * MBean attribute configuration tests
 * 
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81036 $
 */
public class MBeanAttributeConfigurationUnitTestCase extends JBossTestCase
{
   public MBeanAttributeConfigurationUnitTestCase(String name)
   {
      super(name);
   }

   /**
    * Test we can properly configure attributes in a normal deployment
    */
   public void testNormalAttributeConfiguration() throws Exception
   {
      getLog().info("+++ testNormalAttributeConfiguration");
      
      performTest("conftestnormal.sar");      
   }
   
   /**
    * Test we can properly configure Class/Class[] attributes
    * in a scoped deployment
    * 
    * see [JBAS-1709]
    */  
   public void testScopedAttributeConfiguration() throws Exception
   {
      getLog().info("+++ testScopedAttributeConfiguration");

      performTest("conftestscoped.sar");
   }
      
   /**
    * The actual test, common for both normal and scoped deployments
    */
   private void performTest(String testService) throws Exception
   {
      try
      {
         deploy(testService);
         
         MBeanServerConnection server = super.getServer();
         ObjectName target = new ObjectName("test:name=MBeanAttributeConfiguration");
         
         Class clazz = (Class)server.getAttribute(target, "ClassAttr");
         assertTrue("ClassAttr of correct type", clazz.getName().equals("org.jboss.test.jmx.conf.SimpleClass1"));
         
         Class[] clazzes = (Class[])server.getAttribute(target, "ClassArrayAttr");
         assertTrue("ClassArrayAttr array length == 2", clazzes.length == 2);
         assertTrue("ClassArrayAttr[0] of correct type", clazzes[0].getName().equals("org.jboss.test.jmx.conf.SimpleClass1"));
         assertTrue("ClassArrayAttr[1] of correct type", clazzes[1].getName().equals("org.jboss.test.jmx.conf.SimpleClass2"));
         
         SimpleBean bean = (SimpleBean)server.getAttribute(target, "BeanAttr");
         assertTrue("bean.getAString() == 'string'", bean.getAString().equals("string"));
         assertTrue("bean.getAStringArray().length == 2", bean.getAStringArray().length == 2);
         assertTrue("ean.getAStringArray()[0] == 'string1'", bean.getAStringArray()[0].equals("string1"));
         assertTrue("ean.getAStringArray()[0] == 'string1'", bean.getAStringArray()[1].equals("string2"));
      }
      catch (Exception e)
      {
         getLog().warn("Caught exception", e);
         fail("Unexcepted Exception, see the Log file");
      }
      finally
      {
         undeploy(testService);
      }
   }     
}