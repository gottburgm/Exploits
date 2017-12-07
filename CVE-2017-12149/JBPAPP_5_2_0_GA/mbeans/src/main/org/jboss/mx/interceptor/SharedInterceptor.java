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
package org.jboss.mx.interceptor;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;


/**
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 81026 $  
 */
public interface SharedInterceptor
   extends Interceptor, MBeanRegistration
{

   /**
    * Returns the object name of this shared interceptor.
    *
    * @return interceptor's object name
    */
   ObjectName getObjectName();
   
   /**
    * Returns the interceptor's view to the MBean server it has been
    * registered to.
    *
    * @return  interceptor's view to its MBean server
    */
   MBeanServer getMBeanServer();
   
   /**
    * Registers this interceptor to the given MBean server.
    *
    * @param   server   MBean server
    *
    * @return  the object name of the registered interceptor
    *
    * @throws InterceptorNameConflictException if an interceptor was already
    *         registered to the given server
    */
   ObjectName register(MBeanServer server) throws InterceptorNameConflictException;
   

}
      



