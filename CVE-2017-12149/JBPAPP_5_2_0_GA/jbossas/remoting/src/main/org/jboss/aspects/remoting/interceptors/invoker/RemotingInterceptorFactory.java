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
package org.jboss.aspects.remoting.interceptors.invoker;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aspects.remoting.interceptors.marshall.MarshallInterceptor;
import org.jboss.aspects.remoting.interceptors.transport.TransportInterceptor;
import org.jboss.remoting.Client;
import org.jboss.remoting.InvokerLocator;
import org.jboss.remoting.marshal.MarshalFactory;
import org.jboss.remoting.marshal.Marshaller;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author <a href="mailto:telrod@e2technologies.net">Tom Elrod</a>
 */
public class RemotingInterceptorFactory
{
   public static final String REMOTING = "REMOTING";
   public static final String INVOKER_LOCATOR = "INVOKER_LOCATOR";
   public static final String SUBSYSTEM = "SUBSYSTEM";
   public static final String MARSHALLER = "MARSHALLER";
   public static final String LOADER = "LOADER";


   public static Invocation injectRemotingInterceptors(Invocation invocation)
   {
      Invocation newInvocation = null;

      // Have to first get the locator from the invocation
      InvokerLocator locator = (InvokerLocator) invocation.getMetaData(REMOTING, INVOKER_LOCATOR);
      if (locator != null)
      {
         Interceptor[] newInterceptor = null;
         ArrayList interceptorList = new ArrayList();

         // First need to determine which marshalling interceptors are needed
         String dataType = (String) invocation.getMetaData(REMOTING, InvokerLocator.DATATYPE);
         if (dataType == null)
         {
            dataType = getDataType(locator);
         }

         if (dataType != null)
         {
            // If found data type, will get marshaller and create interceptor wrapper for it
            // and then insert into interceptor chain
            Marshaller marshaller = MarshalFactory.getMarshaller(dataType);
            MarshallInterceptor marshallInterceptor = new MarshallInterceptor(marshaller);
            interceptorList.add(marshallInterceptor);
         }

         // Now onto creating transport interceptor
         ClassLoader loader = (ClassLoader) invocation.getMetaData(REMOTING, LOADER);
         String subsystem = (String) invocation.getMetaData(REMOTING, SUBSYSTEM);

         Client client = null;
         try
         {
            if (loader != null)
            {
               client = new Client(loader, locator, subsystem, null);
            }
            else
            {
               client = new Client(locator, subsystem);
            }
         }
         catch (Exception e)
         {
            throw new RuntimeException("Could not create remoting client.", e);
         }

         TransportInterceptor transportInterceptor = new TransportInterceptor(client);
         interceptorList.add(transportInterceptor);

         //TODO -TME Important to note that this will actually be appending new interceptors
         // to end of chain based on my understanding of implementation.  This is fine unless
         // the InvokerInterceptor is NOT the final interceptor in the chain as there may
         // be side effects that will cause problems.

         // Should have all new remoting interceptors, so create new invocation
         newInterceptor = (Interceptor[]) interceptorList.toArray(new Interceptor[interceptorList.size()]);
         newInvocation = invocation.getWrapper(newInterceptor);
      }
      else
      {
         throw new RuntimeException("Require InvokerLocator to make remote invocations.");
      }

      return newInvocation;
   }

   private static String getDataType(InvokerLocator locator)
   {
      String type = null;

      if (locator != null)
      {
         Map params = locator.getParameters();
         if (params != null)
         {
            type = (String) params.get(InvokerLocator.DATATYPE);
         }
      }
      return type;
   }
}
