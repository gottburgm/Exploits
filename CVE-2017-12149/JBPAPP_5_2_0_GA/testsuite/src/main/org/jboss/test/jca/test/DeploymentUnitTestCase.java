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
package org.jboss.test.jca.test;

import java.net.URL;
import javax.management.ObjectName;

import org.jboss.test.JBossTestCase;


/**
 * DeploymentUnitTestCase.java
 *
 *
 * Created: Fri Nov 22 14:01:07 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 */

public class DeploymentUnitTestCase extends JBossTestCase
{

   public DeploymentUnitTestCase(String name)
   {
      super(name);
   }

   public void testMCFDefaultValues() throws Exception
   {
      ObjectName mcfName = new ObjectName("jboss.jca:service=ManagedConnectionFactory,name=JBossTestCF");
      deploy("jbosstestadapter.rar");
      try
      {
         deploy("testadapter-ds.xml");
         try
         {
            assertTrue("MCF should be registered", getServer().isRegistered(mcfName));
            assertTrue("IntegerProperty should be 2, is " + getAttribute(mcfName, "IntegerProperty"), new Integer(2).equals(getAttribute(mcfName, "IntegerProperty")));
            assertTrue("BooleanProperty should be false, is " + getAttribute(mcfName, "BooleanProperty"), Boolean.FALSE.equals(getAttribute(mcfName, "BooleanProperty")));
            assertTrue("DoubleProperty should be 5.5, is " + getAttribute(mcfName, "DoubleProperty"), new Double(5.5).equals(getAttribute(mcfName, "DoubleProperty")));
            assertTrue("UrlProperty should be 'http://www.jboss.org', is " + getAttribute(mcfName, "UrlProperty"), new URL("http://www.jboss.org").equals(getAttribute(mcfName, "UrlProperty")));

            assertTrue("DefaultIntegerProperty should be 3, is " + getAttribute(mcfName, "DefaultIntegerProperty"), new Integer(3).equals(getAttribute(mcfName, "DefaultIntegerProperty")));


         }
         finally
         {
            undeploy("testadapter-ds.xml");
         } // end of try-catch
      }
      finally
      {
         undeploy("jbosstestadapter.rar");
      } // end of try-catch

   }

   private Object getAttribute(ObjectName mcfName, String attrName) throws Exception
   {
      return getServer().invoke(mcfName,
                                "getManagedConnectionFactoryAttribute",
                                new Object[] {attrName},
                                new String[] {String.class.getName()});
   }


}// DeploymentUnitTestCase
