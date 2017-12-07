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
package org.jboss.proxy;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javax.management.ObjectName;

import org.jboss.invocation.InvocationContext;
import org.jboss.invocation.InvocationKey;
import org.jboss.invocation.Invoker;
import org.jboss.logging.Logger;
import org.jboss.system.Registry;
import org.jboss.util.NestedRuntimeException;


/** A generic factory of java.lang.reflect.Proxy that constructs a proxy
 * that is a composite of ClientContainer/Interceptors/Invoker
 *
 * @todo generalize the proxy/invoker factory object
 * @author Scott.Stark@jboss.org
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 * @version $Revision: 81030 $
 */
public class GenericProxyFactory
{
   private static final Logger log = Logger.getLogger(GenericProxyFactory.class);

   /** Create a composite proxy for the given interfaces, invoker.
    @param id the cache id for the target object if any
    @param targetName the name of the server side service
    @param invoker the detached invoker stub to embed in the proxy
    @param jndiName the JNDI name the proxy will be bound under if not null
    @param proxyBindingName the invoker-proxy-binding name if not null
    @param interceptorClasses the Class objects for the interceptors
    @param loader the ClassLoader to associate the the Proxy
    @param ifaces the Class objects for the interfaces the Proxy implements
    */
   public Object createProxy(Object id, ObjectName targetName,
      Invoker invoker, String jndiName, String proxyBindingName,
      ArrayList interceptorClasses, ClassLoader loader, Class[] ifaces)
   {
      return createProxy(id, targetName, invoker, jndiName, proxyBindingName, interceptorClasses, loader, ifaces, null);
   }

   /** Create a composite proxy for the given interfaces, invoker.
    @param id the cache id for the target object if any
    @param targetName the name of the server side service
    @param invokerName the name of the server side JMX invoker
    @param jndiName the JNDI name the proxy will be bound under if not null
    @param proxyBindingName the invoker-proxy-binding name if not null
    @param interceptorClasses the Class objects for the interceptors
    @param loader the ClassLoader to associate the the Proxy
    @param ifaces the Class objects for the interfaces the Proxy implements
    */
   public Object createProxy(Object id, ObjectName targetName, ObjectName invokerName,
      String jndiName, String proxyBindingName,
      ArrayList interceptorClasses, ClassLoader loader, Class[] ifaces)
   {
      Invoker invoker = (Invoker) Registry.lookup(invokerName);
      if (invoker == null)
         throw new RuntimeException("Failed to find invoker for name: " + invokerName);
      return createProxy(id, targetName, invoker, jndiName, proxyBindingName, interceptorClasses, loader, ifaces, null);
   }

   /** Create a composite proxy for the given interfaces, invoker.
    @param id the cache id for the target object if any
    @param targetName the name of the server side service
    @param invoker the detached invoker stub to embed in the proxy
    @param jndiName the JNDI name the proxy will be bound under if not null
    @param proxyBindingName the invoker-proxy-binding name if not null
    @param interceptorClasses the Class objects for the interceptors
    @param loader the ClassLoader to associate the the Proxy
    @param ifaces the Class objects for the interfaces the Proxy implements
    @param ctx any context to add the invocation context proxy
    */
   public Object createProxy(Object id, ObjectName targetName,
      Invoker invoker, String jndiName, String proxyBindingName,
      ArrayList interceptorClasses, ClassLoader loader, Class[] ifaces, HashMap ctx)
   {
      InvocationContext context;
      if (ctx != null)
         context = new InvocationContext(ctx);
      else
         context = new InvocationContext();
      Integer nameHash = new Integer(targetName.hashCode());
      if (log.isTraceEnabled())
      {
         log.trace("Target name " + targetName + " and corresponding hash code" +  nameHash);
      }      
      context.setObjectName(nameHash);
      context.setCacheId(id);
      if( jndiName != null )
         context.setValue(InvocationKey.JNDI_NAME, jndiName);

      if( invoker == null )
         throw new RuntimeException("Null invoker given for name: " + targetName);
      context.setInvoker(invoker);
      if( proxyBindingName != null )
         context.setInvokerProxyBinding(proxyBindingName);

      // If the IClientContainer interceptor was specified, use the ClientContainerEx
      boolean wantIClientAccess = false;
      for(int n = 0; wantIClientAccess == false && n < interceptorClasses.size(); n ++)
      {
         Class type = (Class) interceptorClasses.get(n);
         wantIClientAccess = type.isAssignableFrom(IClientContainer.class);
      }
      ClientContainer client;
      if( wantIClientAccess )
      {
         client = new ClientContainerEx(context);
      }
      else
      {
         client = new ClientContainer(context);
      }

      try
      {
         loadInterceptorChain(interceptorClasses, client);
      }
      catch(Exception e)
      {
         throw new NestedRuntimeException("Failed to load interceptor chain", e);
      }

      ArrayList tmp = new ArrayList(Arrays.asList(ifaces));
      Class[] ifaces2 = new Class[tmp.size()];
      tmp.toArray(ifaces2);
      return Proxy.newProxyInstance(
         // Classloaders
         loader,
         // Interfaces
         ifaces2,
         // Client container as invocation handler
         client);
   }

   /** The loadInterceptorChain create instances of interceptor
    * classes from the list of classes given by the chain array.
    *
    * @exception Exception if an error occurs
    */
   protected void loadInterceptorChain(ArrayList chain, ClientContainer client)
      throws Exception
   {
      Interceptor last = null;
      for (int i = 0; i < chain.size(); i++)
      {
         Class clazz = (Class)chain.get(i);
         Interceptor interceptor = (Interceptor) clazz.newInstance(); 
         if (last == null)
         {
            last = interceptor;
            client.setNext(interceptor);
         }
         else
         {
            last.setNext(interceptor);
            last = interceptor;
         }
      }
   }
}
