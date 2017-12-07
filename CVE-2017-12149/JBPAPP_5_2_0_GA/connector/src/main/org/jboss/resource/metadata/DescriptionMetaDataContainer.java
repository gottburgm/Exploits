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
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An abstract class for meta data that has descriptions
 *
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 84262 $
 */
public class DescriptionMetaDataContainer implements Serializable
{
   private static final long serialVersionUID = 2831943526217092377L;

   /** The descriptions */
   private ConcurrentHashMap descriptions = new ConcurrentHashMap();

   public DescriptionMetaDataContainer()
   {
      DescriptionMetaData dmd = new DescriptionMetaData();
      descriptions.put(dmd.getLanguage(), dmd);
   }

   /**
    * Get the desription for the default language
    * or the first description if there is no default
    * 
    * @return the description for the default language
    */
   public DescriptionMetaData getDescription()
   {
      // Try the default locale
      DescriptionMetaData dmd = (DescriptionMetaData) descriptions.get(Locale.getDefault().getLanguage());
      // No description using the default locale, just use the first
      if (dmd == null)
      {
         for (Iterator i = descriptions.values().iterator(); i.hasNext();)
         {
            dmd = (DescriptionMetaData) i.next();
            break;
         }
      }
      return dmd;
   }
   
   /**
    * Get the description for the give language
    * 
    * @param lang the language
    * @return the description
    */
   public DescriptionMetaData getDescription(String lang)
   {
      return (DescriptionMetaData) descriptions.get(lang);
   }
   
   /**
    * Add a description
    * 
    * @param dmd the description
    */
   public void addDescription(DescriptionMetaData dmd)
   {
      descriptions.put(dmd.getLanguage(), dmd);
   }
   
   /**
    * Get the descriptions
    * 
    * @return the descriptions
    */
   public Collection getDescriptions()
   {
      return descriptions.values();
   }
   
   public String toString()
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append("DescriptionMetaDataContainer").append('@');
      buffer.append(Integer.toHexString(System.identityHashCode(this)));
      buffer.append("[descriptions=").append(descriptions.values());
      buffer.append(']');
      return buffer.toString();
   }
}
