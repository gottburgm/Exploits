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
package org.jboss.naming;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.UnknownHostException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.net.ServerSocketFactory;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.invocation.jrmp.server.JRMPProxyFactoryMBean;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.threadpool.BasicThreadPoolMBean;
import org.jboss.util.threadpool.ThreadPool;
import org.jnp.interfaces.MarshalledValuePair;
import org.jnp.interfaces.Naming;
import org.jnp.server.Main;
import org.jnp.server.NamingBean;

/**
 * A JBoss service that starts the jnp JNDI server.
 *
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Oberg</a>
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>.
 * @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>.
 * @version $Revision: 88695 $
 *
 * @jmx:mbean name="jboss:service=Naming"
 *  extends="org.jboss.system.ServiceMBean, org.jnp.server.MainMBean"
 */
public class NamingService
   extends ServiceMBeanSupport
   implements NamingServiceMBean
{
   /** The actual namingMain service impl bean */
   private NamingBean namingBean;
   /** */
   private Main namingMain = new Main();
   /** The hash mappings of the Naming interface methods */
   private Map marshalledInvocationMapping = new HashMap();
   /** An optional proxy factory for externalizing the Naming proxy transport */
   private JRMPProxyFactoryMBean proxyFactory;

   public NamingService()
   {
   }

   public NamingBean getNaming()
   {
      return getNamingInfo();
   }
   public void setNaming(NamingBean bean)
   {
      setNamingInfo(bean);
   }
   public NamingBean getNamingInfo()
   {
      return namingBean;
   }
   public void setNamingInfo(NamingBean bean)
   {
      this.namingBean = bean;
      this.namingMain.setNamingInfo(bean);
   }

   public Object getNamingProxy()
      throws Exception
   {
      Object proxy = null;
      if(proxyFactory != null)
         proxy = proxyFactory.getProxy();
      else
         proxy = namingMain.getNamingProxy();
      return proxy;
   }
   public void setNamingProxy(Object proxy)
      throws IOException
   {
      namingMain.setNamingProxy(proxy);
   }

   public Naming getNamingInstance()
   {
      return namingBean.getNamingInstance();
   }

   /** Set the thread pool used for the bootstrap lookups
    *
    * @jmx:managed-attribute
    *
    * @param poolMBean 
    */
   public void setLookupPool(BasicThreadPoolMBean poolMBean)
   {
      ThreadPool lookupPool = poolMBean.getInstance();
      namingMain.setLookupPool(lookupPool);
   }

   /** Get the call by value flag for jndi lookups.
    * 
    * @jmx:managed-attribute
    * @return true if all lookups are unmarshalled using the caller's TCL,
    *    false if in VM lookups return the value by reference.
    */ 
   public boolean getCallByValue()
   {
      return MarshalledValuePair.getEnableCallByReference() == false;
   }
   /** Set the call by value flag for jndi lookups.
    *
    * @jmx:managed-attribute
    * @param flag - true if all lookups are unmarshalled using the caller's TCL,
    *    false if in VM lookups return the value by reference.
    */
   public void setCallByValue(boolean flag)
   {
      boolean callByValue = ! flag;
      MarshalledValuePair.setEnableCallByReference(callByValue);
   }

   public void setPort(int port)
   {
      namingMain.setPort(port);
   }

   public int getPort()
   {
      return namingMain.getPort();
   }

   public void setRmiPort(int port)
   {
      namingMain.setRmiPort(port);
   }

   public int getRmiPort()
   {
      return namingMain.getRmiPort();
   }

   public String getBindAddress()
   {
      return namingMain.getBindAddress();
   }

   public void setBindAddress(String host) throws UnknownHostException
   {
      namingMain.setBindAddress(host);
   }

   public String getRmiBindAddress()
   {
      return namingMain.getRmiBindAddress();
   }

   public void setRmiBindAddress(String host) throws UnknownHostException
   {
      namingMain.setRmiBindAddress(host);
   }

   public int getBacklog()
   {
      return namingMain.getBacklog();
   }

   public void setBacklog(int backlog)
   {
      namingMain.setBacklog(backlog);
   }

   public boolean getInstallGlobalService()
   {
      return namingMain.getInstallGlobalService();
   }
   public void setInstallGlobalService(boolean flag)
   {
      namingMain.setInstallGlobalService(flag);
   }
   public boolean getUseGlobalService()
   {
      return namingMain.getUseGlobalService();
   }
   public void setUseGlobalService(boolean flag)
   {
      namingMain.setUseGlobalService(flag);
   }
   public String getClientSocketFactory()
   {
      return namingMain.getClientSocketFactory();
   }
   public void setClientSocketFactory(String factoryClassName)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException
   {
      namingMain.setClientSocketFactory(factoryClassName);
   }

   public RMIClientSocketFactory getClientSocketFactoryBean()
   {
      return namingMain.getClientSocketFactoryBean();
   }
   public void setClientSocketFactoryBean(RMIClientSocketFactory factory)
   {
      namingMain.setClientSocketFactoryBean(factory);
   }

   public String getServerSocketFactory()
   {
      return namingMain.getServerSocketFactory();
   }
   public void setServerSocketFactory(String factoryClassName)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException
   {
      namingMain.setServerSocketFactory(factoryClassName);
   }
   public RMIServerSocketFactory getServerSocketFactoryBean()
   {
      return namingMain.getServerSocketFactoryBean();
   }
   public void setServerSocketFactoryBean(RMIServerSocketFactory factory)
   {
      namingMain.setServerSocketFactoryBean(factory);      
   }

   public String getJNPServerSocketFactory()
   {
      return namingMain.getJNPServerSocketFactory();
   }
   public void setJNPServerSocketFactory(String factoryClassName)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException
   {
      namingMain.setJNPServerSocketFactory(factoryClassName);
   }
   public ServerSocketFactory getJNPServerSocketFactoryBean()
   {
      return namingMain.getJNPServerSocketFactoryBean();
   }
   public void setJNPServerSocketFactoryBean(ServerSocketFactory factory)
   {
      namingMain.setJNPServerSocketFactoryBean(factory);
   }

   public void setInvokerProxyFactory(JRMPProxyFactoryMBean proxyFactory)
   {
      this.proxyFactory = proxyFactory;
   }
   
   public String getBootstrapURL()
   {
      return namingMain.getBootstrapURL();
   }

   public Exception getLookupListenerException()
   {
      return namingMain.getLookupListenerException();
   }

   protected void startService()
      throws Exception
   {
      boolean debug = log.isDebugEnabled();

      // Read jndi.properties into system properties
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      InputStream is = loader.getResourceAsStream("jndi.properties");
      if (is == null)
         throw new RuntimeException("Cannot find jndi.properties, it should be at conf/jndi.properties by default.");
      Properties props = new Properties();
      try
      {
         props.load(is);
      }
      finally
      {
         is.close();
      }

      for (Enumeration keys = props.propertyNames(); keys.hasMoreElements(); )
      {
         String key = (String) keys.nextElement();
         String value = props.getProperty(key);
         if (debug)
         {
            log.debug("System.setProperty, key="+key+", value="+value);
         }
         System.setProperty(key, value);
      }
      if( proxyFactory != null )
         namingMain.setNamingProxy(proxyFactory.getProxy());
      namingMain.start();

      // Build the Naming interface method map
      HashMap tmpMap = new HashMap(13);
      Method[] methods = Naming.class.getMethods();
      for(int m = 0; m < methods.length; m ++)
      {
         Method method = methods[m];
         Long hash = new Long(MarshalledInvocation.calculateHash(method));
         tmpMap.put(hash, method);
      }
      marshalledInvocationMapping = Collections.unmodifiableMap(tmpMap);
   }

   protected void stopService()
      throws Exception
   {
      namingMain.stop();
      log.debug("JNP server stopped");
   }

   /**
    * The <code>getNamingServer</code> method makes this class
    * extensible, but it is a hack.  The NamingServer should be
    * exposed directly as an xmbean, and the startup logic put in
    * either an interceptor, the main class itself, or an auxilliary
    * mbean (for the enc).
    *
    * @return a <code>Main</code> value
    */
   protected Main getNamingServer()
   {
      return namingMain;
   } // end of main()


   /** Expose the Naming service interface mapping as a read-only attribute
    *
    * @jmx:managed-attribute
    *
    * @return A Map<Long hash, Method> of the Naming interface
    */
   public Map getMethodMap()
   {
      return marshalledInvocationMapping;
   }
   
   public void createAlias(String fromName, String toName) throws Exception
   {
      Util.createLinkRef(fromName, toName);
      log.info("Created alias " + fromName + "->" + toName);
   }
   
   public void removeAlias(String name) throws Exception
   {
      log.info("Removing alias " + name);
      Util.removeLinkRef(name);
   }

   /** Expose the Naming service via JMX to invokers.
    *
    * @jmx:managed-operation
    *
    * @param invocation    A pointer to the invocation object
    * @return              Return value of method invocation.
    *
    * @throws Exception    Failed to invoke method.
    */
   public Object invoke(Invocation invocation) throws Exception
   {
      Naming theServer = namingMain.getNamingInstance();
      // Set the method hash to Method mapping
      if (invocation instanceof MarshalledInvocation)
      {
         MarshalledInvocation mi = (MarshalledInvocation) invocation;
         mi.setMethodMap(marshalledInvocationMapping);
      }
      // Invoke the Naming method via reflection
      Method method = invocation.getMethod();
      Object[] args = invocation.getArguments();
      Object value = null;
      try
      {
         value = method.invoke(theServer, args);
      }
      catch(InvocationTargetException e)
      {
         Throwable t = e.getTargetException();
         if( t instanceof Exception )
            throw (Exception) t;
         else
            throw new UndeclaredThrowableException(t, method.toString());
      }

      return value;
   }
}

