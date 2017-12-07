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
package org.jboss.test.server.profileservice.persistence.support;

import java.util.List;
import java.util.Map;

import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementObjectID;
import org.jboss.managed.api.annotation.ManagementProperty;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 88869 $
 */
@ManagementObject
public class TestMetaData
{

   /** The name. */
   String name;
   
   /** The primitive */
   PrimitiveMetaData primitive;
   
   /** A primitive List */
   List<String> list;
   
   Map<String, String> testMap;
   
   Character[] charArray;
   
   char[][] test2dChar;
   
   Map<Integer, String> table;
   
   @ManagementProperty
   @ManagementObjectID
   public String getName()
   {
      return name;
   }
   
   public void setName(String name)
   {
      this.name = name;
   }

   @ManagementProperty
   public PrimitiveMetaData getPrimitive()
   {
      return primitive;
   }
   
   public void setPrimitive(PrimitiveMetaData primitive)
   {
      this.primitive = primitive;
   }
   
   @ManagementProperty
   public List<String> getList()
   {
      return list;
   }
   
   public void setList(List<String> list)
   {
      this.list = list;
   }

   @ManagementProperty
   public Map<String, String> getTestMap()
   {
      return testMap;
   }
   
   public void setTestMap(Map<String, String> testMap)
   {
      this.testMap = testMap;
   }

   @ManagementProperty
   public Character[] getCharArray()
   {
      return charArray;
   }
   
   public void setCharArray(Character[] charArray)
   {
      this.charArray = charArray;
   }

   @ManagementProperty
   public char[][] getTest2dChar()
   {
      return test2dChar;
   }
   
   public void setTest2dChar(char[][] test2dChar)
   {
      this.test2dChar = test2dChar;
   }

   @ManagementProperty
   public Map<Integer, String> getTable()
   {
      return table;
   }
   
   public void setTable(Map<Integer, String> table)
   {
      this.table = table;
   }
   
}
