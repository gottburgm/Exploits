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
package org.jboss.ejb;

import org.jboss.metadata.XmlLoadable;
import org.jboss.ejb.InstancePool;

/**
 * Interface for bean instances Pool Feeder
 *
 * @author <a href="mailto:vincent.harcq@hubmethods.com">Vincent Harcq</a>
 * @version $Revision: 81030 $
 */
public interface InstancePoolFeeder
   extends XmlLoadable
{
   /**
    * Start the pool feeder.
    */
   void start();

   /**
    * Stop the pool feeder.
    */
   void stop();

   /**
    * Sets the instance pool inside the pool feeder.
    *
    * @param ip the instance pool
    */
   void setInstancePool(InstancePool ip);

   /**
    * Tells if the pool feeder is already started.
    * The reason is that we start the PF at first get() on the pool and we
    * want to give a warning to the user when the pool is empty.
    *
    * @return true if started
    */
   boolean isStarted();
}
