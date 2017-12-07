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
package org.jboss.test.classloader.leak.test;

import junit.framework.Test;

/**
 * Test for classloader leaks following deployment, use and undeployment
 * of wars that instantiate AOP-prepared classes. These wars do very little
 * with the classes; just instantiate some objects, populate fields, etc; don't
 * even cache the objects in a session.  Basically testing for pure AOP issues.
 * <p/>
 * If these tests are run with JBoss Profiler's jbossAgent (.dll or .so) on the path
 * and the AS is started with -agentlib:jbossAgent, in case of classloader leakage
 * an extensive report will be logged to the server log, showing the path to root of
 * all references to the classloader.
 * 
 * @author Brian Stansberry
 */
public class AopPreparedClassesClassloaderLeakUnitTestCase 
   extends org.jboss.test.classloader.leak.test.ClassloaderLeakTestBase
{
   private static final String NO_CACHE_REPLICABLE_WAR = "classloader-leak-nocache-replicable.war";
   private static final String NO_CACHE_NO_REPLICABLE_WAR = "classloader-leak-nocache-noreplicable.war";
   
   
   public AopPreparedClassesClassloaderLeakUnitTestCase(String name)
   {
      super(name);
   }


   public static Test suite() throws Exception
   {
      return getDeploySetup(AopPreparedClassesClassloaderLeakUnitTestCase.class, "classloader-leak-test.sar");
   }
   
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      // This test runs in a clustered config, so use the cluster config
      // props to create the base URL
      
      String urlProp = "jbosstest.cluster.node0.http.url";
      String urlDefault = "http://" + getServerHost() + ":8080";
      String urlValue = System.getProperty(urlProp, urlDefault);
      log.debug("Http Url for node is:" + urlValue);
      baseURL = urlValue + "/" + getWarContextPath() + "/";
   }


   /**
    * Test with a version of the classes that haven't been aspectized. This
    * is a form of "control" for the testNoCacheReplicableWar() test. 
    * 
    * @throws Exception
    */
   public void testNoCacheNoReplicableWar() throws Exception
   {
      warTest(NO_CACHE_NO_REPLICABLE_WAR);
   }
   
   /**
    * Test with a version of the classes that have been aopc prepared. This is
    * the real test. 
    * 
    * @throws Exception
    */
   public void testNoCacheReplicableWar() throws Exception
   {
      warTest(NO_CACHE_REPLICABLE_WAR);
   }
   
   protected String getWarContextPath()
   {
      return "classloader-leak";
   }
   
   protected String[] getEjbKeys()
   {
      return new String[]{};
   }
   
   protected void makeEjbRequests() throws Exception
   {
      throw new UnsupportedOperationException("No EJB tests");
   }
}
