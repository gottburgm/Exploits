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
 * Description meta data
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 71554 $
 */
public class DescriptionMetaData implements Serializable
{
   static final long serialVersionUID = -3100028904830435509L;

   /** The language */
   private String lang;
   
   /** The description */
   private String description;

   /**
    * Create a new description meta data using the default langugage
    */
   public DescriptionMetaData()
   {
      this(null);
   }

   /**
    * Create a new description meta data
    * 
    * @param lang the language
    */
   public DescriptionMetaData(String lang)
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
   
   public String toString()
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append("DescriptionMetaData").append('@');
      buffer.append(Integer.toHexString(System.identityHashCode(this)));
      buffer.append("[language=").append(lang);
      if (description != null)
         buffer.append(" description=").append(description);
      buffer.append(']');
      return buffer.toString();
   }
}
