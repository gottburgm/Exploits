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

import org.jboss.mx.server.Invocation;
import org.jboss.mx.server.MBeanInvoker;

/** This interceptor returns the MBeanInfo from the invocation invoker. It
 * only makes sense to use this interceptor as the last interceptor in the
 * getMBeanInfo() call chain.
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81026 $
 */
public class ModelMBeanInfoInterceptor extends AbstractInterceptor
{
   public ModelMBeanInfoInterceptor()
   {
      super("Model MBeanInfo Interceptor");
   }

   public ModelMBeanInfoInterceptor(String name)
   {
      super("Model MBeanInfo Interceptor for " + name);
   }

   /** Return the MBeanInfo from the invocation MBeanInvoker
    * 
    * @param invocation
    * @return
    * @throws InvocationException
    */ 
   public Object invoke(Invocation invocation) throws Throwable
   {
      MBeanInvoker invoker = invocation.getInvoker();
      return invoker.getMetaData();
   }
}
