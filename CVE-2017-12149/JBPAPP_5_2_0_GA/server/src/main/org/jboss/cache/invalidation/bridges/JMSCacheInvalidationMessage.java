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
package org.jboss.cache.invalidation.bridges;

import org.jboss.cache.invalidation.BatchInvalidation;

/**
 * <description>
 *
 * @see <related>
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 81030 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>30. septembre 2002 Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li>
 * </ul>
 */

public class JMSCacheInvalidationMessage
   implements java.io.Serializable
{
   
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   protected BatchInvalidation[] bis = null;
   protected java.rmi.dgc.VMID emitter = null;
   protected String invalidateAllGroupName;

   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   public JMSCacheInvalidationMessage (java.rmi.dgc.VMID source, 
                                       String groupName, 
                                       java.io.Serializable[] keys)
   {
      this.emitter = source;
      this.bis = new BatchInvalidation[] 
      {
         new BatchInvalidation (keys, groupName)
      };
   }
   
   public JMSCacheInvalidationMessage (java.rmi.dgc.VMID source, 
                                       BatchInvalidation[] invalidations)
   {
      this.emitter = source;
      this.bis = invalidations;
   }

   public JMSCacheInvalidationMessage(java.rmi.dgc.VMID source, String groupName)
   {
      this.emitter = source;
      this.invalidateAllGroupName = groupName;
   }
   
   // Public --------------------------------------------------------
   
   public BatchInvalidation[] getInvalidations()
   {
      if (this.bis == null)
         this.bis = new BatchInvalidation[0];
      
      return this.bis;
   }
   
   // Z implementation ----------------------------------------------
   
   // Y overrides ---------------------------------------------------
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
   
}
