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
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * A PropertyTestResourceAdapter.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81036 $
 */
public class PropertyTestResourceAdapter implements ResourceAdapter, PropertyTestResourceAdapterMBean
{
   private static final Logger log = Logger.getLogger(PropertyTestResourceAdapter.class);
   
   private String stringRAR;
   private Boolean booleanRAR;
   private Byte byteRAR;
   private Character characterRAR;
   private Short shortRAR;
   private Integer integerRAR;
   private Long longRAR;
   private Float floatRAR;
   private Double doubleRAR;
   
   public String getStringRAR()
   {
      return stringRAR;
   }
   
   public void setStringRAR(String string)
   {
      this.stringRAR = string;
   }
   
   public Boolean getBooleanRAR()
   {
      return booleanRAR;
   }

   public void setBooleanRAR(Boolean booleanRAR)
   {
      this.booleanRAR = booleanRAR;
   }

   public Byte getByteRAR()
   {
      return byteRAR;
   }

   public void setByteRAR(Byte byteRAR)
   {
      this.byteRAR = byteRAR;
   }

   public Character getCharacterRAR()
   {
      return characterRAR;
   }

   public void setCharacterRAR(Character characterRAR)
   {
      this.characterRAR = characterRAR;
   }

   public Double getDoubleRAR()
   {
      return doubleRAR;
   }

   public void setDoubleRAR(Double doubleRAR)
   {
      this.doubleRAR = doubleRAR;
   }

   public Float getFloatRAR()
   {
      return floatRAR;
   }

   public void setFloatRAR(Float floatRAR)
   {
      this.floatRAR = floatRAR;
   }

   public Integer getIntegerRAR()
   {
      return integerRAR;
   }

   public void setIntegerRAR(Integer integerRAR)
   {
      this.integerRAR = integerRAR;
   }

   public Long getLongRAR()
   {
      return longRAR;
   }

   public void setLongRAR(Long longRAR)
   {
      this.longRAR = longRAR;
   }

   public Short getShortRAR()
   {
      return shortRAR;
   }

   public void setShortRAR(Short shortRAR)
   {
      this.shortRAR = shortRAR;
   }

   public void start(BootstrapContext ctx) throws ResourceAdapterInternalException
   {
      registerMBean();
   }

   public void stop()
   {
      unregisterMBean();
   }
   
   public void endpointActivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) throws ResourceException
   {
      PropertyTestActivationSpec as = (PropertyTestActivationSpec) spec;
      as.registerMBean();
   }

   public void endpointDeactivation(MessageEndpointFactory endpointFactory, ActivationSpec spec)
   {
      PropertyTestActivationSpec as = (PropertyTestActivationSpec) spec;
      as.unregisterMBean();
   }

   public XAResource[] getXAResources(ActivationSpec[] specs) throws ResourceException
   {
      return new XAResource[0];
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
