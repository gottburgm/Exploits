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

import javax.management.MBeanServer;
import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;

import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * A PropertyTestActivationSpec.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81036 $
 */
public class PropertyTestActivationSpec implements ActivationSpec, PropertyTestActivationSpecMBean
{
   private static final Logger log = Logger.getLogger(PropertyTestActivationSpec.class);
   
   private String stringAS;
   private Boolean booleanAS;
   private Byte byteAS;
   private Character characterAS;
   private Short shortAS;
   private Integer integerAS;
   private Long longAS;
   private Float floatAS;
   private Double doubleAS;
   
   private PropertyTestResourceAdapter resourceAdapter;
   
   public String getStringAS()
   {
      return stringAS;
   }
   
   public void setStringAS(String string)
   {
      this.stringAS = string;
   }
   
   public Boolean getBooleanAS()
   {
      return booleanAS;
   }

   public void setBooleanAS(Boolean booleanAS)
   {
      this.booleanAS = booleanAS;
   }

   public Byte getByteAS()
   {
      return byteAS;
   }

   public void setByteAS(Byte byteAS)
   {
      this.byteAS = byteAS;
   }

   public Character getCharacterAS()
   {
      return characterAS;
   }

   public void setCharacterAS(Character characterAS)
   {
      this.characterAS = characterAS;
   }

   public Double getDoubleAS()
   {
      return doubleAS;
   }

   public void setDoubleAS(Double doubleAS)
   {
      this.doubleAS = doubleAS;
   }

   public Float getFloatAS()
   {
      return floatAS;
   }

   public void setFloatAS(Float floatAS)
   {
      this.floatAS = floatAS;
   }

   public Integer getIntegerAS()
   {
      return integerAS;
   }

   public void setIntegerAS(Integer integerAS)
   {
      this.integerAS = integerAS;
   }

   public Long getLongAS()
   {
      return longAS;
   }

   public void setLongAS(Long longAS)
   {
      this.longAS = longAS;
   }

   public Short getShortAS()
   {
      return shortAS;
   }

   public void setShortAS(Short shortAS)
   {
      this.shortAS = shortAS;
   }
   
   public void validate() throws InvalidPropertyException
   {
   }

   public ResourceAdapter getResourceAdapter()
   {
      return resourceAdapter;
   }

   public void setResourceAdapter(ResourceAdapter ra) throws ResourceException
   {
      this.resourceAdapter = (PropertyTestResourceAdapter) ra;
   }

   protected void registerMBean() throws ResourceAdapterInternalException
   {
      MBeanServer server = MBeanServerLocator.locateJBoss();
      try
      {
         server.registerMBean(this, NAME);
      }
      catch (Exception e)
      {
         throw new ResourceAdapterInternalException(e);
      }
   }
   
   protected void unregisterMBean()
   {
      MBeanServer server = MBeanServerLocator.locateJBoss();
      try
      {
         server.unregisterMBean(NAME);
      }
      catch (Exception e)
      {
         log.warn("Unable to unregisterMBean", e);
      }
   }
}
