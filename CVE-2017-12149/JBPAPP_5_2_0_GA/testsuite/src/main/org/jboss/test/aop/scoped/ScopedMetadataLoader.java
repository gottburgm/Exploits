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
package org.jboss.test.aop.scoped;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;

import org.jboss.aop.Advisor;
import org.jboss.aop.metadata.ClassMetaDataBinding;
import org.jboss.aop.metadata.ClassMetaDataLoader;
import org.jboss.aop.util.XmlHelper;
import org.w3c.dom.Element;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 85945 $
 */
public class ScopedMetadataLoader implements ClassMetaDataLoader
{
   public ClassMetaDataBinding importMetaData(Element element, String name, String tag, String classExpr) throws Exception
   {
      ScopedMetadataBinding data = new ScopedMetadataBinding(this, name, tag, classExpr);
      String value = XmlHelper.getOptionalChildContent(element, "data");
      data.setData(value);
      
      return data;
   }

   public void bind(Advisor advisor, ClassMetaDataBinding data, CtMethod[] methods, CtField[] fields, CtConstructor[] constructors) throws Exception
   {
      for (int i = 0 ; i < methods.length ; i++)
      {
         advisor.getMethodMetaData().addMethodMetaData(methods[i], "custom", "data", ((ScopedMetadataBinding)data).getData());
      }
      for (int i = 0 ; i < constructors.length ; i++)
      {
         advisor.getConstructorMetaData().addConstructorMetaData(constructors[i], "custom", "data", ((ScopedMetadataBinding)data).getData());
      }
   }

   public void bind(Advisor advisor, ClassMetaDataBinding data, Method[] methods, Field[] fields, Constructor[] constructors) throws Exception
   {
      for (int i = 0 ; i < methods.length ; i++)
      {
         advisor.getMethodMetaData().addMethodMetaData(methods[i], "custom", "data", ((ScopedMetadataBinding)data).getData());
      }
      for (int i = 0 ; i < constructors.length ; i++)
      {
         advisor.getConstructorMetaData().addConstructorMetaData(constructors[i], "custom", "data", ((ScopedMetadataBinding)data).getData());
      }
   }



}
