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

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import org.jboss.remoting.ConnectionFailedException;
import org.jboss.remoting.InvokerLocator;
import org.jboss.remoting.ident.Identity;
import org.jboss.remoting.network.NetworkInstance;
import org.jboss.remoting.network.NetworkRegistry;
import org.jboss.remoting.network.filter.IdentityFilter;

/**
 * MBeanServerLocator is an object that is used to identify and locate
 * an MBeanServer on the network via JMX Remoting.  <P>
 * <p/>
 * The MBeanServerLocator can be serialized and passed across the network,
 * as long as the target server has access back to the MBeanServer via
 * JMX Remoting Connector and has been detected by a JMX Remoting Detector. <P>
 *
 * @author <a href="mailto:jhaynie@vocalocity.net">Jeff Haynie</a>
 * @version $Revision: 81023 $
 */
public class MBeanServerLocator implements Serializable
{
   static final long serialVersionUID = 7632696197699845344L;
   
   private final Identity identity;
   private boolean autoLocate = true;
   protected transient MBeanServer server;

   public MBeanServerLocator(Identity identity)
   {
      this.identity = identity;
   }

   public int hashCode()
   {
      return identity.hashCode();
   }

   public boolean equals(Object obj)
   {
      if(obj instanceof MBeanServerLocator)
      {
         return identity.equals(((MBeanServerLocator) obj).identity);
      }
      return false;
   }

   public String toString()
   {
      return "MBeanServerLocator [" + identity.getJMXId() + "]";
   }

   /**
    * return the MBeanServer ID
    *
    * @return
    */
   public String getServerId()
   {
      return identity.getJMXId();
   }

   /**
    * return the identity of the server
    *
    * @return
    */
   public final Identity getIdentity()
   {
      return identity;
   }

   /**
    * return the MBeanServer InstanceID
    *
    * @return
    */
   public String getInstanceId()
   {
      return identity.getInstanceId();
   }

   /**
    * return the InetAddress for the MBeanServer
    *
    * @return
    */
   public InetAddress getAddress()
   {
      return identity.getAddress();
   }

   /**
    * return a proxy to the MBeanServer
    *
    * @return
    */
   public MBeanServer getMBeanServer()
   {
      // we use a weak reference so that if the MBeanServer is available to
      // be garbage collected, we shouldn't stand in the way ... and hold a strong
      // reference to it
      if(server == null)
      {
         // try to get a reference
         server = resolveServer();
      }

      if(server == null)
      {
         // if the reference is still null, return null
         throw new ConnectionFailedException("Couldn't find server at: " + identity);
      }
      else
      {
         return server;
      }
   }

   /**
    * try and resolve the serverid to a MBeanServer instance or proxy to a
    * remote server
    *
    * @return
    */
   protected MBeanServer resolveServer() throws ConnectionFailedException
   {
      Object server = null;
      String id = System.getProperty("jboss.identity");
      if(id != null && id.equals(identity.getInstanceId()))
      {
         // it's local
         ArrayList list = MBeanServerFactory.findMBeanServer(null);
         if(list.isEmpty() == false)
         {
            Iterator iter = list.iterator();
            while(iter.hasNext())
            {
               MBeanServer s = (MBeanServer) iter.next();
               try
               {
                  if(JMXUtil.getServerId(s).equals(identity.getJMXId()))
                  {
                     return s;
                  }
               }
               catch(Exception ex)
               {
               }
            }
         }
      }
      // try and get from registry
      server = MBeanServerRegistry.getMBeanServerFor(getServerId());
      if(server == null)
      {
         // not in registry, query for the network instance and try and create
         NetworkInstance ni[] = NetworkRegistry.getInstance().queryServers(new IdentityFilter(identity));
         if(ni != null && ni.length > 0)
         {
            InvokerLocator locators[] = ni[0].getLocators();
            String jmxId = ni[0].getIdentity().getJMXId();
            ArrayList list = MBeanServerFactory.findMBeanServer(jmxId);
            if(list != null && !list.isEmpty())
            {
               return (MBeanServer) list.get(0);
            }
            try
            {
               server = MBeanTransportPreference.getServerByTransport(identity, locators);
            }
            catch(Exception e)
            {
               e.printStackTrace();
            }
         }
      }
      return (server == null) ? null : (MBeanServer) server;
   }

   /**
    * resolve the server object automagically on deserialization
    *
    * @param stream
    * @throws java.io.IOException
    * @throws java.lang.ClassNotFoundException
    *
    */
   private void readObject(java.io.ObjectInputStream stream)
         throws IOException, ClassNotFoundException
   {
      stream.defaultReadObject();
      if(autoLocate)
      {
         resolveServer();
      }
   }

   /**
    * set true (default) to automatically locate the appropriate MBeanServer on deserialization
    * or false to only locate on demand to the call to <tt>getMBeanServer</tt>.
    *
    * @param autoLocate
    */
   public void setAutoLocate(boolean autoLocate)
   {
      this.autoLocate = autoLocate;
   }
}
