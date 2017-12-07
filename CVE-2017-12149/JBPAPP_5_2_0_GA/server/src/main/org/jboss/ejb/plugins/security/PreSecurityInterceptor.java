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
package org.jboss.ejb.plugins.security; 

import java.lang.reflect.Method;

import javax.ejb.TimedObject;
import javax.ejb.Timer;

import org.jboss.ejb.Container;
import org.jboss.ejb.plugins.AbstractInterceptor;
import org.jboss.invocation.Invocation;
import org.jboss.security.AuthenticationManager;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityIdentity;


/**
 *  Interceptor that performs the initialization required for 
 *  the security interceptor. This interceptor performs
 *  Security Context establishment and other initialization required
 *  - The Outgoing run-as from the security context is pushed to incoming run-as
 *  - For EJB Local Invocations, the security context is obtained from the
 *  SecurityContextAssocation threadlocal and placed on the invocation.
 *  @author Anil.Saldhana@redhat.com
 *  @since  Apr 30, 2007 
 *  @version $Revision: 111876 $
 */
public class PreSecurityInterceptor extends AbstractInterceptor
{ 
   private String securityDomain = null;
   
   private String timedObjectMethod = null;
   
   @Override
   public void setContainer(Container container)
   { 
      super.setContainer(container);
      if (container != null)
      { 
         AuthenticationManager am = container.getSecurityManager();
         if(am != null)
         {
            securityDomain = am.getSecurityDomain();
         } 
      }
      try
      {
         timedObjectMethod = TimedObject.class.getMethod("ejbTimeout", new Class[]{Timer.class})
                               .getName();
      }
      catch (Exception e)
      {
         if (log.isTraceEnabled())
         {
            log.trace("Exception in creating TimedObject method:",e);
         }
      } 
   }

   @Override
   public Object invoke(Invocation mi) throws Exception
   { 
      boolean isInvoke = true;
      return this.process(mi, isInvoke);  
   }

   @Override
   public Object invokeHome(Invocation mi) throws Exception
   {  
      boolean isInvoke = false;
      return this.process(mi, isInvoke); 
   }
    
   private Object process(Invocation mi, boolean isInvoke) throws Exception
   {
      //No Security in the absence of SecurityDomain
      if(securityDomain == null)
      {
         if(isInvoke)
            return getNext().invoke(mi);
         else
            return getNext().invokeHome(mi);
      }   

      if (log.isTraceEnabled())
      {
         log.trace("process:isInvoke="+isInvoke + " bean="+ container.getServiceName());
      }
      SecurityIdentity si = null;
      String incomingDomain = null;
      Method m = mi.getMethod();
      boolean isEjbTimeOutMethod =  m!= null && m.getName().equals(timedObjectMethod);
      //For local ejb invocations
      if(mi.isLocal() && !isEjbTimeOutMethod)
      {
         if (log.isTraceEnabled())
         {
            log.trace("True mi.isLocal() && !isEjbTimeOutMethod");
         }
         //Cache the security context
         SecurityContext sc = SecurityActions.getSecurityContext();
         if(sc != null)
         {
           si = SecurityActions.getSecurityIdentity(sc);
           incomingDomain = sc.getSecurityDomain();
         }
         else
         {
           // The security context is null...create a new one
           sc = SecurityActions.createAndSetSecurityContext(securityDomain,
                                             container.getSecurityContextClassName());

           // Get the security identity...creating a new one if needed
           si = SecurityActions.getSecurityIdentity(sc);
         }
        
         SecurityActions.setSecurityManagement(sc, container.getSecurityManagement());
         // set the container's security domain in the security context
         SecurityActions.setSecurityDomain(sc, this.securityDomain);

         if (log.isTraceEnabled())
         {
            log.trace("SecurityIdentity="+SecurityActions.trace(si));
         }
         //Set the security context on the invocation
         mi.setSecurityContext(sc); 
      }
      else
      {
         if (log.isTraceEnabled())
         {
            log.trace("False mi.isLocal() && !isEjbTimeOutMethod");
         }
         establishSecurityContext(mi); 
      } 
      
      try
      { 
         //Establish the run-as on the SC as the caller SC
         SecurityContext currentSC = SecurityActions.getSecurityContext();
         SecurityActions.pushCallerRunAsIdentity(currentSC.getOutgoingRunAs());
         if (log.isTraceEnabled())
         {
            log.trace("Going to the SecurityInterceptor with SC="+SecurityActions.trace(currentSC));
         }
         if(isInvoke)
            return getNext().invoke(mi);
         else
            return getNext().invokeHome(mi); 
      }
      finally
      { 
         SecurityActions.popCallerRunAsIdentity();
         if(mi.isLocal() && si != null)
            SecurityActions.setSecurityIdentity(SecurityActions.getSecurityContext(), si);
         if(mi.isLocal() && incomingDomain != null)
        	 SecurityActions.setSecurityDomain(SecurityActions.getSecurityContext(), incomingDomain);
         if (log.isTraceEnabled())
         {
            log.trace("Exit process():isInvoke="+isInvoke);
         }
      } 
   }
   
   private void establishSecurityContext(Invocation mi) throws Exception
   { 
      //For Local EJB invocations, the security context needs
      //to be obtained from the thread local. For remote ejb
      //invocations, the SC is obtained in the invocation
      SecurityContext sc = mi.getSecurityContext(); 
      SecurityContext newSC = SecurityActions.createAndSetSecurityContext(securityDomain,
            container.getSecurityContextClassName());  
      
      if(sc != null)
      {   
         //Get the run-as, principal, cred etc from the invocation and set it on the context
         SecurityActions.setSecurityIdentity(newSC, SecurityActions.getSecurityIdentity(sc));
      }
      else
      { 
         //Local EJB Invocation or some one created the Invocation object on the server side
         mi.setSecurityContext(newSC);
      }
      //Set the SecurityManagement on the context
      SecurityActions.setSecurityManagement(newSC, container.getSecurityManagement());
      if (log.isTraceEnabled())
      {
         log.trace("establishSecurityIdentity:SecCtx="+SecurityActions.trace(newSC));
      }
   }
}
