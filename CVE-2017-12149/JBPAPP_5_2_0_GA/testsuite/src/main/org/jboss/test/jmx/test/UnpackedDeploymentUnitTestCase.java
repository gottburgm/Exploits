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

import javax.naming.InitialContext;

import org.jboss.test.jmx.eardeployment.a.interfaces.SessionAHome;
import org.jboss.test.jmx.eardeployment.a.interfaces.SessionA;
import org.jboss.test.jmx.eardeployment.b.interfaces.SessionBHome;
import org.jboss.test.jmx.eardeployment.b.interfaces.SessionB;

import org.jboss.test.JBossTestCase;

/** Tests of unpacked deployments.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class UnpackedDeploymentUnitTestCase extends JBossTestCase 
{
   public UnpackedDeploymentUnitTestCase(String name)
   {
      super(name);
   }

   /** Test that an unpacked ejb-jar is deployed
    * @exception Exception if an error occurs
    */
   public void testUnpackedEjbJar() throws Exception
   {
      deploy("unpacked/eardeployment.ear");
      try
      {
         SessionAHome aHome = (SessionAHome)getInitialContext().lookup("eardeployment/SessionA");
         SessionBHome bHome = (SessionBHome)getInitialContext().lookup("eardeployment/SessionB");
         SessionA a = aHome.create();
         SessionB b = bHome.create();
         assertTrue("a call b failed!", a.callB());
         assertTrue("b call a failed!", b.callA());
      }
      finally
      {
         undeploy("unpacked/eardeployment.ear");
      }
   }
  
}

