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
package org.jboss.test.securitymgr.test;

import org.jboss.test.securitymgr.interfaces.Bad;
import org.jboss.test.securitymgr.interfaces.BadHome;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;

/** Tests of the security permission enforcement for items outside of the
 standard EJB programming restrictions.

@author Scott.Stark@jboss.org
@version $Revision: 81036 $
 */
public class SecurityUnitTestCase extends JBossTestCase
{
   org.jboss.logging.Logger log = getLog();

   public SecurityUnitTestCase(String name)
   {
      super(name);
   }

   /** Test that a bean cannot access the SecurityAssociation class
    */
   public void testGetPrincipal() throws Exception
   {
      log.debug("+++ testGetPrincipal()");
      Bad bean = getBadSession();

      try
      {
         bean.getPrincipal();
         fail("Was able to call Bad.getPrincipal");
      }
      catch(Exception e)
      {
         log.debug("Bad.getPrincipal failed as expected", e);
      }
      bean.remove();
   }

   public void testGetCredential() throws Exception
   {
      log.debug("+++ testGetCredential()");
      Bad bean = getBadSession();
      try
      {
         bean.getCredential();
         fail("Was able to call Bad.getCredential");
      }
      catch(Exception e)
      {
         log.debug("Bad.getCredential failed as expected", e);
      }
      bean.remove();
   }

   public void testSetPrincipal() throws Exception
   {
      log.debug("+++ testSetPrincipal()");
      Bad bean = getBadSession();
      try
      {
         bean.setPrincipal(null);
         fail("Was able to call Bad.setPrincipal");
      }
      catch(Exception e)
      {
         log.debug("Bad.setPrincipal failed as expected", e);
      }
      bean.remove();
   }

   public void testSetCredential() throws Exception
   {
      log.debug("+++ testSetCredential()");
      Bad bean = getBadSession();
      try
      {
         char[] password = "secret".toCharArray();
         bean.setCredential(password);
         fail("Was able to call Bad.setCredential");
      }
      catch(Exception e)
      {
         log.debug("Bad.setCredential failed as expected", e);
      }
      bean.remove();
   }

   /** Test that access of the thread subject is allowed
    * @throws Exception
    */ 
   public void testGetSubject() throws Exception
   {
      log.debug("+++ testGetSubject()");
      Bad bean = getBadSession();
      try
      {
         // Access to the thread Subject is allowed
         bean.getSubject();
         log.debug("Called Bad.getSubject");
      }
      catch(Exception e)
      {
         log.error("Was not able to call Bad.getSubject", e);
         fail("Was not able to call Bad.getSubject"+e.getLocalizedMessage());
      }
      bean.remove();
   }

   /** Test that access to the private credentials of the thread subject fails
    * @throws Exception
    */ 
   public void testGetSubjectCredentials() throws Exception
   {
      log.debug("+++ testGetSubjectCredentials()");
      Bad bean = getBadSession();
      try
      {
         bean.getSubjectCredentials();
         fail("Was able to call Bad.getSubjectCredentials");
      }
      catch(Exception e)
      {
         log.debug("Bad.getSubjectCredentials failed as expected", e);
      }
      bean.remove();
   }

   public void testSetSubject() throws Exception
   {
      log.debug("+++ testSetSubject()");
      Bad bean = getBadSession();
      try
      {
         bean.setSubject();
         fail("Was able to call Bad.setSubject");
      }
      catch(Exception e)
      {
         log.debug("Bad.setSubject failed as expected", e);
      }
      bean.remove();
   }

   public void testPopRunAsRole() throws Exception
   {
      log.debug("+++ testPopRunAsRole()");
      Bad bean = getBadSession();
      try
      {
         bean.popRunAsRole();
         fail("Was able to call Bad.popRunAsRole");
      }
      catch(Exception e)
      {
         log.debug("Bad.popRunAsRole failed as expected", e);
      }
      bean.remove();
   }

   public void testPushRunAsRole() throws Exception
   {
      log.debug("+++ testPushRunAsRole()");
      Bad bean = getBadSession();
      try
      {
         bean.pushRunAsRole();
         fail("Was able to call Bad.pushRunAsRole");
      }
      catch(Exception e)
      {
         log.debug("Bad.pushRunAsRole failed as expected", e);
      }
      bean.remove();
   }

   /**
    * Setup the test suite.
    */
   public static Test suite() throws Exception
   {
      return getDeploySetup(SecurityUnitTestCase.class, "securitymgr-ejb.jar");
   }

   private Bad getBadSession() throws Exception
   {
      Object obj = getInitialContext().lookup("secmgr.BadHome");
      BadHome home = (BadHome) obj;
      log.debug("Found secmgr.BadHome");
      Bad bean = home.create();
      log.debug("Created Bad");
      return bean;
   }
}
