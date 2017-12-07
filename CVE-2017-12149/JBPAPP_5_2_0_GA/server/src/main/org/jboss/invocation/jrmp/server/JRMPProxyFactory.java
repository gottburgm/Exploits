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
package org.jboss.invocation.jrmp.server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Method;
import javax.management.ObjectName;
import javax.naming.InitialContext;

import org.jboss.invocation.InvokerInterceptor;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.naming.Util;
import org.jboss.proxy.ClientMethodInterceptor;
import org.jboss.proxy.GenericProxyFactory;
import org.jboss.system.Registry;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.metadata.MetaData;
import org.w3c.dom.Element;

/** Create an interface proxy that uses RMI/JRMP to communicate with the server
 * side object that exposes the corresponding JMX invoke operation. Requests
 * make through the proxy are sent to the JRMPInvoker instance the proxy
 * is bound to. 
 *
 * @author Scott.Stark@jboss.org
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 * @version $Revision: 81030 $
 */
public class JRMPProxyFactory extends ServiceMBeanSupport
   implements JRMPProxyFactoryMBean
{
   /** The server side JRMPInvoker mbean that will handle RMI/JRMP transport */
   private ObjectName invokerName;
   /** The server side mbean that exposes the invoke operation for the
    exported interface */
   private ObjectName targetName;
   /** The Proxy object which uses the proxy as its handler */
   protected Object theProxy;
   /** The JNDI name under which the proxy will be bound */
   private String jndiName;
   /** The interface that the proxy implements */
   private Class[] exportedInterfaces;
   /** The optional definition */
   private Element interceptorConfig;
   /** The interceptor Classes defined in the interceptorConfig */
   private ArrayList interceptorClasses = new ArrayList();
   /** invoke target method */
   private boolean invokeTargetMethod;
   /** methods by their hash code */
   private final Map methodMap = new HashMap();
   /** signatures by method */
   private final Map signatureMap = new HashMap();

   public JRMPProxyFactory()
   {
      interceptorClasses.add(ClientMethodInterceptor.class);
      interceptorClasses.add(InvokerInterceptor.class);
   }

   public ObjectName getInvokerName()
   {
      return invokerName;
   }
   public void setInvokerName(ObjectName invokerName)
   {
      this.invokerName = invokerName;
   }

   public ObjectName getTargetName()
   {
      return targetName;
   }
   public void setTargetName(ObjectName targetName)
   {
      this.targetName = targetName;
   }

   public String getJndiName()
   {
      return jndiName;
   }
   public void setJndiName(String jndiName)
   {
      this.jndiName = jndiName;
   }

   public Class getExportedInterface()
   {
      return exportedInterfaces[0];
   }
   public void setExportedInterface(Class exportedInterface)
   {
      this.exportedInterfaces = new Class[] {exportedInterface};
   }

   public Class[] getExportedInterfaces()
   {
      return exportedInterfaces;
   }
   public void setExportedInterfaces(Class[] exportedInterfaces)
   {
      this.exportedInterfaces = exportedInterfaces;
   }

   public boolean getInvokeTargetMethod()
   {
      return invokeTargetMethod;
   }

   public void setInvokeTargetMethod(boolean invokeTargetMethod)
   {
      this.invokeTargetMethod = invokeTargetMethod;
   }

   public Element getClientInterceptors()
   {
      return interceptorConfig;
   }
   public void setClientInterceptors(Element config) throws Exception
   {
      this.interceptorConfig = config;
      Iterator interceptorElements = MetaData.getChildrenByTagName(interceptorConfig, "interceptor");
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      interceptorClasses.clear();
      while( interceptorElements != null && interceptorElements.hasNext() )
      {
         Element ielement = (Element) interceptorElements.next();
         String className = null;
         className = MetaData.getElementContent(ielement);
         Class clazz = loader.loadClass(className);
         interceptorClasses.add(clazz);
         log.debug("added interceptor type: "+clazz);
      }
   }

   public Object getProxy()
   {
      return theProxy;
   }

   public Object invoke(Invocation mi) throws Exception
   {
      final boolean remoteInvocation = mi instanceof MarshalledInvocation;
      if(remoteInvocation)
      {
         ((MarshalledInvocation)mi).setMethodMap(methodMap);
      }

      final Object result;
      if(invokeTargetMethod)
      {
         String signature[] = (String[])signatureMap.get(mi.getMethod());
         result = server.invoke(targetName, mi.getMethod().getName(), mi.getArguments(), signature);
      }
      else
      {
         result = server.invoke(targetName, "invoke", new Object[]{mi}, Invocation.INVOKE_SIGNATURE);
      }

      return result;
   }

   /** Initializes the servlet.
    */
   protected void startService() throws Exception
   {
      /* Create a binding between the invoker name hash and the jmx name
      This is used by the JRMPInvoker to map from the Invocation ObjectName
      hash value to the target JMX ObjectName.
      */
      Integer nameHash = new Integer(getServiceName().hashCode());
      Registry.bind(nameHash, getServiceName());

      // Create the service proxy
      Object cacheID = null;
      String proxyBindingName = null;
      Class[] ifaces = exportedInterfaces;
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      createProxy(cacheID, proxyBindingName, loader, ifaces);
      log.debug("Created JRMPPRoxy for service="+targetName
         +", nameHash="+nameHash+", invoker="+invokerName);

      if( jndiName != null )
      {
         InitialContext iniCtx = new InitialContext();
         Util.rebind(iniCtx, jndiName, theProxy);
         log.debug("Bound proxy under jndiName="+jndiName);
      }

      for(int i = 0; i < exportedInterfaces.length; ++i)
      {
         final Method[] methods = exportedInterfaces[i].getMethods();
         for(int j = 0; j < methods.length; ++j)
         {
            methodMap.put(new Long(MarshalledInvocation.calculateHash(methods[j])), methods[j]);

            String signature[];
            final Class[] types = methods[j].getParameterTypes();
            if(types == null || types.length == 0)
            {
               signature = null;
            }
            else
            {
               signature = new String[types.length];
               for(int typeInd = 0; typeInd < types.length; ++typeInd)
               {
                  signature[typeInd] = types[typeInd].getName();
               }
            }
            signatureMap.put(methods[j], signature);
         }
      }
   }

   protected void stopService() throws Exception
   {
      Integer nameHash = new Integer(getServiceName().hashCode());
      Registry.unbind(nameHash);
      if( jndiName != null )
      {
         InitialContext iniCtx = new InitialContext();
         Util.unbind(iniCtx, jndiName);
      }
      this.theProxy = null;
   }

   protected void destroyService() throws Exception
   {
      interceptorClasses.clear();
   }

   protected void createProxy
   (
      Object cacheID, 
      String proxyBindingName,
      ClassLoader loader,
      Class[] ifaces
   )
   {
      GenericProxyFactory proxyFactory = new GenericProxyFactory();
      theProxy = proxyFactory.createProxy(cacheID, getServiceName(), invokerName,
         jndiName, proxyBindingName, interceptorClasses, loader, ifaces);
   }

   protected void rebind() throws Exception
   {
      log.debug("(re-)Binding " + jndiName);
      Util.rebind(new InitialContext(), jndiName, theProxy);
   }

   protected ArrayList getInterceptorClasses()
   {
      return interceptorClasses;
   }
}
