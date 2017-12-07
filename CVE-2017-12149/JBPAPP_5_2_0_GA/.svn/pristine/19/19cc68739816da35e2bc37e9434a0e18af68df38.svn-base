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
package org.jboss.ha.framework.server;

import org.jboss.ha.framework.interfaces.ResponseFilter;
import org.jgroups.Address;
import org.jgroups.blocks.RspFilter;
import org.jgroups.stack.IpAddress;

/**
 * JGroups RspFilter adapter class that delegates work to ResponseFilter, 
 * Cluster abstraction of RspFilter.
 * 
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 */
public class RspFilterAdapter implements RspFilter
{
   private ResponseFilter filter;
   
   public RspFilterAdapter(ResponseFilter filter)
   {
      this.filter = filter;
   }

   public boolean isAcceptable(Object response, Address sender)
   {
      return filter.isAcceptable(response, new ClusterNodeImpl((IpAddress)sender));
   }

   public boolean needMoreResponses()
   {
      return filter.needMoreResponses();
   }
}
