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
package org.jboss.ejb.plugins;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationType;

import javax.ejb.TimerHandle;

/**
 * This Interceptor validates the incomming arguments and the return value of the call.
 *
 * Here is the place where you want to make sure that local object don't pass through
 * the remote interface. 
 *
 * @author Thomas.Diesler@jboss.org
 * @version $Revision: 81030 $
 */
public class CallValidationInterceptor
        extends AbstractInterceptor
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------

   // Interceptor implementation --------------------------------------
   
   public Object invokeHome(final Invocation mi) throws Exception
   {
      validateArguments(mi);
      Object obj = getNext().invokeHome(mi);
      return validateReturnValue(mi, obj);
   }

   public Object invoke(final Invocation mi) throws Exception
   {
      validateArguments(mi);
      Object obj = getNext().invoke(mi);
      return validateReturnValue(mi, obj);
   }

   /** Do some validation of the incoming parameters */
   protected void validateArguments(Invocation mi)
   {
      if (mi.getType() == InvocationType.REMOTE)
      {
         Object[] params = mi.getArguments();
         for (int i = 0; i < params.length; i++)
         {
            Object obj = params[i];
            if (obj instanceof TimerHandle)
               throw new IllegalArgumentException("Cannot pass TimerHandle through remote interface");
         }
      }
   }

   /** Do some validation of the return value */
   protected Object validateReturnValue(Invocation mi, Object retValue)
   {
      if (mi.getType() == InvocationType.REMOTE)
      {
         if (retValue instanceof TimerHandle)
            throw new IllegalArgumentException("Cannot return TimerHandle from remote interface");
      }
      return retValue;
   }

}
