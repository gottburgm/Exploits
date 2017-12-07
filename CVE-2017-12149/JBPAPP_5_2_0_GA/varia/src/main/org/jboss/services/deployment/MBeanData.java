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
package org.jboss.services.deployment;

import java.io.Serializable;
import java.util.Properties;

/**
 * Holds information about an MBean, suitable for use to generate the XSLT used
 * to transform the XML configuration file containing the information about that
 * MBean.
 * 
 * @author <a href="peter.johnson2@unisys.com">Peter Johnson</a>
 * @version $Revision: 81038 $
 */
public class MBeanData implements Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -4870385245742489112L;

   /**
    * The JNDI name for the MBean
    */
   private String name;

   /**
    * Partial XPath statement used to locate the name of the MBean.  This is a
    * read-only property set from the name property the first time this property
    * is accessed.
    */
   private String xpath;

   /**
    * The simple file name of the Velocity template to use to generate the XSLT
    * file.
    */
   private String templateName;

   /**
    * Collection of dependencies for this MBean. The key is the value for the
    * optional-attribute-name property, and the value is the name of the
    * depending MBean. The key can be an empty string to represent the default
    * dependency. Both key and value are strings.
    */
   private Properties depends;

   /**
    * The collection of attributes for this MBean. The key is the attribute
    * name, and the value is its value. Both key and value are strings.
    */
   private Properties attributes;

   /**
    * @return Returns the attributes.
    */
   public final Properties getAttributes()
   {
      return attributes;
   }

   /**
    * @param attributes The attributes to set.
    */
   public final void setAttributes(Properties attributes)
   {
      this.attributes = attributes;
   }

   /**
    * @return Returns the depends.
    */
   public final Properties getDepends()
   {
      return depends;
   }

   /**
    * @param depends The depends to set.
    */
   public final void setDepends(Properties depends)
   {
      this.depends = depends;
   }

   /**
    * @return Returns the name.
    */
   public final String getName()
   {
      return name;
   }

   /**
    * @param name The name to set.
    */
   public final void setName(String name)
   {
      this.name = name;
   }

   /**
    * Get the templateName.
    * 
    * @return the templateName.
    */
   public String getTemplateName()
   {
      return templateName;
   }

   /**
    * Set the templateName.
    * 
    * @param templateName The templateName to set.
    */
   public void setTemplateName(String templateName)
   {
      this.templateName = templateName;
   }

   /**
    * @return Returns the xpath condition.
    */
   public final String getXpath()
   {
      if (xpath == null)
      {
         asXpath();
      }
      return xpath;
   }

   /**
    * @see java.lang.Object#toString()
    */
   public String toString()
   {
      return name;
   }
   
   /**
    * Converts an mbean name into the condition used to locate the name as part
    * of an xpath statement.
    * </p>
    * <p>
    * For example, converts
    * </p>
    * 
    * <pre>   jboss.mq:service=InvocationLayer,type=UIL2XA,alias=UIL2XAConnectionFactory
    * </pre>
    * 
    * <p>
    * into
    * </p>
    * 
    * <pre>   starts-with(@name, 'jboss.mq:')<br/>
    *         and contains(@name, 'service=InvocationLayer')<br/>
    *         and contains(@name, 'type=UIL2XA')<br/>
    *         and contains(@name, 'alias=UIL2XAConnectionFactory')<br/>
    *         and string-length(@name) = 74</pre>
    * 
    * <p>
    * and converts a name such as
    * </p>
    * 
    * <pre>   jboss.mq:service=InvocationLayer</pre>
    * 
    * <p>
    * into
    * </p>
    * 
    * <pre>   @name='jboss.mq:service=InvocationLayer'</pre>
    * 
    * <p>
    * The number of commas that appear (thus, by inference, the number of
    * attributes associated with the mbean name) differtiates the format used.
    * If there is no comma (meaning exactly one attribute), the
    * later format is used. If there is at least one comma, meaning two or more
    * attributes, the former format is used.  The string-length check prevents
    * a mismatch with an mbean that has attributes in addition to the attributes
    * referenced by the contains() functions.
    * </p>
    * <p>
    * Perplexed as to why we should even bother to do this? It appears that when
    * you ask for the name of an mbean, the attributes associate with the name
    * can appear in any order. Thus, the order found in the XML file is not
    * always what is given by the mbean.
    * 
    */
   private void asXpath()
   {
      
      // If there is no comma in the name, use the simple form:
      if (name.indexOf(',') == -1)
      {
         xpath = "@name='" + name + "'";
      }
      else
      {
         // There is a comma, need to generate the longer xpath condition
         String[] parts = name.split("[,:]");
         StringBuffer buf = new StringBuffer(2 * name.length());
         buf.append("starts-with(@name, '");
         buf.append(parts[0]);
         for (int i = 1; i < parts.length; i++)
         {
            buf.append("') and contains(@name, '");
            buf.append(parts[i]);
         }
         buf.append("') and string-length(@name) = ");
         buf.append(name.length());
         xpath = buf.toString();
      }
   }
}
