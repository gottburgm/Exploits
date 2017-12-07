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
package org.jboss.services.deployment.metadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple POJO class to model XML configuration data
 * 
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * 
 * @version $Revision: 81038 $
 */
public class ConfigInfo
   implements Serializable
{
   /** @since 4.0.2 */
   private static final long serialVersionUID = 3455531161586923775L;
      
   // name is used as an id for instances of this class
   private String name;
   private String copydir;   
   private String template;
   private String extension;
   private String description;
   
   private List propertyList = new ArrayList();
   private List templateList = new ArrayList();
   
   // Constructors --------------------------------------------------
   /**
    * Default CTOR
    */
   public ConfigInfo()
   {
      // empty
   }
   
   /**
    * CTOR
    * @param name
    * @param template
    * @param extension
    */
   public ConfigInfo(String name, String copydir, 
                     String template, String extension, String description)
   {
      this.name = name;
      this.copydir = copydir;
      this.template = template;
      this.extension = extension;
      this.description = description;
   }
   
   // Accessors/Modifiers -------------------------------------------

   
   /**
    * @return Returns the extension.
    */
   public String getExtension()
   {
      return extension;
   }
   
   /**
    * @param extension The extension to set.
    */
   public void setExtension(String extension)
   {
      this.extension = extension;
   }
   
   /**
    * @return Returns the name.
    */
   public String getName()
   {
      return name;
   }
   /**
    * @param name The name to set.
    */
   public void setName(String name)
   {
      this.name = name;
   }
   
   /**
    * @return Returns the template.
    */
   public String getTemplate()
   {
      return template;
   }
   
   /**
    * @param template The template to set.
    */
   public void setTemplate(String template) 
   {
      this.template = template;
   }
   
   /**
    * @return Returns the description.
    */
   public String getDescription()
   {
      return description;
   }
   
   /**
    * @param description The description to set.
    */
   public void setDescription(String description)
   {
      this.description = description;
   }

   /**
    * @return Returns the copydir.
    */
   public String getCopydir()
   {
      return copydir;
   }
   
   /**
    * @param copydir The copydir to set.
    */
   public void setCopydir(String copydir)
   {
      this.copydir = copydir;
   }
   
   /**
    * @return Returns the propertyList.
    */
   public List getPropertyInfoList()
   {
      return propertyList;
   }
   
   /**
    * @param propertyList The propertyList to set.
    */
   public void setPropertyInfoList(List propertyList)
   {
      this.propertyList = propertyList;
   }
   
   public void addPropertyInfo(PropertyInfo propertyInfo)
   {
      this.propertyList.add(propertyInfo);
   }
   
   /**
    * @return Returns the templateList.
    */
   public List getTemplateInfoList()
   {
      return templateList;
   }
   
   /**
    * @param templateList The templateList to set.
    */
   public void setTemplateInfoList(List templateList)
   {
      this.templateList = templateList;
   }
   
   public void addTemplateInfo(TemplateInfo templateInfo)
   {
      this.templateList.add(templateInfo);
   }
   
   // Object Methods ------------------------------------------------
   
   public String toString()
   {
      StringBuffer sb = new StringBuffer(1024);
      sb.append('[')
         .append("name=").append(name)
         .append(", copydir=").append(copydir)
         .append(", template=").append(template)
         .append(", extension=").append(extension)
         .append(", description=").append(description)
         .append(", propertyList=").append(propertyList)
         .append(", templateList=").append(templateList)
         .append(']');
      return sb.toString();
   }

   public boolean equals(Object other)
   {
      if(this == other) return true;
      if(!(other instanceof ConfigInfo)) return false;

      // base equality on name
      if (name != null && name.equals(((ConfigInfo)other).name))
         return true;
      else
         return false;
   }

   public int hashCode()
   {
      if (name != null)
         return name.hashCode();
      else
         return 0;
   }
}
