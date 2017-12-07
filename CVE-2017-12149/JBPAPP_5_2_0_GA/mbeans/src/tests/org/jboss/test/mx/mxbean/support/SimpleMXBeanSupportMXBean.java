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

/**
 * SimpleMXBeanSupportMXBean.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public interface SimpleMXBeanSupportMXBean extends SimpleInterface
{
   /** The Object name */
   ObjectName REGISTERED_OBJECT_NAME = ObjectNameFactory.create("test:test=SimpleMXBeanSupport");

   void setBoolean(Boolean booleanV);

   void setByte(Byte byteV);

   void setCharacter(Character characterV);
   
   void setDouble(Double doubleV);
   
   void setFloat(Float floatV);

   void setLong(Long longV);

   void setShort(Short shortV);

   void setBigDecimal(BigDecimal bigDecimal);

   void setBigInteger(BigInteger bigInteger);

   void setDate(Date date);

   void setInteger(Integer integer);

   void setObjectName(ObjectName objectName);

   void setPrimitiveBoolean(boolean primitiveBoolean);

   void setPrimitiveByte(byte primitiveByte);

   void setPrimitiveChar(char primitiveChar);

   void setPrimitiveDouble(double primitiveDouble);

   void setPrimitiveFloat(float primitiveFloat);

   void setPrimitiveInt(int primitiveInt);

   void setPrimitiveLong(long primitiveLong);

   void setPrimitiveShort(short primitiveShort);

   void setString(String string);
   
   String echoReverse(String string);
}
