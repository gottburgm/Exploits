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

package org.jboss.test.cluster.web;

import junit.framework.Test;
import org.jboss.test.AbstractTestDelegate;
import org.jboss.test.JBossTestClusteredServices;
import org.jboss.test.cluster.testutil.DelegatingClusteredTestCase;
import org.jboss.test.cluster.testutil.TestSetupDelegate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Brian Stansberry
 *
 */
public class JBossClusteredWebTestCase extends DelegatingClusteredTestCase
{

   public static AbstractTestDelegate getDelegate(Class clazz)
       throws Exception
   {
       return new JBossTestClusteredServices(clazz);
   }
   
   public JBossClusteredWebTestCase(String name)
   {
      super(name);
   } 
   
   public String getCacheConfigName()
   {
      return System.getProperty(CacheHelper.CACHE_CONFIG_PROP);
   }

   public static Test getDeploySetup(Test test, String jarNames)
       throws Exception
   {
      return getDeploySetup(test, jarNames, null);
   }

   public static Test getDeploySetup(Class clazz, String jarNames)
       throws Exception
   {
       return getDeploySetup(clazz, jarNames, null);
   }

   public static Test getDeploySetup(Test test, String jarNames, List<TestSetupDelegate> delegates)
       throws Exception
   {
      return DelegatingClusteredTestCase.getDeploySetup(test, getJarNamesWithHelper(jarNames), ensureJBCConfigDelegate(delegates));
   }

   public static Test getDeploySetup(Class<?> clazz, String jarNames, List<TestSetupDelegate> delegates)
       throws Exception
   {
       return DelegatingClusteredTestCase.getDeploySetup(clazz, getJarNamesWithHelper(jarNames), ensureJBCConfigDelegate(delegates));
   }
   
   private static List<TestSetupDelegate> ensureJBCConfigDelegate(List<TestSetupDelegate> delegates)
   {
      List<TestSetupDelegate> result = delegates;
      if (result == null)
      {
         result = Arrays.asList(new TestSetupDelegate[]{new JBossCacheConfigTestSetupDelegate()});
      }
      else
      {
         boolean hasJBCConfig = false;
         for (TestSetupDelegate setup : delegates)
         {
            if (setup instanceof JBossCacheConfigTestSetupDelegate)
            {
               hasJBCConfig = true;
               break;
            }
         }
         
         if (!hasJBCConfig)
         {
            result = new ArrayList<TestSetupDelegate>(delegates);
            result.add(0, new JBossCacheConfigTestSetupDelegate());
         }
      }
      
      return result;
   }
   
   
   private static String getJarNamesWithHelper(String jarNames)
   {
      if (jarNames == null || jarNames.length() == 0)
         return "jbosscache-helper.sar";
      else
         return "jbosscache-helper.sar, " + jarNames;
   }
   
//   public static class JBossClusteredWebTestSetup extends JBossTestSetup
//   {
//      public static final String SYSTEM_PROPS_SVC = "jboss:type=Service,name=SystemProperties";
//      
//      private String cacheConfigName;
//      private String usePojoCache;
//      private String jarNames = null;
//      private JBossTestClusteredServices clusteredServices;
//      
//      /**
//       * Create a new JBossTestClusteredWebSetup.
//       * 
//       * @param test
//       * @param jarNames
//       * @throws Exception
//       */
//      public JBossClusteredWebTestSetup(Test test, String jarNames) throws Exception
//      {
//         super(JBossClusteredWebTestCase.class, test);
//         this.jarNames = getJarNamesWithHelper(jarNames);
//      }
//   
//      @Override
//      protected void setUp() throws Exception
//      {      
//         super.setUp();        
//         
//         getLog().debug("delegate is " + delegate);
//         
//         clusteredServices = (JBossTestClusteredServices) delegate;
//         
//         cacheConfigName = System.getProperty(CacheHelper.CACHE_CONFIG_PROP);  
//         usePojoCache = System.getProperty(CacheHelper.CACHE_TYPE_PROP, "false");
//         if (cacheConfigName != null || Boolean.parseBoolean(usePojoCache))
//         {
//            setServerSideCacheConfigProperties();
//         }
//         
//         deployJars();
//      }
//   
//      @Override
//      protected void tearDown() throws Exception
//      {
//         try
//         {
//            undeployJars();
//            
//            super.tearDown();
//            if (cacheConfigName != null)
//            {
//               clearServerSideCacheConfigProperties();
//            }
//         }
//         finally
//         {
//            AbstractTestSetup.delegate = null;
//         }
//      }
//   
//      private void setServerSideCacheConfigProperties() throws Exception
//      {
//         getLog().debug("configuring server with cacheConfigName=" + cacheConfigName + " and usePojoCache=" + usePojoCache);
//         
//         ObjectName on = new ObjectName(SYSTEM_PROPS_SVC);
//         for (MBeanServerConnection adaptor : clusteredServices.getAdaptors())
//         {
//            adaptor.invoke(on, "set", 
//                           new Object[]{CacheHelper.CACHE_CONFIG_PROP, cacheConfigName}, 
//                           new String[] {String.class.getName(), String.class.getName()});
//
//            adaptor.invoke(on, "set", 
//                           new Object[]{CacheHelper.CACHE_TYPE_PROP, usePojoCache}, 
//                           new String[] {String.class.getName(), String.class.getName()});
//         }         
//      }
//   
//      private void clearServerSideCacheConfigProperties() throws Exception
//      {
//         ObjectName on = new ObjectName(SYSTEM_PROPS_SVC);
//         for (MBeanServerConnection adaptor : clusteredServices.getAdaptors())
//         {
//            adaptor.invoke(on, "remove", 
//                           new Object[]{CacheHelper.CACHE_CONFIG_PROP}, 
//                           new String[] {String.class.getName()});
//
//            adaptor.invoke(on, "remove", 
//                           new Object[] {CacheHelper.CACHE_TYPE_PROP}, 
//                           new String[] {String.class.getName()});
//         } 
//      }
//      
//      private void deployJars() throws Exception
//      {      
//         JBossTestCase.deploymentException = null;
//         try
//         {
//            // deploy the comma seperated list of jars
//            StringTokenizer st = new StringTokenizer(jarNames, ", ");
//            while (st.hasMoreTokens())
//            {
//               String jarName = st.nextToken();
//               this.redeploy(jarName);
//               this.getLog().debug("deployed package: " + jarName);
//            }
//         }
//         catch (Exception ex)
//         {
//            // Throw this in testServerFound() instead.
//            JBossTestCase.deploymentException = ex;
//         }
//             
//         // wait a couple seconds to let the cluster stabilize
//         synchronized (this)
//         {
//            wait(2000);
//         }
//      }
//      
//      private void undeployJars() throws Exception
//      {
//         // deploy the comma seperated list of jars
//         StringTokenizer st = new StringTokenizer(jarNames, ", ");
//         String[] depoyments = new String[st.countTokens()];
//         for (int i = depoyments.length - 1; i >= 0; i--)
//            depoyments[i] = st.nextToken();
//         for (int i = 0; i < depoyments.length; i++)
//         {
//            String jarName = depoyments[i];
//            this.getLog().debug("Attempt undeploy of " + jarName);
//            this.undeploy(jarName);
//            this.getLog().debug("undeployed package: " + jarName);
//         }   
//      }
//   
//   }

}
