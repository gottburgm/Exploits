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
package org.jboss.test.isolation.test;

import javax.management.Attribute;

import org.jboss.deployment.EARDeployerMBean;
import org.jboss.deployment.EarClassLoaderDeployerMBean;
import org.jboss.test.JBossTestCase;
import org.jboss.test.isolation.interfaces.a.SessionA;
import org.jboss.test.isolation.interfaces.a.SessionAHome;

/**
 * A IsolationUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81036 $
 */
public class IsolationUnitTestCase extends JBossTestCase
{
   public IsolationUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testIsolation() throws Exception
   {
      isolateDeployments(Boolean.TRUE);
      try
      {
         deploy("isolationA.ear");
         try
         {
            // Run the test
            deploy("isolationB.ear");
            try
            {
               doTest();
            }
            finally
            {
               undeploy("isolationB.ear");
            }

            // Run the test after a redeployment
            deploy("isolationB.ear");
            try
            {
               doTest();
            }
            finally
            {
               undeploy("isolationB.ear");
            }
         }
         finally
         {
            undeploy("isolationA.ear");
         }
      }
      finally
      {
         isolateDeployments(Boolean.FALSE);
      }
   }
   
   private void doTest() throws Exception
   {
      SessionAHome home = (SessionAHome) getInitialContext().lookup("SessionA");
      SessionA session = home.create();
      session.invokeSessionB();
   }
   
   private void isolateDeployments(Boolean value) throws Exception
   {
      getServer().setAttribute(EarClassLoaderDeployerMBean.OBJECT_NAME, new Attribute("Isolated", value));
      getServer().setAttribute(EARDeployerMBean.OBJECT_NAME, new Attribute("CallByValue", value));
   }
}
