/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jms.server.destination;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jboss.metatype.api.types.CompositeMetaType;
import org.jboss.metatype.api.types.MapCompositeMetaType;
import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.types.SimpleMetaType;
import org.jboss.metatype.api.values.CompositeValue;
import org.jboss.metatype.api.values.CompositeValueSupport;
import org.jboss.metatype.api.values.MapCompositeValueSupport;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.SimpleValue;
import org.jboss.metatype.api.values.SimpleValueSupport;
import org.jboss.metatype.plugins.types.MutableCompositeMetaType;
import org.jboss.metatype.spi.values.MetaMapper;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A metaMapper mapping Element to a MapCompositeValue.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class SecurityConfigMapper extends MetaMapper<Element>
{
   
   /** The role attributes. */
   protected static final String[] attributes = new String[] { "read", "write", "create"};
   
   /** The meta type. */
   protected static final MapCompositeMetaType metaType;
   
   /** The composite meta type. */
   public static MutableCompositeMetaType composite;
   
   static
   {
      // Create the meta type
      composite = new MutableCompositeMetaType("SecurityConfig", "The security config");
      composite.addItem("read", "read permission", SimpleMetaType.BOOLEAN);
      composite.addItem("write", "write permission", SimpleMetaType.BOOLEAN);
      composite.addItem("create", "create permission", SimpleMetaType.BOOLEAN);
      composite.freeze();
      // The map composite meta type
      metaType = new MapCompositeMetaType(composite);
   }
   
   @Override
   public MetaType getMetaType()
   {
      return metaType;
   }
   
   @Override
   public Type mapToType()
   {
      return Element.class;
   }
   
   @Override
   public MetaValue createMetaValue(MetaType metaType, Element object)
   {
      if(object == null)
         return null;
      
      Map<String, MetaValue> entries = new HashMap<String, MetaValue>();
      // Parse
      NodeList list = object.getElementsByTagName("role");
      int l = list.getLength();
      for(int i = 0; i<l;i++) {
         Element role = (Element)list.item(i);
         Attr na = role.getAttributeNode("name");
         if (na == null)
            continue;

         // Role name
         String name = na.getValue();
         // the role values
         Map<String, MetaValue> values = parseAttributes(role, attributes);
         // Put
         entries.put(name, new CompositeValueSupport(composite, values));
      }
      return new MapCompositeValueSupport(entries, metaType);
   }

   @Override
   public Element unwrapMetaValue(MetaValue metaValue)
   {
      if(metaValue == null)
         return null;
      
      if(metaValue instanceof MapCompositeValueSupport)
      {
         MapCompositeValueSupport value = (MapCompositeValueSupport) metaValue;
         CompositeMetaType metaType = value.getMetaType();
         // Don't create a empty securityConfig
         if(metaType.itemSet().isEmpty())
            return null;
         
         // Create the dom document
         Document d = createDocument();
         // Security
         Element security = d.createElement("security");
         for(String name : metaType.itemSet())
         {
            // Role
            CompositeValue row = (CompositeValue) value.get(name);
            // FIXME the MapMetaType might not be up 2 date
            if(row == null)
               continue;
            
            Element role = d.createElement("role");
            role.setAttribute("name", name);
            
            // For each attribute: read, write, create
            for(String attribute : attributes)
            {
               SimpleValue v = (SimpleValue) row.get(attribute);
               if(v != null && v.getValue() != null)
               {
                  role.setAttribute(attribute, ((Boolean)v.getValue()).toString());
               }
            }
            security.appendChild(role);
         }
         return security;
      }
      return null;
   }
   
   protected static Document createDocument()
   {
      try
      {
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         DocumentBuilder db = dbf.newDocumentBuilder();
         return db.newDocument();
      }
      catch(Exception e)
      {
         throw new RuntimeException(e);
      }
   }
   
   protected static Map<String, MetaValue> parseAttributes(Element element, String... attributeNames)
   {
      Map<String, MetaValue> map = new HashMap<String, MetaValue>();
      for(String attribute : attributeNames)
      {
         map.put(attribute, parseAttribute(attribute, element));
      }
      return map;
   }
   
   protected static SimpleValue parseAttribute(String attributeName, Element element)
   {
      Boolean value = null;
      if(element.getAttributeNode(attributeName) != null)
      {
         value = Boolean.valueOf(element.getAttribute(attributeName));
      }
      return new SimpleValueSupport(SimpleMetaType.BOOLEAN, value);
   }
   
}

