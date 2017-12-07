/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.test.ejb3.jboss51xsd.unit;

import junit.framework.Test;

import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;
import org.jboss.test.ejb3.jboss51xsd.Echo;

/**
 * EJB3DeploymentWithJBoss51xsdTestCase
 * 
 * Tests the bug fix for https://jira.jboss.org/jira/browse/JBAS-7231
 * 
 * The bug in JBAS-7231 relates to missing of jboss_5_1 xsd from the 
 * metadata-deployer-jboss-beans.xml. As a result, the deployment of 
 * the EJB3 deployment used to fallback on the dtd version of jboss.xml
 * which not as robust as the 5_1 xsd.
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class EJB3DeploymentWithJBoss51xsdTestCase extends JBossTestCase
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(EJB3DeploymentWithJBoss51xsdTestCase.class);
   
   /**
    * Constructor
    * @param name
    */
   public EJB3DeploymentWithJBoss51xsdTestCase(String name)
   {
      super(name);
   }

   
   public static Test suite() throws Exception
   {
      return getDeploySetup(EJB3DeploymentWithJBoss51xsdTestCase.class, "jboss51xsd.jar");
   }
   
   public void testDeployment() throws Exception
   {
      // make sure that there were no deployment errors
      // Yes, it's a weird method name for testing that the deployment
      // did not throw errors during deployment :)
      serverFound();
      
      // Now just do a simple test of the EJBs - lookup and invoke a method
      Echo bean = (Echo) this.getInitialContext().lookup("JBAS-7231-BeanJNDINameFromJBossXml");
      String msg = "JBAS-7231 is now fixed!!";
      String returnedMessage = bean.echo(msg);
      
      assertEquals("Bean returned unexpected value", msg, returnedMessage);
      
   }
}
