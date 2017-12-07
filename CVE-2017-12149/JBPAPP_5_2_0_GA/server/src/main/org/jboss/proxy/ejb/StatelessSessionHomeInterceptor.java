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

import java.lang.reflect.Method;

import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.RemoveException;
import javax.ejb.Handle;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.HomeHandle;

import org.jboss.proxy.ejb.handle.HomeHandleImpl;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationContext;
import org.jboss.invocation.InvocationKey;
import org.jboss.invocation.InvocationType;

/**
 * The client-side proxy for a stateless session Home object,
 * that caches the stateless session interface
 *      
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 81030 $
 */
public class StatelessSessionHomeInterceptor
   extends HomeInterceptor
{
   /** Serial Version Identifier */
   private static final long serialVersionUID = 1333656107035759719L;
   
   // Attributes ----------------------------------------------------

   /**
    * The cached interface
    */
   Object cached;
   
   // Constructors --------------------------------------------------
   
   /**
   * No-argument constructor for externalization.
   */
   public StatelessSessionHomeInterceptor() {}
   
   // Public --------------------------------------------------------
   
   public Object invoke(Invocation invocation)
      throws Throwable
   {
      // Is this create()?
      boolean create = invocation.getMethod().getName().equals("create");

      // Do we have a cached version?
      if (create && cached != null)
         return cached;

      // Not a cached create
      Object result = super.invoke(invocation);

      // We now have something to cache
      if (create)
         cached = result;

      return result;
   }
}
