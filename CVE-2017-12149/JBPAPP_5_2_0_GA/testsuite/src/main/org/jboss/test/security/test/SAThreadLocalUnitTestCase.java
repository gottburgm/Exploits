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

import java.io.FilePermission;
import java.net.URL;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.Principal;

import javax.security.auth.Subject;

import junit.framework.TestCase;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;

/**
 * Test that the security context thread locals do NOT propagate to child threads
 *
 @author Scott.Stark@jboss.org
 @version $Revision: 81036 $
 */
public class SAThreadLocalUnitTestCase extends TestCase
{
   private Principal authPrincipal;
   private Subject authSubject;

   public SAThreadLocalUnitTestCase(String name)
   {
      super(name);
   }

   /**
    * Test the order of PermissionNames
    */
   public void testSecurityContext()
   {
      authPrincipal = new SimplePrincipal("jduke");
      authSubject = new Subject();
      authSubject.getPrincipals().add(authPrincipal);
      SecurityAssociation.pushSubjectContext(authSubject, authPrincipal, "theduke");
      validateSettings(false);
   }
   public void testThreadLocal() throws Exception
   {
      testSecurityContext();
      TestThread t = new TestThread("testThreadLocal", true);
      t.start();
      t.join();
      if( t.error != null )
      {
         t.error.printStackTrace();
         fail("TestThread saw an error");
      }
   }

   /**
    * SecurityAssociation.getSubject() == authSubject
    * SecurityAssociation.getPrincipal() == authPrincipal
    */
   private void validateSettings(boolean expectNull)
   {
      Subject s = SecurityAssociation.getSubject();
      Principal p = SecurityAssociation.getPrincipal();

      if( expectNull )
      {
         assertNull("getSubject() == null", s);
         assertNull("getPrincipal() == null", p);         
      }
      else
      {
         assertTrue("getSubject() == authSubject", authSubject.equals(s));
         assertTrue("getPrincipal() == authPrincipal", authPrincipal.equals(p));
      }
   }
   
   class TestThread extends Thread
   {
      Throwable error;
      boolean expectNull;
      TestThread(String name, boolean expectNull)
      {
         super(name);
         this.expectNull = expectNull;
      }

      public void run()
      {
         try
         {
            validateSettings(expectNull);
         }
         catch(Throwable e)
         {
            error = e;
         }
      }
   }

   protected void setUp()
   {
      System.setProperty("org.jboss.security.SecurityAssociation.ThreadLocal", "true");
      SecurityAssociation.setServer();
   }
}
