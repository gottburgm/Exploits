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
package org.jboss.test.perf.test;

import java.io.IOException;
import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jboss.test.perf.interfaces.PerfResult;
import org.jboss.test.perf.interfaces.PerfTestSession;
import org.jboss.test.perf.interfaces.PerfTestSessionHome;
import org.jboss.test.perf.interfaces.Probe;
import org.jboss.test.perf.interfaces.ProbeHome;

import org.jboss.test.JBossTestCase;

/** Tests of the Probe session bean method call overhead inside
of the JBoss VM. This is performed using the PerfTestSession
wrapper.
 
 @author Scott.Stark@jboss.org
 @version $Revision: 81036 $
*/
public class PerfUnitTestCase extends JBossTestCase
{
   int iterationCount;
   
   public PerfUnitTestCase(String name)
   {
      super(name);
   }

   public void testInVMCalls() throws Exception
   {
      getLog().debug("+++ testInVMCalls()");
      Object obj = getInitialContext().lookup("PerfTestSession");
      obj = PortableRemoteObject.narrow(obj, PerfTestSessionHome.class);
      PerfTestSessionHome home = (PerfTestSessionHome) obj;
      getLog().debug("Found PerfTestSessionHome @ jndiName=PerfTestSessionHome");
      PerfTestSession bean = home.create();
      getLog().debug("Created PerfTestSession");
      long start = System.currentTimeMillis();
      PerfResult result = bean.runProbeTests(iterationCount);
      String report = result.report;
      long end = System.currentTimeMillis();
      long elapsed = end - start;
      getLog().debug("Elapsed time = "+(elapsed / iterationCount));
      getLog().info("The testInVMCalls report is:\n"+report);
      if( result.error != null )
         throw result.error;
   }

   public void testInVMLocalCalls() throws Exception
   {
      getLog().debug("+++ testInVMLocalCalls()");
      Object obj = getInitialContext().lookup("PerfTestSession");
      obj = PortableRemoteObject.narrow(obj, PerfTestSessionHome.class);
      PerfTestSessionHome home = (PerfTestSessionHome) obj;
      getLog().debug("Found PerfTestSessionHome @ jndiName=PerfTestSessionHome");
      PerfTestSession bean = home.create();
      getLog().debug("Created PerfTestSession");
      long start = System.currentTimeMillis();
      PerfResult result = bean.runProbeLocalTests(iterationCount);
      String report = result.report;
      long end = System.currentTimeMillis();
      long elapsed = end - start;
      getLog().debug("Elapsed time = "+(elapsed / iterationCount));
      getLog().info("The testInVMLocalCalls report is:\n"+report);
      if( result.error != null )
         throw result.error;
   }

   public static Test suite() throws Exception
   {
      Test test = getDeploySetup(PerfUnitTestCase.class, "probe.jar");
      return test;
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      iterationCount = getIterationCount();
   }
   
}
