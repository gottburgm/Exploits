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
package org.jboss.ha.hasessionstate.server;

import javax.management.ObjectName;

import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.mx.util.ObjectNameFactory;

/**
 * MBEAN interface for HASessionState service.
 *
 * @see org.jboss.ha.hasessionstate.interfaces.HASessionState
 *
 * @author sacha.labourey@cogito-info.ch
 * @version $Revision: 81001 $
 *
 * <p><b>Revisions:</b><br>
 */
public interface HASessionStateServiceMBean
   extends org.jboss.system.ServiceMBean
{
   ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss:service=HASessionState");

   String getJndiName();
   void setJndiName(String newName);
   
   /** 
    * Gets the name of the partition used by this service.  This is a 
    * convenience method as the partition name is an attribute of HAPartition.
    * 
    * @return the name of the partition
    */
   String getPartitionName();
   
   /**
    * Get the underlying partition used by this service.
    * 
    * @return the partition
    */
   HAPartition getHAPartition();
   
   /**
    * Sets the underlying partition used by this service.
    * 
    * @param clusterPartition the partition
    */
   void setHAPartition(HAPartition clusterPartition);
   
   long getBeanCleaningDelay();
   void setBeanCleaningDelay(long newDelay);
}
