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
package org.jboss.aspects.remoting.interceptors.transport;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aspects.remoting.interceptors.invoker.RemotingInterceptorFactory;
import org.jboss.remoting.Client;
import org.jboss.remoting.marshal.Marshaller;

/**
 * @author <a href="mailto:tom@jboss.org">Tom Elrod</a>
 */
public class TransportInterceptor implements Interceptor
{
   private Client client;

   public TransportInterceptor(Client client)
   {
      this.client = client;
   }

   public String getName()
   {
      return "TransportInterceptor";
   }

   public Object invoke(Invocation invocation) throws Throwable
   {
      // need to add marshaller to invocation so can be found by transport later on
      Marshaller marshaller = (Marshaller) invocation.getMetaData().getMetaData(RemotingInterceptorFactory.REMOTING,
            RemotingInterceptorFactory.MARSHALLER);

      if (marshaller != null)
      {
         client.setMarshaller(marshaller);
      }

      return client.invoke(invocation, null);

   }
}