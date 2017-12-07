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
package org.jboss.test.server.profileservice.component.persistence.support;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class CompositeMetaData
{

   String string;
   char character;   
   Integer integer;
   
   public CompositeMetaData()
   {
      //
   }
   
   public CompositeMetaData(String string, char character, Integer integer)
   {
      this.string = string;
      this.character = character;
      this.integer = integer;
   }
   
   public String getString()
   {
      return string;
   }
   
   public void setString(String string)
   {
      this.string = string;
   }
   
   public char getCharacter()
   {
      return character;
   }
   
   public void setCharacter(char character)
   {
      this.character = character;
   }
   
   public Integer getInteger()
   {
      return integer;
   }
   
   public void setInteger(Integer integer)
   {
      this.integer = integer;
   }

}

