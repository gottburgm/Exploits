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

package org.jboss.services.binding;

/**
 * Exception thrown by {@link ServiceBindingManager} when no binding 
 * can be found that matches a binding request.
 * 
 * @author Brian Stansberry
 * @version $Revision: 85945 $
 */
public class NoSuchBindingException extends Exception
{

   /** The serialVersionUID */
   private static final long serialVersionUID = -9055052272112983527L;

   public NoSuchBindingException(String serverName, String serviceName, String bindingName)
   {
      this("No binding " + bindingName + " found for service " + serviceName + " in set " + serverName);
   }
   
   public NoSuchBindingException(String msg)
   {
      super(msg);
   }
}
