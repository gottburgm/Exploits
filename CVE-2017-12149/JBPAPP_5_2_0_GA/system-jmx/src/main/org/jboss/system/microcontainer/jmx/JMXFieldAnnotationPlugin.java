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
package org.jboss.system.microcontainer.jmx;

import java.lang.annotation.ElementType;

import org.jboss.reflect.spi.FieldInfo;

/**
 * Supporting @JMX on fields.
 *
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public class JMXFieldAnnotationPlugin extends JMXAnnotationPlugin<FieldInfo>
{
   protected boolean isElementTypeSupported(ElementType elementType)
   {
      return ElementType.FIELD == elementType;
   }

   @SuppressWarnings("deprecation")
   protected Class<?> getExposedInterface(FieldInfo info)
   {
      return info.getType().getType();
   }

   protected String getName(FieldInfo info)
   {
      return info.getName();
   }
}