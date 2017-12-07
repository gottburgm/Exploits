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
package org.jboss.invocation.http.server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeOperationsException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Transaction;

import org.jboss.bootstrap.spi.util.ServerConfigUtil;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.Invoker;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.invocation.http.interfaces.HttpInvokerProxy;
import org.jboss.system.Registry;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.StringPropertyReplacer;

/**
 * The HttpInvoker ... into the JMX base.
 *
 * @author <a href="mailto:scott.stark@jboss.org>Scott Stark</a>
 * @version $Revision: 81030 $
 */
public class HttpInvoker extends ServiceMBeanSupport
   implements HttpInvokerMBean
{
   private String invokerURL;
   private String invokerURLPrefix = "http://";
   private String invokerURLSuffix = ":8080/invoker/JMXInvokerServlet";
   private boolean useHostName = false;

   // Public --------------------------------------------------------

   public String getInvokerURL()
   {
      return invokerURL;
   }
   public void setInvokerURL(String invokerURL)
   {
      // Replace any system properties in the URL
      String tmp = StringPropertyReplacer.replaceProperties(invokerURL);
      this.invokerURL = tmp;
      log.debug("Set invokerURL to "+this.invokerURL);
   }

   public String getInvokerURLPrefix()
   {
      return invokerURLPrefix;
   }
   public void setInvokerURLPrefix(String invokerURLPrefix)
   {
      this.invokerURLPrefix = invokerURLPrefix;
   }

   public String getInvokerURLSuffix()
   {
      return invokerURLSuffix;
   }
   public void setInvokerURLSuffix(String invokerURLSuffix)
   {
      this.invokerURLSuffix = invokerURLSuffix;
   }

   public boolean getUseHostName()
   {
      return useHostName;
   }
   public void setUseHostName(boolean flag)
   {
      this.useHostName = flag;
   }

   protected void startService()
      throws Exception
   {
      checkInvokerURL();
      Invoker delegateInvoker = new HttpInvokerProxy(invokerURL);

      // Export the Invoker interface
      ObjectName name = super.getServiceName();
      Registry.bind(name, delegateInvoker);
      log.debug("Bound Http invoker for JMX node");         
   }

   protected void stopService()
   {
      try
      {
         InitialContext ctx = new InitialContext();
         ctx.unbind("invokers/"+InetAddress.getLocalHost().getHostName()+"/http");
      }
      catch (NamingException ignore)
      {
      }
      catch (Throwable e)
      {
         log.error("Failed", e);
         return;
      }
   }

   protected void destroyService()
   {
      // Export references to the bean
      Registry.unbind(serviceName);
   }
  
   /**
    * Invoke a Remote interface method.
    */
   public Object invoke(Invocation invocation)
      throws Exception
   {
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      try
      {
         // Deserialize the transaction if it is there
         MarshalledInvocation mi = (MarshalledInvocation) invocation;
         Object tpc = mi.getTransactionPropagationContext();
         Transaction tx = importTPC(tpc);
         invocation.setTransaction(tx);

         Integer nameHash = (Integer) invocation.getObjectName();
         ObjectName mbean = (ObjectName) Registry.lookup(nameHash);

         // The cl on the thread should be set in another interceptor
         Object[] args = {invocation};
         String[] sig = {"org.jboss.invocation.Invocation"};
         Object obj = super.getServer().invoke(mbean, 
            "invoke", args, sig);

         // Return the raw object and let the http layer marshall it
         return obj;
      }
      catch (Exception e)
      {
         if (e instanceof MBeanException)
            e = ((MBeanException)e).getTargetException();
         
         if (e instanceof RuntimeMBeanException)
            e = ((RuntimeMBeanException)e).getTargetException();
         
         if (e instanceof RuntimeOperationsException)
            e = ((RuntimeOperationsException)e).getTargetException();
         
         // Only log errors if trace is enabled
         if( log.isTraceEnabled() )
            log.trace("operation failed", e);
         throw e;
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }

   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   /** Not implemented, and should not be
    */
   protected Transaction importTPC(Object tpc)
   {
      return null;
   }

   /** Validate that the invokerURL is set, and if not build it from
    * the invokerURLPrefix + host + invokerURLSuffix. The host value will be
    * taken from the jboss.bind.address system property if its a valid
    * address, InetAddress.getLocalHost otherwise.
    */
   protected void checkInvokerURL() throws UnknownHostException
   {
      if( invokerURL == null )
      {
         InetAddress addr = InetAddress.getLocalHost();
         // First check for a global bind address
         String host = ServerConfigUtil.getSpecificBindAddress();
         if( host == null )
         {
            host = useHostName ? addr.getHostName() : addr.getHostAddress();
         }
         String url = invokerURLPrefix + host + invokerURLSuffix;
         setInvokerURL(url);
      }
   }
}

