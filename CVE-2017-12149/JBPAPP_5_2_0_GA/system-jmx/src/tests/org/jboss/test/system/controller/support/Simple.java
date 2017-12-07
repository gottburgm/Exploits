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

import org.jboss.system.ServiceMBeanSupport;
import org.jboss.test.system.controller.integration.support.SimpleBean;
import org.jboss.util.NotImplementedException;
import org.w3c.dom.Element;

/**
 * Simple.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class Simple extends ServiceMBeanSupport implements SimpleMBean, SimpleStandardMBeanInterface
{
   public String constructorUsed;
   
   public int createOrder;
   public int startOrder;
   public int stopOrder;
   public int destroyOrder;
   
   boolean touched = false;
   
   /** An object */
   private Object object;
   
   /** A string */
   private String aString;

   /** Byte */
   private Byte aByte;

   /** Boolean */
   private Boolean aBoolean;

   /** Character */
   private Character aCharacter;

   /** Short */
   private Short aShort;

   /** Int */
   private Integer anInt;

   /** Long */
   private Long aLong;

   /** Float */
   private Float aFloat;

   /** Double */
   private Double aDouble;

   /** Date */
   private Date aDate;

   /** BigDecimal */
   private BigDecimal aBigDecimal;

   /** BigDecimal */
   private BigInteger aBigInteger;

   /** byte */
   private byte abyte;

   /** boolean */
   private boolean aboolean;

   /** char */
   private char achar;

   /** short */
   private short ashort;

   /** int */
   private int anint;

   /** long */
   private long along;

   /** float */
   private float afloat;

   /** double */
   private double adouble;

   /** number */
   private Number aNumber;
   
   /** objectName */
   private ObjectName objectName;
   
   /** objectNames */
   private Collection<ObjectName> objectNames;
   
   /** First attribute */
   private String attribute1;
   
   /** Second attribute */
   private String attribute2;
   
   /** First attribute */
   private ObjectName objectNameAttribute1;
   
   /** Second attribute */
   private ObjectName objectNameAttribute2;
   
   /** First attribute */
   private Collection<ObjectName> objectNamesAttribute1;
   
   /** Second attribute */
   private Collection<ObjectName> objectNamesAttribute2;

   private SimpleStandardMBeanInterface proxy;

   private Element element;

   private JavaBean javaBean;
   
   private Simple simple;
   
   private SimpleBean simpleBean;

   public Simple()
   {
      constructorUsed = "()";
   }
   
   public Simple(String string)
   {
      constructorUsed = "(java.lang.String)";
      aString = string;
      if ("error".equals(string))
         throw new Error();
      if ("exception".equals(string))
         throw new RuntimeException();
   }
   
   public Simple(int integer)
   {
      constructorUsed = "(int)";
      anint = integer;
   }
   
   public Simple(int integer, float number)
   {
      constructorUsed = "(int,float)";
      anint = integer;
      afloat = number;
   }
   
   public Simple getInstance()
   {
      return this;
   }
   
   protected void createService()
   {
      createOrder = Order.getOrder();
      if ("ERRORINCREATE".equals(aString))
         throw new Error("BROKEN CREATE");
   }
   
   protected void startService()
   {
      startOrder = Order.getOrder();
      if ("ERRORINSTART".equals(aString))
         throw new Error("BROKEN START");
   }
   
   protected void stopService()
   {
      stopOrder = Order.getOrder();
      if ("ERRORINSTOP".equals(aString))
         throw new Error("BROKEN STOP");
   }
   
   protected void destroyService()
   {
      destroyOrder = Order.getOrder();
      if ("ERRORINDESTROY".equals(aString))
         throw new Error("BROKEN DESTROY");
   }

   public Object getObject()
   {
      return object;
   }

   public void setObject(Object object)
   {
      this.object = object;
   }

   public BigDecimal getABigDecimal()
   {
      return aBigDecimal;
   }

   public void setABigDecimal(BigDecimal bigDecimal)
   {
      aBigDecimal = bigDecimal;
   }

   public BigInteger getABigInteger()
   {
      return aBigInteger;
   }

   public void setABigInteger(BigInteger bigInteger)
   {
      aBigInteger = bigInteger;
   }

   public boolean isAboolean()
   {
      return aboolean;
   }

   public void setAboolean(boolean aboolean)
   {
      this.aboolean = aboolean;
   }

   public Boolean getABoolean()
   {
      return aBoolean;
   }

   public void setABoolean(Boolean boolean1)
   {
      aBoolean = boolean1;
   }

   public Number getANumber()
   {
      return aNumber;
   }

   public void setANumber(Number number)
   {
      aNumber = number;
   }

   public byte getAbyte()
   {
      return abyte;
   }

   public void setAbyte(byte abyte)
   {
      this.abyte = abyte;
   }

   public Byte getAByte()
   {
      return aByte;
   }

   public void setAByte(Byte byte1)
   {
      aByte = byte1;
   }

   public char getAchar()
   {
      return achar;
   }

   public void setAchar(char achar)
   {
      this.achar = achar;
   }

   public Character getACharacter()
   {
      return aCharacter;
   }

   public void setACharacter(Character character)
   {
      aCharacter = character;
   }

   public Date getADate()
   {
      return aDate;
   }

   public void setADate(Date date)
   {
      aDate = date;
   }

   public double getAdouble()
   {
      return adouble;
   }

   public void setAdouble(double adouble)
   {
      this.adouble = adouble;
   }

   public Double getADouble()
   {
      return aDouble;
   }

   public void setADouble(Double double1)
   {
      aDouble = double1;
   }

   public float getAfloat()
   {
      return afloat;
   }

   public void setAfloat(float afloat)
   {
      this.afloat = afloat;
   }

   public Float getAFloat()
   {
      return aFloat;
   }

   public void setAFloat(Float float1)
   {
      aFloat = float1;
   }

   public long getAlong()
   {
      return along;
   }

   public void setAlong(long along)
   {
      this.along = along;
   }

   public Long getALong()
   {
      return aLong;
   }

   public void setALong(Long long1)
   {
      aLong = long1;
   }

   public int getAnint()
   {
      return anint;
   }

   public void setAnint(int anint)
   {
      this.anint = anint;
   }

   public Integer getAnInt()
   {
      return anInt;
   }

   public void setAnInt(Integer anInt)
   {
      this.anInt = anInt;
   }

   public short getAshort()
   {
      return ashort;
   }

   public void setAshort(short ashort)
   {
      this.ashort = ashort;
   }

   public Short getAShort()
   {
      return aShort;
   }

   public void setAShort(Short short1)
   {
      aShort = short1;
   }

   public String getAString()
   {
      return aString;
   }

   public void setAString(String string)
   {
      aString = string;
      if ("ERRORINPROPERTY".equals(aString))
         throw new Error("BROKEN PROPERTY");
   }

   public ObjectName getObjectName()
   {
      return objectName;
   }

   public void setObjectName(ObjectName objectName)
   {
      this.objectName = objectName;
   }

   public Collection<ObjectName> getObjectNames()
   {
      return objectNames;
   }

   public void setObjectNames(Collection<ObjectName> objectNames)
   {
      this.objectNames = objectNames;
   }

   public String echoReverse(String test)
   {
      StringBuilder builder = new StringBuilder(test);
      return builder.reverse().toString();
   }
   
   public void touch()
   {
      this.touched = true;
   }
   
   public boolean isTouched()
   {
      return touched;
   }

   public void setBrokenAttribute(String broken)
   {
      throw new Error("BROKEN");
   }

   public String getAttribute1()
   {
      return attribute1;
   }

   public void setAttribute1(String attribute1)
   {
      this.attribute1 = attribute1;
   }

   public String getAttribute2()
   {
      return attribute2;
   }

   public void setAttribute2(String attribute2)
   {
      this.attribute2 = attribute2;
   }
   
   public void setBrokenObjectNameAttribute(ObjectName broken)
   {
      throw new Error("BROKEN");
   }

   public ObjectName getObjectNameAttribute1()
   {
      return objectNameAttribute1;
   }

   public void setObjectNameAttribute1(ObjectName objectNameAttribute1)
   {
      this.objectNameAttribute1 = objectNameAttribute1;
   }

   public ObjectName getObjectNameAttribute2()
   {
      return objectNameAttribute2;
   }

   public void setObjectNameAttribute2(ObjectName objectNameAttribute2)
   {
      this.objectNameAttribute2 = objectNameAttribute2;
   }

   public void setBrokenObjectNamesAttribute(Collection<ObjectName> broken)
   {
      throw new Error("BROKEN");
   }

   public Collection<ObjectName> getObjectNamesAttribute1()
   {
      return objectNamesAttribute1;
   }
   
   public void setObjectNamesAttribute1(Collection<ObjectName> objectNamesAttribute1)
   {
      this.objectNamesAttribute1 = objectNamesAttribute1;
   }

   public Collection<ObjectName> getObjectNamesAttribute2()
   {
      return objectNamesAttribute2;
   }

   public void setObjectNamesAttribute2(Collection<ObjectName> objectNamesAttribute2)
   {
      this.objectNamesAttribute2 = objectNamesAttribute2;
   }

   public String getReadOnly()
   {
      return "ReadOnly!";
   }
   
   public void setReadOnly(String readOnly)
   {
      throw new NotImplementedException("THIS ATTRIBUTE SHOULD BE READ ONLY!");
   }

   public SimpleStandardMBeanInterface getProxy()
   {
      return proxy;
   }

   public void setProxy(SimpleStandardMBeanInterface proxy)
   {
      this.proxy = proxy;
   }
   
   public void touchProxy()
   {
      proxy.touch();
   }

   public Element getElement()
   {
      return element;
   }

   public void setElement(Element element)
   {
      this.element = element;
   }

   public JavaBean getJavaBean()
   {
      return javaBean;
   }

   public void setJavaBean(JavaBean javaBean)
   {
      this.javaBean = javaBean;
   }
   
   public Simple getSimple()
   {
      return simple;
   }

   public void setSimple(Simple simple)
   {
      this.simple = simple;
   }

   public SimpleBean getSimpleBean()
   {
      return simpleBean;
   }

   public void setSimpleBean(SimpleBean simpleBean)
   {
      this.simpleBean = simpleBean;
   }
}
