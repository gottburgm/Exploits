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
package org.jboss.cache.invalidation;

import java.io.Serializable;

/**
 * Represent an invalidable resource, such as a cache
 * @see InvalidationGroup
 * @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 85945 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>21. septembre 2002 Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li>
 * </ul>
 */

public interface Invalidatable
{
   /**
    * Indicates that the resource with the given key should be invalidated (i.e. removed
    * from cache)
    * @param key Key of the resource to be invalidated
    */   
   public void isInvalid (Serializable key);   
   
   /** Indicates that the resources with the give keys should be invalidated (i.e.
    * removed from cache)
    *
    * @param keys Keys of the resources to be invalidated
    */   
   public void areInvalid (Serializable[] keys);   

   /**
    * All entries should be invalidated.
    */
   public void invalidateAll();
}