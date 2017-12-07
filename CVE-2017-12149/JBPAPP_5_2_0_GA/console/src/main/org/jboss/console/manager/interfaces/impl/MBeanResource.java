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
package org.jboss.console.manager.interfaces.impl;

import org.jboss.console.manager.interfaces.ManageableResource;

import javax.management.ObjectName;

/**
 * <description>
 *
 * @see <related>
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 81010 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>December 16, 2002 Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li>
 * </ul>
 */

public class MBeanResource
   implements ManageableResource
{
   
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   String className = null;
   ObjectName oj = null;
   transient Object mbean = null; // SUPPORT FOR REMOTE MBEANS!!!!
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   public MBeanResource () {}
   
   public MBeanResource (ObjectName oj, String clazz)
   {
      this.oj = oj;
      this.className = clazz;
   }
   
   public MBeanResource (ObjectName oj, String clazz, Object proxy)
   {
      this.oj = oj;
      this.className = clazz;
      this.mbean = proxy;
   }
   
   // Public --------------------------------------------------------
   
   public String getClassName ()
   {
      return this.className;
   }
   
   public ObjectName getObjectName ()
   {
      return this.oj;
   }
   
   public Object getMBeanProxy ()
   {
      return this.mbean;
   }   
   
   // ManageableResource implementation ----------------------------------------------
   
   public String getId ()
   {
      return this.oj.toString ();
   }
   
   // Object overrides ---------------------------------------------------
   
   public boolean equals (Object other)
   {
      if (other instanceof MBeanResource)
         return this.oj.equals (((MBeanResource)other).oj);
      else
         return false;
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
   
}
