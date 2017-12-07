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

import javax.management.ObjectName;

import org.jboss.test.JBossTestCase;
import org.jboss.test.jmx.eardeployment.a.interfaces.SessionA;
import org.jboss.test.jmx.eardeployment.a.interfaces.SessionAHome;
import org.jboss.test.jmx.eardeployment.b.interfaces.SessionB;
import org.jboss.test.jmx.eardeployment.b.interfaces.SessionBHome;


/** Tests of ear deployment issues
 *
 *
 * Created: Thu Feb 21 20:54:55 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */

public class EarDeploymentUnitTestCase extends JBossTestCase
{
   // Constants -----------------------------------------------------
   protected final static int INSTALLED = 0;
   protected final static int CONFIGURED = 1;
   protected final static int CREATED = 2;
   protected final static int RUNNING = 3;
   protected final static int FAILED = 4;
   protected final static int STOPPED = 5;
   protected final static int DESTROYED = 6;
   protected final static int NOTYETINSTALLED = 7;

   private ObjectName serviceControllerName;

   public EarDeploymentUnitTestCase(String name)
   {
      super(name);

      try
      {
         serviceControllerName =
            new ObjectName("jboss.system:service=ServiceController");
      }
      catch (Exception ignore)
      {
      }
   }

   /**
    * The <code>testEarSubpackageVisibility</code> method tests if the classes in
    * subpackages of an ear are visible to each other when ejb's are deployed.
    * SessionA and SessionB are in different jars, and each refer to the other.
    * 
    *
    * @exception Exception if an error occurs
    */
   public void testEarSubpackageVisibility() throws Exception
   {
      getLog().info("+++ testEarSubpackageVisibility");
      String testUrl = "eardeployment.ear";
      deploy(testUrl);

      try
      {
         SessionAHome aHome = (SessionAHome) getInitialContext().lookup("eardeployment/SessionA");
         SessionBHome bHome = (SessionBHome) getInitialContext().lookup("eardeployment/SessionB");
         SessionA a = aHome.create();
         SessionB b = bHome.create();
         assertTrue("a called b", a.callB());
         assertTrue("b called a", b.callA());
      }
      finally
      {
         undeploy(testUrl);
      }
   }

   public void testEarDepends() throws Exception
   {
      getLog().info("+++ testEarDepends");
      String testUrl = "eardepends.ear";

      ObjectName dependentAName =
         new ObjectName("jboss.j2ee:jndiName=test/DependentA,service=EJB");
      ObjectName dependentBName =
         new ObjectName("jboss.j2ee:jndiName=test/DependentB,service=EJB");

      ObjectName independentName =
         new ObjectName("jboss.j2ee:jndiName=test/Independent,service=EJB");

      ObjectName testName = new ObjectName("test:name=Test");

      if (removeService(dependentAName))
      {
         log.warn(dependentAName + " is registered, removed");
      }
      if (removeService(dependentBName))
      {
         log.warn(dependentBName + " is registered, removed");
      }

      deploy(testUrl);

      try
      {
         assertTrue(dependentAName + " is registered",
            getServer().isRegistered(dependentAName));
         assertTrue(dependentBName + " is registered",
            getServer().isRegistered(dependentBName));

         // Validate that all expected mbeans are in the running state
         assertTrue(dependentAName + " in RUNNING state", checkState(dependentAName, RUNNING));
         assertTrue(dependentBName + " in RUNNING state", checkState(dependentBName, RUNNING));
         assertTrue(testName + " in RUNNING state", checkState(testName, RUNNING));
         assertTrue(independentName + " in RUNNING state", checkState(independentName, RUNNING));
      }
      finally
      {
         undeploy(testUrl);
      }

      try
      {
         assertTrue(dependentAName + " is not registered",
            !getServer().isRegistered(dependentAName));
         assertTrue(dependentBName + " is not registered",
            !getServer().isRegistered(dependentBName));
      }
      finally
      {
         removeService(dependentAName);
         removeService(dependentBName);
      }
   }

   protected boolean removeService(final ObjectName mbean) throws Exception
   {
      boolean isRegistered = getServer().isRegistered(mbean);
      if (isRegistered)
      {
         Object[] args = {mbean};
         String[] sig = {ObjectName.class.getName()};
         invoke(serviceControllerName,
            "remove",
            args,
            sig
         );
      }

      return isRegistered;
   }

   protected boolean checkState(ObjectName mbean, int state) throws Exception
   {
      Integer mbeanState = (Integer) getServer().getAttribute(mbean, "State");
      return state == mbeanState.intValue();
   }

}// EarDeploymentUnitTestCase
