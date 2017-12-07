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
package org.jboss.jmx.connector.invoker.client;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.PayloadKey;
import org.jboss.proxy.Interceptor;

import javax.management.ObjectName;

/**
* An Interceptor that plucks the object name out of the arguments
* into an unmarshalled part of the payload.
* 
* @author <a href="mailto:adrian.brock@happeningtimes.com">Adrian Brock</a>
* @version $Revision: 81030 $
*/
public class InvokerAdaptorClientInterceptor
   extends Interceptor
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   public InvokerAdaptorClientInterceptor()
   {
      // For externalization to work
   }
   
   // Public --------------------------------------------------------
   
   /**
    * Invoke using the invoker for remote invocations
    */
   public Object invoke(Invocation invocation)
      throws Throwable
   {
      // Retrieve any relevent object name for this invocation
      ObjectName objectName = getObjectNameFromArguments(invocation);
      if (objectName != null)
         invocation.setValue("JMX_OBJECT_NAME", objectName, PayloadKey.AS_IS);

      try
      {
         return getNext().invoke(invocation);
      }
      catch (InvokerAdaptorException e)
      {
         throw e.getWrapped();
      }
   }

   /**
    * Return any target object name relevent for this invocation.<p>
    *
    * Methods that don't pass arguments that could be custom classes are ignored.<p>
    *
    * Classloading and registerMBean are ignored, 
    * they shouldn't be available remotely
    */
   public ObjectName getObjectNameFromArguments(Invocation invocation)
   {
      String method = invocation.getMethod().getName();
      if (method.equals("invoke") ||
         method.equals("setAttribute") ||
         method.equals("setAttributes") ||
         method.equals("addNotificationListener") ||
         method.equals("removeNotificationListener"))
      {
         return (ObjectName) invocation.getArguments()[0];
      }

      return null;
   }

   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
