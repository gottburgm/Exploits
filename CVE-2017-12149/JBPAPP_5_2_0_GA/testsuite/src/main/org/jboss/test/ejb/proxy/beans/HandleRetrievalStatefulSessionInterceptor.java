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
package org.jboss.test.ejb.proxy.beans;

import java.rmi.RemoteException;

import javax.ejb.EJBObject;
import javax.ejb.Handle;

import org.jboss.invocation.InvocationContext;
import org.jboss.invocation.Invoker;
import org.jboss.proxy.ejb.StatefulSessionInterceptor;
import org.jboss.proxy.ejb.handle.StatefulHandleImpl;

/**
 * StatefulSessionInterceptor.
 * 
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 */
public class HandleRetrievalStatefulSessionInterceptor extends StatefulSessionInterceptor
{
   private RetrievalMethodHandle handle;
   
   @Override
   protected Handle createHandle(int objectName, String jndiName, Invoker invoker, Object id, InvocationContext ctx)
   {
      handle = new RetrievalMethodHandle(objectName, jndiName, invoker, ctx.getInvokerProxyBinding(), id, ctx.getValue("InvokerID"));
      return handle;
   }

   public class RetrievalMethodHandle extends StatefulHandleImpl
   {
      /** The serialVersionUID */
      private static final long serialVersionUID = -5424836611145344994L;

      private boolean gotEjbObjectViaInvoker;
      
      private boolean gotEjbObjectViaJndi;
      
      public RetrievalMethodHandle()
      {
      }

      public RetrievalMethodHandle(int objectName, String jndiName, Invoker invoker, String invokerProxyBinding,
            Object id, Object invokerID)
      {
         super(objectName, jndiName, invoker, invokerProxyBinding, id, invokerID);
      }

      public boolean isGotEjbObjectViaInvoker()
      {
         return gotEjbObjectViaInvoker;
      }

      public boolean isGotEjbObjectViaJndi()
      {
         return gotEjbObjectViaJndi;
      }
      
      @Override
      public EJBObject getEJBObject() throws RemoteException
      {
         gotEjbObjectViaInvoker = false;
         gotEjbObjectViaJndi = false;
         return super.getEJBObject();
      }

      @Override
      protected EJBObject getEjbObjectViaInvoker() throws Exception
      {
         EJBObject ejbObject = super.getEjbObjectViaInvoker();
         gotEjbObjectViaInvoker = true;
         return ejbObject;
      }

      @Override
      protected EJBObject getEjbObjectViaJndi() throws RemoteException
      {
         EJBObject ejbObject = super.getEjbObjectViaJndi();
         gotEjbObjectViaJndi = true;
         return ejbObject;
      }
   }
}
