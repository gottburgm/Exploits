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
package org.jboss.system.server.jmx;

import java.io.ObjectInputStream;
import java.lang.reflect.Constructor;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanException;
import javax.management.NotCompliantMBeanException;
import javax.management.InstanceNotFoundException;
import javax.management.QueryExp;
import javax.management.AttributeNotFoundException;
import javax.management.AttributeList;
import javax.management.Attribute;
import javax.management.InvalidAttributeValueException;
import javax.management.NotificationListener;
import javax.management.NotificationFilter;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanInfo;
import javax.management.IntrospectionException;
import javax.management.OperationsException;
import javax.management.MBeanServerDelegate;
import javax.management.loading.ClassLoaderRepository;

/**
 * An MBeanServer implementation that bridges between the platform MBeanServer
 * and the jboss implementation. This is used by the @{link MBeanServerBuilderImpl}
 * to bridge the period from the vm startup until the jboss server is loaded
 * and the jboss MBeanServer is available.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class LazyMBeanServer
   implements MBeanServer
{
   /** The MBeanServer that was registered */
   private static MBeanServer registeredServer;
   /** The jboss MBeanServer implementation */
   private static MBeanServer theServer;
   /** The jdk MBeanServer implementation */
   private static MBeanServer platformServer;
   /** The default domain to use for mbeans */
   private static String defaultDomain;
   private static MBeanServerDelegate theDelegate;
   /** A HashSet<String> of the jmx domain prefixes that need to be
    * handled by the platform server
    */
   private static Set platformServerDomains = Collections.synchronizedSet(new HashSet());
   /** A HashSet<String> of the jmx domain prefixes that need to be
    * handled by the jboss server
    */
   private static Set serverDomains = Collections.synchronizedSet(new HashSet());

   /**
    * Called by the ServerImpl to reset theServer implementation to the jboss
    * version so that our xmbean and the like function correctly.
    * 
    * @param server - the existing MBeanServer
    * @return org.jboss.mx.server.MBeanServerImp
    * @throws Exception
    */ 
   public static MBeanServer resetToJBossServer(MBeanServer server)
      throws Exception
   {
      MBeanServer coreServer = server;
      if( theDelegate != null )
      {
         Class[] sig = {String.class, MBeanServer.class, MBeanServerDelegate.class};
         Object[] args = {defaultDomain, registeredServer, theDelegate};
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         Class c = loader.loadClass("org.jboss.mx.server.MBeanServerImpl");
         Constructor ctor = c.getConstructor(sig);
         theServer = (MBeanServer) ctor.newInstance(args);
         coreServer = theServer;
         String[] domains = theServer.getDomains();
         for(int n = 0; n < domains.length; n ++)
         {
            serverDomains.add(domains[n]);
         }
      }
      return coreServer;
   }
   /**
    * Provide access to the original MBeanServer that was registered so that
    * it can be removed on shutdown for example.
    * 
    * @param server - the ServerImple MBeanServer
    * @return either the argument, or the last LazyMBeanServer created
    */ 
   public static MBeanServer getRegisteredMBeanServer(MBeanServer server)
   {
      MBeanServer outerServer = server;
      if( registeredServer != null )
         outerServer = registeredServer;
      return outerServer;
   }

   /**
    * Updates the platformServerDomains and serverDomains sets of jmx
    * domain names.
    */
   public static synchronized void reloadDomains()
   {
      // Initialize the platform server domains
      String[] domains = platformServer.getDomains();
      for(int n = 0; n < domains.length; n ++)
      {
         platformServerDomains.add(domains[n]);
      }
      domains = theServer.getDomains();
      for(int n = 0; n < domains.length; n ++)
      {
         serverDomains.add(domains[n]);
      }
   }
   LazyMBeanServer(String domain, MBeanServer outer,
      MBeanServerDelegate delegate)
   {
      defaultDomain = domain;
      platformServer = outer;
      theServer = outer;
      registeredServer = this;
      theDelegate = delegate;
      // Initialize the platform server domains
      String[] domains = platformServer.getDomains();
      for(int n = 0; n < domains.length; n ++)
      {
         platformServerDomains.add(domains[n]);
      }
   }

   public ObjectInstance createMBean(String className, ObjectName name)
      throws ReflectionException, InstanceAlreadyExistsException,
      MBeanRegistrationException, MBeanException,
      NotCompliantMBeanException
   {
      try
      {
         return createMBean(className, name, null);
      }
      catch (InstanceNotFoundException e)
      {
         throw new MBeanException(e);
      }
   }

   public ObjectInstance createMBean(String className, ObjectName name,
                 ObjectName loaderName) 
      throws ReflectionException, InstanceAlreadyExistsException,
      MBeanRegistrationException, MBeanException,
      NotCompliantMBeanException, InstanceNotFoundException
   {
      return getServer(name).createMBean(className, name, loaderName);
   }

   public ObjectInstance createMBean(String className, ObjectName name,
                 Object params[], String signature[]) 
      throws ReflectionException, InstanceAlreadyExistsException,
      MBeanRegistrationException, MBeanException,
      NotCompliantMBeanException
   {
      return getServer(name).createMBean(className, name, params, signature);
   }

   public ObjectInstance createMBean(String className, ObjectName name,
                 ObjectName loaderName, Object params[],
                 String signature[]) 
      throws ReflectionException, InstanceAlreadyExistsException,
      MBeanRegistrationException, MBeanException,
      NotCompliantMBeanException, InstanceNotFoundException
   {
      return getServer(name).createMBean(className, name, loaderName, params, signature);
   }

   public ObjectInstance registerMBean(Object object, ObjectName name)
      throws InstanceAlreadyExistsException, MBeanRegistrationException,
      NotCompliantMBeanException
   {
      return theServer.registerMBean(object, name);
   }

   public void unregisterMBean(ObjectName name)
      throws InstanceNotFoundException, MBeanRegistrationException
   {
      theServer.unregisterMBean(name);
   }

   public ObjectInstance getObjectInstance(ObjectName name)
      throws InstanceNotFoundException
   {
      ObjectInstance oi = getServer(name).getObjectInstance(name);
      return oi;
   }

   public Set queryMBeans(ObjectName name, QueryExp query)
   {
      // query both servers
      Set beans = platformServer.queryMBeans(name, query);
      Set beans2 = theServer.queryMBeans(name, query);
      beans.addAll(beans2);
      return beans;
   }

   public Set queryNames(ObjectName name, QueryExp query)
   {
      Set names = platformServer.queryNames(name, query);
      Set names2 = theServer.queryNames(name, query);
      names.addAll(names2);
      return names;
   }

   public boolean isRegistered(ObjectName name)
   {
      return getServer(name).isRegistered(name);
   }

   public Integer getMBeanCount()
   {
      return theServer.getMBeanCount();
   }

   public Object getAttribute(ObjectName name, String attribute)
      throws MBeanException, AttributeNotFoundException,
      InstanceNotFoundException, ReflectionException
   {
      return getServer(name).getAttribute(name, attribute);
   }

   public AttributeList getAttributes(ObjectName name, String[] attributes)
      throws InstanceNotFoundException, ReflectionException
   {
      return getServer(name).getAttributes(name, attributes);
   }

   public void setAttribute(ObjectName name, Attribute attribute)
      throws InstanceNotFoundException, AttributeNotFoundException,
      InvalidAttributeValueException, MBeanException, 
      ReflectionException
   {
      getServer(name).setAttribute(name, attribute);
   }

   public AttributeList setAttributes(ObjectName name,
                  AttributeList attributes)
  throws InstanceNotFoundException, ReflectionException
   {
      return getServer(name).setAttributes(name, attributes);
   }

   public Object invoke(ObjectName name, String operationName,
         Object params[], String signature[])
      throws InstanceNotFoundException, MBeanException,
      ReflectionException
   {
      Object value = getServer(name).invoke(name, operationName, params, signature);
      return value;
   }

   public String getDefaultDomain()
   {
      return theServer.getDefaultDomain();
   }

   public String[] getDomains()
   {
      return theServer.getDomains();
   }

   public void addNotificationListener(ObjectName name,
              NotificationListener listener,
              NotificationFilter filter,
              Object handback)
      throws InstanceNotFoundException
   {
      getServer(name).addNotificationListener(name, listener, filter, handback);
   }

   public void addNotificationListener(ObjectName name,
              ObjectName listener,
              NotificationFilter filter,
              Object handback)
      throws InstanceNotFoundException
   {
      getServer(name).addNotificationListener(name, listener, filter, handback);
   }

   public void removeNotificationListener(ObjectName name,
                 ObjectName listener) 
  throws InstanceNotFoundException, ListenerNotFoundException
   {
      getServer(name).removeNotificationListener(name, listener);
   }

   public void removeNotificationListener(ObjectName name,
                 ObjectName listener,
                 NotificationFilter filter,
                 Object handback)
      throws InstanceNotFoundException, ListenerNotFoundException
   {
      getServer(name).removeNotificationListener(name, listener, filter, handback);
   }

   public void removeNotificationListener(ObjectName name,
                 NotificationListener listener)
      throws InstanceNotFoundException, ListenerNotFoundException
   {
      getServer(name).removeNotificationListener(name, listener);
   }

   public void removeNotificationListener(ObjectName name,
                 NotificationListener listener,
                 NotificationFilter filter,
                 Object handback)
      throws InstanceNotFoundException, ListenerNotFoundException
   {
      getServer(name).removeNotificationListener(name, listener, filter, handback);
   }

   /**
    * Obtains the MBeanInfo for the given ObjectName. In the event of an
    * InstanceNotFoundException, the jmx domain to server sets are reloaded
    * by calling reloadDomains(), and the lookup retried.
    * @param name
    * @return
    * @throws InstanceNotFoundException
    * @throws IntrospectionException
    * @throws ReflectionException
    */
   public MBeanInfo getMBeanInfo(ObjectName name)
      throws InstanceNotFoundException, IntrospectionException,
      ReflectionException
   {
      MBeanInfo info = null;
      try
      {
         info = getServer(name).getMBeanInfo(name);
      }
      catch(InstanceNotFoundException e)
      {
         reloadDomains();
         info = getServer(name).getMBeanInfo(name);
      }
      return info;
   }

   public boolean isInstanceOf(ObjectName name, String className)
      throws InstanceNotFoundException
   {
      boolean isInstanceOf = false;
      try
      {
         isInstanceOf = getServer(name).isInstanceOf(name, className);
      }
      catch(InstanceNotFoundException e)
      {
         reloadDomains();
         isInstanceOf = getServer(name).isInstanceOf(name, className);
      }
      return isInstanceOf;
   }

   public Object instantiate(String className)
      throws ReflectionException, MBeanException
   {
      return theServer.instantiate(className);
   }

   public Object instantiate(String className, ObjectName loaderName) 
      throws ReflectionException, MBeanException,
      InstanceNotFoundException
   {
      return theServer.instantiate(className, loaderName);
   }

   public Object instantiate(String className, Object params[],
              String signature[]) 
      throws ReflectionException, MBeanException
   {
      return theServer.instantiate(className, params, signature);
   }

   public Object instantiate(String className, ObjectName loaderName,
              Object params[], String signature[]) 
      throws ReflectionException, MBeanException,
      InstanceNotFoundException
   {
      return theServer.instantiate(className, loaderName, params, signature);
   }

   public ObjectInputStream deserialize(ObjectName name, byte[] data)
      throws InstanceNotFoundException, OperationsException
   {
      return getServer(name).deserialize(name, data);
   }

   public ObjectInputStream deserialize(String className, byte[] data)
      throws OperationsException, ReflectionException
   {
      return theServer.deserialize(className, data);
   }

   public ObjectInputStream deserialize(String className,
               ObjectName loaderName,
               byte[] data)
      throws InstanceNotFoundException, OperationsException,
      ReflectionException
   {
      return theServer.deserialize(className, loaderName, data);
   }

   public ClassLoader getClassLoaderFor(ObjectName mbeanName) 
      throws InstanceNotFoundException
   {
      return getServer(mbeanName).getClassLoaderFor(mbeanName);
   }

   public ClassLoader getClassLoader(ObjectName loaderName)
  throws InstanceNotFoundException
   {
      return getServer(loaderName).getClassLoader(loaderName);
   }

   public ClassLoaderRepository getClassLoaderRepository()
   {
      return theServer.getClassLoaderRepository();
   }

   /**
    * Choose the MBeanServer based on the ObjectName domain. If the serverDomain
    * contains the domain of the name, theServer is returned. If the 
    * platformServerDomains set contains the domain of the name, the
    * platformServer is returned. For an unknown domain theServer is returned.
    * 
    * @param name
    * @return Either the platform MBeanServer, or the jboss MBeanServer
    */ 
   private MBeanServer getServer(ObjectName name)
   {
      String domain = name != null ? name.getDomain() : "";
      if( serverDomains.contains(domain) )
         return theServer;
      if( platformServerDomains.contains(domain) )
         return platformServer;
      return theServer;
   }
}
