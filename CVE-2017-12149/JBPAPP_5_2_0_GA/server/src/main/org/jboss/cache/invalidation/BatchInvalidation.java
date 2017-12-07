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
 * Structure that contains keys to be invalidated and the name of the group
 * on which these invalidation must be performed.
 *
 * @see InvalidationManagerMBean
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 81030 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>24 septembre 2002 Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li>
 * </ul>
 */

public class BatchInvalidation implements java.io.Serializable
{
   
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   protected Serializable[] ids = null;
   protected String invalidationGroupName = null;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   public BatchInvalidation ()
   {
   }
   
   public BatchInvalidation (Serializable[] ids, String invalidationGroupName)
   {
      this.ids = ids;
      this.invalidationGroupName = invalidationGroupName;
   }
   
   public Serializable[] getIds ()
   {
      return this.ids;
   }
   
   public void setIds (Serializable[] ids)
   {
      this.ids = ids;
   }
   
   public String getInvalidationGroupName ()
   {
      return invalidationGroupName;
   }
   
   public void setInvalidationGroupName (String invalidationGroupName)
   {
      this.invalidationGroupName = invalidationGroupName;
   }
   
   // Public --------------------------------------------------------
   
   // Z implementation ----------------------------------------------
   
   // Y overrides ---------------------------------------------------
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
   
}
