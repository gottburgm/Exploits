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
package org.jboss.ejb.plugins;

import org.jboss.system.ServiceMBean;

/**
 * MBean interface.
 * 
 * @version $Revision: 81030 $
 */
public interface StatefulSessionFilePersistenceManagerMBean extends ServiceMBean
{
   /**
    * Set the sub-directory name under the server data directory
    * where session data will be stored. This value will be appened
    * to the value of <tt>jboss-server-data-dir</tt>.
    * 
    * This value is only used during creation and will not dynamically
    * change the store directory when set after the create step has finished.
    * 
    * @param dirName A sub-directory name.    */
   void setStoreDirectoryName(String dirName);

}
