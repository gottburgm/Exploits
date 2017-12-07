/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.test.naming.test;

import java.util.Properties;

import junit.framework.Test;

import org.jboss.test.naming.restart.NamingRestartTestBase;
import org.jnp.interfaces.NamingContext;

/**
 * A NamingRestartUnitTestCase involving HA-JNDI autodiscovery.
 * This test is separate from the other HA-JNDI tests
 * so it can be excluded in tests of configs that disable HA-JNDI auto-discovery.
 * 
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 106766 $
 */
public class NamingRestartAutoDiscoveryUnitTestCase extends NamingRestartTestBase
{
   private static final String DISCOVERY_ADDRESS = "230.9.9.9";
   private static final String DISCOVERY_PORT = "19102";
   
   private static boolean deployed = false;
   
   public NamingRestartAutoDiscoveryUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(NamingRestartAutoDiscoveryUnitTestCase.class, null);
   }
   
   @Override
   protected boolean isDeployed()
   {
      return deployed;
   }

   @Override
   protected void setDeployed(boolean deployed)
   {
      NamingRestartAutoDiscoveryUnitTestCase.deployed = deployed;
   }
   
   public void testAutoDiscoveryLookupAfterHANamingRestart() throws Exception
   {
      log.info("Running testAutoDiscoveryLookupAfterHANamingRestart");
      
      lookupTest(createNamingEnvironment(DISCOVERY_ADDRESS, DISCOVERY_PORT));
   }

   private Properties createNamingEnvironment(String mcastAddress, String mcastPort)
   {
      Properties env = new Properties();
//      env.setProperty(NamingContext.JNP_PARTITION_NAME, PARTITION_NAME);
      env.setProperty(NamingContext.JNP_LOCAL_ADDRESS, getServerHost());
      env.setProperty(NamingContext.JNP_DISCOVERY_GROUP, mcastAddress);
      env.setProperty(NamingContext.JNP_DISCOVERY_PORT, mcastPort);
      return env;
   }

}
