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
package org.jboss.test.system.controller.support;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;

import javax.management.ObjectName;

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.ServiceMBean;
import org.jboss.test.system.controller.integration.support.SimpleBean;
import org.w3c.dom.Element;

/**
 * SimpleMBean.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public interface SimpleMBean extends ServiceMBean
{
   ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.test:type=test");
   
   Simple getInstance();

   Object getObject();
   
   void setObject(Object object);
   
   BigDecimal getABigDecimal();

   void setABigDecimal(BigDecimal bigDecimal);

   BigInteger getABigInteger();

   void setABigInteger(BigInteger bigInteger);

   boolean isAboolean();

   void setAboolean(boolean aboolean);

   Boolean getABoolean();

   void setABoolean(Boolean boolean1);

   Number getANumber();

   void setANumber(Number number);

   byte getAbyte();

   void setAbyte(byte abyte);

   Byte getAByte();

   void setAByte(Byte byte1);

   char getAchar();

   void setAchar(char achar);

   Character getACharacter();

   void setACharacter(Character character);

   Date getADate();

   void setADate(Date date);

   double getAdouble();

   void setAdouble(double adouble);

   Double getADouble();

   void setADouble(Double double1);

   float getAfloat();

   void setAfloat(float afloat);

   Float getAFloat();

   void setAFloat(Float float1);

   long getAlong();

   void setAlong(long along);

   Long getALong();

   void setALong(Long long1);

   int getAnint();

   void setAnint(int anint);

   Integer getAnInt();

   void setAnInt(Integer anInt);

   short getAshort();

   void setAshort(short ashort);

   Short getAShort();

   void setAShort(Short short1);

   String getAString();

   void setAString(String string);

   ObjectName getObjectName();

   void setObjectName(ObjectName objectName);

   Collection<ObjectName> getObjectNames();

   void setObjectNames(Collection<ObjectName> objectNames);
   
   void setBrokenAttribute(String broken);

   String getAttribute1();

   void setAttribute1(String attribute1);

   String getAttribute2();

   void setAttribute2(String attribute2);
   
   void setBrokenObjectNameAttribute(ObjectName broken);

   ObjectName getObjectNameAttribute1();

   void setObjectNameAttribute1(ObjectName objectNameAttribute1);

   ObjectName getObjectNameAttribute2();

   void setObjectNameAttribute2(ObjectName objectNameAttribute2);

   void setBrokenObjectNamesAttribute(Collection<ObjectName> broken);

   Collection<ObjectName> getObjectNamesAttribute1();
   
   void setObjectNamesAttribute1(Collection<ObjectName> objectNamesAttribute1);

   Collection<ObjectName> getObjectNamesAttribute2();

   void setObjectNamesAttribute2(Collection<ObjectName> objectNamesAttribute2);
   
   String getReadOnly();

   SimpleStandardMBeanInterface getProxy();

   void setProxy(SimpleStandardMBeanInterface proxy);

   Element getElement();

   void setElement(Element element);

   JavaBean getJavaBean();

   void setJavaBean(JavaBean javaBean);

   Simple getSimple();

   void setSimple(Simple simple);
   
   SimpleBean getSimpleBean();

   void setSimpleBean(SimpleBean simpleBean);
}
