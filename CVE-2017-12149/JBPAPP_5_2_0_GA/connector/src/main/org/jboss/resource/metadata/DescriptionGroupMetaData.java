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

import java.io.Serializable;
import java.util.Locale;

/**
 * Description group meta data
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 71554 $
 */
public class DescriptionGroupMetaData implements Serializable
{
   static final long serialVersionUID = 1324619949051028127L;

   /** The language */
   private String lang;
   
   /** The description */
   private String description;
   
   /** The display name */
   private String displayName;
   
   /** The small icon */
   private String smallIcon;
   
   /** The large icon */
   private String largeIcon;

   /**
    * Create a new description group meta data using the default langugage
    */
   public DescriptionGroupMetaData()
   {
      this(null);
   }

   /**
    * Create a new description group meta data
    * 
    * @param lang the language
    */
   public DescriptionGroupMetaData(String lang)
   {
      if (lang == null)
         this.lang = Locale.getDefault().getLanguage();
      else
         this.lang = lang;
   }

   /**
    * Get the language
    * 
    * @return the language
    */
   public String getLanguage()
   {
      return lang;
   }

   /**
    * Get the description
    * 
    * @return the description
    */
   public String getDescription()
   {
      return description;
   }

   /**
    * Set the description
    * 
    * @param description the description
    */
   public void setDescription(String description)
   {
      this.description = description;
   }

   /**
    * Get the display name
    * 
    * @return the display name
    */
   public String getDisplayName()
   {
      return displayName;
   }

   /**
    * Set the display name
    * 
    * @param displayName the display name
    */
   public void setDisplayName(String displayName)
   {
      this.displayName = displayName;
   }

   /**
    * Get the small icon
    * 
    * @return the small icon
    */
   public String getSmallIcon()
   {
      return smallIcon;
   }

   /**
    * Set the small icon
    * 
    * @param icon the icon
    */
   public void setSmallIcon(String icon)
   {
      this.smallIcon = icon;
   }

   /**
    * Get the large icon
    * 
    * @return the large icon
    */
   public String getLargeIcon()
   {
      return largeIcon;
   }

   /**
    * Set the large icon
    * 
    * @param icon the icon
    */
   public void setLargeIcon(String icon)
   {
      this.largeIcon = icon;
   }
   
   public String toString()
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append("DescriptionGroupMetaData").append('@');
      buffer.append(Integer.toHexString(System.identityHashCode(this)));
      buffer.append("[language=").append(lang);
      if (description != null)
         buffer.append(" description=").append(description);
      if (displayName != null)
         buffer.append(" displayName=").append(displayName);
      if (smallIcon != null)
         buffer.append(" smallIcon=").append(smallIcon);
      if (largeIcon != null)
         buffer.append(" largeIcon=").append(largeIcon);
      buffer.append(']');
      return buffer.toString();
   }
}
