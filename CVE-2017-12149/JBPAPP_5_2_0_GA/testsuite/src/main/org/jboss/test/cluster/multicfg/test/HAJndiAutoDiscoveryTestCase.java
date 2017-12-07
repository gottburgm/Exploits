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
package org.jboss.test.cluster.multicfg.test;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.test.NamingUtil;
import org.jnp.interfaces.NamingContext;

/**
 * HA-JNDI clustering tests involving autodiscovery.
 * This test is separate from the other HA-JNDI tests
 * so it can be excluded in tests of configs that disable HA-JNDI auto-discovery.
 *
 * @author Jerry Gauthier
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 * @version $Revision: 113161 $
 */
public class HAJndiAutoDiscoveryTestCase
      extends HAJndiTestBase      
{   
   // BINDING KEYS and VALUES
   private static final String LOCAL1_KEY = "org.jboss.test.cluster.test.Local1Key";
   private static final String LOCAL1_VALUE = "Local1Value";
   private static final String JNDI_KEY3 = "org.jboss.test.cluster.test.JNDIKey3";
   private static final String JNDI_VALUE3 = "JNDIValue3";
	
   public HAJndiAutoDiscoveryTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(HAJndiAutoDiscoveryTestCase.class, "cross-server.jar,naming-util.war");
   }
   
   /**
    * Test HA-JNDI AutoDiscovery
    *
    * @throws Exception
    */
   public void testAutoDiscovery()
      throws Exception
   {
      getLog().debug("HAJndiTestCase.testAutoDiscovery()");
      validateUrls();
      
      // this test doesn't run properly if node0=localhost or node0=127.0.0.1
      // because the jndi code would find localhost:1099 server and would use 
      // that one
      if (NODE0 != null && (NODE0.equalsIgnoreCase("localhost") || NODE0.equalsIgnoreCase("127.0.0.1")))
      {
         getLog().debug("testAutoDiscovery() - test skipped because node0=localhost");
         return;
      }
      
      // bind to node1 locally
      NamingUtil.createRemoteTestJNDIBinding("/", LOCAL1_KEY, LOCAL1_VALUE, NamingUtil.extractHostnameFromUrl(NODE1_JNDI), false);

      
      // bind to node0 using HA-JNDI
      NamingUtil.createRemoteTestJNDIBinding("/", JNDI_KEY3, JNDI_VALUE3, NamingUtil.extractHostnameFromUrl(NODE0_HAJNDI), true);
     
      //create context with AutoDiscovery enabled
      Context naming = getAutoDiscoveryContext(false);
      
      // lookup local binding using HA-JNDI AutoDiscovery - should succeed
      String value = (String)lookup(naming, LOCAL1_KEY, true);
      assertEquals("local lookup with AutoDiscovery enabled", LOCAL1_VALUE, value);
      
      // lookup HA binding using HA-JNDI AutoDiscovery - should succeed
      value = (String)lookup(naming, JNDI_KEY3, true);
      assertEquals("lookup of HA-JNDI binding with AutoDiscovery enabled", JNDI_VALUE3, value);     
      
      // now disable AutoDiscovery and confirm that the same lookups fail
      closeContext(naming);
      naming = getAutoDiscoveryContext(true);
      
      // lookup local binding without HA-JNDI AutoDiscovery - should fail
      value = (String)lookup(naming, LOCAL1_KEY, false);
      assertNull("local lookup with AutoDiscovery disabled", value);
      
      // lookup HA binding without HA-JNDI AutoDiscovery - should fail
      value = (String)lookup(naming, JNDI_KEY3, false);
      assertNull("lookup of HA-JNDI binding with AutoDiscovery disabled", value);
      
      closeContext(naming);

   }
   
   private Context getAutoDiscoveryContext(boolean autoDisabled)
      throws Exception
   {
      // do not add any urls to the context
      Properties env = new Properties();        
      env.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
      env.setProperty(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
      // Don't let the discovery packet off the test server so we don't
      // get spurious responses from other servers on the network
      env.setProperty(NamingContext.JNP_DISCOVERY_TTL, DISCOVERY_TTL);

      if (autoDisabled)
      {
         env.put(NamingContext.JNP_DISABLE_DISCOVERY, "true");
      }
      else 
      {
         if (DISCOVERY_GROUP != null && "".equals(DISCOVERY_GROUP) == false)      
         {
            // Use the multicast address this test environment is using
            env.put(NamingContext.JNP_DISCOVERY_GROUP, DISCOVERY_GROUP);
         }
         if (DISCOVERY_PARTITION != null && "".equals(DISCOVERY_PARTITION) == false)
         {
            // Limit to the partition this test environment is using
            env.put(NamingContext.JNP_PARTITION_NAME, DISCOVERY_PARTITION);
         }
      }  
        
      Context naming = new InitialContext (env);
      return naming;

   }

}
