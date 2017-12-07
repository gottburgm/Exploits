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
package org.jboss.test.bootstrapdependencies.jbas5349.test;

import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;


/**
 * Tests bootstrap dependencies.
 * 
 * @author <a href="istudens@redhat.com">Ivo Studensky</a>
 * @version $Revision: 85945 $
 */
public class BootstrapDependenciesTestCase
   extends JBossTestCase
{
   protected static Logger staticLog = Logger.getLogger(BootstrapDependenciesTestCase.class);

   // Constants -----------------------------------------------------
   public final static String bootstrapDependenciesTestMBeanName      = 
         "org.jboss.test.bootstrapdependencies.jbas5349.sar.bootstrap.dependencies.test:service=BootstrapDependenciesTest";
   public final static int expectedMBeanState                         = 3;

   
   public BootstrapDependenciesTestCase(String name)
   {
      super(name);
   }
   
   
   /**
    * Tests the status of deployment of testing SAR.
    * If it fails, probably the deployment of a tested module included in the SAR fails.
    * See details in server's log.
    * 
    * @throws Exception Description of Exception
    */
   public void testDeploymentStatus() throws Exception
   {
      assertEquals(expectedMBeanState, checkMBeanState());
   }

   
   /**
    * Checks the state of testing MBean.
    *
    * @exception Exception  Description of Exception
    */
   protected int checkMBeanState() throws Exception
   {
      Object state = invoke(new ObjectName(bootstrapDependenciesTestMBeanName), "getState", null, null);
      staticLog.debug("state=" + state);
      
      return ((Integer) state).intValue();
   }

}

