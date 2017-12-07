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
package org.jboss.naming;

import org.jboss.system.ServiceMBean;

/**     
 * The management interface for the link ref pair service.
 * 
 * @author Adrian Brock (adrian@jboss.com)
 * @version $Revision: 81030 $
 */
public interface LinkRefPairServiceMBean extends ServiceMBean
{
   // Constants -----------------------------------------------------
    
   // Public --------------------------------------------------------

   /**
    * The jndi name where the link ref pair is bound
    * 
    * @return the jndi name
    */
   public String getJndiName();

   /**
    * Set the jndi name where the link ref pair is bound
    * 
    * @param jndiName the jndi name
    */
   public void setJndiName(String jndiName);

   /**
    * The jndi name of the remote binding
    * 
    * @return the jndi name
    */
   public String getRemoteJndiName();

   /**
    * Set the jndi name of the remote binding
    * 
    * @param jndiName the jndi name
    */
   public void setRemoteJndiName(String jndiName);

   /**
    * The jndi name of the local binding
    * 
    * @return the jndi name
    */
   public String getLocalJndiName();

   /**
    * Set the jndi name of the local binding
    * 
    * @param jndiName the jndi name
    */
   public void setLocalJndiName(String jndiName);
}