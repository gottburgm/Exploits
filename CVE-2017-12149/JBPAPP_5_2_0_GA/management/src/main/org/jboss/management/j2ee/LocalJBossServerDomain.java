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
package org.jboss.management.j2ee;

import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.MainDeployerConstants;
import org.jboss.deployment.SubDeployer;
import org.jboss.logging.Logger;
import org.jboss.management.j2ee.factory.DefaultManagedObjectFactoryMap;
import org.jboss.management.j2ee.factory.ManagedObjectFactory;
import org.jboss.management.j2ee.factory.ManagedObjectFactoryMap;
import org.jboss.system.ServiceControllerMBean;
import org.jboss.system.ServiceMBean;
import org.jboss.system.ServiceMBeanSupport;

import javax.management.JMException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * The integration MBean for the local JBoss server management domain. This
 * bridges between the core JBoss JSR-77 agnostic code to the JSR-77
 * managed object interfaces.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @author  <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @version $Revision: 81025 $
 */
public class LocalJBossServerDomain extends ServiceMBeanSupport
   implements NotificationListener, LocalJBossServerDomainMBean
{
   /**
    * Class logger.
    */
   private static final Logger log = Logger.getLogger(LocalJBossServerDomain.class);

   private J2EEDomain serverDomain;

   /**
    * The name of the MainDeployer
    */
   private ObjectName mainDeployer;
   /**
    * The name of the SARDeployer
    */
   private ObjectName sarDeployer;
   /**
    * The name of the EARDeployer
    */
   private ObjectName earDeployer;
   /**
    * The name of the EJBDeployer
    */
   private ObjectName ejbDeployer;
   /**
    * The name of the RARDeployer
    */
   private ObjectName rarDeployer;
   /**
    * The name of the JCA connection manager deployer
    */
   private ObjectName jcaCMDeployer;
   /**
    * The name of the WARDeployer
    */
   private ObjectName warDeployer;
   /**
    * The client application deployer
    */ 
   private ObjectName carDeployer;
   /**
    * The name of the JMS service
    */
   private ObjectName jmsService;
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
   private Class managedObjFactoryMapClass = DefaultManagedObjectFactoryMap.class;

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   public LocalJBossServerDomain()
   {
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------

   /**
    * @return The MainDeployer mbean name
    * @jmx:managed-attribute
    */
   public ObjectName getMainDeployer()
   {
      return mainDeployer;
   }

   /**
    * @param name The MainDeployer mbean name
    * @jmx:managed-attribute
    */
   public void setMainDeployer(ObjectName name)
   {
      this.mainDeployer = name;
   }

   /**
    * @return The SARDeployer mbean name
    * @jmx:managed-attribute
    */
   public ObjectName getSARDeployer()
   {
      return sarDeployer;
   }

   /**
    * @param name The SARDeployer mbean name
    * @jmx:managed-attribute
    */
   public void setSARDeployer(ObjectName name)
   {
      this.sarDeployer = name;
   }

   /**
    * @return The EARDeployer mbean name
    * @jmx:managed-attribute
    */
   public ObjectName getEARDeployer()
   {
      return earDeployer;
   }

   /**
    * @param name The EARDeployer mbean name
    * @jmx:managed-attribute
    */
   public void setEARDeployer(ObjectName name)
   {
      this.earDeployer = name;
   }

   /**
    * @return The EJBDeployer mbean name
    * @jmx:managed-attribute
    */
   public ObjectName getEJBDeployer()
   {
      return ejbDeployer;
   }

   /**
    * @param name The EJBDeployer mbean name
    * @jmx:managed-attribute
    */
   public void setEJBDeployer(ObjectName name)
   {
      this.ejbDeployer = name;
   }

   /**
    * @return The RARDeployer mbean name
    * @jmx:managed-attribute
    */
   public ObjectName getRARDeployer()
   {
      return rarDeployer;
   }

   /**
    * @param name The RARDeployer mbean name
    * @jmx:managed-attribute
    */
   public void setRARDeployer(ObjectName name)
   {
      this.rarDeployer = name;
   }

   /**
    * @return The JCA Connection manager deployer name
    * @jmx:managed-attribute
    */
   public ObjectName getCMDeployer()
   {
      return jcaCMDeployer;
   }

   /**
    * @param name The JCA Connection manager deployer name
    * @jmx:managed-attribute
    */
   public void setCMDeployer(ObjectName name)
   {
      this.jcaCMDeployer = name;
   }

   /**
    * @return The WARDeployer mbean name
    * @jmx:managed-attribute
    */
   public ObjectName getWARDeployer()
   {
      return warDeployer;
   }

   /**
    * @param name The WARDeployer mbean name
    * @jmx:managed-attribute
    */
   public void setWARDeployer(ObjectName name)
   {
      this.warDeployer = name;
   }

   /**
    * @return The CARDeployer mbean name
    * @jmx:managed-attribute
    */
   public ObjectName getCARDeployer()
   {
      return carDeployer;
   }
   /**
    * @param name The CARDeployer mbean name
    * @jmx:managed-attribute
    */
   public void setCARDeployer(ObjectName name)
   {
      this.carDeployer = name;
   }

   /**
    * @return The JMS service mbean name
    * @jmx:managed-attribute
    */
   public ObjectName getJMSService()
   {
      return jmsService;
   }

   /**
    * @param name The JMS service mbean name
    * @jmx:managed-attribute
    */
   public void setJMSService(ObjectName name)
   {
      this.jmsService = name;
   }

   /**
    * @return The JNDI service mbean name
    * @jmx:managed-attribute
    */
   public ObjectName getJNDIService()
   {
      return jndiService;
   }

   /**
    * @param name The JNDI service mbean name
    * @jmx:managed-attribute
    */
   public void setJNDIService(ObjectName name)
   {
      this.jndiService = name;
   }

   /**
    * @return The JTA service mbean name
    * @jmx:managed-attribute
    */
   public ObjectName getJTAService()
   {
      return jtaService;
   }

   /**
    * @param name The JTA service mbean name
    * @jmx:managed-attribute
    */
   public void setJTAService(ObjectName name)
   {
      this.jtaService = name;
   }

   /**
    * @return The JavaMail service mbean name
    * @jmx:managed-attribute
    */
   public ObjectName getMailService()
   {
      return mailService;
   }

   /**
    * @param name The JavaMail service mbean name
    * @jmx:managed-attribute
    */
   public void setMailService(ObjectName name)
   {
      this.mailService = name;
   }

   /**
    * @return The UserTransaction service mbean name
    * @jmx:managed-attribute
    */
   public ObjectName getUserTransactionService()
   {
      return userTxService;
   }

   /**
    * @param name The UserTransaction service mbean name
    * @jmx:managed-attribute
    */
   public void setUserTransactionService(ObjectName name)
   {
      this.userTxService = name;
   }

   /**
    * @return The RMI/IIOP service mbean name
    * @jmx:managed-attribute
    */
   public ObjectName getRMI_IIOPService()
   {
      return rmiiiopService;
   }

   /**
    * @param name The RMI/IIOP service mbean name
    * @jmx:managed-attribute
    */
   public void setRMI_IIOPService(ObjectName name)
   {
      this.rmiiiopService = name;
   }

   /**
    * @return The Jndi binding service mbean name
    * @jmx:managed-attribute
    */
   public ObjectName getJndiBindingService()
   {
      return jndiBindingService;
   }

   /**
    * @param name The Jndi binding service mbean name
    * @jmx:managed-attribute
    */
   public void setJndiBindingService(ObjectName name)
   {
      this.jndiBindingService = name;
   }

   /**
    * @return The ManagementObjFactoryMap class
    * @jmx:managed-attribute
    */
   public Class getManagementObjFactoryMapClass()
   {
      return managedObjFactoryMapClass;
   }

   /**
    * @param cls The ManagementObjFactoryMap class
    * @jmx:managed-attribute
    */
   public void setManagementObjFactoryMapClass(Class cls)
   {
      this.managedObjFactoryMapClass = cls;
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
      MBeanServer mbeanServer = getServer();
      if (managedObjFactoryMap == null || mbeanServer == null)
      {
         return;
      }

      log.debug("handleNotification: " + msg);
      String type = msg.getType();
      Object userData = msg.getUserData();
      try
      {
         /* As this section keeps growing I should change this to a command
         dispatch pattern as well. An issue here is that there is a choice
         made about what 'data' to pass the ManagedObjectFactory based on
         the event type that probably should be hidden in the factory map.
         */
         if (type.equals(ServiceMBean.CREATE_EVENT))
         {
            ManagedObjectFactory factory = managedObjFactoryMap.getFactory(msg);
            if (factory != null)
            {
               factory.create(mbeanServer, userData);
            }
         }
         else if (type.equals(ServiceMBean.DESTROY_EVENT))
         {
            ManagedObjectFactory factory = managedObjFactoryMap.getFactory(msg);
            if (factory != null)
            {
               factory.destroy(mbeanServer, userData);
            }
         }
         else if (type.equals(SubDeployer.START_NOTIFICATION))
         {
            ManagedObjectFactory factory = managedObjFactoryMap.getFactory(msg);
            if (factory != null)
            {
               factory.create(mbeanServer, userData);
            }
         }
         else if (type.equals(SubDeployer.DESTROY_NOTIFICATION))
         {
            ManagedObjectFactory factory = managedObjFactoryMap.getFactory(msg);
            if (factory != null)
            {
               DeploymentInfo di = (DeploymentInfo) msg.getUserData();
               factory.destroy(mbeanServer, di);
            }
         }
         else if (type.equals(MainDeployerConstants.ADD_DEPLOYER))
         {
            ObjectName deployerName = (ObjectName) msg.getUserData();
            registerWithDeployer(deployerName);
         }
         else if (type.equals(MainDeployerConstants.REMOVE_DEPLOYER))
         {
            ObjectName deployerName = (ObjectName) msg.getUserData();
            unregisterWithDeployer(deployerName);
         }
      }
      catch (Throwable t)
      {
         log.debug("Failed to handle event", t);
      }
   }

   public String toString()
   {
      return "LocalJBossServerDomain { " + super.toString() + " } []";
   }

   public void createService() throws Exception
   {
      MBeanServer server = getServer();

      setupJ2EEMBeans(server);

      registerWithController(server);

      populateFactoryMap();
      registerWithCurrentDeployers(server);
   }

   /**
    * Called to destroy the service. This unregisters with all deployers and
    * then removes all MBeans in this services domain to remove all JSR77
    * beans.
    *
    * @throws Exception
    */
   protected void destroyService() throws Exception
   {
      MBeanServer server = getServer();

      unregisterWithCurrentDeployers(server);

      cleanupLeftoverMBeans(server);

      unregisterWithController(server);
   }

   /**
    * Register as a listener of the given deployer.
    *
    * @param deployerName
    */
   protected void registerWithDeployer(ObjectName deployerName)
   {
      log.debug("Registering as listener of deployer: " + deployerName);
      try
      {
         getServer().addNotificationListener(deployerName, this, null, null);
      }
      catch (Exception e)
      {
         log.debug("Failed to register with deployer: " + deployerName, e);
      }
   }

   /**
    * Unregister as a listener of the given deployer.
    *
    * @param deployerName
    */
   protected void unregisterWithDeployer(ObjectName deployerName)
   {
      log.debug("Unregistering as listener of deployer: " + deployerName);
      try
      {
         getServer().removeNotificationListener(deployerName, this);
      }
      catch (Exception e)
      {
         log.debug("Failed to unregister with deployer: " + deployerName, e);
      }
   }

   /**
    * Build the ManagedObjectFactoryMap used to obtain the ManagedObjectFactory
    * instances from notification msgs.
    *
    * @throws Exception
    */
   private void populateFactoryMap() throws Exception
   {
      // Create the ManagedObjectFactoryMap
      managedObjFactoryMap = (ManagedObjectFactoryMap) managedObjFactoryMapClass.newInstance();
      managedObjFactoryMap.setSARDeployer(sarDeployer);
      managedObjFactoryMap.setEARDeployer(earDeployer);
      managedObjFactoryMap.setEJBDeployer(ejbDeployer);
      managedObjFactoryMap.setRARDeployer(rarDeployer);
      managedObjFactoryMap.setCMDeployer(jcaCMDeployer);
      managedObjFactoryMap.setWARDeployer(warDeployer);
      managedObjFactoryMap.setJMSResource(jmsService);
      managedObjFactoryMap.setJNDIResource(jndiService);
      managedObjFactoryMap.setJTAResource(jtaService);
      managedObjFactoryMap.setJTAResource(userTxService);
      managedObjFactoryMap.setJavaMailResource(mailService);
      managedObjFactoryMap.setRMI_IIOPResource(rmiiiopService);
   }

   /**
    * Create the J2EEServer and JVM MBeans.
    *
    * @param mbeanServer the MBeanServer to register the mbeans with.
    */
   private void setupJ2EEMBeans(MBeanServer mbeanServer)
   {
      // Create Server Component
      try
      {
         log.debug("setupJ2EEMBeans(), create J2EEServer instance");
         Package pkg = Package.getPackage("org.jboss");
         String vendor = pkg.getSpecificationVendor();
         String version = pkg.getImplementationVersion();
         // Create the createService
         serverDomain = new J2EEDomain(serviceName.getDomain());
         ObjectName domain = serverDomain.getObjectName();
         mbeanServer.registerMBean(serverDomain, domain);
         // Create single Local J2EEServer MBean
         J2EEServer j2eeServer = new J2EEServer("Local", domain, vendor, version);
         ObjectName lServer = j2eeServer.getObjectName();
         mbeanServer.registerMBean(j2eeServer, lServer);

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
         mbeanServer.registerMBean(jvm, jvmName);
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
    *
    * @param mbeanServer
    */
   private void registerWithController(MBeanServer mbeanServer)
   {
      try
      {
         mbeanServer.addNotificationListener(ServiceControllerMBean.OBJECT_NAME,
                 this, null, null);
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
    *
    * @param mbeanServer
    */
   private void unregisterWithController(MBeanServer mbeanServer)
   {
      try
      {
         mbeanServer.removeNotificationListener(ServiceControllerMBean.OBJECT_NAME, this);
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
    * Register with deployers known to the MainDeployer
    *
    * @param mbeanServer
    * @throws Exception thrown on failure to register as a listener of the
    *                   MainDeployer or to obtain the list of deployers
    */
   private void registerWithCurrentDeployers(MBeanServer mbeanServer)
           throws Exception
   {
      log.debug("Registering with all deployers, mainDeployer=" + mainDeployer);
      mbeanServer.addNotificationListener(mainDeployer, this, null, null);

      // Obtain the deployers list
      log.debug("Getting current deployers");
      Object[] args = {};
      String[] sig = {};
      Collection deployers = (Collection) mbeanServer.invoke(mainDeployer,
              "listDeployers", args, sig);
      Iterator iter = deployers.iterator();
      while (iter.hasNext())
      {
         ObjectName name = (ObjectName) iter.next();
         registerWithDeployer(name);
      }
   }

   /**
    * Unregister with all deployers known to the MainDeployer
    *
    * @param mbeanServer
    * @throws Exception thrown on failure to unregister as a listener of the
    *                   MainDeployer or to obtain the list of deployers
    */
   private void unregisterWithCurrentDeployers(MBeanServer mbeanServer)
           throws Exception
   {
      log.debug("Unregistering with all deployers, mainDeployer=" + mainDeployer);
      mbeanServer.removeNotificationListener(mainDeployer, this);

      // Obtain the deployers list
      log.debug("Getting current deployers");
      Object[] args = {};
      String[] sig = {};
      Collection deployers = (Collection) mbeanServer.invoke(mainDeployer,
              "listDeployers", args, sig);
      Iterator iter = deployers.iterator();
      while (iter.hasNext())
      {
         ObjectName name = (ObjectName) iter.next();
         unregisterWithDeployer(name);
      }
   }

   /**
    * Query for all mbeans in this services domain and unregisters them.
    *
    * @throws Exception if the domain query fails
    */
   private void cleanupLeftoverMBeans(MBeanServer mbeanServer) throws Exception
   {
      String domain = serviceName.getDomain();
      ObjectName domainName = new ObjectName(domain + ":*");
      Set domainNames = mbeanServer.queryNames(domainName, null);
      log.debug("Found " + domainNames.size() + " domain mbeans");
      Iterator domainIter = domainNames.iterator();
      while (domainIter.hasNext())
      {
         try
         {
            ObjectName name = (ObjectName) domainIter.next();
            if (name.equals(serviceName))
               continue;
            server.unregisterMBean(name);
         }
         catch (MBeanException ignore)
         {
         }
      }
   }
}
