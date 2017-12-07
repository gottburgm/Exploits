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
package org.jboss.monitor;

import java.util.Collection;
import javax.management.JMException;
import org.jboss.monitor.client.BeanCacheSnapshot;

/**
 * The JMX management interface for the {@link BeanCacheMonitor} MBean.
 * 
 * @see Monitorable
 * @author <a href="mailto:simone.bordet@compaq.com">Simone Bordet</a>
 * @version $Revision: 81030 $
 */
public interface BeanCacheMonitorMBean
{
   /**
    * Returns the cache data at the call instant.
    * @return null if a problem is encountered while sampling the cache,
    * 
    * otherwise an array (possibly of size 0) with the cache data.
    */
   BeanCacheSnapshot[] getSnapshots();

   /**
    * Describe <code>listSnapshots</code> method here.
    * Returns as a collection, throws JMException on problem
    *
    * @return a <code>Collection</code> value
    */
   Collection listSnapshots() throws JMException;
}
