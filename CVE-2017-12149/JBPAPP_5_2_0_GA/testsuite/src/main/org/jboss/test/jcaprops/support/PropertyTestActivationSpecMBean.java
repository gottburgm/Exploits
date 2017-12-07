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

import javax.management.ObjectName;

import org.jboss.mx.util.ObjectNameFactory;

/**
 * A PropertyTestActivationSpecMBean.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81036 $
 */
public interface PropertyTestActivationSpecMBean
{
   ObjectName NAME = ObjectNameFactory.create("jboss.test.jcaprops:name=PropertyTestActivationSpec");
   
   String getStringAS();
   void setStringAS(String value);
   
   Boolean getBooleanAS();
   void setBooleanAS(Boolean value);
   
   Byte getByteAS();
   void setByteAS(Byte value);
   
   Character getCharacterAS();
   void setCharacterAS(Character value);
   
   Short getShortAS();
   void setShortAS(Short value);
   
   Integer getIntegerAS();
   void setIntegerAS(Integer value);
   
   Long getLongAS();
   void setLongAS(Long value);
   
   Float getFloatAS();
   void setFloatAS(Float value);
   
   Double getDoubleAS();
   void setDoubleAS(Double value);
}
