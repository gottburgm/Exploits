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
 * of a simple EJB3 jar packaged in an ear.
 * <p/>
 * If these tests are run with JBoss Profiler's jbossAgent (.dll or .so) on the path
 * and the AS is started with -agentlib:jbossAgent, in case of classloader leakage
 * an extensive report will be logged to the server log, showing the path to root of
 * all references to the classloader.
 * 
 * @author Brian Stansberry
 */
public class SimpleEjb3EarClassloaderLeakTestCase extends Ejb3ClassloaderLeakTestBase
{
   private static final String SIMPLE_EAR = "classloader-leak-simple-ejb3.ear"; 
   
   public SimpleEjb3EarClassloaderLeakTestCase(String name)
   {
      super(name);
   }


   public static Test suite() throws Exception
   {
      return getDeploySetup(SimpleEjb3EarClassloaderLeakTestCase.class, "classloader-leak-test.sar");
   }
   
   public void testSimpleEar() throws Exception
   {
      earTest(SIMPLE_EAR);
   }
}
