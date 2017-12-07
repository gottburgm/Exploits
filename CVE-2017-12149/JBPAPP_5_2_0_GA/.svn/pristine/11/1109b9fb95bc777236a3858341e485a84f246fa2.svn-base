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
package org.jboss.web.tomcat.security.jaspi;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.auth.message.callback.GroupPrincipalCallback;
import javax.security.auth.message.callback.PasswordValidationCallback;

import org.apache.log4j.Logger;

/**
 * Callback handler for JASPI Web Profile
 * @author Anil.Saldhana@redhat.com
 * @since Oct 8, 2008
 */
public class TomcatJASPICallbackHandler implements CallbackHandler
{
   private static Logger log = Logger.getLogger(TomcatJASPICallbackHandler.class);
   
   private CallerPrincipalCallback callerPrincipalCallback;
   private PasswordValidationCallback passwordValidationCallback;
   private GroupPrincipalCallback groupPrincipalCallback;

   public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
   {
      if(callbacks.length > 0)
      {
         for(Callback callback: callbacks)
         {
           if(callback instanceof CallerPrincipalCallback)
           {
              callback = this.callerPrincipalCallback;
           }
           else
           if(callback instanceof PasswordValidationCallback)
           {
              callback = this.passwordValidationCallback;
           }
           else
           if(callback instanceof GroupPrincipalCallback)
           {
              callback = this.groupPrincipalCallback;
           }
           else
              log.trace("Callback " + callback.getClass().getCanonicalName() + " not supported");
         } 
      }
   }
    
   public CallerPrincipalCallback getCallerPrincipalCallback()
   {
      return callerPrincipalCallback;
   }

   public PasswordValidationCallback getPasswordValidationCallback()
   {
      return passwordValidationCallback;
   }

   public GroupPrincipalCallback getGroupPrincipalCallback()
   {
      return groupPrincipalCallback;
   }

   public void setCallerPrincipalCallback(CallerPrincipalCallback callerPrincipalCallback)
   {
     this.callerPrincipalCallback =  callerPrincipalCallback;
   }
   
   public void setGroupPrincipalCallback(GroupPrincipalCallback groupPrincipalCallback)
   {
      this.groupPrincipalCallback = groupPrincipalCallback;
   }
   
   public void setPasswordValidationCallback(PasswordValidationCallback passwordValidationCallback)
   {
      this.passwordValidationCallback = passwordValidationCallback;
   }
}