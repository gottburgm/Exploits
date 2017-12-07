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

import org.jboss.mx.mxbean.MXBeanSupport;

/**
 * SimpleMXBeanSupport.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class SimpleMXBeanSupport extends MXBeanSupport implements SimpleMXBeanSupportMXBean
{
   private BigDecimal bigDecimal = bigDecimalValue;
   private BigInteger bigInteger = bigIntegerValue;
   private Boolean booleanV = booleanValue;
   private Byte byteV = byteValue;
   private Character characterV = characterValue;
   private Date date = dateValue;
   private Double doubleV = doubleValue;
   private Float floatV = floatValue;
   private Integer integer = integerValue;
   private Long longV = longValue;
   private ObjectName objectName = objectNameValue;
   private boolean primitiveBoolean = primitiveBooleanValue;
   private byte primitiveByte = primitiveByteValue;
   private char primitiveChar = primitiveCharValue;
   private double primitiveDouble = primitiveDoubleValue;
   private float primitiveFloat = primitiveFloatValue;
   private int primitiveInt = primitiveIntValue;
   private long primitiveLong = primitiveLongValue;
   private Short shortV = shortValue; 
   private short primitiveShort = primitiveShortValue;
   private String string = stringValue;

   public BigDecimal getBigDecimal()
   {
      return bigDecimal;
   }
   
   public BigInteger getBigInteger()
   {
      return bigInteger;
   }

   public Boolean getBoolean()
   {
      return booleanV;
   }

   public Byte getByte()
   {
      return byteV;
   }
   
   public Character getCharacter()
   {
      return characterV;
   }

   public Date getDate()
   {
      return date;
   }

   public Double getDouble()
   {
      return doubleV;
   }

   public Float getFloat()
   {
      return floatV;
   }

   public Integer getInteger()
   {
      return integer;
   }

   public Long getLong()
   {
      return longV;
   }

   public ObjectName getObjectName()
   {
      return objectName;
   }

   public byte getPrimitiveByte()
   {
      return primitiveByte;
   }

   public char getPrimitiveChar()
   {
      return primitiveChar;
   }

   public double getPrimitiveDouble()
   {
      return primitiveDouble;
   }

   public float getPrimitiveFloat()
   {
      return primitiveFloat;
   }

   public int getPrimitiveInt()
   {
      return primitiveInt;
   }

   public long getPrimitiveLong()
   {
      return primitiveLong;
   }

   public short getPrimitiveShort()
   {
      return primitiveShort;
   }

   public Short getShort()
   {
      return shortV;
   }

   public String getString()
   {
      return string;
   }

   public boolean isPrimitiveBoolean()
   {
      return primitiveBoolean;
   }

   public void setBoolean(Boolean booleanV)
   {
      this.booleanV = booleanV;
   }

   public void setByte(Byte byteV)
   {
      this.byteV = byteV;
   }

   public void setCharacter(Character characterV)
   {
      this.characterV = characterV;
   }
   
   public void setDouble(Double doubleV)
   {
      this.doubleV = doubleV;
   }
   
   public void setFloat(Float floatV)
   {
      this.floatV = floatV;
   }

   public void setLong(Long longV)
   {
      this.longV = longV;
   }

   public void setShort(Short shortV)
   {
      this.shortV = shortV;
   }

   public void setBigDecimal(BigDecimal bigDecimal)
   {
      this.bigDecimal = bigDecimal;
   }

   public void setBigInteger(BigInteger bigInteger)
   {
      this.bigInteger = bigInteger;
   }

   public void setDate(Date date)
   {
      this.date = date;
   }

   public void setInteger(Integer integer)
   {
      this.integer = integer;
   }

   public void setObjectName(ObjectName objectName)
   {
      this.objectName = objectName;
   }

   public void setPrimitiveBoolean(boolean primitiveBoolean)
   {
      this.primitiveBoolean = primitiveBoolean;
   }

   public void setPrimitiveByte(byte primitiveByte)
   {
      this.primitiveByte = primitiveByte;
   }

   public void setPrimitiveChar(char primitiveChar)
   {
      this.primitiveChar = primitiveChar;
   }

   public void setPrimitiveDouble(double primitiveDouble)
   {
      this.primitiveDouble = primitiveDouble;
   }

   public void setPrimitiveFloat(float primitiveFloat)
   {
      this.primitiveFloat = primitiveFloat;
   }

   public void setPrimitiveInt(int primitiveInt)
   {
      this.primitiveInt = primitiveInt;
   }

   public void setPrimitiveLong(long primitiveLong)
   {
      this.primitiveLong = primitiveLong;
   }

   public void setPrimitiveShort(short primitiveShort)
   {
      this.primitiveShort = primitiveShort;
   }

   public void setString(String string)
   {
      this.string = string;
   }

   public String echoReverse(String string)
   {
      StringBuilder builder = new StringBuilder(string);
      builder.reverse();
      return builder.toString();
   }
}
