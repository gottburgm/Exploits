/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.profileservice.remoting;

import java.util.ArrayList;
import java.util.List;

import javax.naming.InitialContext;

import org.jboss.aop.Dispatcher;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.proxy.Proxy;
import org.jboss.aop.proxy.container.GeneratedAOPProxyFactory;
import org.jboss.aspects.remoting.InvokeRemoteInterceptor;
import org.jboss.aspects.remoting.MergeMetaDataInterceptor;
import org.jboss.aspects.remoting.Remoting;
import org.jboss.aspects.security.SecurityClientInterceptor;
import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.deployers.spi.management.deploy.DeploymentManager;
import org.jboss.logging.Logger;
import org.jboss.profileservice.spi.ProfileService;
import org.jboss.remoting.InvokerLocator;
import org.jboss.util.id.GUID;
import org.jboss.util.naming.NonSerializableFactory;
import org.jboss.util.naming.Util;

/**
 * An aop/remoting proxy factory bean that exposes the ProfileService
 * interfaces.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 91308 $
 */
public class ProxyFactory
{
   private static final Logger log = Logger.getLogger(ProxyFactory.class);
   private String dispatchName = "ProfileService";
   private String jndiName = "ProfileService";
   private String localJndiName = "java:ProfileService";
   private String mgtViewJndiName = "java:ManagementView";
   private String deployMgrJndiName = "java:DeploymentManager";
   private InvokerLocator locator;
   /** The ProfileService bean the ps proxies delegate to */
   private ProfileService ps;
   /** The ManagementView bean the */
   private ManagementView mgtView;
   /** The DeploymentManager bean the */
   private DeploymentManager deployMgr;
   /** The remoting ProfileService proxy */
   private Proxy psProxy;
   /** The remoting ProfileService proxy */
   private Proxy mgtViewProxy;
   /** The remoting ProfileService proxy */
   private Proxy deployMgrProxy;
   /** The server side secured ProfileService proxy */
   private ProfileService psProxySecure;

   /** The secure server interceptor stack */
   private List<Interceptor> serverProxyInterceptors;
   /** The remoting client interceptor stack */
   private List<Interceptor> proxyInterceptors;
   /** Flag to create links to the old ejb3 facade proxy locations */
   private boolean createEjb3Links;

   public String getDispatchName()
   {
      return dispatchName;
   }

   public void setDispatchName(String dispatchName)
   {
      this.dispatchName = dispatchName;
   }

   public String getJndiName()
   {
      return jndiName;
   }

   public void setJndiName(String jndiName)
   {
      this.jndiName = jndiName;
   }

   
   public String getMgtViewJndiName()
   {
      return mgtViewJndiName;
   }

   public void setMgtViewJndiName(String mgtViewJndiName)
   {
      this.mgtViewJndiName = mgtViewJndiName;
   }

   public String getDeployMgrJndiName()
   {
      return deployMgrJndiName;
   }

   public void setDeployMgrJndiName(String deployMgrJndiName)
   {
      this.deployMgrJndiName = deployMgrJndiName;
   }

   public InvokerLocator getLocator()
   {
      return locator;
   }

   public void setLocator(InvokerLocator locator)
   {
      this.locator = locator;
   }

   public ProfileService getProfileService()
   {
      return ps;
   }

   public void setProfileService(ProfileService ps)
   {
      this.ps = ps;
   }

   public Proxy getProfileServiceProxy()
   {
      return psProxy;
   }

   public ManagementView getViewManager()
   {
      return mgtView;
   }
   public void setViewManager(ManagementView mgtView)
   {
      this.mgtView = mgtView;
   }

   public Proxy getManagementViewProxy()
   {
      return mgtViewProxy;
   }

   public DeploymentManager getDeploymentManager()
   {
      return deployMgr;
   }
   public void setDeploymentManager(DeploymentManager deployMgr)
   {
      this.deployMgr = deployMgr;
   }

   public Proxy getDeployMgrProxy()
   {
      return deployMgrProxy;
   }

   
   public List<Interceptor> getProxyInterceptors()
   {
      return proxyInterceptors;
   }
   public void setProxyInterceptors(List<Interceptor> proxyInterceptors)
   {
      this.proxyInterceptors = proxyInterceptors;
   }

   public List<Interceptor> getServerProxyInterceptors()
   {
      return serverProxyInterceptors;
   }
   public void setServerProxyInterceptors(List<Interceptor> serverProxyInterceptors)
   {
      this.serverProxyInterceptors = serverProxyInterceptors;
   }

   public boolean isCreateEjb3Links()
   {
      return createEjb3Links;
   }

   public void setCreateEjb3Links(boolean createEjb3Links)
   {
      this.createEjb3Links = createEjb3Links;
   }

   public void start()
      throws Exception
   {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Class<?>[] ifaces = {ProfileService.class};

      // Create the server side secured proxy
      if(this.serverProxyInterceptors == null)
      {
         serverProxyInterceptors = new ArrayList<Interceptor>();
      }
      // Add the instance interceptor which delegates to the injected ps
      //serverProxyInterceptors.add(new InstanceInterceptor("ProfileService", ps));
      SecurityContainer container = new SecurityContainer(serverProxyInterceptors, ps);
      psProxySecure = (ProfileService) java.lang.reflect.Proxy.newProxyInstance(loader, ifaces, container);

      // Bind the unsecure proxy under the local jndi name
      InitialContext ctx = new InitialContext();      
      NonSerializableFactory.rebind(ctx, localJndiName, ps);
      
      // Create the remote ProfileService proxy using the secured proxy as the target
      Dispatcher.singleton.registerTarget(dispatchName, psProxySecure);
      // Create a default client proxy interceptor stack
      if(proxyInterceptors == null)
      {
         proxyInterceptors = new ArrayList<Interceptor>();
         proxyInterceptors.add(SecurityClientInterceptor.singleton);
         proxyInterceptors.add(MergeMetaDataInterceptor.singleton);
         proxyInterceptors.add(InvokeRemoteInterceptor.singleton);
      }

      // Create the remoting proxy that invokes back to the secured proxy target
      psProxy = Remoting.createRemoteProxy(dispatchName, loader, ifaces, locator, proxyInterceptors, "ProfileService");
      Util.bind(ctx, jndiName, psProxy);
      log.debug("Bound ProfileService proxy under: "+jndiName);

      // A server side secured ManagementView proxy
      SecurityContainer container2 = new SecurityContainer(serverProxyInterceptors, mgtView);
      Class<?>[] ifaces2 = {ManagementView.class};
      ManagementView mgtViewSecure = (ManagementView) java.lang.reflect.Proxy.newProxyInstance(loader, ifaces2, container2);
      // Create the ManagementView proxy
      Class[] mvIfaces = {ManagementView.class};
      String mvDispatchName = dispatchName+".ManagementView";
      Dispatcher.singleton.registerTarget(mvDispatchName, mgtViewSecure);
      mgtViewProxy = Remoting.createRemoteProxy(mvDispatchName, loader, mvIfaces, locator, proxyInterceptors, "ProfileService");
      log.debug("Created ManagementView proxy");
      if(mgtViewJndiName != null && mgtViewJndiName.length() > 0)
      {
         Util.bind(ctx, mgtViewJndiName, mgtViewProxy);
         log.debug("Bound ManagementView proxy under: "+mgtViewJndiName);
      }

      // A server side secured DeploymentManager proxy
      SecurityContainer container3 = new SecurityContainer(serverProxyInterceptors, deployMgr);
      Class<?>[] ifaces3 = {DeploymentManager.class};
      DeploymentManager deployMgrSecure = (DeploymentManager) java.lang.reflect.Proxy.newProxyInstance(loader, ifaces3, container3);
      // Create the DeploymentManager proxy
      Class[] dmIfaces = {DeploymentManager.class};
      String dmDispatchName = dispatchName+".DeploymentManager";
      Dispatcher.singleton.registerTarget(dmDispatchName, deployMgrSecure);
      deployMgrProxy = Remoting.createRemoteProxy(dmDispatchName, loader, dmIfaces, locator, proxyInterceptors, "DeploymentManager");
      log.debug("Created DeploymentManager proxy");      
      if(deployMgrJndiName != null && deployMgrJndiName.length() > 0)
      {
         Util.bind(ctx, deployMgrJndiName, deployMgrProxy);
         log.debug("Bound DeploymentManager proxy under: "+deployMgrJndiName);
      }

      //
      if(createEjb3Links)
      {
         Util.createLinkRef("SecureProfileService/remote", this.jndiName);
         Util.createLinkRef("SecureManagementView/remote", mgtViewJndiName);
         Util.createLinkRef("SecureDeploymentManager/remote", deployMgrJndiName);
         log.debug("Bound links back to secure ejb names");
      }
   }

   public void stop()
      throws Exception
   {
      Dispatcher.singleton.unregisterTarget(dispatchName);
      String mvDispatchName = dispatchName+".ManagementView";
      Dispatcher.singleton.unregisterTarget(mvDispatchName);
      InitialContext ctx = new InitialContext();
      Util.unbind(ctx, jndiName);
      log.debug("Unbound ProfileService proxy");
      if(mgtViewJndiName != null && mgtViewJndiName.length() > 0)
      {
         Util.unbind(ctx, mgtViewJndiName);
         log.debug("Unbound ManagementView proxy");
      }
      if(deployMgrJndiName != null && deployMgrJndiName.length() > 0)
      {
         Util.unbind(ctx, deployMgrJndiName);
         log.debug("Unbound DeploymentManager proxy");
      }
      //
      if(createEjb3Links)
      {
         Util.unbind(ctx, "SecureProfileService/remote");
         Util.unbind(ctx, "SecureManagementView/remote");
         Util.unbind(ctx, "SecureDeploymentManager/remote");
         log.debug("Unbound links back to secure ejb names");
      }
   }
}
