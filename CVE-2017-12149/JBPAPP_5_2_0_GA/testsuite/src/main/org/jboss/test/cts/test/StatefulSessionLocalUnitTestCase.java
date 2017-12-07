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
import javax.naming.InitialContext;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.cts.interfaces.StatelessSession;
import org.jboss.test.cts.interfaces.StatelessSessionHome;
import org.jboss.test.util.jms.JMSDestinationsUtil;

/** Tests of stateful session beans
 *
 *   @author kimptoc 
 *   @author Scott.Stark@jboss.org 
 *   @author d_jencks converted to JBossTestCase, added logging.
 *   @version $Revision: 105321 $
 */
public class StatefulSessionLocalUnitTestCase
   extends JBossTestCase
{

   public StatefulSessionLocalUnitTestCase (String name)
   {
      super(name);
   }

   /** Create a StatefulSessionBean and then force passivation by waiting
    for 45 seconds.
    This relies on a custom container config that specifies
    container-cache-conf/cache-policy-conf/remover-period=65
    container-cache-conf/cache-policy-conf/overager-period=40
    container-cache-conf/cache-policy-conf/max-bean-age=30
    container-cache-conf/cache-policy-conf/max-bean-life=60
     */
   public void testPassivationByTimeLocal() throws Exception
   {
      Context ctx = new InitialContext();
      getLog().debug("+++ testPassivationByTimeLocal");
      StatelessSessionHome sessionHome = ( StatelessSessionHome ) ctx.lookup("ejbcts/StatelessSessionHome");
      StatelessSession session = sessionHome.create();
      session.testPassivationByTimeLocal();
      getLog().debug("--- testPassivationByTimeLocal");
   }

   public static Test suite() throws Exception
   {
      return new JBossTestSetup(new TestSuite(StatefulSessionLocalUnitTestCase.class))
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
