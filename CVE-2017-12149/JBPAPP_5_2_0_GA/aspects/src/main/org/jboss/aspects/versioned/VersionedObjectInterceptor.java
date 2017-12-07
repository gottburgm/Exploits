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
package org.jboss.aspects.versioned;

import org.jboss.aop.joinpoint.FieldReadInvocation;
import org.jboss.aop.joinpoint.FieldWriteInvocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.logging.Logger;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

/**
 *  This interceptor handles chooses an object to invoke
 *  on based on the transaction
 *
 *  @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 *  @version $Revision: 80997 $
 */
public class VersionedObjectInterceptor implements org.jboss.aop.advice.Interceptor
{
   /** 
    * Logging instance 
    */
   protected Logger log = Logger.getLogger(this.getClass());

   private TransactionManager tm;
   public VersionedObjectInterceptor(TransactionManager tm)
   {
      this.tm = tm;
   }

   public String getName() { return "VersionedObjectInterceptor"; }

   /**
    *
    */
   public Object invoke(org.jboss.aop.joinpoint.Invocation invocation) throws Throwable
   {
      Transaction tx = tm.getTransaction();
      VersionedObject manager = (VersionedObject)invocation.getMetaData(Versioned.VERSIONED, Versioned.VERSIONED_OBJECT);

      Object version = manager.getVersion(tx);
      if (version != null)
      {
         invocation.setTargetObject(version);
         return invocation.invokeNext();
      }

      boolean isReadonly = false;
      if (invocation instanceof MethodInvocation)
      {
         String readonly = (String)invocation.getMetaData(Versioned.VERSIONED, Versioned.READONLY);
         if (readonly != null)
         {
            isReadonly = Boolean.getBoolean(readonly.toLowerCase());
         }
      }
      else if (invocation instanceof FieldReadInvocation)
      {
         isReadonly = true;
      }
      else if (invocation instanceof FieldWriteInvocation)
      {
         isReadonly = false;
      }
      if (isReadonly) return invocation.invokeNext();

      // Ok, we're in a tx, we're not readonly, there is no previous version, so create another
      
      version = manager.createVersion(tx);
      invocation.setTargetObject(version);
      return invocation.invokeNext();
   }

}
