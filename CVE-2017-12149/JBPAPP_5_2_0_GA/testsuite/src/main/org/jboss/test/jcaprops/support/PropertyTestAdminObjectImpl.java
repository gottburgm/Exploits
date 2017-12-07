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
package org.jboss.test.jcaprops.support;

import java.io.Serializable;

/**
 * A PropertyTestAdminObjectImpl.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81036 $
 */
public class PropertyTestAdminObjectImpl implements Serializable, PropertyTestAdminObject
{

   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   private String stringAO;
   private Boolean booleanAO;
   private Byte byteAO;
   private Character characterAO;
   private Short shortAO;
   private Integer integerAO;
   private Long longAO;
   private Float floatAO;
   private Double doubleAO;
   private String stringAOMBean;
   private Boolean booleanAOMBean;
   private Byte byteAOMBean;
   private Character characterAOMBean;
   private Short shortAOMBean;
   private Integer integerAOMBean;
   private Long longAOMBean;
   private Float floatAOMBean;
   private Double doubleAOMBean;
   
   public Boolean getBooleanAO()
   {
      return booleanAO;
   }
   public void setBooleanAO(Boolean booleanAO)
   {
      this.booleanAO = booleanAO;
   }
   public Boolean getBooleanAOMBean()
   {
      return booleanAOMBean;
   }
   public void setBooleanAOMBean(Boolean booleanAOMBean)
   {
      this.booleanAOMBean = booleanAOMBean;
   }
   public Byte getByteAO()
   {
      return byteAO;
   }
   public void setByteAO(Byte byteAO)
   {
      this.byteAO = byteAO;
   }
   public Byte getByteAOMBean()
   {
      return byteAOMBean;
   }
   public void setByteAOMBean(Byte byteAOMBean)
   {
      this.byteAOMBean = byteAOMBean;
   }
   public Character getCharacterAO()
   {
      return characterAO;
   }
   public void setCharacterAO(Character characterAO)
   {
      this.characterAO = characterAO;
   }
   public Character getCharacterAOMBean()
   {
      return characterAOMBean;
   }
   public void setCharacterAOMBean(Character characterAOMBean)
   {
      this.characterAOMBean = characterAOMBean;
   }
   public Double getDoubleAO()
   {
      return doubleAO;
   }
   public void setDoubleAO(Double doubleAO)
   {
      this.doubleAO = doubleAO;
   }
   public Double getDoubleAOMBean()
   {
      return doubleAOMBean;
   }
   public void setDoubleAOMBean(Double doubleAOMBean)
   {
      this.doubleAOMBean = doubleAOMBean;
   }
   public Float getFloatAO()
   {
      return floatAO;
   }
   public void setFloatAO(Float floatAO)
   {
      this.floatAO = floatAO;
   }
   public Float getFloatAOMBean()
   {
      return floatAOMBean;
   }
   public void setFloatAOMBean(Float floatAOMBean)
   {
      this.floatAOMBean = floatAOMBean;
   }
   public Integer getIntegerAO()
   {
      return integerAO;
   }
   public void setIntegerAO(Integer integerAO)
   {
      this.integerAO = integerAO;
   }
   public Integer getIntegerAOMBean()
   {
      return integerAOMBean;
   }
   public void setIntegerAOMBean(Integer integerAOMBean)
   {
      this.integerAOMBean = integerAOMBean;
   }
   public Long getLongAO()
   {
      return longAO;
   }
   public void setLongAO(Long longAO)
   {
      this.longAO = longAO;
   }
   public Long getLongAOMBean()
   {
      return longAOMBean;
   }
   public void setLongAOMBean(Long longAOMBean)
   {
      this.longAOMBean = longAOMBean;
   }
   public Short getShortAO()
   {
      return shortAO;
   }
   public void setShortAO(Short shortAO)
   {
      this.shortAO = shortAO;
   }
   public Short getShortAOMBean()
   {
      return shortAOMBean;
   }
   public void setShortAOMBean(Short shortAOMBean)
   {
      this.shortAOMBean = shortAOMBean;
   }
   public String getStringAO()
   {
      return stringAO;
   }
   public void setStringAO(String stringAO)
   {
      this.stringAO = stringAO;
   }
   public String getStringAOMBean()
   {
      return stringAOMBean;
   }
   public void setStringAOMBean(String stringAOMBean)
   {
      this.stringAOMBean = stringAOMBean;
   }
}
