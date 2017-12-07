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
package org.jboss.test.aop.bean;

import java.security.Principal;

import org.jboss.logging.Logger; 
import org.jboss.security.SimplePrincipal;
import org.jboss.security.client.SecurityClient;
import org.jboss.security.client.SecurityClientFactory;
import org.jboss.system.ServiceMBeanSupport;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.security.auth.login.LoginException;
/**
 *
 * @see Monitorable
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 81036 $
 */
public class SecurityTester
   extends ServiceMBeanSupport
   implements SecurityTesterMBean, MBeanRegistration
{
   // Constants ----------------------------------------------------
   // Attributes ---------------------------------------------------
   static Logger log = Logger.getLogger(SecurityTester.class);
   MBeanServer m_mbeanServer;

   // Static -------------------------------------------------------
   
   // Constructors -------------------------------------------------
   public SecurityTester()
   {}
   
   // Public -------------------------------------------------------
   
   // MBeanRegistration implementation -----------------------------------
   public ObjectName preRegister(MBeanServer server, ObjectName name)
   throws Exception
   {
      m_mbeanServer = server;
      return name;
   }
   
   public void postRegister(Boolean registrationDone)
   {}
   public void preDeregister() throws Exception
   {}
   public void postDeregister()
   {}

   protected void startService()
      throws Exception
   {
   }

   protected void stopService() {
   }


   public void testXml()
   {
      try{
         log.info("TESTING XML Security");

         char[] password = "password".toCharArray();
         SecurityClient securityClient = SecurityClientFactory.getSecurityClient();
         setSecurity(securityClient,"somebody", password); 
         //SecurityAssociation.pushSubjectContext(null, new SimplePrincipal("somebody"), password);

         log.info("testing unchecked constructor");
         SecuredPOJO pojo = new SecuredPOJO(); // unchecked construction
         log.info("testing unchecked method");
         pojo.unchecked();
         log.info("testing unchecked field");
         pojo.uncheckedField = 5;

         /*SecurityAssociation.popSubjectContext();
         SecurityAssociation.pushSubjectContext(null, new SimplePrincipal("authfail"), password);
*/
         securityClient.logout();
         
         setSecurity(securityClient,"authfail", password);
          
         boolean securityFailure = true;
         try
         {
            log.info("testing auth failure method");
            pojo.someMethod();
         }
         catch (SecurityException ignored)
         {
            log.info(ignored.getMessage());
            securityFailure = false;
         }

         if (securityFailure) throw new RuntimeException("auth failure was not caught for method");

         securityFailure = true;
         try
         {
            log.info("testing auth failure field");
            pojo.someField = 5;
         }
         catch (SecurityException ignored)
         {
            log.info(ignored.getMessage());
            securityFailure = false;
         }

         if (securityFailure) throw new RuntimeException("auth failure was not caught for field");
         securityFailure = true;
         try
         {
            log.info("testing auth failure constructor");
            pojo = new SecuredPOJO(4);
         }
         catch (SecurityException ignored)
         {
            log.info(ignored.getMessage());
            securityFailure = false;
         }

         if (securityFailure) throw new RuntimeException("auth failure was not caught for constructor");

         securityFailure = true;
         
         securityClient.logout();
         setSecurity(securityClient,"rolefail", password);
         /*
         SecurityAssociation.popSubjectContext();
         SecurityAssociation.pushSubjectContext(null, new SimplePrincipal("rolefail"), password);
         */
         try
         {
            log.info("testing role failure method");
            pojo.someMethod();
         }
         catch (SecurityException ignored) 
         {
            log.info(ignored.getMessage());
            securityFailure = false;
         }
         if (securityFailure) throw new RuntimeException("role failure was not caught for method");

         securityFailure = true;
         try
         {
            log.info("testing role failure field");
            pojo.someField = 5;
         }
         catch (SecurityException ignored) 
         {
            log.info(ignored.getMessage());
            securityFailure = false;
         }
         if (securityFailure) throw new RuntimeException("role failure was not caught field");

         securityFailure = true;
         try
         {
            log.info("testing role failure constructor");
            pojo = new SecuredPOJO(4);
         }
         catch (SecurityException ignored)
         {
            log.info(ignored.getMessage());
            securityFailure = false;
         }

         if (securityFailure) throw new RuntimeException("role failure was not caught for constructor");

         securityClient.logout();
         setSecurity(securityClient,"pass", password);
         /*
         SecurityAssociation.popSubjectContext();
         SecurityAssociation.pushSubjectContext(null, new SimplePrincipal("pass"), password);
         */
         log.info("test pass");
         pojo.someMethod();
         pojo.someField = 5;
         pojo = new SecuredPOJO(5);
         
         log.info("test exclusion");
         securityFailure = true;
         try
         {
            pojo.excluded();
         }
         catch (SecurityException ignored)
         {
            log.info(ignored.getMessage());
            securityFailure = false;
         }
         if (securityFailure) throw new RuntimeException("excluded failure was not caught for method");

         securityFailure = true;
         try
         {
            pojo.excludedField = "hello";
         }
         catch (SecurityException ignored)
         {
            log.info(ignored.getMessage());
            securityFailure = false;
         }
         if (securityFailure) throw new RuntimeException("excluded failure was not caught for field");

         securityFailure = true;
         try
         {
            pojo = new SecuredPOJO("hello");
         }
         catch (SecurityException ignored)
         {
            log.info(ignored.getMessage());
            securityFailure = false;
         }
         if (securityFailure) throw new RuntimeException("excluded failure was not caught for constructor");
      }
      catch (Throwable ex)
      {
         log.error("failed", ex);
         throw new RuntimeException(ex);
      }
   }

   public void testAnnotated()
   {
      try{
         log.info("TESTING Annotated Security");

         char[] password = "password".toCharArray();
         SecurityClient client = SecurityClientFactory.getSecurityClient();
         setSecurity(client,"somebody", password);

         log.info("testing unchecked constructor");
         AnnotatedSecuredPOJO pojo = new AnnotatedSecuredPOJO(); // unchecked construction
         log.info("testing unchecked method");
         pojo.unchecked();
         log.info("testing unchecked field");
         pojo.uncheckedField = 5;

         client.logout();
         setSecurity(client,"authfail", password);

         boolean securityFailure = true;
         try
         {
            log.info("testing auth failure method");
            pojo.someMethod();
         }
         catch (SecurityException ignored)
         {
            log.info(ignored.getMessage());
            securityFailure = false;
         }

         if (securityFailure) throw new RuntimeException("auth failure was not caught for method");

         securityFailure = true;
         try
         {
            log.info("testing auth failure field");
            pojo.someField = 5;
         }
         catch (SecurityException ignored)
         {
            log.info(ignored.getMessage());
            securityFailure = false;
         }

         if (securityFailure) throw new RuntimeException("auth failure was not caught for field");
         securityFailure = true;
         try
         {
            log.info("testing auth failure constructor");
            pojo = new AnnotatedSecuredPOJO(4);
         }
         catch (SecurityException ignored)
         {
            log.info(ignored.getMessage());
            securityFailure = false;
         }

         if (securityFailure) throw new RuntimeException("auth failure was not caught for constructor");

         securityFailure = true;
         client.logout();
         setSecurity(client,"rolefail", password);
         try
         {
            log.info("testing role failure method");
            pojo.someMethod();
         }
         catch (SecurityException ignored)
         {
            log.info(ignored.getMessage());
            securityFailure = false;
         }
         if (securityFailure) throw new RuntimeException("role failure was not caught for method");

         securityFailure = true;
         try
         {
            log.info("testing role failure field");
            pojo.someField = 5;
         }
         catch (SecurityException ignored)
         {
            log.info(ignored.getMessage());
            securityFailure = false;
         }
         if (securityFailure) throw new RuntimeException("role failure was not caught field");

         securityFailure = true;
         try
         {
            log.info("testing role failure constructor");
            pojo = new AnnotatedSecuredPOJO(4);
         }
         catch (SecurityException ignored)
         {
            log.info(ignored.getMessage());
            securityFailure = false;
         }

         if (securityFailure) throw new RuntimeException("role failure was not caught for constructor");

         client.logout();
         setSecurity(client,"pass", password);
         
         log.info("test pass");
         pojo.someMethod();
         pojo.someField = 5;
         pojo = new AnnotatedSecuredPOJO(5);
        
         log.info("test exclusion");
         securityFailure = true;
         try
         {
            pojo.excluded();
         }
         catch (SecurityException ignored)
         {
            log.info(ignored.getMessage());
            securityFailure = false;
         }
         if (securityFailure) throw new RuntimeException("excluded failure was not caught for method");

         securityFailure = true;
         try
         {
            pojo.excludedField = "hello";
         }
         catch (SecurityException ignored)
         {
            log.info(ignored.getMessage());
            securityFailure = false;
         }
         if (securityFailure) throw new RuntimeException("excluded failure was not caught for field");

         securityFailure = true;
         try
         {
            pojo = new AnnotatedSecuredPOJO("hello");
         }
         catch (SecurityException ignored)
         {
            log.info(ignored.getMessage());
            securityFailure = false;
         }
         if (securityFailure) throw new RuntimeException("excluded failure was not caught for constructor");
      }
      catch (Throwable ex)
      {
         log.error("failed", ex);
         throw new RuntimeException(ex);
      }
   }
   
   private void setSecurity(SecurityClient client, String name, Object credential) 
   throws LoginException
   {
	   client.setSimple(name, credential);
	   client.login();
   }
}