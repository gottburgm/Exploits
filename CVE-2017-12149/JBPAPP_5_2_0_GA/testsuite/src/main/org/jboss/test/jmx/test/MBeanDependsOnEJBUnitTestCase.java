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
import org.jboss.test.JBossTestServices;
import org.jboss.deployers.spi.DeploymentException;

/**
 * @see       <related>
 * @author    <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version   $Revision: 81036 $
 */
public class MBeanDependsOnEJBUnitTestCase
       extends JBossTestCase
{
   // Constants -----------------------------------------------------
   protected final static int INSTALLED = 0;
   protected final static int CONFIGURED = 1;
   protected final static int CREATED = 2;
   protected final static int RUNNING = 3;
   protected final static int FAILED = 4;
   protected final static int STOPPED = 5;
   protected final static int DESTROYED = 6;
   protected final static int NOTYETINSTALLED = 7;
   // Attributes ----------------------------------------------------

   ObjectName serviceControllerName;
   // Static --------------------------------------------------------
   // Constructors --------------------------------------------------
   /**
    * Constructor for the DeployServiceUnitTestCase object
    *
    * @param name  Test case name
    */
   public MBeanDependsOnEJBUnitTestCase(String name)
   {
      super(name);
      try 
      {
         serviceControllerName = new ObjectName("jboss.system:service=ServiceController");
      } 
      catch (Exception e) 
      {
      } // end of try-catch

   }

   // Public --------------------------------------------------------


   /**
    * <code>testMBeanDependsOnEJB</code> tests that an mbean can depend
    * on an ejb.  All classes are loaded, the mbean is deployed, checked 
    * that it has not started, then the ejb is deployed, and the mbean
    * is checked that it has started.
    *
    * @exception Exception if an error occurs
    */
   public void testMBeanDependsOnEJB() throws Exception
   {
      String mBeancodeUrl = "testdeploy.sar";
      String mBeanUrl = "testmbeandependsOnEjb-service.xml";
      //random choice of ejb...
      String ejbUrl = "jmxtest.jar";
      getLog().debug("testUrls are : " + mBeanUrl + ", " + ejbUrl);
      ObjectName objectNameMBean = new ObjectName("test:name=TestMBeanDependsOnEjb");
      ObjectName objectNameEJB = new ObjectName("jboss.j2ee:service=EJB,name=test/TestDataSource");

      try 
      {
         //deploy jar
         deploy(mBeancodeUrl);
         
         try 
         {
            deploy(mBeanUrl);
            
            // FIXME
            // get deployer to throw an DeploymentException
            invoke(new ObjectName(JBossTestServices.DEPLOYER_NAME),
                  "checkIncompleteDeployments",
                  new Object[]{},
                  new String[]{}
                  );
            
            fail("suceeded in deploying mbean with unsatisfied dependency!");
         }
         catch (DeploymentException e)
         {
            //This is what we expect
         } // end of try-catch
      
         //Double check state
         assertTrue("MBean started!", !((String)getServer().getAttribute(objectNameMBean, "StateString")).equals("Started"));
         
         try 
         {
            deploy(ejbUrl);
            assertTrue("MBean not started!", ((String)getServer().getAttribute(objectNameMBean, "StateString")).equals("Started"));
         }
         finally
         {
            undeploy(ejbUrl);
            assertTrue("MBean started!", !((String)getServer().getAttribute(objectNameMBean, "StateString")).equals("Started"));
         } // end of try-catch
      }
      finally
      {
         undeploy(mBeanUrl);
         undeploy(mBeancodeUrl);
      } // end of try-catch
   }


}
