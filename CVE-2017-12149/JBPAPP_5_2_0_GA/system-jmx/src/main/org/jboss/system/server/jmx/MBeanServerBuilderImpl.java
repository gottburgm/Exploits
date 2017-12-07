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
package org.jboss.system.server.jmx;

import javax.management.MBeanServerBuilder;
import javax.management.MBeanServerDelegate;
import javax.management.MBeanServer;

/**
 * An @link{MBeanServerBuilder} that creates a LazyMBeanServer instance to allow
 * the jboss MBeanServerImpl to be created when the ServerImpl is loaded and
 * the jboss jmx classes are available. It can be used at startup using
 * -Djavax.management.builder.initial=org.jboss.system.server.jmx.MBeanServerBuilderImpl
 * This is needed when enabling the jdk5 jconsole remote adaptor since this
 * adaptor initializes the platform MBeanServer before the jboss ServerImpl
 * is loaded.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class MBeanServerBuilderImpl
   extends MBeanServerBuilder
{
   /**
    * 
    * @return @{link MBeanServerDelegate}
    */ 
   public MBeanServerDelegate newMBeanServerDelegate()
   {
      return new MBeanServerDelegate();
   }

   /**
    * Creates a @{link LazyMBeanServer} as the MBeanServer. If the outer
    * parameter is null, the outer passed to the LazyMBeanServer is the
    * platform mbean server as obtained from
    * super.newMBeanServer(defaultDomain, outer, delegate).
    * 
    * @param defaultDomain
    * @param outer
    * @param delegate
    * @return LazyMBeanServer
    */ 
   public MBeanServer newMBeanServer(String defaultDomain, MBeanServer outer,
      MBeanServerDelegate delegate)
   {
      if( outer == null )
         outer = super.newMBeanServer(defaultDomain, outer, delegate);
      return new LazyMBeanServer(defaultDomain, outer, delegate);
   }
}
