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
package org.jboss.test.xml.mbeanserver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * An encapsulation of the mbean deployment data
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class MBeanData
{
   /** mbean/@name */
   private String name;
   /** mbean/@code */
   private String code;
   /** mbean/attribute == ArrayList<javax.management.Attribute> */
   private ArrayList attributes = new ArrayList();
   /** mbean/depends == ArrayList<javax.management.ObjectName> */
   private ArrayList depends = new ArrayList();

   public String getName()
   {
      
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getCode()
   {
      return code;
   }

   public void setCode(String code)
   {
      this.code = code;
   }

   public List getAttributes()
   {
      return attributes;
   }

   public void setAttributes(List attributes)
   {
      this.attributes.clear();
      this.attributes.addAll(attributes);
   }

   public List getDepends()
   {
      return depends;
   }
   public void setDepends(List depends)
   {
      this.depends.clear();
      this.depends.addAll(depends);
   }

   public Map getAttributeMap()
   {
      HashMap map = new HashMap();
      for(int n = 0; n < attributes.size(); n ++)
      {
         MBeanAttribute attr = (MBeanAttribute) attributes.get(n);
         map.put(attr.getName(), attr);
      }
      return map;
   }
}
