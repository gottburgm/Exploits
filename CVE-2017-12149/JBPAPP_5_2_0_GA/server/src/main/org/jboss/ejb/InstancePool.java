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
package org.jboss.ejb;


/**
 * Defines the model for a EnterpriseContext instance pool.
 *
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Oberg</a>
 * @version $Revision: 81030 $
 */
public interface InstancePool
   extends ContainerPlugin
{
   /**
    * Get an instance without identity.
    *
    * <p>Can be used by finders and create-methods, or stateless beans
    *
    * @return    Context/w instance
    *
    * @throws Exception    RemoteException
    */
   EnterpriseContext get() throws Exception;

   /**
    * Return an anonymous instance after invocation.
    *
    * @param ctx    The context to free.
    */
   void free(EnterpriseContext ctx);

   /**
    * Discard an anonymous instance after invocation.
    * This is called if the instance should not be reused, perhaps due to some
    * exception being thrown from it.
    *
    * @param ctx    The context to discard.
    */
   void discard(EnterpriseContext ctx);

   /**
    * Return the size of the pool.
    *
    * @return the size of the pool.
    */
   int getCurrentSize();

   /**
    * Get the maximum size of the pool.
    *
    * @return the size of the pool.
    */
   public int getMaxSize();

}

