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
package org.jboss.ejb3.iiop;

import org.jboss.classloading.spi.RealClassLoader;
import org.jboss.ejb3.InitialContextFactory;
import org.jboss.ejb3.annotation.IIOP;
import org.jboss.ejb3.common.lang.ClassHelper;
import org.jboss.ejb3.core.proxy.spi.EJB2RemoteProxyFactory;
import org.jboss.ejb3.core.proxy.spi.RemoteProxyFactory;
import org.jboss.ejb3.proxy.factory.ProxyFactoryHelper;
import org.jboss.ejb3.session.SessionContainer;
import org.jboss.iiop.CorbaORBService;
import org.jboss.iiop.codebase.CodebasePolicy;
import org.jboss.iiop.csiv2.CSIv2Policy;
import org.jboss.iiop.rmi.InterfaceAnalysis;
import org.jboss.iiop.rmi.ir.InterfaceRepository;
import org.jboss.invocation.iiop.ReferenceFactory;
import org.jboss.invocation.iiop.ServantRegistries;
import org.jboss.invocation.iiop.ServantRegistry;
import org.jboss.invocation.iiop.ServantRegistryKind;
import org.jboss.logging.Logger;
import org.jboss.metadata.IorSecurityConfigMetaData;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.proxy.ejb.handle.HandleDelegateImpl;
import org.jboss.system.Registry;
import org.jboss.util.naming.NonSerializableFactory;
import org.jboss.web.WebClassLoader;
import org.jboss.web.WebServiceMBean;
import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.Current;
import org.omg.PortableServer.CurrentHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import java.io.Serializable;
import java.lang.Object;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: 112294 $
 */
public class IORFactory
//   implements SessionProxyFactory
{
   private static final Logger log = Logger.getLogger(IORFactory.class);
   
   private SessionContainer container;
   //private RemoteBinding binding;
   private String webServiceName = "jboss:service=WebService"; // TODO: make webServiceName configurable
   
   // after start available
   private String beanRepositoryIds[];
   private String homeRepositoryIds[];
//   private InterfaceAnalysis interfaceAnalysis;
   private ORB orb;
//   private POA poa;
   private POA irPoa;
   private InterfaceRepository iri;
   private ServantRegistry servantRegistry;
   private ServantRegistry homeServantRegistry;
   private WebClassLoader wcl;
   private ReferenceFactory referenceFactory;
   private ReferenceFactory homeReferenceFactory;

   private EJB2RemoteProxyFactory remoteProxyFactory = new EJB2RemoteProxyFactory()
   {   
      public EJBObject create(Serializable id)
      {
         if(id == null)
            return createProxy();
         else
            return createProxy(id);
      }

      public EJBHome createHome()
      {
         return createHomeProxy();
      }
   };
   
   public IORFactory(SessionContainer container)
   {
      assert container != null : "container is null";
      //assert binding != null : "binding is null";
      
      this.container = container;
      //this.binding = binding;
   }
   
   // TODO: create a default IIOP annotation
   private static final IIOP defaultIIOP = new IIOP()
   {
      public boolean interfaceRepositorySupported()
      {
         return false;
      }

      public String poa()
      {
         return POA_PER_SERVANT;
      }
      
      public Class<? extends Annotation> annotationType()
      {
         return IIOP.class;
      }
   };
   
   public EJBHome createHomeProxy()
   {
      try
      {
         org.omg.CORBA.Object corbaRef = homeReferenceFactory.createReference(homeRepositoryIds[0]);
         
         EJBHome corbaObj = (EJBHome) PortableRemoteObject.narrow(corbaRef, EJBHome.class);
         
         return corbaObj;
      }
      catch(Exception e)
      {
         throw new RuntimeException(e);
      }
   }
   
   public EJBObject createProxy()
   {
      try
      {
         org.omg.CORBA.Object corbaRef = referenceFactory.createReference(beanRepositoryIds[0]);
         
         EJBObject corbaObj = (EJBObject) PortableRemoteObject.narrow(corbaRef, EJBObject.class);
         
         return corbaObj;
      }
      catch(Exception e)
      {
         throw new RuntimeException(e);
      }
   }
   
   public EJBObject createProxy(Object id)
   {
      try
      {
         org.omg.CORBA.Object corbaRef = referenceFactory.createReferenceWithId(id, beanRepositoryIds[0]);
         
         EJBObject corbaObj = (EJBObject) PortableRemoteObject.narrow(corbaRef, EJBObject.class);
         
         return corbaObj;
      }
      catch(Exception e)
      {
         throw new RuntimeException(e);
      }
   }
   
   private IIOP getIIOP()
   {
      IIOP iiop = container.getAnnotation(IIOP.class);
      if(iiop != null)
         return iiop;
      
      return defaultIIOP;
   }
   
   private String getJndiName()
   {
      return ProxyFactoryHelper.getDefaultRemoteBusinessJndiName(container);
   }

   /**
    * A slow method that works around a lot of hacks to return the remote interface.
    * @return session bean’s remote interface, if the session bean provides an EJB 2.1 remote client view.
    */
   protected Class<?> getRemoteInterface()
   {
      Class<?> homeInterface = ProxyFactoryHelper.getRemoteHomeInterface(container);
      assert homeInterface != null : "homeInterface is null";
      List<Method> createMethods = ClassHelper.getAllMethodsByPrefix(homeInterface, "create");
      assert createMethods.size() > 0 : "no create methods on " + homeInterface;
      return createMethods.get(0).getReturnType();
   }

   protected RemoteProxyFactory getRemoteProxyFactory()
   {
      return remoteProxyFactory;
   }
   
   private String getServantName()
   {
      // TODO: is "Servant/" a good prefix for servant name
      return "Servant/" + getJndiName();
   }
   
   private WebServiceMBean getWebServer() throws MalformedObjectNameException
   {
      if(webServiceName == null)
         throw new IllegalStateException("iiop is not going to work without a web service");
      
      return (WebServiceMBean) MBeanProxyExt.create(WebServiceMBean.class, webServiceName);
   }
   
   /**
    * Literal copy from org.jboss.proxy.ejb.IORFactory
    */
   private void rebind(NamingContextExt ctx, String strName, org.omg.CORBA.Object obj) throws Exception
   {
      NameComponent[] name = ctx.to_name(strName);
      NamingContext intermediateCtx = ctx;

      for (int i = 0; i < name.length - 1; i++ ) {
         NameComponent[] relativeName = new NameComponent[] { name[i] };
         try {
            intermediateCtx = NamingContextHelper.narrow(
                  intermediateCtx.resolve(relativeName));
         }
         catch (NotFound e) {
            intermediateCtx = intermediateCtx.bind_new_context(relativeName);
         }
      }
      intermediateCtx.rebind(new NameComponent[] { name[name.length - 1] }, obj);
   }
   
   private void removeWebClassLoader() throws MalformedObjectNameException
   {
      getWebServer().removeClassLoader(wcl);
   }
   
   public void setWebServiceName(String name)
   {
      this.webServiceName = name;
   }
   
   public void start() throws Exception
   {
      Class<?> remoteInterface = getRemoteInterface();
      InterfaceAnalysis interfaceAnalysis = InterfaceAnalysis.getInterfaceAnalysis(remoteInterface);
      this.beanRepositoryIds = interfaceAnalysis.getAllTypeIds();
      
      InterfaceAnalysis homeInterfaceAnalysis = null;
      Class homeInterface = ProxyFactoryHelper.getRemoteHomeInterface(container);
      if(homeInterface != null)
      {
         if(!EJBHome.class.isAssignableFrom(homeInterface))
            throw new IllegalArgumentException("home interface " + homeInterface.getName() + " must extend javax.ejb.EJBHome (EJB3 4.6.8)");
         homeInterfaceAnalysis = InterfaceAnalysis.getInterfaceAnalysis(homeInterface);
         this.homeRepositoryIds = homeInterfaceAnalysis.getAllTypeIds();
      }
      // To allow EJB3 Stateless beans to operate we can function without a home interface.
      // Note that a lot of other constructs both in the deployer and this class prevent a non-homed bean to work,
      // so basically a bean without an EJB 2.1 remote view does not work.
      
      // Get orb and irPoa references
      try {
         orb = (ORB)InitialContextFactory.getInitialContext().lookup("java:/" + CorbaORBService.ORB_NAME);
      }
      catch (NamingException e) {
         throw new Exception("Cannot lookup java:/" + CorbaORBService.ORB_NAME + ": " + e);
      }
      try {
         irPoa = (POA)InitialContextFactory.getInitialContext().lookup("java:/" + CorbaORBService.IR_POA_NAME);
      }
      catch (NamingException e) {
         throw new Exception("Cannot lookup java:/" + CorbaORBService.IR_POA_NAME + ": " + e);
      }
      
      IIOP iiop = getIIOP();
      if(iiop.interfaceRepositorySupported())
      {
         this.iri = new InterfaceRepository(orb, irPoa, getJndiName());
         iri.mapClass(remoteInterface);
         if(homeInterface != null)
            iri.mapClass(homeInterface);
         iri.finishBuild();
      }
      
      // TODO: obtain the iiop invoker name properly
      ObjectName invokerName = new ObjectName("jboss:service=invoker,type=iiop");
      ServantRegistries servantRegistries = (ServantRegistries) Registry.lookup(invokerName);
      if(servantRegistries == null)
         throw new Exception("can't find iiop invoker");
      ServantRegistryKind registryWithTransientPOA;
      ServantRegistryKind registryWithPersistentPOA; 
      if(iiop.poa().equals(IIOP.POA_PER_SERVANT))
      {
         registryWithTransientPOA = ServantRegistryKind.TRANSIENT_POA_PER_SERVANT;
         registryWithPersistentPOA = ServantRegistryKind.PERSISTENT_POA_PER_SERVANT;
      }
      else if(iiop.poa().equals(IIOP.POA_SHARED))
      {
         registryWithTransientPOA = ServantRegistryKind.SHARED_TRANSIENT_POA;
         registryWithPersistentPOA = ServantRegistryKind.SHARED_PERSISTENT_POA;
      }
      else
         throw new IllegalArgumentException("@IIOP.poa can only be 'per-servant' or 'shared'");
      // Only works for session container
      this.servantRegistry = servantRegistries.getServantRegistry(registryWithTransientPOA);
      this.homeServantRegistry = servantRegistries.getServantRegistry(registryWithPersistentPOA); // TODO: why is home interface in persistent poa?
      
      // Hack in a WebCL (from org.jboss.ejb.EjbModule.initializeContainer)
      // TODO:  seting up a WebClassLoader needs to be done somewhere where
      ObjectName on = container.getObjectName();
      this.wcl = new EJB3IIOPWebClassLoader(on, (RealClassLoader) container.getClassloader(), getJndiName());
      WebServiceMBean webServer = getWebServer();
      URL[] codebaseURLs = {webServer.addClassLoader(wcl)};
      wcl.setWebURLs(codebaseURLs);
      
      // setup a codebase policy, the CodebaseInterceptor will translate this to a TAG_JAVA_CODEBASE
      String codebaseString = wcl.getCodebaseString();
      log.debug("codebase = " + codebaseString);
      Any codebase = orb.create_any();
      codebase.insert_string(codebaseString);
      Policy codebasePolicy;
      codebasePolicy = orb.create_policy(CodebasePolicy.TYPE, codebase);
      
      // Create csiv2Policy for both home and remote containing
      // IorSecurityConfigMetadata
      Any secPolicy = orb.create_any();
//      IorSecurityConfigMetaData iorSecurityConfigMetaData =
//         container.getBeanMetaData().getIorSecurityConfigMetaData();
      IorSecurityConfigMetaData iorSecurityConfigMetaData = new IorSecurityConfigMetaData(); // TODO: make ior security configurable
      secPolicy.insert_Value(iorSecurityConfigMetaData);
      Policy csiv2Policy = orb.create_policy(CSIv2Policy.TYPE, secPolicy);
      
      Policy policies[] = { codebasePolicy, csiv2Policy };
      
      InterfaceDef interfaceDef = null;
      if(iri != null)
      {
         Repository ir = iri.getReference();
         interfaceDef = InterfaceDefHelper.narrow(ir.lookup_id(beanRepositoryIds[0]));
      }
      
      Current poaCurrent = CurrentHelper.narrow(orb.resolve_initial_references("POACurrent"));

      NamingContextExt ctx = getNamingContextExt();

      log.debug("binding servant name " + getServantName());
      
      Servant servant = new BeanCorbaServant(this, poaCurrent, container, container.getClassloader(), interfaceDef, interfaceAnalysis);
      this.referenceFactory = servantRegistry.bind(getServantName(), servant, policies);
      
      EJBObject corbaObj = (EJBObject) createProxy();
      
      rebind(ctx, getJndiName(), (org.omg.CORBA.Object) corbaObj);
      
      // TODO: use iri
      if(homeInterfaceAnalysis != null)
      {
         servant = new BeanCorbaServant(this, poaCurrent, container, container.getClassloader(), null, homeInterfaceAnalysis);
         this.homeReferenceFactory = homeServantRegistry.bind(getServantName() + "Home", servant, policies);
         
         Object homeObject = createHomeProxy();
         
         rebind(ctx, ProxyFactoryHelper.getHomeJndiName(container), (org.omg.CORBA.Object) homeObject);
      }
      
      // bind HandleDelegate stuff for client usage
      Context compCtx = (Context) InitialContextFactory.getInitialContext().lookup("java:comp");
      NonSerializableFactory.rebind(compCtx, "ORB", orb);
      NonSerializableFactory.rebind(compCtx, "HandleDelegate", new HandleDelegateImpl());
   }
   
   public void stop() throws Exception
   {
      if(homeReferenceFactory != null)
      {
         unbind(ProxyFactoryHelper.getHomeJndiName(container));
         unbindHomeServant();
      }
      unbind(getJndiName());
      
      unbindServant();
      
      removeWebClassLoader();
   }
   
   /**
    * Unbind the bean from CosNaming
    */
   private void unbind(String strName)
   {
      try
      {
         NamingContextExt corbaContext = getNamingContextExt();
         NameComponent n[] = corbaContext.to_name(strName);
         getNamingContextExt().unbind(n);
      }
      catch(Exception e)
      {
         log.warn("unable to unbind '" + strName + "'", e);
      }
   }
   
   private void unbindHomeServant()
   {
      try
      {
         homeServantRegistry.unbind(getServantName() + "Home");
      }
      catch(Exception e)
      {
         log.warn("unable to unbind home servant", e);
      }
   }
   
   private void unbindServant()
   {
      try
      {
         servantRegistry.unbind(getServantName());
      }
      catch(Exception e)
      {
         log.warn("unable to unbind servant", e);
      }
   }

   private NamingContextExt getNamingContextExt() throws NamingException
   {
      Context initialContext = InitialContextFactory.getInitialContext();
      
      // NOTE: eclipse editor parser crashes silently on this line (because of org.jboss.iiop.CorbaNamingService) with unknown reason
      // that's why this method is at the end
      return NamingContextExtHelper.narrow((org.omg.CORBA.Object) initialContext.lookup("java:/" + org.jboss.iiop.CorbaNamingService.NAMING_NAME));
   }
}