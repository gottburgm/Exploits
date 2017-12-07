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
package org.jboss.ha.framework.server;

import org.jboss.ha.framework.interfaces.HAPartition;

/**
 * MBean interface for the Distributed Replicant Manager (DRM) service
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 81751 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>12 janvier 2002 Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li> 
 * </ul>
 */

public interface DistributedReplicantManagerImplMBean
   extends org.jboss.ha.framework.interfaces.DistributedReplicantManager
{   
   /**
    * Get the {@link HAPartition#getPartitionName() name of the underlying partition}
    * used by this service.
    * 
    * @return the name of the partition
    */
   String getPartitionName();
   
   String listContent () throws Exception;
   String listXmlContent () throws Exception;
}
