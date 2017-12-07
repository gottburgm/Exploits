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
package org.jboss.management.j2ee.deployers;

import java.net.InetAddress;
import java.util.Set;
import javax.management.JMException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.MBeanRegistration;

import org.jboss.logging.Logger;
import org.jboss.management.j2ee.J2EEDomain;
import org.jboss.management.j2ee.J2EEServer;
import org.jboss.management.j2ee.JVM;
import org.jboss.management.j2ee.factory.DefaultManagedObjectFactoryMap;
import org.jboss.management.j2ee.factory.ManagedObjectFactory;
import org.jboss.management.j2ee.factory.ManagedObjectFactoryMap;
import org.jboss.system.ServiceControllerMBean;
import org.jboss.system.ServiceMBean;

/**
 * Port of the old LocalJBossServerDomain to POJO.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @author  <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author  <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class LocalJBossServerDomain implements NotificationListener, LocalJBossServerDomainMBean, MBeanRegistration
{
   /**
    * Class logger.
    */
   private static final Logger log = Logger.getLogger(LocalJBossServerDomain.class);

   /** The mbean server */
   private MBeanServer server;

   /** The service name */
   private ObjectName serviceName;

   /**
    * The name of the JNDI service
    */
   private ObjectName jndiService;
   /**
    * The name of the JTA service
    */
   private ObjectName jtaService;
   /**
    * The name of the UserTransaction service
    */
   private ObjectName userTxService;
   /**
    * The name of the JavaMail service
    */
   private ObjectName mailService;
   /**
    * The name of the RMI_IIOP service
    */
   private ObjectName rmiiiopService;
   /**
    * The name of the service which emites URL binding events
    */
   private ObjectName jndiBindingService;

   /**
    * A mapping of JMX notifications to ManagedObjectFactory instances
    */
   private ManagedObjectFactoryMap managedObjFactoryMap;

   /**
    * The managed factory map class
    */
   private Class<?> managedObjFactoryMapClass = DefaultManagedObjectFactoryMap.class;

   /**
    * @return The JNDI service mbean name
    */
   public ObjectName getJNDIService()
   {
      return jndiService;
   }

   /**
    * @param name The JNDI service mbean name
    */
   public void setJNDIService(ObjectName name)
   {
      this.jndiService = name;
   }

   /**
    * @return The JTA service mbean name
    */
   public ObjectName getJTAService()
   {
      return jtaService;
   }

   /**
    * @param name The JTA service mbean name
    */
   public void setJTAService(ObjectName name)
   {
      this.jtaService = name;
   }

   /**
    * @return The JavaMail service mbean name
    */
   public ObjectName getMailService()
   {
      return mailService;
   }

   /**
    * @param name The JavaMail service mbean name
    */
   public void setMailService(ObjectName name)
   {
      this.mailService = name;
   }

   /**
    * @return The UserTransaction service mbean name
    */
   public ObjectName getUserTransactionService()
   {
      return userTxService;
   }

   /**
    * @param name The UserTransaction service mbean name
    */
   public void setUserTransactionService(ObjectName name)
   {
      this.userTxService = name;
   }

   /**
    * @return The RMI/IIOP service mbean name
    */
   public ObjectName getRMI_IIOPService()
   {
      return rmiiiopService;
   }

   /**
    * @param name The RMI/IIOP service mbean name
    */
   public void setRMI_IIOPService(ObjectName name)
   {
      this.rmiiiopService = name;
   }

   /**
    * @return The Jndi binding service mbean name
    */
   public ObjectName getJndiBindingService()
   {
      return jndiBindingService;
   }

   /**
    * @param name The Jndi binding service mbean name
    */
   public void setJndiBindingService(ObjectName name)
   {
      this.jndiBindingService = name;
   }

   /**
    * @return The ManagementObjFactoryMap class
    */
   public Class<?> getManagementObjFactoryMapClass()
   {
      return managedObjFactoryMapClass;
   }

   /**
    * @param cls The ManagementObjFactoryMap class
    */
   public void setManagementObjFactoryMapClass(Class<?> cls)
   {
      this.managedObjFactoryMapClass = cls;
   }

   public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception
   {
      this.server = server;
      this.serviceName = name;
      return name;
   }

   public void postRegister(Boolean registrationDone)
   {
      if (registrationDone)
      {
         try
         {
            createService();
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }
   }

   public void preDeregister() throws Exception
   {
      destroyService();
   }

   public void postDeregister()
   {
   }

   /**
    * The JMX nofication callback. Here we create/destroy JSR77 MBeans based
    * on the create/destory notifications.
    *
    * @param msg      the notification msg
    * @param handback currently unused
    */
   public void handleNotification(Notification msg, Object handback)
   {
      if (managedObjFactoryMap == null || server == null)
      {
         return;
      }

      log.debug("handleNotification: " + msg);
      String type = msg.getType();
      Object userData = msg.getUserData();
      try
      {
         if (type.equals(ServiceMBean.CREATE_EVENT))
         {
            ManagedObjectFactory factory = managedObjFactoryMap.getFactory(msg);
            if (factory != null)
            {
               factory.create(server, userData);
            }
         }
         else if (type.equals(ServiceMBean.DESTROY_EVENT))
         {
            ManagedObjectFactory factory = managedObjFactoryMap.getFactory(msg);
            if (factory != null)
            {
               factory.destroy(server, userData);
            }
         }
      }
      catch (Throwable t)
      {
         log.debug("Failed to handle event", t);
      }
   }

   public String toString()
   {
      return "LocalJBossServerDomain { " + super.toString() + " }";
   }

   protected void createService() throws Exception
   {
      setupJ2EEMBeans();
      registerWithController();
      populateFactoryMap();
   }

   /**
    * Called to destroy the service. This unregisters with all deployers and
    * then removes all MBeans in this services domain to remove all JSR77
    * beans.
    *
    * @throws Exception for any error
    */
   protected void destroyService() throws Exception
   {
      cleanupLeftoverMBeans();
      unregisterWithController();
   }

   /**
    * Build the ManagedObjectFactoryMap used to obtain the ManagedObjectFactory
    * instances from notification msgs.
    *
    * @throws Exception for any error
    */
   private void populateFactoryMap() throws Exception
   {
      // Create the ManagedObjectFactoryMap
      managedObjFactoryMap = (ManagedObjectFactoryMap) managedObjFactoryMapClass.newInstance();
      managedObjFactoryMap.setJNDIResource(jndiService);
      managedObjFactoryMap.setJTAResource(jtaService);
      managedObjFactoryMap.setJTAResource(userTxService);
      managedObjFactoryMap.setJavaMailResource(mailService);
      managedObjFactoryMap.setRMI_IIOPResource(rmiiiopService);
   }

   /**
    * Create the J2EEServer and JVM MBeans.
    */
   private void setupJ2EEMBeans()
   {
      // Create Server Component
      try
      {
         log.debug("setupJ2EEMBeans(), create J2EEServer instance");
         Package pkg = Package.getPackage("org.jboss");
         String vendor = pkg.getSpecificationVendor();
         String version = pkg.getImplementationVersion();
         // Create the createService
         J2EEDomain serverDomain = new J2EEDomain(serviceName.getDomain());
         ObjectName domain = serverDomain.getObjectName();
         server.registerMBean(serverDomain, domain);
         // Create single Local J2EEServer MBean
         J2EEServer j2eeServer = new J2EEServer("Local", domain, vendor, version);
         ObjectName lServer = j2eeServer.getObjectName();
         server.registerMBean(j2eeServer, lServer);

         // Create the JVM MBean
         String hostName = "localhost";
         try
         {
            InetAddress lLocalHost = InetAddress.getLocalHost();
            hostName = lLocalHost.getHostName();
         }
         catch (Exception e)
         {
            // Ignore when host address is not accessible (localhost is used instead)
         }
         String vmVendor = System.getProperty("java.vendor");
         String vmVersion = System.getProperty("java.version");
         String name = vmVendor + " " + vmVersion;
         JVM jvm = new JVM(name, lServer, vmVersion, vmVendor, hostName);
         ObjectName jvmName = jvm.getObjectName();
         server.registerMBean(jvm, jvmName);
      }
      catch (JMException jme)
      {
         log.debug("setupJ2EEMBeans - unexpected JMException", jme);
      }
      catch (Exception e)
      {
         log.debug("setupJ2EEMBeans - unexpected exception", e);
      }
   }

   /**
    * Register as a listener of the ServiceControllerMBean
    */
   private void registerWithController()
   {
      try
      {
         server.addNotificationListener(ServiceControllerMBean.OBJECT_NAME, this, null, null);
         log.debug("Registered as listener of: " + ServiceControllerMBean.OBJECT_NAME);
      }
      catch (JMException jme)
      {
         log.debug("unexpected exception", jme);
      }
      catch (Exception e)
      {
         log.debug("unexpected exception", e);
      }
   }

   /**
    * Unregister as a listener of the ServiceControllerMBean.
    */
   private void unregisterWithController()
   {
      try
      {
         server.removeNotificationListener(ServiceControllerMBean.OBJECT_NAME, this);
         log.debug("UNRegistered as listener of: " + ServiceControllerMBean.OBJECT_NAME);
      }
      catch (JMException jme)
      {
         log.debug("unexpected exception", jme);
      }
      catch (Exception e)
      {
         log.debug("unexpected exception", e);
      }
   }

   /**
    * Query for all mbeans in this services domain and unregisters them.
    *
    * @throws Exception if the domain query fails
    */
   private void cleanupLeftoverMBeans() throws Exception
   {
      String domain = serviceName.getDomain();
      ObjectName domainName = new ObjectName(domain + ":*");
      Set domainNames = server.queryNames(domainName, null);
      log.debug("Found " + domainNames.size() + " domain mbeans");
      for (Object name : domainNames)
      {
         try
         {
            ObjectName oname = (ObjectName)name;
            if (oname.equals(serviceName) == false)
               server.unregisterMBean(oname);
         }
         catch (MBeanException ignore)
         {
         }
      }
   }
}
