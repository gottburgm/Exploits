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

import java.util.HashSet;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import junit.framework.Test;
import org.jboss.security.SimplePrincipal;
import org.jboss.test.JBossTestCase;
import org.jboss.test.security.interfaces.SubjectSession;
import org.jboss.test.security.interfaces.SubjectSessionHome;


/** Tests of the caller context state
 
 @author Scott.Stark@jboss.org
 @version $Revision: 81036 $
 */
public class CallerInfoUnitTestCase
   extends JBossTestCase
{
   public CallerInfoUnitTestCase(String name)
   {
      super(name);
   }

   /** Test return of a custom principal from getCallerPrincipal.
    */
   public void testCallerSubject() throws Exception
   {
      log.debug("+++ testCallerSubject()");
      Properties env = new Properties();
      env.setProperty(Context.INITIAL_CONTEXT_FACTORY,
         "org.jboss.security.jndi.JndiLoginInitialContextFactory");
      env.setProperty(Context.SECURITY_PRINCIPAL, "jduke");
      env.setProperty(Context.SECURITY_CREDENTIALS, "theduke");
      InitialContext ctx = new InitialContext(env);
      Object obj = ctx.lookup("caller-info.SubjectSessionFacade");
      obj = PortableRemoteObject.narrow(obj, SubjectSessionHome.class);
      SubjectSessionHome home = (SubjectSessionHome) obj;
      log.debug("Found CustomPrincipalHome");
      SubjectSession bean = home.create();
      log.debug("Created CustomPrincipal");

      HashSet principals = new HashSet();
      principals.add(new SimplePrincipal("CallerInfoFacadeRole"));
      principals.add(new SimplePrincipal("CallerInfoStatelessRole"));
      principals.add(new SimplePrincipal("CallerInfoStatefulRole"));
      principals.add(new SimplePrincipal("CallerInfoEntityRole"));
      bean.validateCallerContext("callerJduke", principals);
      bean.remove();
   }

   public static Test suite() throws Exception
   {
      return JBossTestCase.getDeploySetup(CallerInfoUnitTestCase.class, "caller-info.jar");
   }

}
