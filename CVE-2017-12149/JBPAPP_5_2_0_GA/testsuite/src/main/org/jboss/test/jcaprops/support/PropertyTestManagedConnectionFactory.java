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

import java.io.PrintWriter;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;

/**
 * A PropertyTestManagedConnectionFactory.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81036 $
 */
public class PropertyTestManagedConnectionFactory implements ManagedConnectionFactory
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;
   
   private String stringRAR;
   private Boolean booleanRAR;
   private Byte byteRAR;
   private Character characterRAR;
   private Short shortRAR;
   private Integer integerRAR;
   private Long longRAR;
   private Float floatRAR;
   private Double doubleRAR;
   private String stringCD;
   private Boolean booleanCD;
   private Byte byteCD;
   private Character characterCD;
   private Short shortCD;
   private Integer integerCD;
   private Long longCD;
   private Float floatCD;
   private Double doubleCD;
   private String stringMCF;
   private Boolean booleanMCF;
   private Byte byteMCF;
   private Character characterMCF;
   private Short shortMCF;
   private Integer integerMCF;
   private Long longMCF;
   private Float floatMCF;
   private Double doubleMCF;

   public Boolean getBooleanCD()
   {
      return booleanCD;
   }

   public void setBooleanCD(Boolean booleanCD)
   {
      this.booleanCD = booleanCD;
   }

   public Boolean getBooleanMCF()
   {
      return booleanMCF;
   }

   public void setBooleanMCF(Boolean booleanMCF)
   {
      this.booleanMCF = booleanMCF;
   }

   public Boolean getBooleanRAR()
   {
      return booleanRAR;
   }

   public void setBooleanRAR(Boolean booleanRAR)
   {
      this.booleanRAR = booleanRAR;
   }

   public Byte getByteCD()
   {
      return byteCD;
   }

   public void setByteCD(Byte byteCD)
   {
      this.byteCD = byteCD;
   }

   public Byte getByteMCF()
   {
      return byteMCF;
   }

   public void setByteMCF(Byte byteMCF)
   {
      this.byteMCF = byteMCF;
   }

   public Byte getByteRAR()
   {
      return byteRAR;
   }

   public void setByteRAR(Byte byteRAR)
   {
      this.byteRAR = byteRAR;
   }

   public Character getCharacterCD()
   {
      return characterCD;
   }

   public void setCharacterCD(Character characterCD)
   {
      this.characterCD = characterCD;
   }

   public Character getCharacterMCF()
   {
      return characterMCF;
   }

   public void setCharacterMCF(Character characterMCF)
   {
      this.characterMCF = characterMCF;
   }

   public Character getCharacterRAR()
   {
      return characterRAR;
   }

   public void setCharacterRAR(Character characterRAR)
   {
      this.characterRAR = characterRAR;
   }

   public Double getDoubleCD()
   {
      return doubleCD;
   }

   public void setDoubleCD(Double doubleCD)
   {
      this.doubleCD = doubleCD;
   }

   public Double getDoubleMCF()
   {
      return doubleMCF;
   }

   public void setDoubleMCF(Double doubleMCF)
   {
      this.doubleMCF = doubleMCF;
   }

   public Double getDoubleRAR()
   {
      return doubleRAR;
   }

   public void setDoubleRAR(Double doubleRAR)
   {
      this.doubleRAR = doubleRAR;
   }

   public Float getFloatCD()
   {
      return floatCD;
   }

   public void setFloatCD(Float floatCD)
   {
      this.floatCD = floatCD;
   }

   public Float getFloatMCF()
   {
      return floatMCF;
   }

   public void setFloatMCF(Float floatMCF)
   {
      this.floatMCF = floatMCF;
   }

   public Float getFloatRAR()
   {
      return floatRAR;
   }

   public void setFloatRAR(Float floatRAR)
   {
      this.floatRAR = floatRAR;
   }

   public Integer getIntegerCD()
   {
      return integerCD;
   }

   public void setIntegerCD(Integer integerCD)
   {
      this.integerCD = integerCD;
   }

   public Integer getIntegerMCF()
   {
      return integerMCF;
   }

   public void setIntegerMCF(Integer integerMCF)
   {
      this.integerMCF = integerMCF;
   }

   public Integer getIntegerRAR()
   {
      return integerRAR;
   }

   public void setIntegerRAR(Integer integerRAR)
   {
      this.integerRAR = integerRAR;
   }

   public Long getLongCD()
   {
      return longCD;
   }

   public void setLongCD(Long longCD)
   {
      this.longCD = longCD;
   }

   public Long getLongMCF()
   {
      return longMCF;
   }

   public void setLongMCF(Long longMCF)
   {
      this.longMCF = longMCF;
   }

   public Long getLongRAR()
   {
      return longRAR;
   }

   public void setLongRAR(Long longRAR)
   {
      this.longRAR = longRAR;
   }

   public Short getShortCD()
   {
      return shortCD;
   }

   public void setShortCD(Short shortCD)
   {
      this.shortCD = shortCD;
   }

   public Short getShortMCF()
   {
      return shortMCF;
   }

   public void setShortMCF(Short shortMCF)
   {
      this.shortMCF = shortMCF;
   }

   public Short getShortRAR()
   {
      return shortRAR;
   }

   public void setShortRAR(Short shortRAR)
   {
      this.shortRAR = shortRAR;
   }

   public String getStringCD()
   {
      return stringCD;
   }

   public void setStringCD(String stringCD)
   {
      this.stringCD = stringCD;
   }

   public String getStringMCF()
   {
      return stringMCF;
   }

   public void setStringMCF(String stringMCF)
   {
      this.stringMCF = stringMCF;
   }

   public String getStringRAR()
   {
      return stringRAR;
   }

   public void setStringRAR(String stringRAR)
   {
      this.stringRAR = stringRAR;
   }

   public Object createConnectionFactory() throws ResourceException
   {
      return null;
   }

   public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException
   {
      return new PropertyTestConnectionFactoryImpl(cxManager);
   }

   public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException
   {
      return new PropertyTestManagedConnection(this);
   }

   public PrintWriter getLogWriter() throws ResourceException
   {
      return null;
   }

   public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException
   {
      return (ManagedConnection) connectionSet.iterator().next();
   }

   public void setLogWriter(PrintWriter out) throws ResourceException
   {
   }

}
