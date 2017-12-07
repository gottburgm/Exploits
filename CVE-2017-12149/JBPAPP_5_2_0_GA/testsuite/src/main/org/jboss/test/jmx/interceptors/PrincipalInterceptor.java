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

import java.security.Principal;
import org.jboss.mx.interceptor.AbstractInterceptor;
import org.jboss.mx.server.Invocation;
import org.jboss.logging.Logger;
import org.jboss.security.SecurityAssociation;

/** An interceptor that simply asserts the caller is jduke
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public final class PrincipalInterceptor
   extends AbstractInterceptor
{
   private static Logger log = Logger.getLogger(PrincipalInterceptor.class);

   // Interceptor overrides -----------------------------------------
   public Object invoke(Invocation invocation) throws Throwable
   {
      /* Allow access to the getMBeanInfo since this is bbean is deployed
      but not accessed in non-secure tests (JMXInvokerUnitTestCase).
      */
      String type = invocation.getType();
      if( type != Invocation.OP_GETMBEANINFO )
      {
         Principal caller = SecurityAssociation.getPrincipal();
         String opName = invocation.getName();
         log.info("invoke, opName="+opName+", caller="+caller);
         if( caller == null || caller.getName().equals("jduke") == false )
         {
            throw new SecurityException("Caller="+caller+" is not jduke");
         }
      }
      return invocation.nextInterceptor().invoke(invocation);
   }
}
