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
package org.jboss.invocation.local;

import java.net.InetAddress;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.transaction.TransactionManager;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.Invoker;
import org.jboss.invocation.InvokerInterceptor;
import org.jboss.mx.util.JMXExceptionDecoder;
import org.jboss.proxy.TransactionInterceptor;
import org.jboss.system.Registry;
import org.jboss.system.ServiceMBeanSupport;

/**
 * The Invoker is a local gate in the JMX system.
 *
 * @author <a href="mailto:marc.fleury@jboss.org>Marc Fleury</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81030 $
 * @jmx:mbean extends="org.jboss.system.ServiceMBean"
 */
public class LocalInvoker
   extends ServiceMBeanSupport
   implements Invoker, LocalInvokerMBean
{
   private MBeanServerAction serverAction = new MBeanServerAction();

   protected void createService() throws Exception
   {
      // note on design: We need to call it ourselves as opposed to 
      // letting the client InvokerInterceptor look it 
      // up through the use of Registry, the reason being including
      // the classes in the client. 
      // If we move to a JNDI format (with local calls) for the
      // registry we could remove the call below
      InvokerInterceptor.setLocal(this);

      Registry.bind(serviceName, this);
   }

   protected void startService() throws Exception
   {
      InitialContext ctx = new InitialContext();
      try
      {

         /**
          * FIXME marcf: what is this doing here?
          */
         TransactionManager tm = (TransactionManager) ctx.lookup("java:/TransactionManager");
         TransactionInterceptor.setTransactionManager(tm);
      }
      finally
      {
         ctx.close();
      }

      log.debug("Local invoker for JMX node started");
   }

   protected void destroyService()
   {
      Registry.unbind(serviceName);
   }
   
   // Invoker implementation --------------------------------
   
   public String getServerHostName()
   {
      try
      {
         return InetAddress.getLocalHost().getHostName();
      }
      catch (Exception ignored)
      {
         return null;
      }
   }

   /**
    * Invoke a method.
    */
   public Object invoke(Invocation invocation) throws Exception
   {
      ClassLoader oldCl = TCLAction.UTIL.getContextClassLoader();

      ObjectName mbean = (ObjectName) Registry.lookup((Integer) invocation.getObjectName());
      try
      {
         Object[] args = {invocation};
         Object rtnValue = serverAction.invoke(mbean, "invoke", args,
            Invocation.INVOKE_SIGNATURE);
         return rtnValue;
      }
      catch (Exception e)
      {
         e = (Exception) JMXExceptionDecoder.decode(e);
         if (log.isTraceEnabled())
            log.trace("Failed to invoke on mbean: " + mbean, e);
         throw e;
      }
      finally
      {
         TCLAction.UTIL.setContextClassLoader(oldCl);
      }
   }

   /** Perform the MBeanServer.invoke op in a PrivilegedExceptionAction if
    * running with a security manager.
    */ 
   class MBeanServerAction implements PrivilegedExceptionAction
   {
      private ObjectName target;
      String method;
      Object[] args;
      String[] sig;

      MBeanServerAction()
      {  
      }
      MBeanServerAction(ObjectName target, String method, Object[] args, String[] sig)
      {
         this.target = target;
         this.method = method;
         this.args = args;
         this.sig = sig;
      }

      public Object run() throws Exception
      {
         Object rtnValue = server.invoke(target, method, args, sig);
         return rtnValue;
      }
      Object invoke(ObjectName target, String method, Object[] args, String[] sig)
         throws Exception
      {
         SecurityManager sm = System.getSecurityManager();
         Object rtnValue = null;
         if( sm == null )
         {
            // Direct invocation on MBeanServer
            rtnValue = server.invoke(target, method, args, sig);
         }
         else
         {
            try
            {
               // Encapsulate the invocation in a PrivilegedExceptionAction
               MBeanServerAction action = new MBeanServerAction(target, method, args, sig);
               rtnValue = AccessController.doPrivileged(action);
            }
            catch (PrivilegedActionException e)
            {
               Exception ex = e.getException();
               throw ex;
            }
         }
         return rtnValue;
      }
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

