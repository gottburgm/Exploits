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
package org.jboss.proxy;

import java.util.ArrayList;

import org.jboss.invocation.InvocationContext;

/** An interface implemented by the ClientContainer to provide access to
 * the client proxy interceptors and InvocationContext.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81030 $
 */
public interface IClientContainer
{
   /**
    * Access a copy of the proxy container interceptor stack.
    * @return ArrayList<org.jboss.proxy.Interceptor>
    */ 
   public ArrayList getInterceptors();
   /**
    * Set the proxy container interceptor stack.
    * @param interceptors - ArrayList<org.jboss.proxy.Interceptor> to
    * install as the new interceptor stack
    */ 
   public void setInterceptors(ArrayList interceptors);
   /**
    * Access the InvocationContext associated with the proxy by the
    * server side proxy factory. The contents of this will depend on
    * the proxy factory.
    * @return The proxy creation time InvocationContext
    */ 
   public InvocationContext getInvocationContext();
}
