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
package org.jboss.ha.singleton;

import org.jboss.ha.jmx.HAServiceMBean;

/**
 * HA-Singleton interface.
 * Only one mbean is active at any point in time, cluster-wide.
 * <p> 
 * The service provides a simple way for a concrete mbean to detect whether
 * or not it is the active one in the cluster.
 * <p>
 * Concrete mbeans would usually do activities like regular clean up of database tables
 * or saving statistics about cluster usage.         
 * 
 * Replaced by {@link org.jboss.ha.framework.interfaces.HASingletonMBean}
 * 
 * @author <a href="mailto:ivelin@apache.org">Ivelin Ivanov</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81001 $
 */
@Deprecated
public interface HASingletonMBean extends HAServiceMBean, org.jboss.ha.framework.interfaces.HASingletonMBean
{
   // Constants -----------------------------------------------------
   
   /** Notifications emitted locally by an HASingleton to indicate state transitions */
   String HASINGLETON_STARTING_NOTIFICATION = "org.jboss.ha.singleton.starting";
   String HASINGLETON_STARTED_NOTIFICATION  = "org.jboss.ha.singleton.started";   
   String HASINGLETON_STOPPING_NOTIFICATION = "org.jboss.ha.singleton.stopping";
   String HASINGLETON_STOPPED_NOTIFICATION  = "org.jboss.ha.singleton.stopped";
}
