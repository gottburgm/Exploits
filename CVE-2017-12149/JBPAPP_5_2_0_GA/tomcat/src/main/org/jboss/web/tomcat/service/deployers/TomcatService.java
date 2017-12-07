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

package org.jboss.web.tomcat.service.deployers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;

import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.jacc.PolicyContext;
import javax.xml.namespace.QName;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Catalina;
import org.apache.catalina.startup.CatalinaProperties;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.tomcat.util.modeler.Registry;
import org.jboss.bootstrap.spi.Server;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.security.plugins.JaasSecurityManagerServiceMBean;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerImplMBean;
import org.jboss.util.StringPropertyReplacer;
import org.jboss.util.xml.JBossEntityResolver;
import org.jboss.web.tomcat.metadata.AnyXmlMetaData;
import org.jboss.web.tomcat.metadata.ConnectorMetaData;
import org.jboss.web.tomcat.metadata.EngineMetaData;
import org.jboss.web.tomcat.metadata.HostMetaData;
import org.jboss.web.tomcat.metadata.ListenerMetaData;
import org.jboss.web.tomcat.metadata.ServerMetaData;
import org.jboss.web.tomcat.metadata.ServiceMetaData;
import org.jboss.web.tomcat.metadata.ValveMetaData;
import org.jboss.web.tomcat.security.HttpServletRequestPolicyContextHandler;
import org.jboss.web.tomcat.service.request.ActiveRequestResponseCacheValve;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBinding;
import org.jboss.xb.builder.JBossXBBuilder;

/**
 * Temporary workaround to support controlling the lifecycle of the webserver runtime portion of TomcatDeployer via a
 * JMX service in the deploy directory. We want it in deploy so dependencies on services in deploy can be properly
 * expressed. We want it as a JMX service so the ServiceBindingManager can alter the connector ports.
 * <p>
 * A more long term solution involves:
 * <ol>
 * <li>separating out the JBossWeb runtime aspects from TomcatDeployer and putting them in a separate class</li>
 * <li>developing a ProfileService-based alternative to ServiceBindingManager</li>
 * </ol>
 * </p>
 * 
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 111877 $
 */
public class TomcatService extends ServiceMBeanSupport implements NotificationListener, TomcatServiceMBean
{
   
   /** The associated Tomcat deployer * */
   private TomcatDeployer tomcatDeployer;
   
   /** The executor the Tomcat service should use * */
   private Executor executor;
   
   // Use a flag because isInShutdown doesn't appear to be correct
   private boolean connectorsRunning = false;

   // Dependency inject the Executor pojo

   public Executor getExecutor()
   {
      return executor;
   }

   public void setExecutor(Executor executor)
   {
      this.executor = executor;
   }

   // Dependency inject the TomcatDeployer pojo

   public TomcatDeployer getTomcatDeployer()
   {
      return tomcatDeployer;
   }

   public void setTomcatDeployer(TomcatDeployer tomcatDeployer)
   {
      this.tomcatDeployer = tomcatDeployer;
   }

   // In our lifecycle, we invoke the webserver lifecycle-related operations
   // in the TomcatDeployer

   @Override
   protected void startService() throws Exception
   {
      if (tomcatDeployer == null)
         throw new IllegalStateException("Must set TomcatDeployer before starting");

      tomcatDeployer.setServiceClassLoader(getClass().getClassLoader());

      // Load Catalina properties
      CatalinaProperties.getProperty("");

      log.debug("Starting tomcat deployer");
      MBeanServer server = super.getServer();
      SecurityActions.setSystemProperty("catalina.ext.dirs", 
            (SecurityActions.getSystemProperty("jboss.server.home.dir", null) + File.separator + "lib"));

      String objectNameS = tomcatDeployer.getDomain() + ":type=Catalina";
      ObjectName objectName = new ObjectName(objectNameS);

      // Parse main server.xml
      // FIXME: this could be done somewhere else
      SchemaBinding schema = JBossXBBuilder.build(ServerMetaData.class);
      Unmarshaller u = UnmarshallerFactory.newInstance().newUnmarshaller();
      u.setSchemaValidation(false);
      u.setValidation(false);
      u.setEntityResolver(new JBossEntityResolver());
      InputStream is = null;
      ServerMetaData serverMetaData = null;
      try {
         File configFile = new File(tomcatDeployer.getConfigFile());
         if (configFile.exists())
         {
            is = new FileInputStream(configFile);
         }
         else
         {
            is = getClass().getClassLoader().getResourceAsStream(tomcatDeployer.getConfigFile());
         }
         if (is == null) {
            log.error("Could not read configured server.xml (will try default): " + tomcatDeployer.getConfigFile());
            is = getClass().getClassLoader().getResourceAsStream("server.xml");
         }
         serverMetaData = ServerMetaData.class.cast(u.unmarshal(is, schema));
      } finally {
         if (is != null) {
            try {
               is.close();
            } catch (IOException e) {
               // Ignore
            }
         }
      }
      
      // FIXME: could try to do stuff with EngineConfig and HostConfig, although neither
      //        should be useful in JBoss
      
      // Create the Catalina instance
      Catalina catalina = new Catalina();
      catalina.setCatalinaHome(System.getProperty("jboss.server.home.dir"));
      catalina.setUseNaming(false);
      catalina.setUseShutdownHook(false);
      catalina.setAwait(false);
      catalina.setRedirectStreams(false);
      
      // Set the modeler Registry MBeanServer to the that of the tomcat service
      Registry.getRegistry(null, null).setMBeanServer(server);
      // Register the Catalina instance
      Registry.getRegistry(null, null).registerComponent(catalina, objectName, "org.apache.catalina.startup.Catalina");
      
      // Use the server.xml metadata to create a Server instance and assign it to the Catalina instance
      
      // Server
      org.apache.catalina.Server catalinaServer = 
         (org.apache.catalina.Server) getInstance(serverMetaData, "org.apache.catalina.core.StandardServer");
      Registry.getRegistry(null, null).registerComponent(catalinaServer, 
            new ObjectName(tomcatDeployer.getDomain() + ":type=Server"), "org.apache.catalina.startup.StandardServer");
      addLifecycleListeners(catalinaServer, serverMetaData.getListeners());
      
      // Server/Service
      if (serverMetaData.getServices() == null)
      {
         throw new IllegalArgumentException("No services");
      }
      Iterator<ServiceMetaData> serviceMetaDatas = serverMetaData.getServices().iterator();
      while (serviceMetaDatas.hasNext())
      {
         ServiceMetaData serviceMetaData = serviceMetaDatas.next();
         org.apache.catalina.Service service = 
            (org.apache.catalina.Service) getInstance(serviceMetaData, "org.apache.catalina.core.StandardService");
         addLifecycleListeners(service, serviceMetaData.getListeners());
         service.setName(serviceMetaData.getName());
         service.setServer(catalinaServer);
         catalinaServer.addService(service);
         
         // Server/Service/Executor
         // Executor is useless in JBoss: the Executor will get injected in the executor field
         // and used directly
         
         // Server/Service/Connector
         if (serviceMetaData.getConnectors() != null)
         {
            Iterator<ConnectorMetaData> connectorMetaDatas = serviceMetaData.getConnectors().iterator();
            while (connectorMetaDatas.hasNext())
            {
               ConnectorMetaData connectorMetaData = connectorMetaDatas.next();
               Connector connector = new Connector(connectorMetaData.getProtocol());
               if (connectorMetaData.getAttributes() != null)
               {
                  Iterator<QName> names = connectorMetaData.getAttributes().keySet().iterator();
                  while (names.hasNext())
                  {
                     QName name = names.next();
                     String value = (String) connectorMetaData.getAttributes().get(name);
                     // FIXME: This should be done by XB
                     value = StringPropertyReplacer.replaceProperties(value);
                     IntrospectionUtils.setProperty(connector, name.getLocalPart(), value);
                  }
               }
               if (executor != null)
               {
                  IntrospectionUtils.callMethod1(connector.getProtocolHandler(), "setExecutor", 
                        executor, java.util.concurrent.Executor.class.getName(), getClass().getClassLoader());
               }
               service.addConnector(connector);
            }
         }
         
         // Server/Service/Engine
         EngineMetaData engineMetaData = serviceMetaData.getEngine();
         org.apache.catalina.Engine engine = 
            (org.apache.catalina.Engine) getInstance(engineMetaData, "org.apache.catalina.core.StandardEngine");
         addLifecycleListeners(engine, engineMetaData.getListeners());
         engine.setName(engineMetaData.getName());
         // FIXME: This should be done by XB
         if (engineMetaData.getJvmRoute() != null) {
            engine.setJvmRoute(StringPropertyReplacer.replaceProperties(engineMetaData.getJvmRoute()));
         }
         //engine.setJvmRoute(engineMetaData.getJvmRoute());
         engine.getPipeline().addValve(new ActiveRequestResponseCacheValve());
         engine.setDefaultHost(engineMetaData.getDefaultHost());
         service.setContainer(engine);
         
         // Server/Service/Engine/Realm
         if (engineMetaData.getRealm() != null) {
            engine.setRealm((org.apache.catalina.Realm) getInstance(engineMetaData.getRealm(), null));
         }
         
         // Server/Service/Engine/Valve
         addValves(engine, engineMetaData.getValves());
         
         // Server/Service/Engine/Host
         if (engineMetaData.getHosts() != null)
         {
            Iterator<HostMetaData> hostMetaDatas = engineMetaData.getHosts().iterator();
            while (hostMetaDatas.hasNext())
            {
               HostMetaData hostMetaData = hostMetaDatas.next();
               org.apache.catalina.Host host =
                  (org.apache.catalina.Host) getInstance(hostMetaData, "org.apache.catalina.core.StandardHost");
               addLifecycleListeners(host, hostMetaData.getListeners());
               host.setName(hostMetaData.getName());
               engine.addChild(host);
               
               // Server/Service/Engine/Host/Realm
               if (hostMetaData.getRealm() != null) {
                  host.setRealm((org.apache.catalina.Realm) getInstance(hostMetaData.getRealm(), null));
               }
               
               // Server/Service/Engine/Host/Valve
               addValves(host, hostMetaData.getValves());
               
               // Server/Service/Engine/Host/Alias
               if (hostMetaData.getAliases() != null) {
                  Iterator<String> aliases = hostMetaData.getAliases().iterator();
                  while (aliases.hasNext()) {
                     host.addAlias(aliases.next());
                  }
               }
               
            }
         }
         
      }
      
      // Set the resulting Server to the Catalina instance
      catalina.setServer(catalinaServer);
      
      // Start Tomcat
      catalina.create();
      catalinaServer.initialize();
      catalina.start();

      // Set up the authenticators in JNDI such that they can be configured for web apps
      InitialContext ic = new InitialContext();
      try
      {
         ic.bind("TomcatAuthenticators", tomcatDeployer.getAuthenticators());
      }
      catch (NamingException ne)
      {
         if (log.isTraceEnabled())
            log.trace("Binding Authenticators to JNDI failed", ne);
      }
      finally
      {
         try
         {
            ic.close();
         }
         catch (NamingException nee)
         {
         }
      }

      // Register the web container JACC PolicyContextHandlers
      HttpServletRequestPolicyContextHandler handler = new HttpServletRequestPolicyContextHandler();
      PolicyContext.registerHandler(HttpServletRequestPolicyContextHandler.WEB_REQUEST_KEY, handler, true);

      // If we are hot-deployed *after* the overall server is started
      // we'll never receive Server.START_NOTIFICATION_TYPE, so check
      // with the Server and start the connectors immediately, if this is the case.
      // Otherwise register to receive the server start-up notification.
      Boolean started = (Boolean) server.getAttribute(ServerImplMBean.OBJECT_NAME, "Started");
      if (started.booleanValue() == true)
      {
         log.debug("Server '" + ServerImplMBean.OBJECT_NAME + "' already started, starting connectors now");

         startConnectors();
      }
      else
      {
         // Register for notification of the overall server startup
         log.debug("Server '" + ServerImplMBean.OBJECT_NAME + "' not started, registering for start-up notification");

         server.addNotificationListener(ServerImplMBean.OBJECT_NAME, this, null, null);
      }

   }

   /**
    * Create a JavaBean corresponding to the given metadata, similar to what the digester is doing.
    */
   protected static Object getInstance(AnyXmlMetaData metaData, String defaultClassName) throws Exception
   {
      String className = metaData.getClassName();
      if (className == null) {
         className = defaultClassName;
      }
      if (className == null) {
         throw new IllegalArgumentException("No className specified for element");
      }
      Object instance = TomcatService.class.getClassLoader().loadClass(className).newInstance();
      if (metaData.getAttributes() != null) {
         Iterator<QName> names = metaData.getAttributes().keySet().iterator();
         while (names.hasNext()) {
            QName name = names.next();
            String value = (String) metaData.getAttributes().get(name);
            // FIXME: This should be done by XB
            value = StringPropertyReplacer.replaceProperties(value);
            IntrospectionUtils.setProperty(instance, name.getLocalPart(), value);
         }
      }
      return instance;
   }
   
   /**
    * Associate lifecycle listeners with the instance, if it implements Lifecycle.
    */
   protected static void addLifecycleListeners(Object instance, List<ListenerMetaData> list) throws Exception
   {
      if (list == null) {
         return;
      }
      org.apache.catalina.Lifecycle lifecycle = null;
      if (!(instance instanceof org.apache.catalina.Lifecycle))
      {
         return;
      }
      else
      {
         lifecycle = (org.apache.catalina.Lifecycle) instance;
      }
      Iterator<ListenerMetaData> listenerMetaDatas = list.iterator();
      while (listenerMetaDatas.hasNext())
      {
         ListenerMetaData listenerMetaData = listenerMetaDatas.next();
         lifecycle.addLifecycleListener((org.apache.catalina.LifecycleListener) getInstance(listenerMetaData, null));
      }

   }
   
   
   /**
    * Associate valves with the instance, if it implements Lifecycle.
    */
   protected static void addValves(Object instance, List<ValveMetaData> list) throws Exception
   {
      if (list == null) {
         return;
      }
      org.apache.catalina.Pipeline pipeline = null;
      if (!(instance instanceof org.apache.catalina.Pipeline))
      {
         return;
      }
      else
      {
         pipeline = (org.apache.catalina.Pipeline) instance;
      }
      Iterator<ValveMetaData> valveMetaDatas = list.iterator();
      while (valveMetaDatas.hasNext())
      {
         ValveMetaData valveMetaData = valveMetaDatas.next();
         pipeline.addValve((org.apache.catalina.Valve) getInstance(valveMetaData, null));
      }

   }
   
   
   @Override
   protected void stopService() throws Exception
   {

      if (tomcatDeployer == null)
         throw new IllegalStateException("Must set TomcatDeployer before stopping");

      // Hot undeploy
      Boolean inShutdown = (Boolean) server.getAttribute(ServerImplMBean.OBJECT_NAME, "InShutdown");
      if (inShutdown.booleanValue() == false)
      {
         log.debug("Server '" + ServerImplMBean.OBJECT_NAME + "' already started, stopping connectors now");

         stopConnectors();
      }

      MBeanServer server = super.getServer();
      String objectNameS = tomcatDeployer.getDomain() + ":type=Catalina";
      ObjectName objectName = new ObjectName(objectNameS);

      server.invoke(objectName, "stop", new Object[]{}, new String[]{});

      server.invoke(objectName, "destroy", new Object[]{}, new String[]{});

      server.unregisterMBean(objectName);

      MBeanServer server2 = server;

      // Unregister any remaining jboss.web or Catalina MBeans
      ObjectName queryObjectName = new ObjectName(tomcatDeployer.getDomain() + ":*");
      Iterator iterator = server2.queryMBeans(queryObjectName, null).iterator();
      while (iterator.hasNext())
      {
         ObjectInstance oi = (ObjectInstance) iterator.next();
         ObjectName toRemove = oi.getObjectName();
         // Exception: Don't unregister the service right now
         if (!"WebServer".equals(toRemove.getKeyProperty("service")))
         {
            if (server2.isRegistered(toRemove))
            {
               server2.unregisterMBean(toRemove);
            }
         }
      }

   }

   // Expose the TomcatDeployer MBean interface

   public String getConfigFile()
   {
      return tomcatDeployer == null ? null : tomcatDeployer.getConfigFile();
   }

   public String getContextMBeanCode()
   {
      return tomcatDeployer == null ? null : tomcatDeployer.getContextMBeanCode();
   }

   public boolean getUseJBossWebLoader()
   {
      return tomcatDeployer == null ? false : tomcatDeployer.getUseJBossWebLoader();
   }

   public String getDomain()
   {
      return tomcatDeployer == null ? null : tomcatDeployer.getDomain();
   }

   public String[] getFilteredPackages()
   {
      return tomcatDeployer == null ? null : tomcatDeployer.getFilteredPackages();
   }

   public String getManagerClass()
   {
      return tomcatDeployer == null ? null : tomcatDeployer.getManagerClass();
   }

   public String getSessionIdAlphabet()
   {
      return tomcatDeployer == null ? null : tomcatDeployer.getSessionIdAlphabet();
   }

   public String getSubjectAttributeName()
   {
      return tomcatDeployer == null ? null : tomcatDeployer.getSubjectAttributeName();
   }

   public boolean getDeleteWorkDirOnContextDestroy()
   {
      return tomcatDeployer == null ? false : tomcatDeployer.getDeleteWorkDirOnContextDestroy();
   }

   public boolean isAllowSelfPrivilegedWebApps()
   {
      return tomcatDeployer == null ? false : tomcatDeployer.isAllowSelfPrivilegedWebApps();
   }

   public void setAllowSelfPrivilegedWebApps(boolean flag)
   {
      if (tomcatDeployer != null)
         tomcatDeployer.setAllowSelfPrivilegedWebApps(flag);
   }

   public void setConfigFile(String configFile)
   {
      if (tomcatDeployer != null)
         tomcatDeployer.setConfigFile(configFile);
   }

   public void setContextMBeanCode(String className)
   {
      if (tomcatDeployer != null)
         tomcatDeployer.setContextMBeanCode(className);
   }

   public void setDeleteWorkDirOnContextDestroy(boolean flag)
   {
      if (tomcatDeployer != null)
         tomcatDeployer.setDeleteWorkDirOnContextDestroy(flag);
   }

   public void setDomain(String domainName)
   {
      if (tomcatDeployer != null)
         tomcatDeployer.setDomain(domainName);
   }

   public void setFilteredPackages(String[] pkgs)
   {
      if (tomcatDeployer != null)
         tomcatDeployer.setFilteredPackages(pkgs);
   }

   public void setManagerClass(String managerClass)
   {
      if (tomcatDeployer != null)
         tomcatDeployer.setManagerClass(managerClass);
   }

   public void setSecurityManagerService(JaasSecurityManagerServiceMBean mgr)
   {
      if (tomcatDeployer != null)
         tomcatDeployer.setSecurityManagerService(mgr);
   }

   public void setSessionIdAlphabet(String sessionIdAlphabet)
   {
      if (tomcatDeployer != null)
         tomcatDeployer.setSessionIdAlphabet(sessionIdAlphabet);
   }

   public void setSubjectAttributeName(String name)
   {
      if (tomcatDeployer != null)
         tomcatDeployer.setSubjectAttributeName(name);
   }

   public void setUseJBossWebLoader(boolean flag)
   {
      if (tomcatDeployer != null)
         tomcatDeployer.setUseJBossWebLoader(flag);
   }

   public void startConnectors() throws Exception
   {
      if (tomcatDeployer == null)
         throw new IllegalStateException("Must set TomcatDeployer before starting connectors");
      if (connectorsRunning)
         return;

      MBeanServer server = super.getServer();
      ObjectName service = new ObjectName(tomcatDeployer.getDomain() + ":type=Service,serviceName=jboss.web");
      Object[] args = {};
      String[] sig = {};
      Connector[] connectors = (Connector[]) server.invoke(service, "findConnectors", args, sig);
      for (int n = 0; n < connectors.length; n++)
      {
         Lifecycle lc = connectors[n];
         lc.start();
      }

      connectorsRunning = true;

      // Notify listeners that connectors have stqred processing requests (JBPAPP-4960)
      sendNotification(new Notification(TOMCAT_CONNECTORS_STARTED, this, getNextNotificationSequenceNumber()));
   }

   public void stopConnectors() throws Exception
   {
      if (tomcatDeployer == null)
         throw new IllegalStateException("Must set TomcatDeployer before stopping connectors");
      if (!connectorsRunning)
         return;

      // Notify listeners that connectors have stopped processing requests (JBPAPP-4960)
      sendNotification(new Notification(TOMCAT_CONNECTORS_STOPPED, this, getNextNotificationSequenceNumber()));

      MBeanServer server = super.getServer();
      ObjectName service = new ObjectName(tomcatDeployer.getDomain() + ":type=Service,serviceName=jboss.web");
      Object[] args = {};
      String[] sig = {};
      Connector[] connectors = (Connector[]) server.invoke(service, "findConnectors", args, sig);
      for (int n = 0; n < connectors.length; n++)
      {
         connectors[n].pause();
         connectors[n].stop();
      }
      connectorsRunning = false;
   }

   /**
    * Used to receive notification of the server start msg so the tomcat connectors can be started after all web apps
    * are deployed.
    */
   public void handleNotification(Notification msg, Object handback)
   {
      String type = msg.getType();
      if (type.equals(Server.START_NOTIFICATION_TYPE))
      {
         log.debug("Saw " + type + " notification, starting connectors");
         try
         {
            startConnectors();
         }
         catch (Exception e)
         {
            log.warn("Failed to startConnectors", e);
         }
      }
      if (type.equals(Server.STOP_NOTIFICATION_TYPE))
      {
         log.debug("Saw " + type + " notification, stopping connectors");
         try
         {
            stopConnectors();
         }
         catch (Exception e)
         {
            log.warn("Failed to stopConnectors", e);
         }
      }
   }

   public String getDefaultSecurityDomain()
   {
      return tomcatDeployer == null ? null : tomcatDeployer.getDefaultSecurityDomain();
   }

   public boolean getJava2ClassLoadingCompliance()
   {
      return tomcatDeployer == null ? false : tomcatDeployer.getJava2ClassLoadingCompliance();
   }

   public boolean getLenientEjbLink()
   {
      return tomcatDeployer == null ? false : tomcatDeployer.getLenientEjbLink();
   }

   public boolean getUnpackWars()
   {
      return tomcatDeployer == null ? false : tomcatDeployer.getUnpackWars();
   }

   public void setDefaultSecurityDomain(String defaultSecurityDomain)
   {
      if (tomcatDeployer != null)
         tomcatDeployer.setDefaultSecurityDomain(defaultSecurityDomain);
   }

   public void setJava2ClassLoadingCompliance(boolean flag)
   {
      if (tomcatDeployer != null)
         tomcatDeployer.setJava2ClassLoadingCompliance(flag);
   }

   public void setLenientEjbLink(boolean flag)
   {
      if (tomcatDeployer != null)
         tomcatDeployer.setLenientEjbLink(flag);
   }

   public void setUnpackWars(boolean flag)
   {
      if (tomcatDeployer != null)
         tomcatDeployer.setUnpackWars(flag);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.web.tomcat.service.deployers.TomcatDeployerMBean#getHttpHeaderForSSOAuth()
    */
   public String getHttpHeaderForSSOAuth()
   {
      return tomcatDeployer == null ? null : tomcatDeployer.getHttpHeaderForSSOAuth();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.web.tomcat.service.deployers.TomcatDeployerMBean#setHttpHeaderForSSOAuth(java.lang.String)
    */
   public void setHttpHeaderForSSOAuth(String httpHeaderForSSOAuth)
   {
      if (this.tomcatDeployer != null)
         this.tomcatDeployer.setHttpHeaderForSSOAuth(httpHeaderForSSOAuth);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.web.tomcat.service.deployers.TomcatDeployerMBean#getSessionCookieForSSOAuth()
    */
   public String getSessionCookieForSSOAuth()
   {
      return tomcatDeployer == null ? null : tomcatDeployer.getSessionCookieForSSOAuth();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.web.tomcat.service.deployers.TomcatDeployerMBean#setSessionCookieForSSOAuth(java.lang.String)
    */
   public void setSessionCookieForSSOAuth(String sessionCookieForSSOAuth)
   {
      if (this.tomcatDeployer != null)
         this.tomcatDeployer.setSessionCookieForSSOAuth(sessionCookieForSSOAuth);
   }
   
   /**
    * {@inheritDoc}
    * 
    * Overrides the superclass version to inject the <code>KernelController</code>
    * into {@link JBossWebMicrocontainerBeanLocator}.
    */
   @Override
   public void setKernelControllerContext(KernelControllerContext controllerContext) throws Exception
   {
      super.setKernelControllerContext(controllerContext);
      KernelController kernelController = controllerContext == null ? null : controllerContext.getKernel().getController();
      JBossWebMicrocontainerBeanLocator.setKernelController(kernelController);
   }
   
   /**
    * {@inheritDoc}
    * 
    * Overrides the superclass version to clear the <code>KernelController</code>
    * from {@link JBossWebMicrocontainerBeanLocator}.
    */
   @Override
   public void unsetKernelControllerContext(KernelControllerContext controllerContext) throws Exception
   {
      super.unsetKernelControllerContext(controllerContext);
      JBossWebMicrocontainerBeanLocator.setKernelController(null);
   }
}
