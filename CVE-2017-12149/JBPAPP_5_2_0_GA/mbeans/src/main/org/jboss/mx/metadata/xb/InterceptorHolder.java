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
package org.jboss.mx.metadata.xb;

import org.jboss.mx.interceptor.Interceptor;
import org.jboss.util.NestedRuntimeException;
import org.jboss.xb.binding.GenericValueContainer;

import javax.xml.namespace.QName;

/**
 A container for the interceptor elements.
 
 @author Scott.Stark@jboss.org
 @version $Revision: 81026 $
 */
public class InterceptorHolder
   implements GenericValueContainer
{
   private Class clazz;

   public void addChild(QName name, Object value)
   {
      String lname = name.getLocalPart();
      if( lname.equals("code") )
      {
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         String cname = (String) value;
         try
         {
            clazz = loader.loadClass(cname);
         }
         catch(Throwable e)
         {
            throw new NestedRuntimeException("Unable to load code: "+cname, e);
         }
      }
   }

   public Object instantiate()
   {
      Interceptor i;
      // todo: Check for a ctor()
      try
      {
         i = (Interceptor) clazz.newInstance();
      }
      catch (Throwable e)
      {
         throw new NestedRuntimeException("Failed to instantiate interceptor", e);
      }
      return i;
   }

   public Class getTargetClass()
   {
      return Interceptor.class;
   }
}
