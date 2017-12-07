/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.ejb3.client;

import org.jboss.ejb3.Container;
import org.jboss.ejb3.DependencyPolicy;
import org.jboss.ejb3.InitialContextFactory;
import org.jboss.ejb3.client.injection.ClientPersistenceUnitHandler;
import org.jboss.ejb3.vfs.spi.VirtualFile;
import org.jboss.injection.*;
import org.jboss.logging.Logger;
import org.jboss.metadata.client.jboss.JBossClientMetaData;
import org.jboss.metadata.javaee.spec.LifecycleCallbacksMetaData;
import org.jboss.metadata.javaee.spec.RemoteEnvironment;
import org.jboss.util.NotImplementedException;

import javax.naming.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.URL;
import java.util.*;

/**
 * Injection of the application client main class is handled from here.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 104907 $
 */
public class ClientContainer implements InjectionContainer
{
   private static final Logger log = Logger.getLogger(ClientContainer.class);
   private static final String VERSION = "$Revision: 104907 $";
   private static ThreadLocal<Properties> clientJndiEnv = new ThreadLocal<Properties>();

   private Class<?> mainClass;
   private JBossClientMetaData xml;
   private String applicationClientName;
   
   // for performance there is an array.
   private List<Injector> injectors = new ArrayList<Injector>();
   private Context enc;
   private DependencyPolicy dependsPolicy;

   private List<Method> postConstructs = new ArrayList<Method>();

   public static Properties getJndiEnv()
   {
      return clientJndiEnv.get();
   }

   public ClientContainer(JBossClientMetaData xml, Class<?> mainClass, String applicationClientName)
      throws Exception
   {
      this(xml, mainClass, applicationClientName, null);
   }
   public ClientContainer(JBossClientMetaData xml, Class<?> mainClass, String applicationClientName, Properties jndiEnv)
      throws Exception
   {
      log.info("ClientContainer(version="+VERSION+")");
      log.info("DependencyPolicy.CS: "+DependencyPolicy.class.getProtectionDomain().getCodeSource());
      log.info("ClientContainer.CS: "+getClass().getProtectionDomain().getCodeSource());
      ClassLoader mainClassLoader = mainClass.getClassLoader();
      log.info("mainClass.ClassLoader: "+mainClassLoader);
      clientJndiEnv.set(jndiEnv);
      this.xml = xml;
      this.mainClass = mainClass;
      this.applicationClientName = applicationClientName;
      ClientJavaEEComponent client = new ClientJavaEEComponent(applicationClientName);
      this.dependsPolicy = new NoopDependencyPolicy(client);

      URL jndiPropertiesURL = mainClassLoader.getResource("jndi.properties");
      log.info("mainClassLoader jndi.properties: "+jndiPropertiesURL);
      Context ctx = InitialContextFactory.getInitialContext(jndiEnv);
      enc = (Context) ctx.lookup(applicationClientName);
      StringBuffer encInfo = new StringBuffer("Client ENC("+applicationClientName+"):\n");
      list(enc, "", encInfo, true);
      log.info(encInfo.toString());

      //encEnv = (Context) enc.lookup("env");
//      enc = ThreadLocalENCFactory.create(ctx);
//      encEnv = Util.createSubcontext(enc, "env");
      
      processMetadata(null);

      for(Injector injector : injectors)
      {
         log.debug("injector: " + injector);
         injector.inject((Object) null);
      }
      
      postConstruct();
   }
   
   /* (non-Javadoc)
    * @see org.jboss.injection.InjectionContainer#getAnnotation(java.lang.Class, java.lang.Class)
    */
   public <T extends Annotation> T getAnnotation(Class<T> annotationClass, Class<?> clazz)
   {
      return clazz.getAnnotation(annotationClass);
   }
   
   /* (non-Javadoc)
    * @see org.jboss.injection.InjectionContainer#getAnnotation(java.lang.Class, java.lang.Class, java.lang.reflect.Method)
    */
   public <T extends Annotation> T getAnnotation(Class<T> annotationClass, Class<?> clazz, Method method)
   {
      return method.getAnnotation(annotationClass);
   }

   /* (non-Javadoc)
    * @see org.jboss.injection.InjectionContainer#getAnnotation(java.lang.Class, java.lang.reflect.Method)
    */
   public <T extends Annotation> T getAnnotation(Class<T> annotationClass, Method method)
   {
      return method.getAnnotation(annotationClass);
   }

   /* (non-Javadoc)
    * @see org.jboss.injection.InjectionContainer#getAnnotation(java.lang.Class, java.lang.Class, java.lang.reflect.Field)
    */
   public <T extends Annotation> T getAnnotation(Class<T> annotationClass, Class<?> clazz, Field field)
   {
      return field.getAnnotation(annotationClass);
   }

   /* (non-Javadoc)
    * @see org.jboss.injection.InjectionContainer#getAnnotation(java.lang.Class, java.lang.reflect.Field)
    */
   public <T extends Annotation> T getAnnotation(Class<T> annotationClass, Field field)
   {
      return field.getAnnotation(annotationClass);
   }

   /* (non-Javadoc)
    * @see org.jboss.injection.InjectionContainer#getClassloader()
    */
   public ClassLoader getClassloader()
   {
      //throw new RuntimeException("NYI");
      return Thread.currentThread().getContextClassLoader();
   }

   /* (non-Javadoc)
    * @see org.jboss.injection.InjectionContainer#getDependencyPolicy()
    */
   public DependencyPolicy getDependencyPolicy()
   {
      return dependsPolicy;
   }

   /* (non-Javadoc)
    * @see org.jboss.injection.InjectionContainer#getDeploymentDescriptorType()
    */
   public String getDeploymentDescriptorType()
   {
      return "application-client.xml";
   }

   /* (non-Javadoc)
    * @see org.jboss.injection.InjectionContainer#getEjbJndiName(java.lang.Class)
    */
   public String getEjbJndiName(Class businessInterface) throws NameNotFoundException
   {
      throw new RuntimeException("NYI");
      //return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.injection.InjectionContainer#getEjbJndiName(java.lang.String, java.lang.Class)
    */
   public String getEjbJndiName(String link, Class<?> businessInterface)
   {
      throw new NotImplementedException();
      //return "java:comp/env/" + link + "/remote";
      //return applicationClientName + "/" + link + "/remote";
      //return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.injection.InjectionContainer#getEnc()
    */
   public Context getEnc()
   {
      return enc;
   }

   /* (non-Javadoc)
    * @see org.jboss.injection.InjectionContainer#getEncInjections()
    */
   public Map<String, Map<AccessibleObject, Injector>> getEncInjections()
   {
      throw new IllegalStateException("ENC setup happens on the server");
   }

   /* (non-Javadoc)
    * @see org.jboss.injection.InjectionContainer#getEncInjectors()
    */
   public Map<String, EncInjector> getEncInjectors()
   {
      throw new IllegalStateException("ENC setup happens on the server");
   }

   /* (non-Javadoc)
    * @see org.jboss.injection.InjectionContainer#getEnvironmentRefGroup()
    */
   public RemoteEnvironment getEnvironmentRefGroup()
   {
      return xml;
   }

   /* (non-Javadoc)
    * @see org.jboss.injection.InjectionContainer#getIdentifier()
    */
   public String getIdentifier()
   {
//      throw new NotImplementedException;
      // FIXME: return the real identifier
      //return "client-identifier";
      return applicationClientName;
   }

   /* (non-Javadoc)
    * @see org.jboss.injection.InjectionContainer#getInjectors()
    */
   public List<Injector> getInjectors()
   {
      return injectors;
   }

   public Class<?> getMainClass()
   {
      return mainClass;
   }
   
   public boolean hasJNDIBinding(String jndiName)
   {
      return false;
   }
   
   public void invokeMain(String args[]) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
   {
      Class<?> parameterTypes[] = { args.getClass() };
      Method method = mainClass.getDeclaredMethod("main", parameterTypes);
      try
      {
         log.info("Invoking main: "+method);
         method.invoke(null, (Object) args);
         log.info("Successfully invoked main");
      }
      catch(Throwable e)
      {
         e.printStackTrace();
         log.error("Invocation of client main failed", e);
      }
   }
 
   /**
    * Call post construct methods.
    * @throws IllegalAccessException  
    * @throws InstantiationException 
    * @throws InvocationTargetException 
    * @throws IllegalArgumentException 
    *
    */
   private void postConstruct() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
   {
      log.info("postConstructs = " + postConstructs);
      for(Method method : postConstructs)
      {
         method.setAccessible(true);
         Object instance;
         if(Modifier.isStatic(method.getModifiers()))
            instance = null;
         else
            instance = method.getDeclaringClass().newInstance();
         Object args[] = null;
         method.invoke(instance, args);
      }
   }
   
   private void processMetadata(DependencyPolicy dependencyPolicy) throws Exception
   {
      log.debug("processMetadata");
      processPostConstructs();
      
      // TODO: check which handlers a client container should support
      Collection<InjectionHandler<RemoteEnvironment>> handlers = new ArrayList<InjectionHandler<RemoteEnvironment>>();
      handlers.add(new ClientEJBHandler<RemoteEnvironment>());
      // This currently has no use in the ClientContainer, maybe in the future when running an mc
      handlers.add(new DependsHandler<RemoteEnvironment>());
      //handlers.add(new JndiInjectHandler<RemoteEnvironment>());
      handlers.add(new ClientPersistenceUnitHandler<RemoteEnvironment>());
      handlers.add(new ClientResourceHandler<RemoteEnvironment>(this.mainClass));
      handlers.add(new WebServiceRefHandler<RemoteEnvironment>());

      // TODO: we're going to use a jar class loader
//      ClassLoader old = Thread.currentThread().getContextClassLoader();
//      Thread.currentThread().setContextClassLoader(classloader);
      try
      {
         // EJB container's XML must be processed before interceptor's as it may override interceptor's references
         for (InjectionHandler<RemoteEnvironment> handler : handlers)
            handler.loadXml(xml, this);

         /*
         Map<AccessibleObject, Injector> tmp = InjectionUtil.processAnnotations(this, handlers, getMainClass());
         injectors.addAll(tmp.values());
         */
      }
      finally
      {
//         Thread.currentThread().setContextClassLoader(old);
      }
   }
   
   /**
    * Populate the list of the post construct callbacks ordered according to the spec rules defined in
    * 12.4.1 Multiple Callback Interceptor Methods for a Life Cycle Callback Event.
    * 
    * @throws ClassNotFoundException 
    * @throws NoSuchMethodException 
    * @throws SecurityException 
    *
    */
   private void processPostConstructs()
      throws ClassNotFoundException, SecurityException, NoSuchMethodException
   {
      LifecycleCallbacksMetaData callbacks = xml.getPostConstructs();
      if(callbacks == null || callbacks.isEmpty())
         return;

      List<Method> methods = callbacks.getOrderedCallbacks(mainClass);
      postConstructs.addAll(methods);
   }

   /* (non-Javadoc)
    * @see org.jboss.injection.InjectionContainer#resolveEjbContainer(java.lang.String, java.lang.Class)
    */
   public Container resolveEjbContainer(String link, Class businessIntf)
   {
      log.warn("resolveEjbContainer(" + link + ", " + businessIntf + ") not implemented");
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.injection.InjectionContainer#resolveEjbContainer(java.lang.Class)
    */
   public Container resolveEjbContainer(Class businessIntf) throws NameNotFoundException
   {
      return null;
   }

   public String resolveMessageDestination(String link)
   {
      // Resolving something here is a nop
      return null;
   }
   
   public VirtualFile getRootFile()
   {
      throw new NotImplementedException();
   }

   /**
    * Recursively display the naming context information into the buffer.
    * 
    * @param ctx
    * @param indent
    * @param buffer
    * @param verbose
    */
   private static void list(Context ctx, String indent, StringBuffer buffer,
         boolean verbose)
   {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      try
      {
         NamingEnumeration ne = ctx.list("");
         while (ne.hasMore())
         {
            NameClassPair pair = (NameClassPair) ne.next();

            String name = pair.getName();
            String className = pair.getClassName();
            boolean recursive = false;
            boolean isLinkRef = false;
            boolean isProxy = false;
            Class c = null;
            try
            {
               c = loader.loadClass(className);

               if (Context.class.isAssignableFrom(c))
                  recursive = true;
               if (LinkRef.class.isAssignableFrom(c))
                  isLinkRef = true;

               isProxy = Proxy.isProxyClass(c);
            }
            catch (ClassNotFoundException cnfe)
            {
               // If this is a $Proxy* class its a proxy
               if (className.startsWith("$Proxy"))
               {
                  isProxy = true;
                  // We have to get the class from the binding
                  try
                  {
                     Object p = ctx.lookup(name);
                     c = p.getClass();
                  }
                  catch (NamingException e)
                  {
                     Throwable t = e.getRootCause();
                     if (t instanceof ClassNotFoundException)
                     {
                        // Get the class name from the exception msg
                        String msg = t.getMessage();
                        if (msg != null)
                        {
                           // Reset the class name to the CNFE class
                           className = msg;
                        }
                     }
                  }
               }
            }

            buffer.append(indent + " +- " + name);

            // Display reference targets
            if (isLinkRef)
            {
               // Get the 
               try
               {
                  Object obj = ctx.lookupLink(name);

                  LinkRef link = (LinkRef) obj;
                  buffer.append("[link -> ");
                  buffer.append(link.getLinkName());
                  buffer.append(']');
               }
               catch (Throwable t)
               {
                  buffer.append("invalid]");
               }
            }

            // Display proxy interfaces
            if (isProxy)
            {
               buffer.append(" (proxy: " + pair.getClassName());
               if (c != null)
               {
                  Class[] ifaces = c.getInterfaces();
                  buffer.append(" implements ");
                  for (int i = 0; i < ifaces.length; i++)
                  {
                     buffer.append(ifaces[i]);
                     buffer.append(',');
                  }
                  buffer.setCharAt(buffer.length() - 1, ')');
               }
               else
               {
                  buffer.append(" implements " + className + ")");
               }
            }
            else if (verbose)
            {
               buffer.append(" (class: " + pair.getClassName() + ")");
            }

            buffer.append('\n');
            if (recursive)
            {
               try
               {
                  Object value = ctx.lookup(name);
                  if (value instanceof Context)
                  {
                     Context subctx = (Context) value;
                     list(subctx, indent + " |  ", buffer, verbose);
                  }
                  else
                  {
                     buffer.append(indent + " |   NonContext: " + value);
                     buffer.append('\n');
                  }
               }
               catch (Throwable t)
               {
                  buffer.append("Failed to lookup: " + name + ", errmsg=" + t.getMessage());
                  buffer.append('\n');
               }
            }
         }
         ne.close();
      }
      catch (NamingException ne)
      {
         buffer.append("error while listing context " + ctx.toString() + ": " + ne.toString(true));
         formatException(buffer, ne);
      }
   }
   private static void formatException(StringBuffer buffer, Throwable t)
   {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      buffer.append("<pre>\n");
      t.printStackTrace(pw);
      buffer.append(sw.toString());
      buffer.append("</pre>\n");
   }
}
