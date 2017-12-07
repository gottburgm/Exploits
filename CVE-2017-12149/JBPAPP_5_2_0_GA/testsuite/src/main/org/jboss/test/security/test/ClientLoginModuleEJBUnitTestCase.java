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
package org.jboss.test.security.test;

import java.util.Properties;
import java.security.Principal;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.jboss.test.JBossTestCase;
import org.jboss.test.security.clientlogin.IClientLoginHome;
import org.jboss.test.security.clientlogin.IClientLogin;
import junit.framework.Test;

/** Tests of the interaction between the ClientLoginModule and the
 SecurityAssocation context for secured ejbs

 @author Scott.Stark@jboss.org
 @version $Revision: 81036 $
 */
public class ClientLoginModuleEJBUnitTestCase
   extends JBossTestCase
{
   public ClientLoginModuleEJBUnitTestCase(String name)
   {
      super(name);
   }

   /**
    Call BeanA using jduke/theduke
    +-- call BeanB switching idenity using ClientLoginModule
    +---- call BeanC switching idenity using ClientLoginModule
    validing the expected caller principal with different ejb method permissions
    @throws Exception
    */
   public void testClientLoginModule() throws Exception
   {
      log.debug("+++ testPublicMethod()");
      Properties env = new Properties();
      env.setProperty(Context.INITIAL_CONTEXT_FACTORY,
         "org.jboss.security.jndi.JndiLoginInitialContextFactory");
      env.setProperty(Context.SECURITY_PRINCIPAL, "jduke");
      env.setProperty(Context.SECURITY_CREDENTIALS, "theduke");
      InitialContext ctx = new InitialContext(env);
      Object obj = ctx.lookup("client-login-tests/BeanA");
      obj = PortableRemoteObject.narrow(obj, IClientLoginHome.class);
      IClientLoginHome home = (IClientLoginHome) obj;
      log.debug("Found IClientLoginHome");
      IClientLogin bean = home.create();
      log.debug("Created IClientLogin");

      Principal user = bean.callBeanAsClientLoginUser();
      assertTrue("callBeanAsClientLoginUser value == jduke",
         user.getName().equals("jduke"));
      bean.remove();
   }

   public static Test suite() throws Exception
   {
      return JBossTestCase.getDeploySetup(ClientLoginModuleEJBUnitTestCase.class,
         "client-login-tests.jar");
   }

}
