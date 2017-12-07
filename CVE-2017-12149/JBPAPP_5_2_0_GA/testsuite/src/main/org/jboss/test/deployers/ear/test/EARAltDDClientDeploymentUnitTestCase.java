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
package org.jboss.test.deployers.ear.test;

import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import junit.framework.Test;
import org.jboss.test.deployers.AbstractDeploymentTest;

/**
 * @author <a href="alex@jboss.com">Alexey Loubyansky</a>
 * @version $Revision: 85526 $
 */
public class EARAltDDClientDeploymentUnitTestCase extends AbstractDeploymentTest
{
   public static Test suite() throws Exception
   {
      return getManagedDeployment(EARAltDDClientDeploymentUnitTestCase.class, earAltDDClientDeployment);
   }

   public EARAltDDClientDeploymentUnitTestCase(String test)
   {
      super(test);
   }

   public void testENC() throws Exception
   {
      Context enc = getENC();
      String ddFile = (String) enc.lookup("dd-file");
      assertEquals("altdd-client.xml", ddFile);
   }

   /** Build the InitialContext factory 
    * @return
    * @throws NamingException
    */ 
   private Context getENC() throws NamingException
   {
      Properties env = new Properties();
      env.setProperty(Context.INITIAL_CONTEXT_FACTORY,
         "org.jnp.interfaces.NamingContextFactory");
      env.setProperty(Context.URL_PKG_PREFIXES, "org.jboss.naming.client");
      env.setProperty(Context.PROVIDER_URL, "jnp://" + getServerHost() + ":1099");
      env.setProperty("j2ee.clientName", "test-client");
      InitialContext ctx = new InitialContext(env);
      Context enc = (Context) ctx.lookup("java:comp/env");
      return enc;
   }
}
