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
package org.jboss.proxy.ejb;

import java.security.Principal;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationKey;
import org.jboss.proxy.Interceptor; 
import org.jboss.security.RunAs;
import org.jboss.security.SecurityContext;

/**
 * Establishes a SecurityContext to be sent 
 * over the invocation
 * @author Anil.Saldhana@redhat.com
 * @since Nov 30, 2008
 */
public class SecurityContextInterceptor extends Interceptor
{ 
   @Override
   public Object invoke(Invocation invocation) throws Throwable
   {
      SecurityContext sc  = null;
      boolean compatib = validateASVersionCompatibility(invocation); 
      
      if(compatib)
      {
         sc  = SecurityActions.getSecurityContext();
         RunAs callerRAI =  SecurityActions.getCallerRunAsIdentity();
         SecurityContext newSc = createSecurityContext(invocation);
         //Push the caller run-as identity onto the security context 
         if(callerRAI != null)
         {
            SecurityActions.setOutgoingRunAs(newSc, callerRAI);
            SecurityActions.setIncomingRunAs(newSc, callerRAI);
         }
         /**
          * Push the security context on the invocation
          */
         invocation.setSecurityContext(newSc); 
      }
      try
      { 
         return getNext().invoke(invocation); 
      }
      finally
      { 
         if(compatib && sc != null)
            SecurityActions.setSecurityContext(sc); 
      }
   }
   
   /**
    * Return loaded Security Context to be passed on the invocation
    * @param invocation invocation instance
    * @return
    */
   private SecurityContext createSecurityContext(Invocation invocation) throws Exception
   {   
      //There may be principal set on the invocation
      Principal p = invocation.getPrincipal();
      Object cred = invocation.getCredential(); 
      
      //Create a new SecurityContext
      String domain = (String) invocation.getInvocationContext().getValue(InvocationKey.SECURITY_DOMAIN);
      if(domain == null)
         domain = "CLIENT_PROXY";
      return SecurityActions.createSecurityContext(p,cred, domain);
   } 
   
   /**
    * JBAS-6275: Validates that the server is AS5+ such that we can send the security context
    * over the invocation
    * @param invocation
    * @return
    */
   private boolean validateASVersionCompatibility(Invocation invocation)
   {
      try
      {
         invocation.getInvocationContext().getValue(InvocationKey.SECURITY_DOMAIN);
         //So the field exists. We are in AS5+         
      }
      catch(NoSuchFieldError nsfe)
      {
         //Probably we are in 4.2.x
         return false;
      }
      return true;
   }
}