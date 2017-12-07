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
package org.jboss.mx.persistence;

import javax.management.AttributeList;

import org.w3c.dom.Element;
/**
 * AttributePersistenceManager interface. 
 * 
 * Implementations of this interface are created by an
 * MBean service that acts as factory and a manager
 * for the active AttributePersistenceManager implementation
 * 
 * The DelegatingPersistenceManager will contact the MBean
 * to get an AttributePersistenceManager implementation.
 * 
 * In this way, the Persistence Manager can be controlled
 * externally as an MBean.
 *
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81026 $
 */
public interface AttributePersistenceManager
{
   // AttributePersistenceManager lifecycle -------------------------
   
   /**
    * Initializes the AttributePersistenceManager using
    * the supplied configuration element CONFIG_ELEMENT
    * whose content will be probably different for each
    * particular implementation.
    * 
    * The version string is a tag that must be used by the
    * AttributePersistenceManager implementation to make
    * sure that data saved/loaded under different version
    * tags are partitioned. It can be null or empty to 
    * indicate that no particular version tag is required.
    * 
    * Once created, the configuration of the implementation
    * object cannot change.
    * 
    * Calling any other method before create() is executed
    * will result in a IllegalStateException
    * 
    * Finally, the implementation should be prepared to
    * receive multiple concurrent calls. 
    * 
    * @param  version   a tag to identify the version
    * @param  config    XML Element to load arbitrary config
    * @throws Exception	when any error occurs during create
    */
   public void create(String version, Element config)
      throws Exception;

   /**
    * Returns true if the AttributePersistenceManager
    * is "in-service" state, i.e. after create() and
    * before destroy() has been called, false otherwise.
    * 
    * @return true if in operational state
    */
   public boolean getState();
   
   /**
    * Releases resources and destroys the AttributePersistenceManager.
    * The object is unusable after destroy() has been called.
    * 
    * Any call to any method will result to an
    * IllegalStateException.
    *
    */
   public void destroy();
   
   // AttributePersistenceManager Persistence -----------------------
   
   /**
    * Checks if a persistened AttributeList for this particular
    * id exists
    * 
    * @param  id		the key of the image
    * @return true 		if an image exists; false otherwise
    * @throws Exception on any error
    */
   public boolean exists(String id)
      throws Exception;

   /**
    * Uses the specified id to retrieve a previously persisted
    * AttributeList. If no data can be found under the specified
    * id, a null will be returned.
    * 
    * @param id			the key for retrieving the data
    * @return			the data, or null
    * @throws Exception when an error occurs
    */
   public AttributeList load(String id)
      throws Exception;
   
   /**
    * Persists an AttributeList (name/value pair list),
    * under a specified id. The id can be used to retrieve the
    * AttributeList later on. The actual mechanism will differ
    * among implementations.
    * 
    * @param  id 		the key for retrieving the data later on, not null
    * @param  attrs 	the data to be persisted, not null
    * @throws Exception	when data cannot be persisted
    */
   public void store(String id, AttributeList attrs)
      throws Exception;
   
   /**
    * Removes the persisted AttributeList, if exists 
    *
    * @param  id		the key of the image
    * @throws Exception	when any error occurs
    */
   public void remove(String id)
      throws Exception;
   
   /**
    * Removes all the persisted data stored under
    * the configured version tag.
    *  
    * @throws Exception when any error occurs
    */
   public void removeAll()
      throws Exception;
   
   /**
    * Returns a String array with all the saved ids
    * under the configured version tag.
    * 
    * @return			array with all persisted ids
    * @throws Exception when any error occurs
    */
   public String[] listAll()
      throws Exception;
   
}
