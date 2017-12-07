/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.resource.deployment;

import javax.management.ObjectName;

import org.jboss.deployment.SubDeployerExtMBean;
import org.jboss.mx.util.ObjectNameFactory;

/**
 * MBean interface.
 * 
 * @author  <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 71554 $
 */
public interface RARDeployerMBean extends SubDeployerExtMBean
{
   /** The default ObjectName */
   static final ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.jca:service=RARDeployer");

   /**
    * Get the work manager name
    * 
    * @return the work manager name 
    */
   ObjectName getWorkManagerName();

   /**
    * Set the thread pool name
    * 
    * @param workManagerName the work manager name 
    */
   void setWorkManagerName(ObjectName workManagerName);

   /**
    * Get the XATerminator
    * 
    * @return the xa terminator
    */
   ObjectName getXATerminatorName();

   /**
    * Set the xa terminator
    * 
    * @param xaTerminatorName name the xa terminator name
    */
   void setXATerminatorName(ObjectName xaTerminatorName);
}
