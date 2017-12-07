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
package org.jboss.mx.server;

import javax.management.MBeanServer;
import javax.management.MBeanServerBuilder;
import javax.management.MBeanServerDelegate;

/**
 * The JBossMX implementation of the MBeanServer builder
 * 
 * @see javax.management.MBeanServer
 * @see javax.management.MBeanServerBuilder
 * @see javax.management.MBeanServerDelegate
 * @see javax.management.MBeanServerFactory
 * @see org.jboss.mx.server.MBeanServerImpl
 * 
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 81022 $
 */
public class MBeanServerBuilderImpl
   extends MBeanServerBuilder
{
   // Constants ---------------------------------------------------

   // Attributes --------------------------------------------------

   // Static  -----------------------------------------------------

   // Constructors ------------------------------------------------

   /**
    * Construct an MBeanServerBuilder
    */
   public MBeanServerBuilderImpl()
   {
   }

   /**
    * Construct an MBeanServer.
    *
    * @param defaultDomain the default domain when an MBean is
    *        registered with an ObjectName without a domain.
    * @param outer the wrapping MBeanServer, passed to MBeans
    *        at registration.
    * @param delegate the delegate to use for Notifications.
    */
   public MBeanServer newMBeanServer(String defaultDomain,
                                     MBeanServer outer,
                                     MBeanServerDelegate delegate)
   {
      return new MBeanServerImpl(defaultDomain, outer, delegate);
   }

   /**
    * Construct an MBeanServerDelegate.
    */
   public MBeanServerDelegate newMBeanServerDelegate()
   {
      return new MBeanServerDelegate();
   }

   // Public ------------------------------------------------------

   // X Implementation --------------------------------------------

   // Y Overrides -------------------------------------------------

   // Protected ---------------------------------------------------

   // Package Private ---------------------------------------------

   // Private -----------------------------------------------------

   // Inner Classes -----------------------------------------------
}
