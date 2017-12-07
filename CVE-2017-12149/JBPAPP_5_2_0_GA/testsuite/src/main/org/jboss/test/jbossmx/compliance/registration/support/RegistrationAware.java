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
package org.jboss.test.jbossmx.compliance.registration.support;

import javax.management.MBeanRegistration;
import javax.management.ObjectName;
import javax.management.MBeanServer;

public class RegistrationAware implements RegistrationAwareMBean, MBeanRegistration
{
   private String something;

   private boolean preRegisterCalled = false;
   private boolean preDeRegisterCalled = false;
   private boolean postRegisterCalled = false;
   private boolean postDeRegisterCalled = false;
   private boolean postRegisterRegistrationDone = false;

   private MBeanServer registeredServer = null;
   private ObjectName registeredObjectName = null;

   private Exception exceptionOnPreRegister = null;
   private Exception exceptionOnPreDeRegister = null;

   private ObjectName alternateObjectName = null;
   private boolean returnAlternateObjectName = false;

   public RegistrationAware()
   {
   }

   public void setSomething(String something)
   {
      this.something = something;
   }

   public String getSomething()
   {
      return something;
   }


   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws Exception
   {
      preRegisterCalled = true;
      registeredServer = server;
      registeredObjectName = name;

      if (null != exceptionOnPreRegister)
      {
         exceptionOnPreRegister.fillInStackTrace();
         throw exceptionOnPreRegister;
      }

      if (returnAlternateObjectName)
      {
         return alternateObjectName;
      }

      return name;
   }

   // MBeanRegistration impl -------------------------------------
   public void postRegister(Boolean registrationDone)
   {
      postRegisterCalled = true;
      postRegisterRegistrationDone = registrationDone.booleanValue();
   }

   public void preDeregister()
      throws Exception
   {
      preDeRegisterCalled = true;

      if (null != exceptionOnPreDeRegister)
      {
         exceptionOnPreDeRegister.fillInStackTrace();
         throw exceptionOnPreDeRegister;
      }
   }

   public void postDeregister()
   {
      postDeRegisterCalled = true;
   }

   // Settings setters -------------------------------------------
   public void setExceptionOnPreRegister(Exception exceptionOnPreRegister)
   {
      this.exceptionOnPreRegister = exceptionOnPreRegister;
   }

   public void setExceptionOnPreDeRegister(Exception exceptionOnPreDeRegister)
   {
      this.exceptionOnPreDeRegister = exceptionOnPreDeRegister;
   }

   public void setAlternateObjectName(ObjectName alternateObjectName)
   {
      this.returnAlternateObjectName = true;
      this.alternateObjectName = alternateObjectName;
   }

   // Status getters ---------------------------------------------
   public boolean isPreRegisterCalled()
   {
      return preRegisterCalled;
   }

   public boolean isPreDeRegisterCalled()
   {
      return preDeRegisterCalled;
   }

   public boolean isPostRegisterCalled()
   {
      return postRegisterCalled;
   }

   public boolean isPostDeRegisterCalled()
   {
      return postDeRegisterCalled;
   }

   public boolean isPostRegisterRegistrationDone()
   {
      return postRegisterRegistrationDone;
   }

   public MBeanServer getRegisteredServer()
   {
      return registeredServer;
   }

   public ObjectName getRegisteredObjectName()
   {
      return registeredObjectName;
   }
}
