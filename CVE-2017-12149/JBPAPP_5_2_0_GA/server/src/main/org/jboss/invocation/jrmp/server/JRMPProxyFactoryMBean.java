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
package org.jboss.invocation.jrmp.server;

import javax.management.ObjectName;

import org.jboss.system.ServiceMBean;
import org.jboss.invocation.Invocation;

import org.w3c.dom.Element;

/** An mbean interface for a proxy factory that can expose any interface
 * with RMI compatible semantics for access to remote clients using JRMP
 * as the transport protocol.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81030 $
 */
public interface JRMPProxyFactoryMBean extends ServiceMBean
{
   /** Get the server side JRMPInvoker mbean that will be used as the
    * RMI/JRMP transport handler.
    */
   public ObjectName getInvokerName();
   /** Set the server side JRMPInvoker mbean that will be used as the
    * RMI/JRMP transport handler.
    */
   public void setInvokerName(ObjectName jmxInvokerName);

   /** Get the server side mbean that exposes the invoke operation for the
    exported interface */
   public ObjectName getTargetName();
   /** Set the server side mbean that exposes the invoke operation for the
    exported interface */
   public void setTargetName(ObjectName targetName);

   /** Get the JNDI name under which the HttpInvokerProxy will be bound */
   public String getJndiName();
   /** Set the JNDI name under which the HttpInvokerProxy will be bound */
   public void setJndiName(String jndiName);

   /** Get the RMI compatible interface that the JRMPInvokerProxy implements */
   public Class getExportedInterface();
   /** Set the RMI compatible interface that the JRMPInvokerProxy implements */
   public void setExportedInterface(Class exportedInterface);

   /** Get the RMI compatible interface that the JRMPInvokerProxy implements */
   public Class[] getExportedInterfaces();
   /** Set the RMI compatible interface that the JRMPInvokerProxy implements */
   public void setExportedInterfaces(Class[] exportedInterface);

   /** Get the proxy client side interceptor configuration
    * 
    * @return the proxy client side interceptor configuration
    */ 
   public Element getClientInterceptors();
   /** Set the proxy client side interceptor configuration
    * 
    * @param config the proxy client side interceptor configuration
    */ 
   public void setClientInterceptors(Element config) throws Exception;

   /**
    * @return  whether invocations go to the target method instead of invoke(Invocation mi)
    */
   public boolean getInvokeTargetMethod();
   /**
    * @param invokeTargetMethod  whether invocations should go to the target method instead of invoke(Invocation mi)
    */
   public void setInvokeTargetMethod(boolean invokeTargetMethod);

   /** Get the proxy instance created by the factory.
    */
   public Object getProxy();

   public Object invoke(Invocation mi) throws Exception;
}
