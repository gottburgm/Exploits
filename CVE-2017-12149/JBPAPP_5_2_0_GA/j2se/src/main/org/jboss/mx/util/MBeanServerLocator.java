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

import java.util.Iterator;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

/**
 * A helper class to locate an MBeanServer.
 *      
 * MBeanServer lookup strategy enhanced to allow the explicit
 * setting of a particular instance to be returned. This is needed to
 * allow re-using the jdk5 ManagementFactory.getPlatformMBeanServer()
 * as our main MBeanServer. The DefaultDomain name of this server cannot
 * be set, and it seems to be "null" by default (probably a bug).
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author Scott.Stark@jboss.org
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a> 
 * @version $Revision: 81019 $
 */
public class MBeanServerLocator
{
   /** The MBeanServer to return, if set */
   private static MBeanServer instance = null;
   
   /**
    * Private CTOR to disable instantiation
    */
   private MBeanServerLocator()
   {
      // empty
   }   

   /**
    * Optionally set the MBeanServer to be returned
    * by calls to locateJBoss(). Setting this back
    * to null reverts to the normal lookup strategy.
    * 
    * @param server the main jboss MBeanServer or null
    */
   public static void setJBoss(MBeanServer server)
   {
      synchronized (MBeanServerLocator.class)
      {
         instance = server;
      }
   }
   
   /**
    * Returns the first MBeanServer registered under the agentID
    * 
    * @param agentID the id of the MBeanServer to look for
    * @return the first MBeanServer with an agentID
    */
   public static MBeanServer locate(final String agentID)
   {
      MBeanServer server = (MBeanServer)
         MBeanServerFactory.findMBeanServer(agentID).iterator().next();

      return server;
   }

   /**
    * Returns the first available MBeanServer
    * 
    * @return the first available MBeanServer
    */
   public static MBeanServer locate()
   {
      return locate(null);
   }

   /**
    * Returns the main jboss MBeanServer.
    * 
    * If there is one set using setJBoss(), it will be returned.
    * Otherwise the strategy is to return the first MBeanServer
    * registered under the "jboss" id (or else, default domain name)
    * 
    * @return the main jboss MBeanServer
    * @throws IllegalStateException when no MBeanServer can be found
    */
   public static MBeanServer locateJBoss()
   {
      synchronized (MBeanServerLocator.class)
      {
         if (instance != null)
         {
            return instance;
         }
      }      
      for (Iterator i = MBeanServerFactory.findMBeanServer(null).iterator(); i.hasNext(); )
      {
         MBeanServer server = (MBeanServer) i.next();
         if (server.getDefaultDomain().equals("jboss"))
         {
            return server;
         }
      }
      throw new IllegalStateException("No 'jboss' MBeanServer found!");
   }   
}
