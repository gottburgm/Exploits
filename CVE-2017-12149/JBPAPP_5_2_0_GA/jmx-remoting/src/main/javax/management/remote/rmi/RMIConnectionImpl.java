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
package javax.management.remote.rmi;

import java.io.IOException;
import java.rmi.MarshalledObject;
import java.rmi.server.Unreferenced;
import java.security.SecureClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationFilter;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.loading.ClassLoaderRepository;
import javax.management.remote.NotificationResult;
import javax.security.auth.Subject;
import org.jboss.logging.Logger;
import org.jboss.mx.remoting.rmi.ClientListenerHolder;
import org.jboss.mx.remoting.rmi.ClientNotificationProxy;

/**
 * This is the implementation for the rmi connection.  The un-marhsalling of marshalled
 * objects is driven by the JSR-160 spec, section 2.11.2.  Since this section is not very
 * clear in certain situations, here is how it has been implemented:<p><ul>
 * <li> <i>setAttribute(s)</i> uses the target MBean's classloader, then tries the default class loader </li>
 * <li> <i>invoke</i> uses the target MBean's classloader, then tries the default class loader </li>
 * <li> <i>createMBean</i> uses the traget MBeanServer's classloader, then tries the default class loader </li>
 * <li> <i>queryNames/queryMBeans</i> uses only the default class loader </li>
 * <li> <i>NotificationFilter/Object handback</i> uses the notification broadcaster's classloader, then
 * tries the default class loader </li>
 * </ul>
 *
 * @author <a href="mailto:tom.elrod@jboss.com">Tom Elrod</a>
 */
public class RMIConnectionImpl implements RMIConnection, Unreferenced
{
   private RMIServerImpl rmiServer;
   private String connectionId;
   private ClassLoader defaultClassLoader;
   private Subject subject;
   private Map environment;
   private ClientNotificationProxy notificationProxy;

   protected static Logger log = Logger.getLogger(RMIConnectionImpl.class.getName());

   //TODO: -TME -Need to fill out all the implementations which often does not do security checks, etc.
   public RMIConnectionImpl(RMIServerImpl rmiServer, String connectionId, ClassLoader defaultClassLoader,
                            Subject subject, Map env)
   {
      this.rmiServer = rmiServer;
      this.connectionId = connectionId;
      this.defaultClassLoader = defaultClassLoader;
      this.subject = subject;
      if(env != null)
      {
         this.environment = env;
      }
      else
      {
         this.environment = new HashMap();
      }
      notificationProxy = new ClientNotificationProxy();
   }

   public String getConnectionId() throws IOException
   {
      return connectionId;
   }

   public void close() throws IOException
   {
      ClientListenerHolder[] holders = notificationProxy.getListeners();
      if(holders != null)
      {
         for(int x = 0; x < holders.length; x++)
         {
            ClientListenerHolder holder = holders[x];
            try
            {
               rmiServer.getMBeanServer().removeNotificationListener(holder.getObjectName(), notificationProxy, holder.getFilter(), holder.getHandback());
            }
            catch(InstanceNotFoundException e)
            {
               log.debug("Could not remove listener for target mbean " + holder.getObjectName() + " as instance is not found.");
            }
            catch(ListenerNotFoundException e)
            {
               log.debug("Could not remove listener for target mbean " + holder.getObjectName() + " as listener is not found.");
            }
         }
      }
      rmiServer.clientClosed(this);
   }

   public ObjectInstance createMBean(String className, ObjectName name, Subject delegationSubject)
         throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, IOException
   {
      return rmiServer.getMBeanServer().createMBean(className, name);
   }

   public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName, Subject delegationSubject)
         throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException,
                MBeanException, NotCompliantMBeanException, InstanceNotFoundException, IOException
   {
      return rmiServer.getMBeanServer().createMBean(className, name, loaderName);
   }

   /**
    * Loads the marshalled object using first the ClassLoaders passed as a parameter (if it is not null) and
    * if that throws a ClassNotFoundException, will try the default class loader.
    *
    * @param classLoaderReposWrapper
    * @param params
    * @return
    * @throws IOException
    */
   private Object loadMarshalledObject(ClassLoader classLoaderReposWrapper, MarshalledObject params)
         throws IOException
   {
      Object arg;
      // try using the class loader of the mbean server before default class loader
      ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
      // per spec (end of section 2), should try the classloader for the mbean server first
      if(classLoaderReposWrapper != null)
      {
         try
         {
            Thread.currentThread().setContextClassLoader(classLoaderReposWrapper);
            arg = params.get();
         }
         catch(ClassNotFoundException e)
         {
            Thread.currentThread().setContextClassLoader(defaultClassLoader);
            try
            {
               arg = params.get();
            }
            catch(ClassNotFoundException e1)
            {
               throw new IOException(e1.getMessage());
            }
         }
         finally
         {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
         }
      }
      else
      {
         try
         {
            Thread.currentThread().setContextClassLoader(defaultClassLoader);
            arg = params.get();
         }
         catch(ClassNotFoundException cnfe)
         {
            throw new IOException(cnfe.getMessage());
         }
         finally
         {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
         }
      }
      return arg;
   }

   public ObjectInstance createMBean(String className, ObjectName name, MarshalledObject params,
                                     String[] signature, Subject delegationSubject)
         throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException,
                MBeanException, NotCompliantMBeanException, IOException
   {
      // since have a MarshalledObject to deal with, need to use classloader from mbean server
      ClassLoaderRepository classLoaderRepos = rmiServer.getMBeanServer().getClassLoaderRepository();
      ClassLoader classLoaderReposWrapper = new ClassLoaderRepositoryWrapper(classLoaderRepos);

      Object arg = loadMarshalledObject(classLoaderReposWrapper, params);

      Object[] args = new Object[]{arg};
      return rmiServer.getMBeanServer().createMBean(className, name, args, signature);
   }

   public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName,
                                     MarshalledObject params, String[] signature, Subject delegationSubject)
         throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException,
                MBeanException, NotCompliantMBeanException, InstanceNotFoundException, IOException
   {
      ClassLoader mbeanSvrClassLoader = rmiServer.getMBeanServer().getClassLoader(loaderName);
      Object arg = loadMarshalledObject(mbeanSvrClassLoader, params);
      Object[] args = new Object[]{arg};
      return rmiServer.getMBeanServer().createMBean(className, name, loaderName, args, signature);
   }

   public void unregisterMBean(ObjectName name, Subject delegationSubject)
         throws InstanceNotFoundException, MBeanRegistrationException, IOException
   {
      rmiServer.getMBeanServer().unregisterMBean(name);
   }

   public ObjectInstance getObjectInstance(ObjectName name, Subject delegationSubject) throws InstanceNotFoundException, IOException
   {
      return rmiServer.getMBeanServer().getObjectInstance(name);
   }

   public Set queryMBeans(ObjectName name, MarshalledObject query, Subject delegationSubject) throws IOException
   {
      QueryExp filter = (QueryExp) loadMarshalledObject(query);
      return rmiServer.getMBeanServer().queryMBeans(name, filter);
   }

   /**
    * Loads the marshall object using only the default class loader only.
    *
    * @param object
    * @return
    * @throws IOException
    */
   private Object loadMarshalledObject(MarshalledObject object) throws IOException
   {
      return loadMarshalledObject(null, object);
   }

   public Set queryNames(ObjectName name, MarshalledObject query, Subject delegationSubject) throws IOException
   {
      QueryExp filter = (QueryExp) loadMarshalledObject(query);
      return rmiServer.getMBeanServer().queryNames(name, filter);
   }

   public boolean isRegistered(ObjectName name, Subject delegationSubject) throws IOException
   {
      return rmiServer.getMBeanServer().isRegistered(name);
   }

   public Integer getMBeanCount(Subject delegationSubject) throws IOException
   {
      return rmiServer.getMBeanServer().getMBeanCount();
   }

   public Object getAttribute(ObjectName name, String attribute, Subject delegationSubject)
         throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException, IOException
   {
      return rmiServer.getMBeanServer().getAttribute(name, attribute);
   }

   public AttributeList getAttributes(ObjectName name, String[] attributes, Subject delegationSubject)
         throws InstanceNotFoundException, ReflectionException, IOException
   {
      return rmiServer.getMBeanServer().getAttributes(name, attributes);
   }

   public void setAttribute(ObjectName name, MarshalledObject attribute, Subject delegationSubject)
         throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException,
                MBeanException, ReflectionException, IOException
   {
      ClassLoader mbeanLoader = rmiServer.getMBeanServer().getClassLoaderFor(name);
      Attribute attrib = (Attribute) loadMarshalledObject(mbeanLoader, attribute);
      rmiServer.getMBeanServer().setAttribute(name, attrib);
   }

   public AttributeList setAttributes(ObjectName name, MarshalledObject attributes, Subject delegationSubject)
         throws InstanceNotFoundException, ReflectionException, IOException
   {
      ClassLoader mbeanLoader = rmiServer.getMBeanServer().getClassLoaderFor(name);
      AttributeList attrib = (AttributeList) loadMarshalledObject(mbeanLoader, attributes);
      return rmiServer.getMBeanServer().setAttributes(name, attrib);
   }

   public Object invoke(ObjectName name, String operationName, MarshalledObject params, String[] signature, Subject delegationSubject) throws InstanceNotFoundException, MBeanException, ReflectionException, IOException
   {
      ClassLoader mbeanLoader = rmiServer.getMBeanServer().getClassLoaderFor(name);
      Object[] args = (Object[]) loadMarshalledObject(mbeanLoader, params);
      return rmiServer.getMBeanServer().invoke(name, operationName, args, signature);
   }

   public String getDefaultDomain(Subject delegationSubject) throws IOException
   {
      return rmiServer.getMBeanServer().getDefaultDomain();
   }

   public String[] getDomains(Subject delegationSubject) throws IOException
   {
      return rmiServer.getMBeanServer().getDomains();
   }

   public MBeanInfo getMBeanInfo(ObjectName name, Subject delegationSubject) throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException
   {
      return rmiServer.getMBeanServer().getMBeanInfo(name);
   }

   public boolean isInstanceOf(ObjectName name, String className, Subject delegationSubject) throws InstanceNotFoundException, IOException
   {
      return rmiServer.getMBeanServer().isInstanceOf(name, className);
   }

   public void addNotificationListener(ObjectName name, ObjectName listener, MarshalledObject filter, MarshalledObject handback, Subject delegationSubject) throws InstanceNotFoundException, IOException
   {
      ClassLoader mbeanLoader = rmiServer.getMBeanServer().getClassLoaderFor(name);
      NotificationFilter f = (NotificationFilter) loadMarshalledObject(mbeanLoader, filter);
      Object o = loadMarshalledObject(mbeanLoader, handback);
      rmiServer.getMBeanServer().addNotificationListener(name, listener, f, o);
   }

   public void removeNotificationListener(ObjectName name, ObjectName listener, Subject delegationSubject) throws InstanceNotFoundException, ListenerNotFoundException, IOException
   {
      rmiServer.getMBeanServer().removeNotificationListener(name, listener);
   }

   public void removeNotificationListener(ObjectName name, ObjectName listener, MarshalledObject filter, MarshalledObject handback, Subject delegationSubject) throws InstanceNotFoundException, ListenerNotFoundException, IOException
   {
      ClassLoader mbeanLoader = rmiServer.getMBeanServer().getClassLoaderFor(name);
      NotificationFilter f = (NotificationFilter) loadMarshalledObject(mbeanLoader, filter);
      Object o = loadMarshalledObject(mbeanLoader, handback);
      rmiServer.getMBeanServer().removeNotificationListener(name, listener, f, o);
   }

   public Integer[] addNotificationListeners(ObjectName[] names, MarshalledObject[] filters, Subject[] delegationSubjects) throws InstanceNotFoundException, IOException
   {
      if(names == null || names.length == 0)
      {
         throw new IllegalArgumentException("Can not add notification listener without providing target mbean object name.");
      }
      Integer[] ids = new Integer[names.length];
      for(int x = 0; x < names.length; x++)
      {
         ObjectName name = names[x];
         MarshalledObject marshalledFilter = filters[x];
         NotificationFilter filter = null;
         if(marshalledFilter != null)
         {
            ClassLoader mbeanLoader = rmiServer.getMBeanServer().getClassLoaderFor(name);
            filter = (NotificationFilter) loadMarshalledObject(mbeanLoader, marshalledFilter);
         }
         Integer id = notificationProxy.createListenerId(name, filter);
         try
         {
            rmiServer.getMBeanServer().addNotificationListener(name, notificationProxy, filter, id);
         }
         catch(InstanceNotFoundException e)
         {
            notificationProxy.removeListener(id);
            throw e;
         }
         ids[x] = id;
      }
      return ids;
   }

   public void removeNotificationListeners(ObjectName name, Integer[] listenerIDs, Subject delegationSubject) throws InstanceNotFoundException, ListenerNotFoundException, IOException
   {

      for(int x = 0; x < listenerIDs.length; ++x)
      {
         Integer id = listenerIDs[x];
         NotificationFilter filter = notificationProxy.removeListener(id);
         rmiServer.getMBeanServer().removeNotificationListener(name, notificationProxy, filter, id);
      }
   }

   public NotificationResult fetchNotifications(long clientSequenceNumber, int maxNotifications, long timeout) throws IOException
   {
      return notificationProxy.fetchNotifications(clientSequenceNumber, maxNotifications, timeout);
   }

   /**
    * Called by the RMI runtime sometime after the runtime determines that
    * the reference list, the list of clients referencing the remote object,
    * becomes empty.
    *
    * @since JDK1.1
    */
   public void unreferenced()
   {
      log.debug("RMIConnectionImpl::unreferenced called (meaning no client is referencing this connections any longer.  " +
                "Will close connection.");
      try
      {
         close();
      }
      catch(IOException e)
      {
         log.error("Error closing connection due to unreferenced.", e);
      }

   }

   private static class ClassLoaderRepositoryWrapper extends SecureClassLoader
   {
      private final ClassLoaderRepository mbeanSvrRepository;

      private ClassLoaderRepositoryWrapper(ClassLoaderRepository repos)
      {
         this.mbeanSvrRepository = repos;
      }

      public Class loadClass(String name) throws ClassNotFoundException
      {
         return mbeanSvrRepository.loadClass(name);
      }
   }
}