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
package org.jboss.ejb;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.RemoveException;

import org.jboss.invocation.Invocation;
import org.jboss.util.UnreachableStatementException;

/**
 * The container for <em>stateless</em> session beans.
 *
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Oberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:docodan@mvcsoft.com">Daniel OConnor</a>
 * @author <a href="mailto:Christoph.Jung@infor.de">Christoph G. Jung</a>
 * @version $Revision: 89854 $
 */
public class StatelessSessionContainer extends SessionContainer
        implements EJBProxyFactoryContainer, InstancePoolContainer
{
   // EJBObject implementation --------------------------------------

   /**
    * No-op.
    */
   public void remove(Invocation mi)
           throws RemoteException, RemoveException
   {
      log.debug("Useless invocation of remove() for stateless session bean");
   }

   // EJBLocalHome implementation

   public EJBLocalObject createLocalHome()
           throws CreateException
   {
      if (localProxyFactory == null)
      {
         String msg = "No ProxyFactory, check for ProxyFactoryFinderInterceptor";
         throw new IllegalStateException(msg);
      }
      createCount++;
      return localProxyFactory.getStatelessSessionEJBLocalObject();
   }

   /**
    * No-op.
    */
   public void removeLocalHome(Object primaryKey)
   {
      log.debug("Useless invocation of remove(Object) for stateless session bean");
   }

   // EJBHome implementation ----------------------------------------

   public EJBObject createHome()
           throws RemoteException, CreateException
   {
      EJBProxyFactory ci = getProxyFactory();
      if (ci == null)
      {
         String msg = "No ProxyFactory, check for ProxyFactoryFinderInterceptor";
         throw new IllegalStateException(msg);
      }
      createCount++;
      Object obj = ci.getStatelessSessionEJBObject();
      return (EJBObject) obj;
   }

   /**
    * No-op.
    */
   public void removeHome(Handle handle)
           throws RemoteException, RemoveException
   {
      throw new UnreachableStatementException();
   }

   /**
    * No-op.
    */
   public void removeHome(Object primaryKey)
           throws RemoteException, RemoveException
   {
      throw new UnreachableStatementException();
   }

   public String getBeanTypeName()
   {
      return "StatelessSession";
   }

   // Protected  ----------------------------------------------------

   protected void setupHomeMapping()
           throws NoSuchMethodException
   {
      Map map = new HashMap();

      if (homeInterface != null)
      {
         Method[] m = homeInterface.getMethods();
         for (int i = 0; i < m.length; i++)
         {
            // Implemented by container
            log.debug("Mapping " + m[i].getName());
            map.put(m[i], getClass().getMethod(m[i].getName() + "Home", m[i].getParameterTypes()));
         }
      }
      if (localHomeInterface != null)
      {
         Method[] m = localHomeInterface.getMethods();
         for (int i = 0; i < m.length; i++)
         {
            // Implemented by container
            log.debug("Mapping " + m[i].getName());
            map.put(m[i], getClass().getMethod(m[i].getName() + "LocalHome", m[i].getParameterTypes()));
         }
      }

      homeMapping = map;
   }

   Interceptor createContainerInterceptor()
   {
      return new ContainerInterceptor();
   }

   /**
    * This is the last step before invocation - all interceptors are done
    */
   class ContainerInterceptor
           extends AbstractContainerInterceptor
   {
      public Object invokeHome(Invocation mi) throws Exception
      {
         Method miMethod = mi.getMethod();
         Method m = (Method) getHomeMapping().get(miMethod);
         if (m == null)
         {
            String msg = "Invalid invocation, check your deployment packaging, method=" + miMethod;
            throw new EJBException(msg);
         }

         try
         {
            return mi.performCall(StatelessSessionContainer.this, m, mi.getArguments());
         }
         catch (Exception e)
         {
            rethrow(e);
         }

         // We will never get this far, but the compiler does not know that
         throw new org.jboss.util.UnreachableStatementException();
      }

      public Object invoke(Invocation mi) throws Exception
      {
         // wire the transaction on the context, this is how the instance remember the tx
         EnterpriseContext ctx = (EnterpriseContext) mi.getEnterpriseContext();
         if (ctx.getTransaction() == null)
            ctx.setTransaction(mi.getTransaction());

         // Get method and instance to invoke upon
         Method miMethod = mi.getMethod();

         Map map = getBeanMapping();
         Method m = (Method) map.get(miMethod);

         // The Invocation might contain the actual bean method
         // e.g. For an invocation based on a JSR-181 @WebMethod annotation
         if (m == null && map.values().contains(miMethod))
         {
            m = miMethod;
         }

         if (m == null)
         {
            String msg = "Invalid invocation, check your deployment packaging, method=" + miMethod;
            throw new EJBException(msg);
         }

         //If we have a method that needs to be done by the container (EJBObject methods)
         if (m.getDeclaringClass().equals(StatelessSessionContainer.class) ||
               m.getDeclaringClass().equals(SessionContainer.class))
         {
            try
            {
               return mi.performCall(StatelessSessionContainer.this, m, new Object[]{mi});
            }
            catch (Exception e)
            {
               rethrow(e);
            }
         }
         else // we have a method that needs to be done by a bean instance
         {
            // Invoke and handle exceptions
            try
            {
               Object bean = ctx.getInstance();
               return mi.performCall(bean, m, mi.getArguments());
            }
            catch (Exception e)
            {
               rethrow(e);
            }
         }

         // We will never get this far, but the compiler does not know that
         throw new org.jboss.util.UnreachableStatementException();
      }
   }
}
