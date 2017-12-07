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

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.test.mx.mxbean.test.AbstractMXBeanTest;

/**
 * SimpleInterface.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public interface SimpleInterface
{
   String BIG_DECIMAL = "bigDecimal";

   BigDecimal bigDecimalValue = new BigDecimal("12e4");
   BigDecimal bigDecimalChangedValue = new BigDecimal("12e5");

   String BIG_INTEGER = "bigInteger";

   BigInteger bigIntegerValue = new BigInteger("123456");
   BigInteger bigIntegerChangedValue = new BigInteger("123457");

   String BOOLEAN = "boolean";

   Boolean booleanValue = Boolean.TRUE;
   Boolean booleanChangedValue = Boolean.FALSE;

   String BOOLEAN_PRIMITIVE = "primitiveBoolean";

   boolean primitiveBooleanValue = booleanValue.booleanValue();
   boolean primitiveBooleanChangedValue = booleanChangedValue.booleanValue();

   String BYTE = "byte";

   Byte byteValue = new Byte("12");
   Byte byteChangedValue = new Byte("13");

   String BYTE_PRIMITIVE = "primitiveByte";

   byte primitiveByteValue = byteValue.byteValue();
   byte primitiveByteChangedValue = byteChangedValue.byteValue();

   String CHARACTER = "character";

   Character characterValue = new Character('a');
   Character characterChangedValue = new Character('b');

   String CHAR_PRIMITIVE = "primitiveChar";

   char primitiveCharValue = characterValue.charValue();
   char primitiveCharChangedValue = characterChangedValue.charValue();

   String DATE = "date";

   Date dateValue = AbstractMXBeanTest.createDate(2001, 1, 1);
   Date dateChangedValue = AbstractMXBeanTest.createDate(2002, 2, 2);

   String DOUBLE = "double";

   Double doubleValue = new Double("3.14e12");
   Double doubleChangedValue = new Double("3.14e13");

   String DOUBLE_PRIMITIVE = "primitiveDouble";

   double primitiveDoubleValue = doubleValue.doubleValue();
   double primitiveDoubleChangedValue = doubleChangedValue.doubleValue();

   String FLOAT = "float";

   Float floatValue = new Float("3.14");
   Float floatChangedValue = new Float("3.15");

   String FLOAT_PRIMITIVE = "primitiveFloat";

   float primitiveFloatValue = floatValue.floatValue();
   float primitiveFloatChangedValue = floatChangedValue.floatValue();

   String INTEGER = "integer";

   Integer integerValue = new Integer("1234");
   Integer integerChangedValue = new Integer("1235");

   String INT_PRIMITIVE = "primitiveInt";

   int primitiveIntValue = integerValue.intValue();
   int primitiveIntChangedValue = integerChangedValue.intValue();

   String LONG = "long";

   Long longValue = new Long("12345");
   Long longChangedValue = new Long("12346");

   String LONG_PRIMITIVE = "primitiveLong";

   long primitiveLongValue = longValue.longValue();
   long primitiveLongChangedValue = longChangedValue.longValue();

   String OBJECT_NAME = "objectName";

   ObjectName objectNameValue = ObjectNameFactory.create("domain:key=property");
   ObjectName objectNameChangedValue = ObjectNameFactory.create("domain:key=property2");

   String SHORT = "short";

   Short shortValue = new Short("123");
   Short shortChangedValue = new Short("124");

   String SHORT_PRIMITIVE = "primitiveShort";

   short primitiveShortValue = shortValue.shortValue();
   short primitiveShortChangedValue = shortChangedValue.shortValue();

   String STRING = "string";

   String stringValue = new String("StringValue");
   String stringChangedValue = new String("ChangedValue");

   String[] KEYS =
   {
      BIG_DECIMAL,
      BIG_INTEGER,
      BOOLEAN,
      BOOLEAN_PRIMITIVE,
      BYTE,
      BYTE_PRIMITIVE,
      CHARACTER,
      CHAR_PRIMITIVE,
      DATE,
      DOUBLE,
      DOUBLE_PRIMITIVE,
      FLOAT,
      FLOAT_PRIMITIVE,
      INTEGER,
      INT_PRIMITIVE,
      LONG,
      LONG_PRIMITIVE,
      OBJECT_NAME,
      SHORT,
      SHORT_PRIMITIVE,
      STRING
   };

   Object[] VALUES = 
   {
      bigDecimalValue,
      bigIntegerValue,
      booleanValue,
      primitiveBooleanValue,
      byteValue,
      primitiveByteValue,
      characterValue,
      primitiveCharValue,
      dateValue,
      doubleValue,
      primitiveDoubleValue,
      floatValue,
      primitiveFloatValue,
      integerValue,
      primitiveIntValue,
      longValue,
      primitiveLongValue,
      objectNameValue,
      shortValue,
      primitiveShortValue,
      stringValue
   };

   Object[] CHANGED_VALUES = 
   {
      bigDecimalChangedValue,
      bigIntegerChangedValue,
      booleanChangedValue,
      primitiveBooleanChangedValue,
      byteChangedValue,
      primitiveByteChangedValue,
      characterChangedValue,
      primitiveCharChangedValue,
      dateChangedValue,
      doubleChangedValue,
      primitiveDoubleChangedValue,
      floatChangedValue,
      primitiveFloatChangedValue,
      integerChangedValue,
      primitiveIntChangedValue,
      longChangedValue,
      primitiveLongChangedValue,
      objectNameChangedValue,
      shortChangedValue,
      primitiveShortChangedValue,
      stringChangedValue
   };

   Class[] TYPES = 
   {
      BigDecimal.class,
      BigInteger.class,
      Boolean.class,
      Boolean.TYPE,
      Byte.class,
      Byte.TYPE,
      Character.class,
      Character.TYPE,
      Date.class,
      Double.class,
      Double.TYPE,
      Float.class,
      Float.TYPE,
      Integer.class,
      Integer.TYPE,
      Long.class,
      Long.TYPE,
      ObjectName.class,
      Short.class,
      Short.TYPE,
      String.class
   };
   
   Object[] NULL_VALUES = 
   {
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null
   };
   
   Object[] LEGAL_NULL_VALUES = 
   {
      null,
      null,
      null,
      primitiveBooleanValue,
      null,
      primitiveByteValue,
      null,
      primitiveCharValue,
      null,
      null,
      primitiveDoubleValue,
      null,
      primitiveFloatValue,
      null,
      primitiveIntValue,
      null,
      primitiveLongValue,
      null,
      null,
      primitiveShortValue,
      null,
   };

   BigDecimal getBigDecimal();

   BigInteger getBigInteger();

   boolean isPrimitiveBoolean();

   Boolean getBoolean();

   byte getPrimitiveByte();

   Byte getByte();

   char getPrimitiveChar();

   Character getCharacter();

   Date getDate();

   double getPrimitiveDouble();

   Double getDouble();

   float getPrimitiveFloat();

   Float getFloat();

   int getPrimitiveInt();

   Integer getInteger();

   long getPrimitiveLong();

   Long getLong();

   ObjectName getObjectName();

   short getPrimitiveShort();

   Short getShort();

   String getString();
}
