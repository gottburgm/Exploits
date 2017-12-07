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
package org.jboss.test.cluster.defaultcfg.ejb2.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Properties;

import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.logging.Logger;
import org.jboss.test.cluster.testutil.DBSetup;
import org.jnp.interfaces.NamingContext;

/**
 * Tests the RetryInterceptor with HA-JNDI autodiscovery.
 * This test is separate from the other SingleRetryInterceptor tests
 * so it can be excluded in tests of configs that disable HA-JNDI auto-discovery.
 * 
 * @author <a href="mailto://brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision $
 */
public class RetryInterceptorAutoDiscoveryUnitTestCase extends RetryInterceptorTestBase
{   
   // NOTE: these variables must be static as apparently a separate instance
   // of this class is created for each test.
   private static boolean deployed0 = false;
   private static boolean deployed1 = false;
   
   /**
    * Create a new RetryInterceptorUnitTestCase.
    * 
    * @param name
    */
   public RetryInterceptorAutoDiscoveryUnitTestCase(String name)
   {
      super(name);
      log = Logger.getLogger(getClass());
   }

   public static Test suite() throws Exception
   {
      return DBSetup.getDeploySetup(RetryInterceptorAutoDiscoveryUnitTestCase.class, "cif-ds.xml");
   }
   
   /**
    * Tests that the retry interceptor works properly if the naming context
    * is established by using org.jnp.interfaces.NamingContextFactory
    * and auto-discovery is enabled.
    *  
    * @throws Exception
    */
   public void testRetryWithJnpAndAutoDiscovery() throws Exception
   {
      getLog().debug("+++ Enter testRetryWithJnpAndAutoDiscovery()");
      
      // Create a jndi.properties in the temp dir with special configs
      // to prevent autodiscovery spuriously discovering random servers
      // on the network. When the RetryCaller runs, it will create a 
      // special classloader that will pick up this file
      if (customJndiDir == null)
         customJndiDir = new File(System.getProperty("java.io.tmpdir"), 
                                  "retry-int-test");
      if (!customJndiDir.exists())
         customJndiDir.mkdir();
      customJndiProperties = new File(customJndiDir, "jndi.properties");
      FileOutputStream fos = new FileOutputStream(customJndiProperties);
      OutputStreamWriter writer = new OutputStreamWriter(fos);
      writer.write(NamingContext.JNP_DISCOVERY_TTL + "=" + DISCOVERY_TTL + "\n");
      if (DISCOVERY_GROUP != null && "".equals(DISCOVERY_GROUP) == false)
      {
         // The server isn't listening on the std multicast address
         writer.write(NamingContext.JNP_DISCOVERY_GROUP + "=" + DISCOVERY_GROUP + "\n");
      }
      if (DISCOVERY_PARTITION != null && "".equals(DISCOVERY_PARTITION) == false)
      {
         // Limit to the partition this test environment is using
         writer.write(NamingContext.JNP_PARTITION_NAME + "=" + DISCOVERY_PARTITION + "\n");
      }
      writer.close();
      getLog().debug("Created custom jndi.properties at " + customJndiProperties + 
                     " -- DISCOVERY_GROUP is " + DISCOVERY_GROUP +
                     " -- DISCOVERY_PARTITION is " + DISCOVERY_PARTITION);
      
      
      Properties env = getNamingProperties("org.jnp.interfaces.NamingContextFactory", true);
      
      InitialContext ctx = new InitialContext(env);
      
      sfsbTest(ctx, env);
   }

   protected boolean isDeployed0()
   {
      return deployed0;
   }

   protected void setDeployed0(boolean deployed)
   {
      deployed0 = deployed;
   }

   protected boolean isDeployed1()
   {
      return deployed1;
   }

   protected void setDeployed1(boolean deployed)
   {
      deployed1 = deployed;
   }

}
