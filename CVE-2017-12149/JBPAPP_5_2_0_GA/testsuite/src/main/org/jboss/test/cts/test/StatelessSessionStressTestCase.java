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


import java.rmi.server.UnicastRemoteObject;

import javax.naming.InitialContext;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.cts.interfaces.StatelessSession;
import org.jboss.test.cts.interfaces.StatelessSessionHome;
import org.jboss.test.util.jms.JMSDestinationsUtil;

/**
 * Class StatelessSessionStressTestCase
 *
 *
 * @author d_jencks converted to JBossTestCase and logging.
 * @author Scott.Stark@jboss.org
 * @version $Revision: 105321 $
 */
public class StatelessSessionStressTestCase
   extends JBossTestCase
{
   StatelessSession sessionBean;

   public StatelessSessionStressTestCase (String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      InitialContext ctx = new InitialContext();
      StatelessSessionHome home =
         ( StatelessSessionHome ) ctx.lookup("ejbcts/StatelessSessionHome");
      sessionBean = home.create();
   }
   protected void tearDown() throws Exception
   {
      if( sessionBean != null )
         sessionBean.remove();
   }

   public void testBasicStatelessSession()
      throws Exception
   {
      getLog().debug("+++ testBasicStatelessSession()");
      String result = sessionBean.method1("testBasicStatelessSession");
      // Test response
      assertTrue(result.equals("testBasicStatelessSession"));
      sessionBean.remove();
   }

   public void testClientCallback()
      throws Exception
   {
      getLog().debug("+++ testClientCallback()");
      ClientCallbackImpl callback = new ClientCallbackImpl();
      UnicastRemoteObject.exportObject(callback);
      sessionBean.callbackTest(callback, "testClientCallback");
      // Test callback data
      this.assertTrue(callback.wasCalled());
      UnicastRemoteObject.unexportObject(callback, true);
      sessionBean.remove();
   }

   public void testRuntimeError()
      throws Exception
   {
      getLog().debug("+++ testRuntimeError()");
      try
      {
         sessionBean.npeError();
         fail("npeError should have thrown an exception");
      }
      catch(Exception e)
      {
         getLog().debug("Call threw exception", e);
      }
      sessionBean.remove();
   }

   public static Test suite() throws Exception
   {
      return new JBossTestSetup(new TestSuite(StatelessSessionStressTestCase.class))
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

