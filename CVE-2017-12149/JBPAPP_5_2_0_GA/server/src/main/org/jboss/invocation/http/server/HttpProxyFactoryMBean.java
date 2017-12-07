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

import javax.management.ObjectName;

import org.jboss.system.Service;

import org.w3c.dom.Element;

/** An mbean interface for a proxy factory that can expose any interface
 * with RMI compatible semantics for access to remote clients using HTTP
 * as the transport.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81030 $
 */
public interface HttpProxyFactoryMBean extends Service
{
   /** Get the server side mbean that exposes the invoke operation for the
    exported interface */
   public ObjectName getInvokerName();
   /** Set the server side mbean that exposes the invoke operation for the
    exported interface */
   public void setInvokerName(ObjectName jmxInvokerName);

   /** Get the JNDI name under which the HttpInvokerProxy will be bound */
   public String getJndiName();
   /** Set the JNDI name under which the HttpInvokerProxy will be bound */
   public void setJndiName(String jndiName);

   /** Get the http URL to the InvokerServlet */
   public String getInvokerURL();
   /** Set the http URL to the InvokerServlet */
   public void setInvokerURL(String invokerURL);

   /** If there is no invokerURL set, then one will be constructed via the
    * concatenation of invokerURLPrefix + the local host + invokerURLSuffix.
    */
   public String getInvokerURLPrefix();
   /** If there is no invokerURL set, then one will be constructed via the
    * concatenation of invokerURLPrefix + the local host + invokerURLSuffix.
    * An example prefix is "http://", and this is the default value.
    */
   public void setInvokerURLPrefix(String invokerURLPrefix);

   /** If there is no invokerURL set, then one will be constructed via the
    * concatenation of invokerURLPrefix + the local host + invokerURLSuffix.
    */
   public String getInvokerURLSuffix();
   /** If there is no invokerURL set, then one will be constructed via the
    * concatenation of invokerURLPrefix + the local host + invokerURLSuffix.
    * An example suffix is "/invoker/JMXInvokerServlet" and this is the default
    * value.
    */
   public void setInvokerURLSuffix(String invokerURLSuffix);

   /** A flag if the InetAddress.getHostName() or getHostAddress() method
    * should be used as the host component of invokerURLPrefix + host +
    * invokerURLSuffix. If true getHostName() is used, false getHostAddress().
    */
   public boolean getUseHostName();
   /** A flag if the InetAddress.getHostName() or getHostAddress() method
    * should be used as the host component of invokerURLPrefix + host +
    * invokerURLSuffix. If true getHostName() is used, false getHostAddress().
    */
   public void setUseHostName(boolean flag);

   /** Get the RMI compatible interface that the HttpInvokerProxy implements */
   public Class getExportedInterface();
   /** Set the RMI compatible interface that the HttpInvokerProxy implements */
   public void setExportedInterface(Class exportedInterface);

   public Element getClientInterceptors();
   public void setClientInterceptors(Element config) throws Exception;
   /** Get the proxy instance created by the factory.
    */
   public Object getProxy();
   /** Create a new proxy instance with the given id, no jndi name and all
    * other settings the same as the jndi bound proxy.
    */
   public Object getProxy(Object id);
}
