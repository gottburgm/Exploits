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
package org.jboss.test.jmx.interceptors;

import org.jboss.mx.server.Invocation;
import org.jboss.system.InterceptorServiceMBeanSupport;

/**
 * AdderInterceptorService will attach dynamically an interceptor
 * to the AdderPOJO (xmbean) and add +1 to the add() method result
 * 
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81036 $
 */
public class AdderInterceptorService extends InterceptorServiceMBeanSupport
   implements AdderInterceptorServiceMBean
{
   public AdderInterceptorService()
   {
      // empty
   }

   protected void startService() throws Exception   
   {
      // attach our interceptor
      super.attach();
   }
   
   protected void stopService()
   {
      // detach our interceptor
      super.detach();
   }
   
   /**
    * Override
    */
   protected Object invoke(Invocation invocation) throws Throwable
   {
      String type = invocation.getType();
      
      if (type.equals(Invocation.OP_INVOKE))
      {
         String name = invocation.getName();
         
         if (name.equals("add"))
         {
            Integer result = (Integer)super.invoke(invocation);
            
            return new Integer(result.intValue() + 1);
         }
      }
      return super.invoke(invocation);
   }
}
