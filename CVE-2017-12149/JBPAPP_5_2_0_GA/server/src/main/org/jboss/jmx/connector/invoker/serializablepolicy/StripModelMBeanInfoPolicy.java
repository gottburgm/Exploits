/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jmx.connector.invoker.serializablepolicy;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.modelmbean.ModelMBeanInfo;

import org.jboss.invocation.MarshalledInvocation;
import org.jboss.jmx.connector.invoker.SerializablePolicy;

/**
 * A policy that converts ModelMBeanInfo to MBeanInfo.
 * 
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @author <a href="mailto:fabcipriano@yahoo.com.br">Fabiano C. de Oliveira</a>
 * @version $Revision: 85945 $
 */
public class StripModelMBeanInfoPolicy implements SerializablePolicy
{   
   public Object filter(MarshalledInvocation input, Object result) throws Throwable
   {
      if ("getMBeanInfo".equals(input.getMethod().getName()) && (result instanceof ModelMBeanInfo))
      {
         MBeanInfo info = (MBeanInfo)result;

         result = new MBeanInfo(
               info.getClassName(),
               info.getDescription(),
               deepCopy(info.getAttributes()), // Strip the Descriptors
               info.getConstructors(),
               info.getOperations(),
               info.getNotifications());
      }
      return result;
   }
   
   private MBeanAttributeInfo[] deepCopy(MBeanAttributeInfo[] attrs)
   {
      MBeanAttributeInfo[] copy = new MBeanAttributeInfo[attrs.length];
      for (int i = 0; i < attrs.length; i++)
      {
         MBeanAttributeInfo attr = attrs[i];
         copy[i] = new MBeanAttributeInfo(
               attr.getName(),
               attr.getType(),
               attr.getDescription(),
               attr.isReadable(),
               attr.isWritable(),
               attr.isIs());
      }
      return copy;
   }
}
