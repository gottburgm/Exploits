/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.resource.binding.remote;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.management.ObjectName;
import javax.naming.BinaryRefAddr;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.resource.Referenceable;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.Invoker;
import org.jboss.invocation.InvokerInterceptor;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.logging.Logger;
import org.jboss.proxy.ClientMethodInterceptor;
import org.jboss.proxy.GenericProxyFactory;
import org.jboss.resource.adapter.jdbc.remote.DataSourceFactory;
import org.jboss.resource.connectionmanager.ConnectionFactoryBindingService;
import org.jboss.system.Registry;
import org.jboss.util.naming.NonSerializableFactory;
import org.jboss.util.naming.Util;

/**
 * A RemoteConnectionFactoryService.
 * 
 * @author <a href="weston.price@jboss.com">Weston Price</a>
 * @version $Revision: 76129 $
 */
public class RemoteConnectionFactoryService extends ConnectionFactoryBindingService implements RemoteConnectionFactoryServiceMBean
{
   //TODO properties -- allow custom overriding of interceptors, lazy method map?
   
   /** The log */
   private static Logger log = Logger.getLogger(RemoteConnectionFactoryService.class);

   /** The jmxInvokerName */
   private ObjectName jmxInvokerName;
   
   /** The delegateInvoker */
   private Invoker delegateInvoker;
   
   /** The theProxy */
   private Object theProxy;
   
   /** The marshalledInvocationMapping */
   private Map marshalledInvocationMapping = new HashMap();
   
   /** The objectMap */
   private Map objectMap = new HashMap();
  
   public ObjectName getJMXInvokerName()
   {
      return jmxInvokerName;
   }

   public void setJMXInvokerName(ObjectName jmxInvokerName)
   {
      this.jmxInvokerName = jmxInvokerName;
   }
   
   protected void startService() throws Exception
   {
      determineBindName();
      createConnectionFactory();
      
      if(jmxInvokerName != null){
         
         createProxy();
         calculateHashes();
         bindConnectionFactory();
         
      }else{
         
         super.bindConnectionFactory();
      }
   
   }
   
   public Object invoke(Invocation invocation) throws Exception
   {
      Object result = null;
      
      if(invocation instanceof MarshalledInvocation){
         
         MarshalledInvocation mi = (MarshalledInvocation)invocation;
         mi.setMethodMap(marshalledInvocationMapping);
      }
      
      final Method targetMethod = invocation.getMethod();
      final Class targetClass = targetMethod.getDeclaringClass();
      final Object[] targetArguments = invocation.getArguments();
      
      final Object retVal = internalInvoke(targetClass, targetMethod, targetArguments, invocation.getId());
      
      if(!(retVal instanceof Serializable) && retVal != null){
         
         log.debug("Creating proxy for instance " + retVal);
         result = createProxy(retVal);
         
      }
      
      return result;
   }
   
   private Object internalInvoke(final Class clazz, final Method method, final Object[] arguments, final Object id)
         throws Exception
   {

      Object result = null;

      //First thing, find the right underlying object to invoke the method on
      //Object to class Map
      if (clazz.isAssignableFrom(cf.getClass()))
      {

         InitialContext initCtx = new InitialContext();
         Object boundCf = initCtx.lookup(bindName);
         result = method.invoke(boundCf, arguments);
         
         
         objectMap.put(Integer.valueOf(String.valueOf(result.hashCode())), result);

      }
      else
      {

         Object target = objectMap.get(id);

         if (target != null)
         {

            result = method.invoke(target, arguments);

            //HACK!
            if (method.getName().equals("close"))
            {
               objectMap.remove(id);
            }

         }

      }

      return result;

   }
  
   private void calculateHashes(Class clazz){
      
      Class[] interfaces = clazz.getInterfaces();
      
      for (int i = 0; i < interfaces.length; i++)
      {
         Class target = interfaces[i];
         Map m = MarshalledInvocation.methodToHashesMap(target);
         marshalledInvocationMapping.putAll(m);
         
      }
   }

   private void calculateHashes(){

      Class[] cfInterfaces = cf.getClass().getInterfaces();
      
      for (int i = 0; i < cfInterfaces.length; i++)
      {
         Class clazz = cfInterfaces[i];
         Method[] methods = clazz.getMethods();
         
         for (int j = 0; j < methods.length; j++)
         {
            
            Method m = methods[j];
            Long hash = new Long(MarshalledInvocation.calculateHash(m));
            marshalledInvocationMapping.put(hash, m);
            
         }
      }
   }

   private Object createProxy(Object value) throws Exception{
      
      delegateInvoker = (Invoker)Registry.lookup(jmxInvokerName);
      log.debug("Using delegate: " + delegateInvoker + " for invoker=" + jmxInvokerName);
      
      //TODO -- Look at this
      calculateHashes(value.getClass());
      
      final Class[] targetInterfaces = value.getClass().getInterfaces();
      final ArrayList interceptorList = new ArrayList();
      Object cacheID = new Integer(value.hashCode());
      String proxyBindingName = null;
      String jndiName = null;
      
      interceptorList.add(ClientMethodInterceptor.class);
      interceptorList.add(InvokerInterceptor.class);
      
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      GenericProxyFactory proxyFactory = new GenericProxyFactory();
      
      Object proxy = proxyFactory.createProxy(cacheID, serviceName,
            delegateInvoker, jndiName, proxyBindingName, interceptorList,
            loader, targetInterfaces);
      
      objectMap.put(cacheID, value);
      
      return proxy;
      
   }
   
   private void createProxy() throws Exception{
   
      delegateInvoker = (Invoker)Registry.lookup(jmxInvokerName);
      log.debug("Using delegate: " + delegateInvoker + " for invoker=" + jmxInvokerName);
      
      final ObjectName serviceName = getServiceName();
      final Integer nameHash = Integer.valueOf(String.valueOf(serviceName.hashCode()));
      Registry.bind(nameHash, serviceName);

      Object cacheID = null;
      String proxyBindingName = null;
      String jndiName = null;
      
      final Class[] connectionFactoryInterface = cf.getClass().getInterfaces();
      final ArrayList interceptorList = new ArrayList();
      interceptorList.add(ClientMethodInterceptor.class);
      interceptorList.add(InvokerInterceptor.class);
      
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      GenericProxyFactory proxyFactory = new GenericProxyFactory();
      theProxy = proxyFactory.createProxy(cacheID, serviceName,
            delegateInvoker, jndiName, proxyBindingName, interceptorList,
            loader, connectionFactoryInterface);
      
   }
 
   protected void bindConnectionFactory() throws Exception
   {
      InitialContext initCtx = new InitialContext();

      try
      {

         NonSerializableFactory.rebind(bindName, cf);
         final Referenceable referenceable = (Referenceable) cf;

         final ByteArrayOutputStream baos = new ByteArrayOutputStream();
         final ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(theProxy);
         oos.close();

         final byte[] proxyBytes = baos.toByteArray();
         final BinaryRefAddr dsAddr = new BinaryRefAddr("ProxyData", proxyBytes);
         final String remoteFactoryName = RemoteConnectionFactoryHelper.class.getName();
         final String localFactoryName = cf.getClass().getName();

         final Reference dsRef = new Reference(localFactoryName, dsAddr, remoteFactoryName, null);
         referenceable.setReference(dsRef);

         baos.reset();
         final ObjectOutputStream oos2 = new ObjectOutputStream(baos);
         oos2.writeObject(RemoteConnectionFactoryHelper.vmID);
         oos2.close();
         final byte[] id = baos.toByteArray();
         final BinaryRefAddr localAddr = new BinaryRefAddr("VMID", id);
         dsRef.add(localAddr);

         final StringRefAddr jndiRef = new StringRefAddr("JndiName", bindName);
         dsRef.add(jndiRef);
         Util.rebind(initCtx, bindName, cf);

         log.info("Bound ConnectionManager '" + serviceName + "' to JNDI name '" + bindName + "'");
      }
      catch (NamingException e)
      {
         log.error("Could not bind ConnectionFactory into jndi: " + bindName, e);
         throw new DeploymentException("Could not bind ConnectionFactory into jndi: " + bindName, e);

      }
      finally
      {

         if (initCtx != null)
         {

            initCtx.close();

         }

      }

   }


}
