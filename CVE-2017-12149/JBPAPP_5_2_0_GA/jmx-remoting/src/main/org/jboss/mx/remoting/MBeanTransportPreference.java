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

import java.util.StringTokenizer;
import javax.management.MBeanServer;
import org.jboss.logging.Logger;
import org.jboss.remoting.ConnectionFailedException;
import org.jboss.remoting.InvokerLocator;
import org.jboss.remoting.ident.Identity;

/**
 * MBeanTransportPreference is a utility class that will take into account the VM's setup transport
 * preferences when trying to create a preferred connection back to a remote MBeanServer.  Since there
 * are cases when multiple invoker transports can exist and all be valid, this will help determine which
 * order the transports should be attempted.  You wouldn't want to connect via SOAP, in most cases, when
 * TCP/IP via Sockets is available.  There are cases however you do want to explicitly choose SOAP vs.
 * Sockets or RMI, and in which case your preference order might be <tt>soap, socket, rmi.</tt>
 *
 * @author <a href="mailto:jhaynie@vocalocity.net">Jeff Haynie</a>
 * @version $Revision: 81023 $
 */
public class MBeanTransportPreference
{
   private static final transient Logger log = Logger.getLogger(MBeanTransportPreference.class.getName());

   // NOTE: we need to maybe think this through on how this really should work long term..
   private static String _preferences = System.getProperty("jboss.transport.preferences", "socket,rmi,soap");
   private static String preferences[] = initialize(_preferences);
   private static MBeanServer ourServer;
   private static Identity ourIdentity;

   public static void setLocalServer(MBeanServer server, Identity identity)
   {
      if(log.isTraceEnabled())
      {
         log.trace("setLocalServer called - server=" + server + ",identity=" + identity);
      }
      ourServer = server;
      ourIdentity = identity;
   }

   private static String[] initialize(String list)
   {
      if(list == null)
      {
         return new String[1];
      }
      StringTokenizer tok = new StringTokenizer(list, ",");
      String pref [] = new String[tok.countTokens()];
      int c = 0;
      while(tok.hasMoreTokens())
      {
         String token = tok.nextToken();
         pref[c++] = token.trim();
      }
      return pref;
   }

   /**
    * set the order to use when selecting transports to connect to a remote server
    *
    * @param order
    */
   public static void setTransportPreferences(String order[])
   {
      preferences = (order == null || order.length <= 0) ? initialize(_preferences) : order;
   }

   /**
    * get the order to use when selecting transports to connect to a remote server
    *
    * @return
    */
   public static String[] getTransportPreferences()
   {
      return preferences;
   }

   /**
    * return a server transport to a MBeanServer on a remote server, using the transport
    * preference order specified by the user
    *
    * @param identity
    * @param locators
    * @return
    * @throws ConnectionFailedException
    */
   public static MBeanServer getServerByTransport(Identity identity, InvokerLocator locators[])
         throws ConnectionFailedException
   {
      if(log.isTraceEnabled())
      {
         log.trace("getServerByTransport for identity=" + identity + ", ours is=" + ourIdentity);
      }
      if(ourIdentity == null)
      {
         if(ourServer == null)
         {
            ourServer = JMXUtil.getMBeanServer();
         }
         ourIdentity = Identity.get(ourServer);
      }
      if(identity.isSameJVM(ourIdentity))
      {
         return ourServer;
      }
      for(int c = 0; c < preferences.length; c++)
      {
         String transport = preferences[c];

         if(transport != null)
         {
            for(int x = 0; x < locators.length; x++)
            {
               if(locators[x].getProtocol().equals(transport))
               {
                  // attempt connect to this first one in our pref list
                  try
                  {
                     MBeanServer svr = MBeanServerRegistry.getMBeanServerFor(locators[x]);
                     if(svr != null)
                     {
                        return svr;
                     }
                     svr = MBeanServerClientInvokerProxy.create(locators[x], ourIdentity.getJMXId(), identity.getJMXId());
                     if(svr != null)
                     {
                        return svr;
                     }
                  }
                  catch(Throwable ex)
                  {
                  }
               }
            }
         }
      }
      for(int x = 0; x < locators.length; x++)
      {
         // attempt connect to this first one in our pref list
         try
         {
            if(log.isTraceEnabled())
            {
               log.trace("attempting to connect via locator[" + x + "] (" + locators[x] + ") to: " + identity);
            }
            MBeanServer svr = MBeanServerRegistry.getMBeanServerFor(locators[x]);
            if(svr != null)
            {
               return svr;
            }
            svr = MBeanServerClientInvokerProxy.create(locators[x], ourIdentity.getJMXId(), identity.getJMXId());
            if(svr != null)
            {
               return svr;
            }
         }
         catch(Throwable ex)
         {
            log.debug("Error connecting ... ", ex);
         }
      }
      throw new ConnectionFailedException("No transport/connection available to connect to: " + identity);
   }
}
