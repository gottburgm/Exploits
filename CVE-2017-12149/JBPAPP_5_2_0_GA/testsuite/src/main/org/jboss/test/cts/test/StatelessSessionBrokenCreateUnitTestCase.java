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
package org.jboss.test.cts.test;

import java.rmi.RemoteException;

import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.cts.interfaces.StatelessSession;
import org.jboss.test.cts.interfaces.StatelessSessionHome;
import org.jboss.test.util.jms.JMSDestinationsUtil;

/**
 *  Simple test to assert the correct exception when
 *  an instance cannot be created.<p>
 *  
 *  Put in a separate test because this currently
 *  stops the session bean from being usable at all.
 *
 *  @author adrian@jboss.com
 *  @version $Revision: 105321 $
 */
public class StatelessSessionBrokenCreateUnitTestCase extends JBossTestCase
{
   public StatelessSessionBrokenCreateUnitTestCase(String name)
   {
      super(name);
   }

   public void testCreateExceptionFromRemoteInterface() throws Exception
   {
      getLog().debug("+++ testCreateExceptionFromInterface()");
      InitialContext ctx = new InitialContext();
      Object ref = ctx.lookup("ejbcts/StatelessSessionHome");
      StatelessSessionHome home = (StatelessSessionHome) PortableRemoteObject.narrow(ref, StatelessSessionHome.class);
      StatelessSession sessionBean = home.create();
      sessionBean.breakCreate();
      ObjectName pool = new ObjectName("jboss.j2ee:jndiName=ejbcts/StatelessSessionHome,plugin=pool,service=EJB");
      getServer().invoke(pool, "clear", null, null);
      try
      {
         sessionBean.method1("This should cause a CreateException");
         fail("Should not be here");
      }
      catch (RemoteException expected)
      {
         log.debug("Expected", expected);
      }
   }

   public static Test suite() throws Exception
   {
      return new JBossTestSetup(new TestSuite(StatelessSessionBrokenCreateUnitTestCase.class))
      {
         public void setUp() throws Exception
         {
            super.setUp();
            JMSDestinationsUtil.setupBasicDestinations();
            deploy("cts.jar");
            
         }
         
         public void tearDown() throws Exception
         {
            undeploy("cts.jar");
            JMSDestinationsUtil.destroyDestinations();
         }
      };
   }
}
