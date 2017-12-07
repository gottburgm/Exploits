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
package org.jboss.mx.util;

import java.lang.reflect.Proxy;

import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

/**
 *
 * @see java.lang.reflect.Proxy
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 81019 $
 *   
 */
public class MBeanProxy
{

   // Static --------------------------------------------------------
   
   /**
    * Creates a proxy to an MBean in the given MBean server.
    *
    * @param   intrface    the interface this proxy implements
    * @param   name        object name of the MBean this proxy connects to
    * @param   agentID     agent ID of the MBean server this proxy connects to
    *
    * @return  proxy instance
    *
    * @throws MBeanProxyCreationException if the proxy could not be created
    */
   public static Object get(Class intrface, ObjectName name, String agentID) throws MBeanProxyCreationException
   {
      return get(intrface, name, (MBeanServer)MBeanServerFactory.findMBeanServer(agentID).get(0));
   }

   /**
    * Creates a proxy to an MBean in the given MBean server.
    *
    * @param   intrface the interface this proxy implements
    * @param   name     object name of the MBean this proxy connects to
    * @param   server   MBean server this proxy connects to
    *
    * @return proxy instance
    *
    * @throws MBeanProxyCreationException if the proxy could not be created
    */
   public static Object get(Class intrface, ObjectName name, MBeanServer server)  throws MBeanProxyCreationException
   {
      return get(new Class[] { intrface, ProxyContext.class, DynamicMBean.class }, name, server);
   }

   /**
    */
   public static Object get(ObjectName name, MBeanServer server)  throws MBeanProxyCreationException
   {
      return get(new Class[] { ProxyContext.class, DynamicMBean.class }, name, server);
   }

   private static Object get(Class[] interfaces, ObjectName name, MBeanServer server) throws MBeanProxyCreationException
   {
      return Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(),
            interfaces, new JMXInvocationHandler(server, name)
      );
   }
   
   /**
    * Convenience method for registering an MBean and retrieving a proxy for it.
    *
    * @param   instance MBean instance to be registered
    * @param   intrface the interface this proxy implements
    * @param   name     object name of the MBean
    * @param   agentID  agent ID of the MBean server this proxy connects to
    *
    * @return proxy instance
    *
    * @throws MBeanProxyCreationException if the proxy could not be created
    */
   public static Object create(Class instance, Class intrface, ObjectName name, String agentID) throws MBeanProxyCreationException
   {
      return create(instance, intrface, name, (MBeanServer)MBeanServerFactory.findMBeanServer(agentID).get(0));
   }   
   
   /**
    * Convenience method for registering an MBean and retrieving a proxy for it.
    *
    * @param   instance MBean instance to be registered
    * @param   intrface the interface this proxy implements
    * @param   name     object name of the MBean
    * @param   server   MBean server this proxy connects to
    *
    * @throws MBeanProxyCreationException if the proxy could not be created
    */
   public static Object create(Class instance, Class intrface, ObjectName name, MBeanServer server) throws MBeanProxyCreationException
   {
      try
      {
         server.createMBean(instance.getName(), name);
         return get(intrface, name, server);
      }
      catch (ReflectionException e) {
         throw new MBeanProxyCreationException("Creating the MBean failed: " + e.toString());
      }
      catch (InstanceAlreadyExistsException e) {
         throw new MBeanProxyCreationException("Instance already exists: " + name);
      }
      catch (MBeanRegistrationException e) {
         throw new MBeanProxyCreationException("Error registering the MBean to the server: " + e.toString());
      }
      catch (MBeanException e) {
         throw new MBeanProxyCreationException(e.toString());
      }
      catch (NotCompliantMBeanException e) {
         throw new MBeanProxyCreationException("Not a compliant MBean " + instance.getClass().getName() + ": " + e.toString());
      }
   }
   
}



