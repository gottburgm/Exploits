/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.deployment.jbpapp6517;

import java.io.File;
import java.net.URI;
import java.util.Properties;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.TestCase;

import org.jboss.deployment.MainDeployerMBean;
import org.jboss.jmx.adaptor.rmi.RMIAdaptor;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;

/**
 * @author bmaxwell
 *
 */
public class CheckCompleteDefaultTestCase extends CheckCompleteTestCase
{      

   public CheckCompleteDefaultTestCase(String name)
   {
      super(name);
   }

   // this is without the deployer
   public void testDefaultWithoutCheckSubDeploymentCompleteDeployer()
   {
      // deploy ear & eat expected exception
      // check jmx or jndi - with CheckSubDeploymentCompleteDeployer the EJB3 should not be in jmx or jndi
      // undeploy ear
      // call fail if not valid
      verifyCheckCompleteNotCalled(testEar, earEjb3JMXName);
      
      // Repeat for sar
      verifyCheckCompleteNotCalled(testSar, sarEjb3JMXName);
   }   
}
