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
package org.jboss.security.integration.password;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.beans.metadata.spi.AnnotationMetaData;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.kernel.plugins.dependency.AbstractKernelControllerContext;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.logging.Logger;

/**
 * AOP Lifecycle callback for the @Password annotation
 * @author Anil.Saldhana@redhat.com
 * @since Apr 1, 2009
 */
public class PasswordLifecycleCallback
{
   private static final Logger log = Logger.getLogger(PasswordLifecycleCallback.class); 

   private PasswordMaskManagement passwordManagement = null;
   
   /**
    * Set the Password Mask Management bean
    * @param passwordManagement
    */
   public void setPasswordManagement(PasswordMaskManagement passwordManagement)
   {
      this.passwordManagement = passwordManagement;
   }

   /**
    * Bind the target on setKernelControllerContext, unbind on any other method provided that
    * the invocation has a Password annotation.
    * 
    * @param invocation the invocation
    * @return the result
    * @throws Throwable for any error
    */
   public Object invoke(Invocation invocation) throws Throwable
   {
      MethodInvocation mi = (MethodInvocation) invocation;
      KernelControllerContext context = (KernelControllerContext) mi.getArguments()[0];

      boolean trace = log.isTraceEnabled();
      Password passwordAnnotation = (Password) invocation.resolveClassAnnotation(Password.class); 
      if( trace )
         log.trace("Checking method: "+mi.getMethod()+", bindingInfo: "+passwordAnnotation);

      // If this is the setKernelControllerContext callback, set the password
      if ("setKernelControllerContext".equals(mi.getMethod().getName()) && passwordAnnotation != null)
      { 
         //Get the password
         String securityDomain = passwordAnnotation.securityDomain(); 
         char[] passwd = this.passwordManagement.getPassword(securityDomain);
         
         Object target = context.getTarget();
         this.setPassword(target, passwordAnnotation, passwd); 
      }
      // If this is the unsetKernelControllerContext callback, unbind the target
      else if( passwordAnnotation != null )
      {
         log.trace("Ignoring unsetKernelControllerContext callback");
      }
      else if ( trace )
      {
         log.trace("Ignoring null password info");
      }

      return null;
   }

   public void install(ControllerContext context) throws Exception
   {
      //Get the password
      List<Password> passwordAnnotations = readPasswordAnnotation(context);
      for (Password passwordAnnotation : passwordAnnotations)
      {

         boolean trace = log.isTraceEnabled();
         if (trace)
            log.trace("Binding into JNDI: " + context.getName() + ", passwordInfo: " + passwordAnnotation);

         String securityDomain = passwordAnnotation.securityDomain();

         char[] passwd = this.passwordManagement.getPassword(securityDomain);

         if (passwd == null)
            log.trace("Password does not exist for security domain=" + securityDomain);
         //The bean in question is the target
         String methodName = passwordAnnotation.methodName();
         Object target = context.getTarget();
         if (trace)
         {
            log.trace("Trying to set password on " + target + " with method :" + methodName);
         }
         this.setPassword(target, passwordAnnotation, passwd);
      }
   }
   
   public void uninstall(ControllerContext context) throws Exception
   {
      //ignore
   }
   
   private List<Password> readPasswordAnnotation(ControllerContext context) throws Exception
   {
      List<Password> passwordAnnotations = new ArrayList<Password>();
      AbstractKernelControllerContext akcc = (AbstractKernelControllerContext) context;
      BeanMetaData bmd = akcc.getBeanMetaData();
      Set<AnnotationMetaData> annotations = bmd.getAnnotations();
      for (AnnotationMetaData annotationMetaData : annotations)
      {
         Annotation annotation = annotationMetaData.getAnnotationInstance();
         if (annotation.annotationType() == Password.class)
         {
            passwordAnnotations.add((Password) annotation);
         }
      }
      return passwordAnnotations;
   }
   
   private void setPassword(Object target, Password passwordAnnotation, char[] passwd) throws Exception
   {
      Class<?> clazz = target.getClass();
      String methodName = passwordAnnotation.methodName();
      if(methodName == null)
         throw new IllegalStateException("methodName " + methodName + " not configured on " +
                 "the Password annotation for target:" + clazz);
      Method m = SecurityActions.getMethod(clazz, methodName);
      
      try
      {
         m.invoke(target, new Object[] {passwd});  
      }
      catch(Exception e)
      {
         log.trace("Error setting password on " + clazz + ". Will try the string version.");
         m.invoke(target, new Object[] { new String(passwd)} ); 
      } 
   }
}
