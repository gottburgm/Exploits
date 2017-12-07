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
package org.jboss.ejb.plugins.local;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Constructor;
import java.rmi.AccessException;
import java.rmi.NoSuchObjectException;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.ejb.AccessLocalException;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.TransactionRequiredLocalException;
import javax.ejb.TransactionRolledbackLocalException;
import javax.naming.Context;
import javax.naming.InitialContext; 
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionRequiredException;
import javax.transaction.TransactionRolledbackException;

import org.jboss.ejb.Container;
import org.jboss.ejb.EJBProxyFactoryContainer;
import org.jboss.ejb.LocalProxyFactory; 
import org.jboss.invocation.InvocationType;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.invocation.LocalEJBInvocation;
import org.jboss.logging.Logger;
import org.jboss.metadata.BeanMetaData;
import org.jboss.naming.Util;   
import org.jboss.security.SecurityContext;  
import org.jboss.security.SecurityContextAssociation; 
import org.jboss.util.NestedRuntimeException;
import org.jboss.tm.TransactionLocal; 

/**
 * The LocalProxyFactory implementation that handles local ejb interface
 * proxies.
 *
 * @author <a href="mailto:docodan@mvcsoft.com">Daniel OConnor</a>
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @author Anil.Saldhana@redhat.com
 * $Revision: 81030 $
 */
public class BaseLocalProxyFactory implements LocalProxyFactory
{
   // Attributes ----------------------------------------------------
   protected static Logger log = Logger.getLogger(BaseLocalProxyFactory.class);

   /**
    * A map of the BaseLocalProxyFactory instances keyed by localJndiName
    */
   protected static Map invokerMap = Collections.synchronizedMap(new HashMap());

   protected Container container;

   /**
    * The JNDI name of the local home interface binding
    */
   protected String localJndiName;

   protected TransactionManager transactionManager;

   // The home can be one.
   protected EJBLocalHome home;

   // The Stateless Object can be one.
   protected EJBLocalObject statelessObject;

   protected Map beanMethodInvokerMap;
   protected Map homeMethodInvokerMap;
   protected Class localHomeClass;
   protected Class localClass;

   protected Constructor proxyClassConstructor;

   private final TransactionLocal cache = new TransactionLocal()
   {
      protected Object initialValue()
      {
         return new HashMap();
      }
   };

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   // ContainerService implementation -------------------------------

   public void setContainer(Container con)
   {
      this.container = con;
   }

   public void create() throws Exception
   {
      BeanMetaData metaData = container.getBeanMetaData();
      localJndiName = metaData.getLocalJndiName();
   }

   public void start()
      throws Exception
   {
      BeanMetaData metaData = container.getBeanMetaData();
      EJBProxyFactoryContainer invokerContainer =
         (EJBProxyFactoryContainer) container;
      localHomeClass = invokerContainer.getLocalHomeClass();
      localClass = invokerContainer.getLocalClass();
      if(localHomeClass == null || localClass == null)
      {
         log.debug(metaData.getEjbName()
            +
            " cannot be Bound, doesn't " +
            "have local and local home interfaces");
         return;
      }

      // this is faster than newProxyInstance
      Class[] intfs = {localClass};
      Class proxyClass = Proxy.getProxyClass(ClassLoaderAction.UTIL.get(localClass), intfs);
      final Class[] constructorParams =
         {InvocationHandler.class};

      proxyClassConstructor = proxyClass.getConstructor(constructorParams);

      Context iniCtx = new InitialContext();
      String beanName = metaData.getEjbName();

      // Set the transaction manager and transaction propagation
      // context factory of the GenericProxy class
      transactionManager =
         (TransactionManager) iniCtx.lookup("java:/TransactionManager");

      // Create method mappings for container invoker
      Method[] methods = localClass.getMethods();
      beanMethodInvokerMap = new HashMap();
      for(int i = 0; i < methods.length; i++)
      {
         long hash = MarshalledInvocation.calculateHash(methods[i]);
         beanMethodInvokerMap.put(new Long(hash), methods[i]);
      }

      methods = localHomeClass.getMethods();
      homeMethodInvokerMap = new HashMap();
      for(int i = 0; i < methods.length; i++)
      {
         long hash = MarshalledInvocation.calculateHash(methods[i]);
         homeMethodInvokerMap.put(new Long(hash), methods[i]);
      }

      // bind that referance to my name
      Util.rebind(iniCtx, localJndiName, getEJBLocalHome());
      invokerMap.put(localJndiName, this);
      log.info("Bound EJB LocalHome '" + beanName + "' to jndi '" + localJndiName + "'");
   }

   public void stop()
   {
      // Clean up the home proxy binding
      try
      {
         if(invokerMap.remove(localJndiName) == this)
         {
            log.info("Unbind EJB LocalHome '" + container.getBeanMetaData().getEjbName() + "' from jndi '" + localJndiName + "'");

            InitialContext ctx = new InitialContext();
            ctx.unbind(localJndiName);
         }
      }
      catch(Exception ignore)
      {
      }
   }

   public void destroy()
   {
      if(beanMethodInvokerMap != null)
      {
         beanMethodInvokerMap.clear();
      }
      if(homeMethodInvokerMap != null)
      {
         homeMethodInvokerMap.clear();
      }
      MarshalledInvocation.removeHashes(localHomeClass);
      MarshalledInvocation.removeHashes(localClass);

      container = null;
   }

   public Constructor getProxyClassConstructor()
   {
      if(proxyClassConstructor == null)
      {
      }
      return proxyClassConstructor;
   }

   // EJBProxyFactory implementation -------------------------------
   public EJBLocalHome getEJBLocalHome()
   {
      if(home == null)
      {
         EJBProxyFactoryContainer cic = (EJBProxyFactoryContainer) container;
         InvocationHandler handler = new LocalHomeProxy(localJndiName, this);
         ClassLoader loader = ClassLoaderAction.UTIL.get(cic.getLocalHomeClass());
         Class[] interfaces = {cic.getLocalHomeClass()};

         home = (EJBLocalHome) Proxy.newProxyInstance(loader,
            interfaces,
            handler);
      }
      return home;
   }

   public EJBLocalObject getStatelessSessionEJBLocalObject()
   {
      if(statelessObject == null)
      {
         EJBProxyFactoryContainer cic = (EJBProxyFactoryContainer) container;
         InvocationHandler handler =
            new StatelessSessionProxy(localJndiName, this);
         ClassLoader loader = ClassLoaderAction.UTIL.get(cic.getLocalClass());
         Class[] interfaces = {cic.getLocalClass()};

         statelessObject = (EJBLocalObject) Proxy.newProxyInstance(loader,
            interfaces,
            handler);
      }
      return statelessObject;
   }

   public EJBLocalObject getStatefulSessionEJBLocalObject(Object id)
   {
      InvocationHandler handler =
         new StatefulSessionProxy(localJndiName, id, this);
      try
      {
         return (EJBLocalObject) proxyClassConstructor.newInstance(new Object[]{handler});
      }
      catch(Exception ex)
      {
         throw new NestedRuntimeException(ex);
      }
   }

   public Object getEntityEJBObject(Object id)
   {
      return getEntityEJBLocalObject(id);
   }

   public EJBLocalObject getEntityEJBLocalObject(Object id, boolean create)
   {
      EJBLocalObject result = null;
      if(id != null)
      {
         final Transaction tx = cache.getTransaction();
         if(tx == null)
         {
            result = createEJBLocalObject(id);
         }
         else
         {
            Map map = (Map) cache.get(tx);
            if(create)
            {
               result = createEJBLocalObject(id);
               map.put(id, result);
            }
            else
            {
               result = (EJBLocalObject) map.get(id);
               if(result == null)
               {
                  result = createEJBLocalObject(id);
                  map.put(id, result);
               }
            }
         }
      }
      return result;
   }

   public EJBLocalObject getEntityEJBLocalObject(Object id)
   {
      return getEntityEJBLocalObject(id, false);
   }

   public Collection getEntityLocalCollection(Collection ids)
   {
      ArrayList list = new ArrayList(ids.size());
      Iterator iter = ids.iterator();
      while(iter.hasNext())
      {
         final Object nextId = iter.next();
         list.add(getEntityEJBLocalObject(nextId));
      }
      return list;
   }

   /**
    * Invoke a Home interface method.
    */
   public Object invokeHome(Method m, Object[] args) throws Exception
   {
      // Set the right context classloader
      ClassLoader oldCl = TCLAction.UTIL.getContextClassLoader();
      boolean setCl = !oldCl.equals(container.getClassLoader());
      if(setCl)
      {
         TCLAction.UTIL.setContextClassLoader(container.getClassLoader());
      }
      container.pushENC();

      SecurityActions sa = SecurityActions.UTIL.getSecurityActions();   
      
      try
      {
         LocalEJBInvocation invocation = new LocalEJBInvocation(null,
            m,
            args,
            getTransaction(),
            sa.getPrincipal(),
            sa.getCredential());
         invocation.setType(InvocationType.LOCALHOME);  
 
         return container.invoke(invocation);
      }
      catch(AccessException ae)
      {
         log.trace(ae);
         throw new AccessLocalException(ae.getMessage(), ae);
      }
      catch(NoSuchObjectException nsoe)
      {
         throw new NoSuchObjectLocalException(nsoe.getMessage(), nsoe);
      }
      catch(TransactionRequiredException tre)
      {
         throw new TransactionRequiredLocalException(tre.getMessage());
      }
      catch(TransactionRolledbackException trbe)
      {
         throw new TransactionRolledbackLocalException(trbe.getMessage(), trbe);
      }
      finally
      {
         container.popENC();
         if(setCl)
         {
            TCLAction.UTIL.setContextClassLoader(oldCl);
         }  
      }
   }

   public String getJndiName()
   {
      return localJndiName;
   }

   /**
    * Return the transaction associated with the current thread.
    * Returns <code>null</code> if the transaction manager was never
    * set, or if no transaction is associated with the current thread.
    */
   Transaction getTransaction() throws javax.transaction.SystemException
   {
      if(transactionManager == null)
      {
         return null;
      }
      return transactionManager.getTransaction();
   }

   /**
    * Invoke a local interface method.
    */
   public Object invoke(Object id, Method m, Object[] args)
      throws Exception
   {
      // Set the right context classloader
      ClassLoader oldCl = TCLAction.UTIL.getContextClassLoader();
      boolean setCl = !oldCl.equals(container.getClassLoader());
      if(setCl)
      {
         TCLAction.UTIL.setContextClassLoader(container.getClassLoader());
      }
      container.pushENC();

      SecurityActions sa = SecurityActions.UTIL.getSecurityActions();  
      try
      {
         LocalEJBInvocation invocation = new LocalEJBInvocation(id,
            m,
            args,
            getTransaction(),
            sa.getPrincipal(),
            sa.getCredential());
         invocation.setType(InvocationType.LOCAL);  
         
         return container.invoke(invocation);
      }
      catch(AccessException ae)
      {
         log.trace(ae);
         throw new AccessLocalException(ae.getMessage(), ae);
      }
      catch(NoSuchObjectException nsoe)
      {
         throw new NoSuchObjectLocalException(nsoe.getMessage(), nsoe);
      }
      catch(TransactionRequiredException tre)
      {
         throw new TransactionRequiredLocalException(tre.getMessage());
      }
      catch(TransactionRolledbackException trbe)
      {
         throw new TransactionRolledbackLocalException(trbe.getMessage(), trbe);
      }
      finally
      {
         container.popENC();
         if(setCl)
         {
            TCLAction.UTIL.setContextClassLoader(oldCl);
         }  
      }
   }

   private EJBLocalObject createEJBLocalObject(Object id)
   {
      InvocationHandler handler = new EntityProxy(localJndiName, id, this);
      try
      {
         return (EJBLocalObject) proxyClassConstructor.newInstance(new Object[]{handler});
      }
      catch(Exception ex)
      {
         throw new NestedRuntimeException(ex);
      }
   }
    

   interface ClassLoaderAction
   {
      class UTIL
      {
         static ClassLoaderAction getClassLoaderAction()
         {
            return System.getSecurityManager() == null ? NON_PRIVILEGED : PRIVILEGED;
         }

         static ClassLoader get(Class clazz)
         {
            return getClassLoaderAction().get(clazz);
         }
      }

      ClassLoaderAction PRIVILEGED = new ClassLoaderAction()
      {
         public ClassLoader get(final Class clazz)
         {
            return (ClassLoader)AccessController.doPrivileged(
               new PrivilegedAction()
               {
                  public Object run()
                  {
                     return clazz.getClassLoader();
                  }
               }
            );
         }
      };

      ClassLoaderAction NON_PRIVILEGED = new ClassLoaderAction()
      {
         public ClassLoader get(Class clazz)
         {
            return clazz.getClassLoader();
         }
      };

      ClassLoader get(Class clazz);
   }

   interface SecurityActions
   {
      class UTIL
      {
         static SecurityActions getSecurityActions()
         {
            return System.getSecurityManager() == null ? NON_PRIVILEGED : PRIVILEGED;
         }
      }

      SecurityActions NON_PRIVILEGED = new SecurityActions()
      {
         public Principal getPrincipal()
         {
            SecurityContext sc = getSecurityContext();
            if(sc == null)
               return null;
            return sc.getUtil().getUserPrincipal();
         }

         public Object getCredential()
         {
            SecurityContext sc = getSecurityContext();
            if(sc == null)
               return null;
            return sc.getUtil().getCredential();
         }
         
         public SecurityContext getSecurityContext()
         {
            return SecurityContextAssociation.getSecurityContext(); 
         }
          
      };

      SecurityActions PRIVILEGED = new SecurityActions()
      {
         private final PrivilegedAction getPrincipalAction = new PrivilegedAction()
         {
            public Object run()
            {
               SecurityContext sc = getSecurityContext();
               if(sc == null)
                  return null;
               return sc.getUtil().getUserPrincipal();
            }
         };

         private final PrivilegedAction getCredentialAction = new PrivilegedAction()
         {
            public Object run()
            {
               SecurityContext sc = getSecurityContext();
               if(sc == null)
                  return null;
               return sc.getUtil().getCredential();
            }
         };

         public Principal getPrincipal()
         {
            return (Principal)AccessController.doPrivileged(getPrincipalAction);
         }

         public Object getCredential()
         {
            return AccessController.doPrivileged(getCredentialAction);
         }
         
         public SecurityContext getSecurityContext()
         {
            return (SecurityContext)AccessController.doPrivileged(
                  new PrivilegedAction(){

                     public Object run()
                     { 
                        return SecurityContextAssociation.getSecurityContext();
                     }});
         } 
      };

      Principal getPrincipal();

      Object getCredential();
      SecurityContext getSecurityContext();  
   }

   interface TCLAction
   {
      class UTIL
      {
         static TCLAction getTCLAction()
         {
            return System.getSecurityManager() == null ? NON_PRIVILEGED : PRIVILEGED;
         }

         static ClassLoader getContextClassLoader()
         {
            return getTCLAction().getContextClassLoader();
         }

         static ClassLoader getContextClassLoader(Thread thread)
         {
            return getTCLAction().getContextClassLoader(thread);
         }

         static void setContextClassLoader(ClassLoader cl)
         {
            getTCLAction().setContextClassLoader(cl);
         }

         static void setContextClassLoader(Thread thread, ClassLoader cl)
         {
            getTCLAction().setContextClassLoader(thread, cl);
         }
      }

      TCLAction NON_PRIVILEGED = new TCLAction()
      {
         public ClassLoader getContextClassLoader()
         {
            return Thread.currentThread().getContextClassLoader();
         }

         public ClassLoader getContextClassLoader(Thread thread)
         {
            return thread.getContextClassLoader();
         }

         public void setContextClassLoader(ClassLoader cl)
         {
            Thread.currentThread().setContextClassLoader(cl);
         }

         public void setContextClassLoader(Thread thread, ClassLoader cl)
         {
            thread.setContextClassLoader(cl);
         }
      };

      TCLAction PRIVILEGED = new TCLAction()
      {
         private final PrivilegedAction getTCLPrivilegedAction = new PrivilegedAction()
         {
            public Object run()
            {
               return Thread.currentThread().getContextClassLoader();
            }
         };

         public ClassLoader getContextClassLoader()
         {
            return (ClassLoader)AccessController.doPrivileged(getTCLPrivilegedAction);
         }

         public ClassLoader getContextClassLoader(final Thread thread)
         {
            return (ClassLoader)AccessController.doPrivileged(new PrivilegedAction()
            {
               public Object run()
               {
                  return thread.getContextClassLoader();
               }
            });
         }

         public void setContextClassLoader(final ClassLoader cl)
         {
            AccessController.doPrivileged(
               new PrivilegedAction()
               {
                  public Object run()
                  {
                     Thread.currentThread().setContextClassLoader(cl);
                     return null;
                  }
               }
            );
         }

         public void setContextClassLoader(final Thread thread, final ClassLoader cl)
         {
            AccessController.doPrivileged(
               new PrivilegedAction()
               {
                  public Object run()
                  {
                     thread.setContextClassLoader(cl);
                     return null;
                  }
               }
            );
         }
      };

      ClassLoader getContextClassLoader();

      ClassLoader getContextClassLoader(Thread thread);

      void setContextClassLoader(ClassLoader cl);

      void setContextClassLoader(Thread thread, ClassLoader cl);
   }
}
