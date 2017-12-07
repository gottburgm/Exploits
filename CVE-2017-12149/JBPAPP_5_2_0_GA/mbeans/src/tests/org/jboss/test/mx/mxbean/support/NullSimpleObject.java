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
package org.jboss.test.mx.mxbean.support;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import javax.management.ObjectName;

/**
 * NullSimpleObject.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class NullSimpleObject implements SimpleInterface
{
   public BigDecimal getBigDecimal()
   {
      return null;
   }

   public BigInteger getBigInteger()
   {
      return null;
   }

   public Boolean getBoolean()
   {
      return null;
   }

   public Byte getByte()
   {
      return null;
   }

   public Character getCharacter()
   {
      return null;
   }

   public Date getDate()
   {
      return null;
   }

   public Double getDouble()
   {
      return null;
   }

   public Float getFloat()
   {
      return null;
   }

   public Integer getInteger()
   {
      return null;
   }

   public Long getLong()
   {
      return null;
   }

   public ObjectName getObjectName()
   {
      return null;
   }

   public byte getPrimitiveByte()
   {
      return primitiveByteValue;
   }

   public char getPrimitiveChar()
   {
      return primitiveCharValue;
   }

   public double getPrimitiveDouble()
   {
      return primitiveDoubleValue;
   }

   public float getPrimitiveFloat()
   {
      return primitiveFloatValue;
   }

   public int getPrimitiveInt()
   {
      return primitiveIntValue;
   }

   public long getPrimitiveLong()
   {
      return primitiveLongValue;
   }

   public short getPrimitiveShort()
   {
      return primitiveShortValue;
   }

   public Short getShort()
   {
      return null;
   }

   public String getString()
   {
      return null;
   }

   public boolean isPrimitiveBoolean()
   {
      return primitiveBooleanValue;
   }
}
