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

package org.jboss.services.binding.impl;

import java.net.InetAddress;

import org.jboss.services.binding.InetAddressServiceBindingValueSource;
import org.jboss.services.binding.IntServiceBindingValueSource;
import org.jboss.services.binding.ServiceBinding;

/**
 * ServiceBindingValueSource that returns the given binding's
 * {@link ServiceBinding#getBindAddress() bind address} and 
 * {@link ServiceBinding#getPort() port}. Does not perform any transformations.
 * 
 * @author Brian Stansberry
 * @version $Revision: 85945 $
 */
public class SimpleServiceBindingValueSourceImpl
      implements
         IntServiceBindingValueSource,
         InetAddressServiceBindingValueSource
{

   /**
    * @return <code>binding.{@link ServiceBinding#getPort() getPort()}</code>
    */
   public int getIntServiceBindingValue(ServiceBinding binding)
   {
      return binding.getPort();
   }

   /**
    * @return <code>binding.{@link ServiceBinding#getBindAddress() getBindAddress()}</code>
    */
   public InetAddress getInetAddressServiceBindingValue(ServiceBinding binding)
   {
      return binding.getBindAddress();
   }

   /**
    * @return <code>new Integer(binding.{@link ServiceBinding#getPort() getPort()})</code>
    */
   public Object getServiceBindingValue(ServiceBinding binding, Object... params)
   {
      if (params != null && params.length > 0)
         throw new IllegalArgumentException(getClass().getSimpleName() + ".getServiceBindingValue() does not accept argument 'params'");
      
      return new Integer(getIntServiceBindingValue(binding));
   }

}
