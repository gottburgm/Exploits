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

import java.util.ArrayList;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.MBeanInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanParameterInfo;

import org.jboss.ha.framework.interfaces.HARMIResponse;
import org.jboss.ha.framework.interfaces.GenericClusteringException;
import org.jboss.ha.framework.server.HATarget;
import org.jboss.invocation.Invocation;
import org.jboss.logging.Logger;
import org.jboss.mx.util.JMXExceptionDecoder;
import org.jboss.mx.util.DynamicMBeanSupport;


/** This is an invoker that delegates to the target invoker and handles the
 * wrapping of the response in an HARMIResponse with any updated HATarget info.
 * @see HttpProxyFactoryHA
 *
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @version $Revision: 81001 $
 */
public class HAInvokerWrapper extends DynamicMBeanSupport
{
   private static Logger log = Logger.getLogger(HAInvokerWrapper.class);
   private MBeanServer mbeanServer;
   private MBeanInfo info;

   private ObjectName targetName;
   private HATarget target;

   public HAInvokerWrapper(MBeanServer mbeanServer, ObjectName targetName, HATarget target)
   {
      this.mbeanServer = mbeanServer;
      this.targetName = targetName;
      this.target = target;
      MBeanAttributeInfo[] attrInfo = null;
      MBeanConstructorInfo[] ctorInfo = null;
      MBeanParameterInfo[] sig = {
        new  MBeanParameterInfo("invocation", Invocation.class.getName(),
           "The invocation content information")
      };
      MBeanOperationInfo[] opInfo = {
         new MBeanOperationInfo("invoke", "The detached invoker entry point",
            sig, "java.lang.Object", MBeanOperationInfo.ACTION)
      };
      MBeanNotificationInfo[] eventInfo = null;
      this.info = new MBeanInfo(getClass().getName(),
         "A wrapper inovker that delegates to the target invoker",
         attrInfo,
         ctorInfo,
         opInfo,
         eventInfo);
   }

   /** The JMX DynamicMBean invoke entry point. This only handles the
    * invoke(Invocation) operation.
    *
    * @param actionName
    * @param params
    * @param signature
    * @return the invocation response
    * @throws MBeanException
    * @throws ReflectionException
    */
   public Object invoke(String actionName, Object[] params, String[] signature)
      throws MBeanException, ReflectionException
   {
      if( params == null || params.length != 1 ||
         (params[0] instanceof Invocation) == false )
      {
         NoSuchMethodException e = new NoSuchMethodException(actionName);
         throw new ReflectionException(e, actionName);
      }

      Invocation invocation = (Invocation) params[0];
      try
      {
         Object value = invoke(invocation);
         return value;
      }
      catch(Exception e)
      {
         throw new ReflectionException(e, "Invoke failure");
      }
   }

   /** The invoker entry point.
    * @param invocation
    * @return A HARMIResponse that wraps the result of calling invoke(Invocation)
    * on the targetName MBean
    * @throws Exception
    */
   public Object invoke(Invocation invocation)
      throws Exception
   {
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      try
      {
         // The cl on the thread should be set in another interceptor
         Object[] args = {invocation};
         String[] sig = {"org.jboss.invocation.Invocation"};
         Object rtn = mbeanServer.invoke(targetName, "invoke", args, sig);

         // Update the targets list if the client view is out of date
         Long clientViewId = (Long) invocation.getValue("CLUSTER_VIEW_ID");
         HARMIResponse rsp = new HARMIResponse();
         if (clientViewId.longValue() != target.getCurrentViewId())
         {
            rsp.newReplicants = new ArrayList(target.getReplicants());
            rsp.currentViewId = target.getCurrentViewId();
         }
         rsp.response = rtn;

         // Return the raw object and let the http layer marshall it
         return rsp;
      }
      catch (Exception e)
      {
         // Unwrap any JMX exceptions
         e = (Exception) JMXExceptionDecoder.decode(e);
         // Don't send JMX exception back to client to avoid needing jmx
         if( e instanceof JMException )
            e = new GenericClusteringException (GenericClusteringException.COMPLETED_NO, e.getMessage());

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

   public MBeanInfo getMBeanInfo()
   {
      return info;
   }
}
