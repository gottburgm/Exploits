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

import java.beans.PropertyEditor;


import org.jboss.common.beans.property.finder.PropertyEditorFinder;
import org.jboss.xb.binding.GenericObjectModelFactory;
import org.jboss.xb.binding.UnmarshallingContext;
import org.xml.sax.Attributes;

/**
 * Class that implements the binding of the XML model
 * to our POJO classes, using the GenericObjectModelFactory
 * facility
 * 
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * 
 * @version $Revision: 113110 $
 */
public class ConfigInfoBinding
   implements GenericObjectModelFactory
{
         
   // ObjectModelFactory implementation -----------------------------
   
   public Object newRoot(Object root, UnmarshallingContext ctx, String namespaceURI,
                         String localName, Attributes attrs)
   {
      final ConfigInfo ci;
      if(root == null)
      {
         root = ci = new ConfigInfo();
      }
      else
      {
         ci = (ConfigInfo) root;
      }

      if(attrs.getLength() > 0)
      {
         for(int i = 0; i < attrs.getLength(); ++i)
         {
            if (attrs.getLocalName(i).equals("copydir"))
            {
               ci.setCopydir(attrs.getValue(i));
            }
            else if (attrs.getLocalName(i).equals("template"))
            {
               ci.setTemplate(attrs.getValue(i));
            }
            else if (attrs.getLocalName(i).equals("extension"))
            {
               ci.setExtension(attrs.getValue(i));
            }
         }
      }
      return root;
   }
   
   public Object completeRoot(Object root, UnmarshallingContext ctx, String namespaceURI, String localName)
   {
      return root;
   }
   
   // GenericObjectModelFactory implementation ----------------------
   
   public Object newChild(Object parent, UnmarshallingContext ctx, String namespaceURI,
                          String localName, Attributes attrs)
   {
      Object child = null;

      if (parent instanceof ConfigInfo)
      {
         if("property".equals(localName))
         {
            PropertyInfo pi = new PropertyInfo();
            child = pi;
   
            if(attrs.getLength() > 0)
            {
               for(int i = 0; i < attrs.getLength(); ++i)
               {
                  if(attrs.getLocalName(i).equals("name"))
                  {  
                     pi.setName(attrs.getValue(i));
                  }
                  else if (attrs.getLocalName(i).equals("type"))
                  {
                     pi.setType(attrs.getValue(i));
                  }
                  else if (attrs.getLocalName(i).equals("optional"))
                  {
                     pi.setOptional(Boolean.valueOf(attrs.getValue(i)).booleanValue());
                  }
               }
            }
            if (pi.getName() == null)
               throw new RuntimeException(
                  "Missing attribute 'name' for <property> element");
            
            // check type if specified, otherwise assume the default
            String type = pi.getType();
            if (type == null)
               pi.setType("java.lang.String");
         }
         else if ("template".equals(localName))
         {
            TemplateInfo ti = new TemplateInfo();
            child = ti;
            
            if(attrs.getLength() > 0)
            {
               for(int i = 0; i < attrs.getLength(); ++i)
               {
                  if(attrs.getLocalName(i).equals("input"))
                  {  
                     ti.setInput(attrs.getValue(i));
                  }
                  else if (attrs.getLocalName(i).equals("output"))
                  {
                     ti.setOutput(attrs.getValue(i));
                  }
               }
            }
            // check both attributes are populated
            if (ti.getInput() == null || ti.getOutput() == null)
               throw new RuntimeException(
                  "Both 'input' and 'output' attribute must be set for a <template> element");
         }
      }
      return child;
   }

   public void addChild(Object parent, Object child, UnmarshallingContext ctx,
                        String namespaceURI, String localName)
   {
      if(parent instanceof ConfigInfo)
      {
         final ConfigInfo ci = (ConfigInfo)parent;
         if(child instanceof PropertyInfo)
         {
            ci.addPropertyInfo((PropertyInfo)child);
         }
         else if(child instanceof TemplateInfo)
         {
            ci.addTemplateInfo((TemplateInfo)child);
         }
      }      
   }

   public void setValue(Object o, UnmarshallingContext ctx, String namespaceURI,
                        String localName, String value)
   {
      if(o instanceof ConfigInfo)
      {
         final ConfigInfo ci = (ConfigInfo)o;
         if("description".equals(localName))
         {
            ci.setDescription(value);
         }
      }
      else if(o instanceof PropertyInfo)
      {
         PropertyInfo pi = (PropertyInfo)o;
         if("description".equals(localName))
         {
            pi.setDescription(value);
         }
         else if("default-value".equals(localName))
         {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Class clazz = null;
            try {
               clazz = cl.loadClass(pi.getType());
            }
            catch (ClassNotFoundException e) {
               throw new RuntimeException("Class not found for property '" + pi.getName() +
                                    "' of type '" + pi.getType() + "'");
            }
            PropertyEditor peditor = PropertyEditorFinder.getInstance().find(clazz);

            if (peditor != null) {
               peditor.setAsText(value);
               pi.setDefaultValue(peditor.getValue());
            }
            else
               throw new RuntimeException("Property editor not found for property '" + pi.getName() +
                                    "' of type '" + pi.getType() + "'");
         }
      }
   }
}
