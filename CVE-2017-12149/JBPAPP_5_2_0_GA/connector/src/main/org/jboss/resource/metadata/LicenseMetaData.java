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
package org.jboss.resource.metadata;

/**
 * License meta data
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 71554 $
 */
public class LicenseMetaData extends DescriptionMetaDataContainer
{
   private static final long serialVersionUID = -1583292998139497934L;

   /** license required */
   private boolean required = false;

   /**
    * Get the license required flag
    * 
    * @return the license required flag
    */
   public boolean getRequired()
   {
      return required;
   }

   /**
    * Set the license required flag
    * 
    * @param required the required flag
    */
   public void setRequired(boolean required)
   {
      this.required = required;
   }
   
   public String toString()
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append("LicenseMetaData").append('@');
      buffer.append(Integer.toHexString(System.identityHashCode(this)));
      buffer.append("[required=").append(required);
      buffer.append(" descriptions=").append(getDescriptions());
      buffer.append(']');
      return buffer.toString();
   }
}
