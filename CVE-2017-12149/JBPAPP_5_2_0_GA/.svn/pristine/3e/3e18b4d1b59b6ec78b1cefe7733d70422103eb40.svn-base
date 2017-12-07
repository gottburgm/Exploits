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
package org.jboss.test.jca.test;

import javax.management.ObjectName;

import org.jboss.resource.metadata.ConfigPropertyMetaData;
import org.jboss.test.JBossTestCase;

/**
 * Test case for resource adapters with primitive config-property definitions
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @version $Revision: $
 */

public class PrimitiveUnitTestCase extends JBossTestCase
{

   public PrimitiveUnitTestCase(String name)
   {
      super(name);
   }

   public void testRA() throws Exception
   {
      ObjectName raName = new ObjectName("jboss.jca:name='jcaprimitive.rar',service=RARDeployment");
      deploy("jcaprimitive.rar");
      try
      {
         deploy("jcaprimitive-ds.xml");
         try
         {
            assertTrue("RA should be registered", getServer().isRegistered(raName));
            assertTrue("someIntegerObjectProperty should be 1, is " + getAttribute(raName, "someIntegerObjectProperty"),
                       new Integer(1).equals(getAttribute(raName, "someIntegerObjectProperty")));
            assertTrue("someIntProperty should be 1, is " + getAttribute(raName, "someIntProperty"),
                       new Integer(1).equals(getAttribute(raName, "someIntProperty")));
            assertTrue("someBooleanObjectProperty should be TRUE, is " + getAttribute(raName, "someBooleanObjectProperty"),
                       Boolean.TRUE.equals(getAttribute(raName, "someBooleanObjectProperty")));
            assertTrue("someBooleanProperty should be TRUE, is " + getAttribute(raName, "someBooleanProperty"),
                       Boolean.TRUE.equals(getAttribute(raName, "someBooleanProperty")));
         }
         finally
         {
            undeploy("jcaprimitive-ds.xml");
         }
      }
      finally
      {
         undeploy("jcaprimitive.rar");
      }
   }

   private Object getAttribute(ObjectName raName, String attrName) throws Exception
   {
      ConfigPropertyMetaData cpmd = (ConfigPropertyMetaData)getServer().getAttribute(raName, attrName);
      if ("java.lang.Integer".equals(cpmd.getType()))
      {
         return Integer.valueOf(cpmd.getValue());
      }
      else if ("java.lang.Boolean".equals(cpmd.getType()))
      {
         return Boolean.valueOf(cpmd.getValue());
      }
      return cpmd.getValue();
   }
}
