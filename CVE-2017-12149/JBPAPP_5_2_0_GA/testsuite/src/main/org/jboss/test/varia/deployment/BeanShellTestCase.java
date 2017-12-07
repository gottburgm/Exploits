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
package org.jboss.test.varia.deployment;

import javax.management.*; import junit.framework.Test;
import org.jboss.test.JBossTestCase;

/** 
 * Deploys services for testing the BeanShellDeployer.
 *
 * @author genman@noderunner.net
 * @version $Revision: 81036 $
 */
public class BeanShellTestCase
   extends JBossTestCase
{


   public BeanShellTestCase (String name)
   {
      super(name);
   }

   public void testBasic() throws Exception
   {
      String file = "../resources/deployers/bsh/test.bsh";
      ObjectName on = new ObjectName("jboss.test:service=test.bsh");
      ObjectName dep = new ObjectName("jboss:name=SystemProperties,type=Service");
      deploy(file);
      try
      {
         String c = Object.class.getName();
         Integer i = (Integer)invoke(on, "compare", 
               new Object[] { "x", "x" }, 
               new String[] { c, c });
         assertEquals(2, i.intValue());
         invoke(dep, "stop", null, null);
         i = (Integer)invoke(on, "compare", 
               new Object[] { "x", "x" }, 
               new String[] { c, c });
         assertEquals(3, i.intValue());
      }
      finally
      {
         undeploy(file);
         invoke(dep, "start", new Object[0], new String[0]);
      }
   }

   public void testFailing() throws Exception
   {
      String file = "../resources/deployers/bsh/test2.bsh";
      try
      {
         deploy(file);
         fail("shouldn't have deployed");
      }
      catch (Exception e)
      {
         getLog().debug(e, e);
      }
      finally
      {
         undeploy(file);
      }
   }

   public void testSimple() throws Exception
   {
      String file = "../resources/deployers/bsh/test3.bsh";
      ObjectName on = new ObjectName("jboss.test:service=test3.bsh");
      deploy(file);
      try
      {
         assertTrue("registered", getServer().isRegistered(on));
      }
      finally
      {
         undeploy(file);
      }
   }

}
