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

import javax.naming.Context;

import org.jboss.test.cts.interfaces.StatefulSession;
import org.jboss.test.cts.interfaces.StatefulSessionHome;
import org.jboss.test.util.jms.JMSDestinationsUtil;
import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Long wait test
 *
 * @author adrian@jboss.org
 * @version $Revision: 105321 $
 */
public class LongWaitStatefulSessionUnitTestCase
   extends JBossTestCase
{
   public LongWaitStatefulSessionUnitTestCase (String name)
   {
      super(name);
   }

	/** 
     * Invoke a bean that waits for longer than the passivation time
     */
	public void testLongWait() throws Exception
	{
      Context ctx = getInitialContext();
      getLog().debug("+++ testLongWait");
      StatefulSessionHome sessionHome = ( StatefulSessionHome ) ctx.lookup("ejbcts/LongWaitStatefulSessionBean");
      StatefulSession sessionBean = sessionHome.create("testLongWait");		

      getLog().debug("Sleeping...");
      sessionBean.sleep(5000);
      sessionBean.ping();
      getLog().debug("+++ testLongWait passed");
	}

   public static Test suite() throws Exception
   {
      return new JBossTestSetup(new TestSuite(LongWaitStatefulSessionUnitTestCase.class))
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
