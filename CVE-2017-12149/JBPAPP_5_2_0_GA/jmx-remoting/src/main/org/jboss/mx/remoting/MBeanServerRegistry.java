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
package org.jboss.mx.remoting;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.management.MBeanServer;
import org.jboss.logging.Logger;
import org.jboss.remoting.InvokerLocator;

/**
 * MBeanServerRegistry is a registry for remote MBeanServer proxies that are registered in the local
 * JVM.
 *
 * @author <a href="mailto:jhaynie@vocalocity.net">Jeff Haynie</a>
 * @version $Revision: 81023 $
 */
public class MBeanServerRegistry
{
   private static final Logger log = Logger.getLogger(MBeanServerRegistry.class.getName());

   private static final Map serversById = Collections.synchronizedMap(new HashMap());
   private static final Map serversByLocator = Collections.synchronizedMap(new HashMap());

   /**
    * return true if the remote MbeanServer is registered for the given MBeanServer ID
    *
    * @param id
    * @return
    */
   public static final boolean isMBeanServerRegistered(String id)
   {
      return getMBeanServerFor(id) != null;
   }

   /**
    * return the MBeanServer proxy for a given MBeanServer id, or null if none registered
    *
    * @param id
    * @return
    */
   public static final MBeanServer getMBeanServerFor(String id)
   {
      return (MBeanServer) serversById.get(id);
   }

   /**
    * returns true if the remote MBeanServer proxy is registered for the given locator
    *
    * @param locator
    * @return
    */
   public static final boolean isMBeanServerRegistered(InvokerLocator locator)
   {
      return getMBeanServerFor(locator) != null;
   }

   /**
    * return the MBeanServer proxy for a given InvokerLocator, or null if none registered
    *
    * @param locator
    * @return
    */
   public static final MBeanServer getMBeanServerFor(InvokerLocator locator)
   {
      return (MBeanServer) serversByLocator.get(locator);
   }

   static synchronized void register(MBeanServer server, MBeanServerClientInvokerProxy proxy)
   {
      String serverid = proxy.getServerId();
      serversById.put(serverid, server);
      serversByLocator.put(proxy.getLocator(), server);
      if(log.isTraceEnabled())
      {
         log.trace("register called with proxy: " + proxy + " and serverid: " + serverid);
      }
   }

   static synchronized void unregister(MBeanServerClientInvokerProxy proxy)
   {
      String serverid = proxy.getServerId();
      serversById.remove(serverid);
      serversByLocator.remove(proxy.getLocator());
      if(log.isTraceEnabled())
      {
         log.trace("unregister called with proxy: " + proxy + " and serverid: " + serverid);
      }
   }
}
