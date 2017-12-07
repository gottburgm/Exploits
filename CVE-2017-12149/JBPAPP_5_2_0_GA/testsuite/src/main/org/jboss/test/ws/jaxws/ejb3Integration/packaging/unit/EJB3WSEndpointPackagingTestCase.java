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
package org.jboss.test.ws.jaxws.ejb3Integration.packaging.unit;

import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.test.ws.jaxws.ejb3Integration.packaging.EchoEJB3WSEndpoint;
import org.jboss.test.ws.jaxws.ejb3Integration.packaging.EchoWSEndpointImplInSAR;

/**
 * Tests that a EJB3 Webservice deployed through a .jar within 
 * a .sar file deploys fine without any issues.
 * 
 * <p>
 *  See https://jira.jboss.org/browse/EJBTHREE-2098 for more details 
 * </p>
 * <p>
 *  In short, the container name generated for EJB3 beans deployed in .jar with a .sar
 *  top level deployment unit, was incorrect. The .sar deployment unit name wasn't being
 *  considered in the container name generation. Only .ear deployment units were considered.
 *  The fix for EJBTHREE-2098 takes care of this issue.
 * </p>
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class EJB3WSEndpointPackagingTestCase extends JBossWSTest
{
   /**
    * Deploy the .sar
    * @return
    */
   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(EJB3WSEndpointPackagingTestCase.class, "ejbwspackaging-test.sar");
   }

   /**
    * Test a simple access to the bean which is also exposed as a WS endpoint
    * @throws Exception
    */
   public void testRemoteAccess() throws Exception
   {
      InitialContext iniCtx = getInitialContext();
      EchoEJB3WSEndpoint ejb3Remote = (EchoEJB3WSEndpoint) iniCtx.lookup(EchoWSEndpointImplInSAR.JNDI_NAME);

      String helloWorld = "Hello world!";
      Object retObj = ejb3Remote.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }
}
