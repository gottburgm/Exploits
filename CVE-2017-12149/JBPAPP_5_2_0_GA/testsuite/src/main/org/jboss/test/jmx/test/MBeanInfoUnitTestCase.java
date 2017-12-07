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

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ObjectName;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;

/** 
 * Tests for that metadata exposes for an mbean is the same,
 * when the mbean is deployed as a standard mbean or an
 * xmbean. 
 *
 * @author Dimitris.Andreadis@jboss.org
 * @version $Revision: 81036 $
 */
public class MBeanInfoUnitTestCase extends JBossTestCase
{
   public MBeanInfoUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
      throws Exception
   {
      return getDeploySetup(MBeanInfoUnitTestCase.class, "mbeaninfo-xmbean.sar");
   }

   public void testMBeanInfoStandardMBean() throws Exception
   {
      getLog().info("+++ testMBeanInfoStandardMBean");
      ObjectName target = new ObjectName("jboss.test:name=mbeaninfo,type=standard");
      MBeanInfo info = getServer().getMBeanInfo(target);
      checkMBeanInfo(info);
   }
   
   public void testMBeanInfoXMBean() throws Exception
   {
      getLog().info("+++ testMBeanInfoXMBean");
      ObjectName target = new ObjectName("jboss.test:name=mbeaninfo,type=xmbean");
      MBeanInfo info = getServer().getMBeanInfo(target);
      checkMBeanInfo(info);
   }   
   
   private void checkMBeanInfo(MBeanInfo info)
   {
      // Verify the 1 attribute we expect
      MBeanAttributeInfo[] attrs = info.getAttributes();
      assertTrue("mbean has 1 attribute", attrs.length == 1);
      assertTrue("attribute[0] name is 'StringAttr'", attrs[0].getName().equals("StringAttr"));
      assertTrue("attribute[0] type is 'java.lang.String'", attrs[0].getType().equals("java.lang.String"));
      
      // Verify the 1 operation we expect
      MBeanOperationInfo[] ops = info.getOperations();
      assertTrue("mbean has 1 operation", ops.length == 1);
      assertTrue("operation[0] is 'echo'", ops[0].getName().equals("echo"));
      assertTrue("operation[0] accepts one parameter", ops[0].getSignature().length == 1);
      assertTrue("operation[0] returns 'java.lang.String'", ops[0].getReturnType().equals("java.lang.String"));
   }

}
