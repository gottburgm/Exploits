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
package org.jboss.invocation;

import javax.transaction.Transaction;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Map;

/**
 * Optimized invocation object for Local interface invocations
 *
 * @author  <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 81030 $
 */
public class LocalEJBInvocation extends Invocation
{
   public LocalEJBInvocation()
   {
   }

   public LocalEJBInvocation(Object id, Method m, Object[] args, Transaction tx,
                Principal identity, Object credential)
   {
      super(id, m, args, tx, identity, credential);
   }

   private Transaction tx;
   private Object credential;
   private Principal principal;
   private Object enterpriseContext;
   private Object id;

   public void setTransaction(Transaction tx)
   {
     this.tx = tx;
   }

   public Transaction getTransaction()
   {
      return this.tx;
   }

   public Object getCredential()
   {
      return credential;
   }

   public void setCredential(Object credential)
   {
      this.credential = credential;
   }

   public Principal getPrincipal()
   {
      return principal;
   }

   public void setPrincipal(Principal principal)
   {
      this.principal = principal;
   }

   public Object getEnterpriseContext()
   {
      return enterpriseContext;
   }

   public void setEnterpriseContext(Object enterpriseContext)
   {
      this.enterpriseContext = enterpriseContext;
   }

   public Object getId()
   {
      return id;
   }

   public void setId(Object id)
   {
      this.id = id;
   }
}
