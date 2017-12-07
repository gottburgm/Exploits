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
package org.jboss.aspects.remoting.interceptors.marshall;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.util.PayloadKey;
import org.jboss.aspects.remoting.interceptors.invoker.RemotingInterceptorFactory;
import org.jboss.remoting.marshal.Marshaller;

/**
 * @author <a href="mailto:tom@jboss.org">Tom Elrod</a>
 */
public class MarshallInterceptor implements Interceptor
{
   private Marshaller marshaller;

   public MarshallInterceptor(Marshaller marshaller)
   {
      this.marshaller = marshaller;
   }

   public String getName()
   {
      return "MarshallInterceptor";
   }

   /**
    * By default, this will only add the marshaller to be used to the invocation meta data,
    * which will later be used by the transport.
    *
    * @param invocation
    * @return
    * @throws Throwable
    */
   public Object invoke(Invocation invocation) throws Throwable
   {
      // need to add marshaller to invocation so can be found by transport later on
      invocation.getMetaData().addMetaData(RemotingInterceptorFactory.REMOTING,
            RemotingInterceptorFactory.MARSHALLER, marshaller, PayloadKey.TRANSIENT);

      return invocation.invokeNext();
   }
}