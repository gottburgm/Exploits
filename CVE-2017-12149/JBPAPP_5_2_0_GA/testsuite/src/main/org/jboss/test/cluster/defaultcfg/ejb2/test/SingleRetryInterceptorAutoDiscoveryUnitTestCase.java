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
package org.jboss.test.cluster.defaultcfg.ejb2.test;

import junit.framework.Test;

import org.jboss.test.cluster.testutil.DBSetup;

/**
 * Tests the SingleRetryInterceptor with HA-JNDI autodiscovery.
 * This test is separate from the other SingleRetryInterceptor tests
 * so it can be exclude in tests of configs that disable HA-JNDI auto-discovery.
 * 
 * @author <a href="mailto://brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 106766 $
 */
public class SingleRetryInterceptorAutoDiscoveryUnitTestCase extends RetryInterceptorAutoDiscoveryUnitTestCase
{   
   // NOTE: these variables must be static as apparently a separate instance
   // of this class is created for each test.
   private static boolean deployed0 = false;
   private static boolean deployed1 = false;
   
   /**
    * Create a new SingleRetryInterceptorUnitTestCase.
    * 
    * @param name
    */
   public SingleRetryInterceptorAutoDiscoveryUnitTestCase(String name)
   {
      super(name);
   }  


   public static Test suite() throws Exception
   {
      return DBSetup.getDeploySetup(SingleRetryInterceptorAutoDiscoveryUnitTestCase.class, "cif-ds.xml");
   }
      
   protected String getJndiSuffix()
   {
      return "_SingleRetry";
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