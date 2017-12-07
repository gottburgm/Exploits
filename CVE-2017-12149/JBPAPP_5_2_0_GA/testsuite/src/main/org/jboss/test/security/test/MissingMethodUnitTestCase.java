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

import org.jboss.test.JBossTestCase;
import org.jboss.test.security.interfaces.SubjectSessionHome;
import org.jboss.test.security.interfaces.SubjectSession;
import org.jboss.test.security.interfaces.CalledSession;
import org.jboss.test.security.interfaces.CalledSessionHome;
import org.jboss.security.SimplePrincipal;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import java.util.Properties;
import java.util.HashSet;
import java.rmi.AccessException;

import junit.framework.Test;

/** Tests missing-method-permissions-excluded-mode

 @author Scott.Stark@jboss.org
 @version $Revision: 81084 $
 */
public class MissingMethodUnitTestCase
   extends JBossTestCase
{
   public MissingMethodUnitTestCase(String name)
   {
      super(name);
   }

   /** Test that methods without a method-permission behave as unchecked
    */
   public void testMissingIsUnchecked() throws Exception
   {
      log.debug("+++ testMissingIsUnchecked()");
      Properties env = new Properties();
      env.setProperty(Context.INITIAL_CONTEXT_FACTORY,
         "org.jboss.security.jndi.JndiLoginInitialContextFactory");
      env.setProperty(Context.SECURITY_PRINCIPAL, "jduke");
      env.setProperty(Context.SECURITY_CREDENTIALS, "theduke");
      InitialContext ctx = new InitialContext(env);
      Object obj = ctx.lookup("unchecked/MissingMethodBean");
      obj = PortableRemoteObject.narrow(obj, CalledSessionHome.class);
      CalledSessionHome home = (CalledSessionHome) obj;
      log.debug("Found CalledSessionHome");
      CalledSession bean = home.create();
      log.debug("Created CalledSession");

      // This should pass due to login role
      bean.invokeEcho("testMissingIsUnchecked");
      // This should pass due to unchecked for missing method-permission
      bean.callEcho();
      bean.remove();
   }

   /** Test that methods without a method-permission behave as excluded
    */
   public void testMissingIsExcluded() throws Exception
   {
      log.debug("+++ testMissingIsExcluded()");
      Properties env = new Properties();
      env.setProperty(Context.INITIAL_CONTEXT_FACTORY,
         "org.jboss.security.jndi.JndiLoginInitialContextFactory");
      env.setProperty(Context.SECURITY_PRINCIPAL, "jduke");
      env.setProperty(Context.SECURITY_CREDENTIALS, "theduke");
      InitialContext ctx = new InitialContext(env);
      Object obj = ctx.lookup("excluded/MissingMethodBean");
      obj = PortableRemoteObject.narrow(obj, CalledSessionHome.class);
      CalledSessionHome home = (CalledSessionHome) obj;
      log.debug("Found CalledSessionHome");
      CalledSession bean = home.create();
      log.debug("Created CalledSession");

      // This should pass due to login role
      bean.invokeEcho("testMissingIsExcluded");
      // This should faile due to excluded for missing method-permission
      try
      {
         bean.callEcho();
         fail("Was able to invoke callEcho");
      }
      catch(AccessException e)
      {
         log.debug("Failed with AccessException");
      }
      bean.remove();
   }

   public static Test suite() throws Exception
   {
      return JBossTestCase.getDeploySetup(MissingMethodUnitTestCase.class,
         "missing-methods-excluded.jar,missing-methods-unchecked.jar");
   }

}
