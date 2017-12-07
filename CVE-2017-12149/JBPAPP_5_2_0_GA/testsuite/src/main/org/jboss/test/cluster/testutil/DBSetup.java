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
package org.jboss.test.cluster.testutil;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestClusteredSetup;

/** A TestSetup that starts hypersonic before the testcase with a tcp
 * listening port at 1701.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revison:$
 */
public class DBSetup extends JBossTestClusteredSetup
{
   private final TestSetupDelegate delegate;
   
   public DBSetup(Test test, String jarNames) throws Exception
   {
      super(test, jarNames);
      this.delegate = new DBSetupDelegate();
   }
   
   public DBSetup(Test test, String jarNames, String dbAddress, int dbPort) throws Exception
   {
      super(test, jarNames);
      this.delegate = new DBSetupDelegate(dbAddress, dbPort);
   }

   public static Test getDeploySetup(final Test test, final String jarNames)
      throws Exception
   {
      return new DBSetup(test, jarNames);
   }

   public static Test getDeploySetup(final Class<?> clazz, final String jarNames)
      throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(clazz));
      return getDeploySetup(suite, jarNames);
   }

   public static Test getDeploySetup(final Test test, final String jarNames, String dbAddress, int dbPort)
      throws Exception
   {
      return new DBSetup(test, jarNames, dbAddress, dbPort);
   }

   public static Test getDeploySetup(final Class<?> clazz, final String jarNames, String dbAddress, int dbPort)
      throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(clazz));
      return getDeploySetup(suite, jarNames, dbAddress, dbPort);
   }

   protected void setUp() throws Exception
   {
         delegate.setUp();
         
         super.setUp();
   }

   protected void tearDown() throws Exception
   {
      try
      {
         super.tearDown();
      }
      finally
      {
         delegate.tearDown();
      }
      
   }

   public static void main(String[] args) throws Exception
   {
      DBSetup setup = new DBSetup(null, null);
      setup.setUp();
      Thread.sleep(120*1000);
      setup.tearDown();
   }
}
