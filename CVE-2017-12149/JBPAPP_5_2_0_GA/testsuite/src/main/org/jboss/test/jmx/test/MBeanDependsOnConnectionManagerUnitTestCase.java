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
package org.jboss.test.jmx.test;

import javax.management.ObjectName;

import org.jboss.test.JBossTestCase;
import org.jboss.deployment.IncompleteDeploymentException;

/**
 * @author  <a href="mailto:corby@users.sourceforge.net">Corby Page</a>
 */
public class MBeanDependsOnConnectionManagerUnitTestCase extends JBossTestCase
{
   // Attributes ----------------------------------------------------
   ObjectName serviceControllerName;

   public MBeanDependsOnConnectionManagerUnitTestCase( String name )
   {
      super( name );
      try
      {
         serviceControllerName = new ObjectName( "jboss.system:service=ServiceController" );
      }
      catch ( Exception e )
      {
      } // end of try-catch
   }

   public void testMBeanDependsOnConnectionManager() throws Exception
   {
      String mBeanCodeUrl = "testdeploy.sar";
      String mBeanUrl = "testmbeandependsOnConnectionManager-service.xml";
      String connectionManagerUrl = "hsqldb-singleconnection-ds.xml";

      ObjectName objectNameMBean = new ObjectName( "test:name=TestMBeanDependsOnConnectionManager" );
      ObjectName objectNameConnectionManager = new ObjectName( "jboss.jca:service=DataSourceBinding,name=SingleConnectionDS" );

      deploy( mBeanCodeUrl );
      try
      {
         deploy( connectionManagerUrl );
         try
         {
            deploy( mBeanUrl );
            try
            {

               try
               {
                  undeploy( connectionManagerUrl );
                  deploy( connectionManagerUrl );
               }
               catch ( IncompleteDeploymentException ex )
               {
                  getLog().info("incomplete deployment exception", ex);
                  fail( "Connection Pool could not be recycled successfully!" );
               }

               // Double-check state
               String mBeanState = (String)getServer().getAttribute( objectNameMBean, "StateString" );
               assertEquals( "Test MBean not started!", "Started", mBeanState );
               String connectionManagerState = (String)getServer().getAttribute(
                  objectNameConnectionManager, "StateString" );
               assertEquals( "Connnection Manager MBean not started!", "Started", connectionManagerState );
            }
            finally
            {
               undeploy( mBeanUrl );
            }
         }
         finally
         {
            undeploy( connectionManagerUrl );
         }
      }
      finally
      {
         undeploy( mBeanCodeUrl );
      }
   }
}
