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
package org.jboss.proxy.ejb;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.Container;
import org.jboss.ejb.EJBProxyFactory;
import org.jboss.ejb.EJBProxyFactoryContainer;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationContext;
import org.jboss.invocation.InvocationKey;
import org.jboss.invocation.Invoker;
import org.jboss.logging.Logger;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.EntityMetaData;
import org.jboss.metadata.InvokerProxyBindingMetaData;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.SessionMetaData;
import org.jboss.naming.Util;
import org.jboss.proxy.ClientContainer;
import org.jboss.proxy.ClientContainerEx;
import org.jboss.proxy.IClientContainer;
import org.jboss.proxy.Interceptor;
import org.jboss.proxy.ejb.handle.HomeHandleImpl;
import org.jboss.system.Registry;
import org.jboss.util.NestedRuntimeException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * As we remove the one one association between container STACK and invoker we
 * keep this around. IN the future the creation of proxies is a task done on a
 * container basis but the container as a logical representation. In other
 * words, the container "Entity with RMI/IIOP" is not a container stack but
 * an association at the invocation level that points to all metadata for
 * a given container.
 * <p/>
 * In other words this is here for legacy reason and to not disrupt the
 * container at once.
 * In particular we declare that we "implement" the container invoker
 * interface when we are just implementing the Proxy generation calls.
 * Separation of concern.
 * <p/>
 * todo eliminate this class, at least in its present form.
 *
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark/a>
 * @author <a href="mailto:thomas.diesler@jboss.org">Thomas Diesler/a>
 * @version $Revision: 81030 $
 */
public class ProxyFactory
   implements EJBProxyFactory
{
   protected static final String HOME_INTERCEPTOR = "home";
   protected static final String BEAN_INTERCEPTOR = "bean";
   protected static final String LIST_ENTITY_INTERCEPTOR = "list-entity";

   protected static Logger log = Logger.getLogger(ProxyFactory.class);

   // Metadata for the proxies
   public EJBMetaData ejbMetaData;

   // as of EJB2.1, we may have the case of web-service enabled beans without
   // remote interface, we will simply "mute" this factory in this case
   protected boolean isServiceEndpointOnly;

   protected EJBHome home;
   protected EJBObject statelessObject;

   // The name of the bean being deployed
   protected String jndiBinding;
   protected ObjectName jmxName;
   protected int jmxNameHash;
   private Integer jmxNameHashInteger;

   // The name of the delegate invoker
   // We have a beanInvoker and homeInvoker
   // because clustering has a different invoker for each
   // and we want to reuse code here.
   protected Invoker beanInvoker;
   protected Invoker homeInvoker;
   protected InvokerProxyBindingMetaData invokerMetaData;

   /**
    * The proxy-config/client-interceptors/home stack
    */
   protected ArrayList homeInterceptorClasses = new ArrayList();
   /**
    * The proxy-config/client-interceptors/bean stack
    */
   protected ArrayList beanInterceptorClasses = new ArrayList();
   /**
    * The proxy-config/client-interceptors/entity-list stack
    */
   protected ArrayList listEntityInterceptorClasses = new ArrayList();
   /** A flag indicating if the IClientContainer interface should be added */
   protected boolean includeIClientIface;
   // A pointer to the container this proxy factory is dedicated to
   protected Container container;

   protected Constructor proxyClassConstructor;


   // Container plugin implementation -----------------------------------------

   public void setContainer(Container con)
   {
      this.container = con;
   }

   public void setInvokerMetaData(InvokerProxyBindingMetaData metadata)
   {
      this.invokerMetaData = metadata;
   }

   public void setInvokerBinding(String binding)
   {
      this.jndiBinding = binding;
   }

   public void create() throws Exception
   {
      jmxName = container.getJmxName();
      jmxNameHash = jmxName.hashCode();
      jmxNameHashInteger = new Integer(jmxNameHash);
      // Create metadata

      BeanMetaData bmd = container.getBeanMetaData();
      boolean isSession = !(bmd instanceof EntityMetaData);
      boolean isStatelessSession = false;
      if(isSession)
      {
         SessionMetaData smd = (SessionMetaData) bmd;
         if(bmd.getRemote() == null)
         {
            isServiceEndpointOnly = true;
            // nothing more to do
            return;
         }
         isStatelessSession = smd.isStateless();
      }
      Class pkClass = null;
      if(!isSession)
      {
         EntityMetaData metaData = (EntityMetaData) bmd;
         String pkClassName = metaData.getPrimaryKeyClass();
         try
         {
            if(pkClassName != null)
            {
               pkClass = container.getClassLoader().loadClass(pkClassName);
            }
            else
            {
               pkClass
                  = container.getClassLoader()
                  .loadClass(metaData.getEjbClass())
                  .getField(metaData.getPrimKeyField())
                  .getClass();
            }
         }
         catch(NoSuchFieldException e)
         {
            log.error(
               "Unable to identify Bean's Primary Key class!"
               + " Did you specify a primary key class and/or field?  Does that field exist?"
            );
            throw new RuntimeException("Primary Key Problem");
         }
         catch(NullPointerException e)
         {
            log.error(
               "Unable to identify Bean's Primary Key class!"
               + " Did you specify a primary key class and/or field?  Does that field exist?"
            );
            throw new RuntimeException("Primary Key Problem");
         }
      }

      ejbMetaData = new EJBMetaDataImpl(
         ((EJBProxyFactoryContainer) container).getRemoteClass(),
         ((EJBProxyFactoryContainer) container).getHomeClass(),
         pkClass, //null if not entity
         isSession, //Session
         isStatelessSession, //Stateless
         new HomeHandleImpl(jndiBinding)
      );
      log.debug("Proxy Factory for " + jndiBinding + " initialized");

      initInterceptorClasses();
   }

   /**
    * Become fully available. At this point our invokers should be started
    * and we can bind the homes into JNDI.
    */
   public void start() throws Exception
   {
      if(!isServiceEndpointOnly)
      {
         setupInvokers();
         bindProxy();
      }
   }

   /**
    * Lookup the invokers in the object registry. This typically cannot
    * be done until our start method as the invokers may need to be started
    * themselves.
    */
   protected void setupInvokers() throws Exception
   {
      ObjectName oname = new ObjectName(invokerMetaData.getInvokerMBean());
      Invoker invoker = (Invoker) Registry.lookup(oname);
      if(invoker == null)
      {
         throw new RuntimeException("invoker is null: " + oname);
      }

      homeInvoker = beanInvoker = invoker;
   }


   /**
    * Load the client interceptor classes
    */
   protected void initInterceptorClasses() throws Exception
   {
      HashMap interceptors = new HashMap();

      Element proxyConfig = invokerMetaData.getProxyFactoryConfig();
      Element clientInterceptors = MetaData.getOptionalChild(
         proxyConfig,
         "client-interceptors", null
      );
      if(clientInterceptors != null)
      {
         String value = MetaData.getElementAttribute(clientInterceptors, "exposeContainer");
         this.includeIClientIface = Boolean.valueOf(value).booleanValue();
         NodeList children = clientInterceptors.getChildNodes();
         for(int i = 0; i < children.getLength(); i++)
         {
            Node currentChild = children.item(i);
            if(currentChild.getNodeType() == Node.ELEMENT_NODE)
            {
               Element interceptor = (Element) children.item(i);
               interceptors.put(interceptor.getTagName(), interceptor);
            }
         }
      }
      else
      {
         log.debug("client interceptors element is null");
      }
      Element homeInterceptorConf = (Element) interceptors.get(HOME_INTERCEPTOR);
      loadInterceptorClasses(homeInterceptorClasses, homeInterceptorConf);
      if(homeInterceptorClasses.size() == 0)
      {
         throw new DeploymentException("There are no home interface interceptors configured");
      }

      Element beanInterceptorConf = (Element) interceptors.get(BEAN_INTERCEPTOR);
      loadInterceptorClasses(beanInterceptorClasses, beanInterceptorConf);
      if(beanInterceptorClasses.size() == 0)
      {
         throw new DeploymentException("There are no bean interface interceptors configured");
      }

      Element listEntityInterceptorConf = (Element) interceptors.get(LIST_ENTITY_INTERCEPTOR);
      loadInterceptorClasses(listEntityInterceptorClasses, listEntityInterceptorConf);
   }

   /**
    * The <code>loadInterceptorClasses</code> load an interceptor classes from
    * configuration
    *
    * @throws Exception if an error occurs
    */
   protected void loadInterceptorClasses(ArrayList classes, Element interceptors)
      throws Exception
   {
      Iterator interceptorElements = MetaData.getChildrenByTagName(interceptors, "interceptor");
      ClassLoader loader = container.getClassLoader();
      while(interceptorElements != null && interceptorElements.hasNext())
      {
         Element ielement = (Element) interceptorElements.next();
         String className = null;
         className = MetaData.getElementContent(ielement);

         // load the invoker interceptor that corresponds to the beans call semantic
         String byValueAttr = MetaData.getElementAttribute(ielement, "call-by-value");
         if(byValueAttr != null)
         {
            if (container.isCallByValue() == new Boolean(byValueAttr).booleanValue())
            {
               Class clazz = loader.loadClass(className);
               classes.add(clazz);
            }
         }
         else
         {
            Class clazz = loader.loadClass(className);
            classes.add(clazz);
         }
      }
   }

   /**
    * The <code>loadInterceptorChain</code> create instances of interceptor
    * classes previously loaded in loadInterceptorClasses
    *
    * @throws Exception if an error occurs
    */
   protected void loadInterceptorChain(ArrayList chain, ClientContainer client)
      throws Exception
   {
      Interceptor last = null;
      for(int i = 0; i < chain.size(); i++)
      {
         Class clazz = (Class) chain.get(i);
         Interceptor interceptor = (Interceptor) clazz.newInstance();
         if(last == null)
         {
            last = interceptor;
            client.setNext(interceptor);
         }
         else
         {
            last.setNext(interceptor);
            last = interceptor;
         }
      }
   }

   /**
    * The <code>bindProxy</code> method creates the home proxy and binds
    * the home into jndi. It also creates the InvocationContext and client
    * container and interceptor chain.
    *
    * @throws Exception if an error occurs
    */
   protected void bindProxy() throws Exception
   {
      try
      {
         // Create a stack from the description (in the future) for now we hardcode it
         InvocationContext context = new InvocationContext();

         context.setObjectName(jmxNameHashInteger);
         context.setValue(InvocationKey.JNDI_NAME, jndiBinding);
         // The behavior for home proxying should be isolated in an interceptor FIXME
         context.setInvoker(homeInvoker);
         context.setValue(InvocationKey.EJB_METADATA, ejbMetaData);
         context.setInvokerProxyBinding(invokerMetaData.getName());
         
         if(container.getSecurityManager() != null)
         {
            String secDomain = container.getSecurityManager().getSecurityDomain();
            context.setValue(InvocationKey.SECURITY_DOMAIN, secDomain); 
         }
         
         ClientContainer client = null;
         EJBProxyFactoryContainer pfc = (EJBProxyFactoryContainer) container;
         Class[] ifaces = {pfc.getHomeClass(), Class.forName("javax.ejb.Handle")};
         if( includeIClientIface )
         {
            ifaces = new Class[] {IClientContainer.class, pfc.getHomeClass(),
                           Class.forName("javax.ejb.Handle")};
            client = new ClientContainerEx(context);
         }
         else
         {
            client = new ClientContainer(context);
         }
         loadInterceptorChain(homeInterceptorClasses, client);

         // Create the EJBHome
         this.home = (EJBHome) Proxy.newProxyInstance(
               // Class loader pointing to the right classes from deployment
               pfc.getHomeClass().getClassLoader(),
               // The classes we want to implement home and handle
               ifaces,
               // The home proxy as invocation handler
               client);

         // Create stateless session object
         // Same instance is used for all objects
         if(ejbMetaData.isStatelessSession() == true)
         {
            // Create a stack from the description (in the future) for now we hardcode it
            context = new InvocationContext();

            context.setObjectName(jmxNameHashInteger);
            context.setValue(InvocationKey.JNDI_NAME, jndiBinding);
            // The behavior for home proxying should be isolated in an interceptor FIXME
            context.setInvoker(beanInvoker);
            context.setInvokerProxyBinding(invokerMetaData.getName());
            context.setValue(InvocationKey.EJB_HOME, home);
            
            if(container.getSecurityManager() != null)
            {
               String secDomain = container.getSecurityManager().getSecurityDomain();
               context.setValue(InvocationKey.SECURITY_DOMAIN, secDomain); 
            }

            Class[] ssifaces = {pfc.getRemoteClass()};
            if( includeIClientIface )
            {
               ssifaces = new Class[] {IClientContainer.class, pfc.getRemoteClass()};               
               client = new ClientContainerEx(context);               
            }
            else
            {
               client = new ClientContainer(context);
            }
            loadInterceptorChain(beanInterceptorClasses, client);

            this.statelessObject = 
               (EJBObject)Proxy.newProxyInstance(
                  // Correct CL
                  pfc.getRemoteClass().getClassLoader(),
                  // Interfaces
                  ssifaces,
                  // SLSB proxy as invocation handler
                  client
               );
         }
         else
         {
            // this is faster than newProxyInstance
            Class[] intfs = {pfc.getRemoteClass()};
            if( this.includeIClientIface )
            {
               intfs = new Class[]{IClientContainer.class, pfc.getRemoteClass()};
            }
            Class proxyClass = Proxy.getProxyClass(pfc.getRemoteClass().getClassLoader(), intfs);
            final Class[] constructorParams = {InvocationHandler.class};
            proxyClassConstructor = proxyClass.getConstructor(constructorParams);
         }


         // Bind the home in the JNDI naming space
         rebindHomeProxy();
      }
      catch(Exception e)
      {
         throw new ServerException("Could not bind home", e);
      }
   }

   protected void rebindHomeProxy() throws NamingException
   {
      // (Re-)Bind the home in the JNDI naming space
      log.debug("(re-)Binding Home " + jndiBinding);
      Util.rebind(
         // The context
         new InitialContext(),
         // Jndi name
         jndiBinding,
         // The Home
         getEJBHome()
      );

      log.info("Bound EJB Home '" + container.getBeanMetaData().getEjbName() + "' to jndi '" + jndiBinding + "'");
   }

   public void stop()
   {
   }

   public void destroy()
   {
      if(!isServiceEndpointOnly)
      {
         log.info("Unbind EJB Home '" + container.getBeanMetaData().getEjbName() + "' from jndi '" + jndiBinding + "'");

         try
         {
            InitialContext ctx = new InitialContext();
            ctx.unbind(jndiBinding);
         }
         catch(Exception e)
         {
            // ignore.
         }
         homeInterceptorClasses.clear();
         beanInterceptorClasses.clear();
         listEntityInterceptorClasses.clear();
      }

      ejbMetaData = null;
      home = null;
      statelessObject = null;
      beanInvoker = null;
      homeInvoker = null;
      proxyClassConstructor = null;
   }

   // EJBProxyFactory implementation -------------------------------------

   public boolean isIdentical(Container container, Invocation mi)
   {
      throw new UnsupportedOperationException("TODO provide a default implementation");
   }

   public EJBMetaData getEJBMetaData()
   {
      return ejbMetaData;
   }

   public Object getEJBHome()
   {
      return home;
   }

   /**
    * Return the EJBObject proxy for stateless sessions.
    */
   public Object getStatelessSessionEJBObject()
   {

      return statelessObject;
   }

   /**
    * Create an EJBObject proxy for a stateful session given its session id.
    */
   public Object getStatefulSessionEJBObject(Object id)
   {
      // Create a stack from the description (in the future) for now we hardcode it
      InvocationContext context = new InvocationContext();

      context.setObjectName(jmxNameHashInteger);
      context.setCacheId(id);
      context.setValue(InvocationKey.JNDI_NAME, jndiBinding);
      context.setInvoker(beanInvoker);
      log.debug("seting invoker proxy binding for stateful session: " + invokerMetaData.getName());
      context.setInvokerProxyBinding(invokerMetaData.getName());
      context.setValue(InvocationKey.EJB_HOME, home);
      context.setValue("InvokerID", Invoker.ID);

      if(container.getSecurityManager() != null)
      {
         String secDomain = container.getSecurityManager().getSecurityDomain();
         context.setValue(InvocationKey.SECURITY_DOMAIN, secDomain); 
      }
      
      ClientContainer client;
      if( includeIClientIface )
      {
         client = new ClientContainerEx(context);
      }
      else
      {
         client = new ClientContainer(context);
      }

      try
      {
         loadInterceptorChain(beanInterceptorClasses, client);
      }
      catch(Exception e)
      {
         throw new NestedRuntimeException("Failed to load interceptor chain", e);
      }

      try
      {
         return (EJBObject) proxyClassConstructor.newInstance(new Object[]{client});
      }
      catch(Exception ex)
      {
         throw new NestedRuntimeException(ex);
      }

   }

   /**
    * Create an EJBObject proxy for an entity given its primary key.
    */
   public Object getEntityEJBObject(Object id)
   {
      Object result;
      if(id == null)
      {
         result = null;
      }
      else
      {
         // Create a stack from the description (in the future) for now we hardcode it
         InvocationContext context = new InvocationContext();

         context.setObjectName(jmxNameHashInteger);
         context.setCacheId(id);
         context.setValue(InvocationKey.JNDI_NAME, jndiBinding);
         context.setInvoker(beanInvoker);
         context.setInvokerProxyBinding(invokerMetaData.getName());
         context.setValue(InvocationKey.EJB_HOME, home);

         if(container.getSecurityManager() != null)
         {
            String secDomain = container.getSecurityManager().getSecurityDomain();
            context.setValue(InvocationKey.SECURITY_DOMAIN, secDomain); 
         }
         
         ClientContainer client;
         if( includeIClientIface )
         {
            client = new ClientContainerEx(context);
         }
         else
         {
            client = new ClientContainer(context);
         }

         try
         {
            loadInterceptorChain(beanInterceptorClasses, client);
         }
         catch(Exception e)
         {
            throw new NestedRuntimeException("Failed to load interceptor chain", e);
         }

         try
         {
            result = proxyClassConstructor.newInstance(new Object[]{client});
         }
         catch(Exception ex)
         {
            throw new NestedRuntimeException(ex);
         }
      }
      return result;
   }

   /**
    * Create a Collection EJBObject proxies for an entity given its primary keys.
    */
   public Collection getEntityCollection(Collection ids)
   {
      ArrayList list = new ArrayList(ids.size());
      Iterator idEnum = ids.iterator();

      while(idEnum.hasNext())
      {
         Object nextId = idEnum.next();
         list.add(getEntityEJBObject(nextId));
      }
      return list;
   }
}
