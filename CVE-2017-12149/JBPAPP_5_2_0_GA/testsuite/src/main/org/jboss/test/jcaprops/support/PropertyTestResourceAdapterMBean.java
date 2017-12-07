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
 * A PropertyTestResourceAdapterMBean.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81036 $
 */
public interface PropertyTestResourceAdapterMBean
{
   ObjectName NAME = ObjectNameFactory.create("jboss.test.jcaprops:name=PropertyTestResourceAdapter");
   
   String getStringRAR();
   void setStringRAR(String value);
   
   Boolean getBooleanRAR();
   void setBooleanRAR(Boolean value);
   
   Byte getByteRAR();
   void setByteRAR(Byte value);
   
   Character getCharacterRAR();
   void setCharacterRAR(Character value);
   
   Short getShortRAR();
   void setShortRAR(Short value);
   
   Integer getIntegerRAR();
   void setIntegerRAR(Integer value);
   
   Long getLongRAR();
   void setLongRAR(Long value);
   
   Float getFloatRAR();
   void setFloatRAR(Float value);
   
   Double getDoubleRAR();
   void setDoubleRAR(Double value);
}
