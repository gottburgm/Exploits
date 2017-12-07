/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.system.metadata;

import java.io.Serializable;
import java.lang.annotation.Annotation;

import org.jboss.annotation.factory.AnnotationCreator;
import org.jboss.annotation.factory.ast.TokenMgrError;
import org.jboss.util.JBossObject;
import org.jboss.util.JBossStringBuilder;
import org.jboss.util.StringPropertyReplacer;

/**
 * Service annotation metadata
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class ServiceAnnotationMetaData extends JBossObject
   implements Serializable
{
   private static final long serialVersionUID = 1L;

   public String annotation;

   protected Annotation ann;

   protected boolean replace = true;

   /**
    * Create a new annotation meta data
    */
   public ServiceAnnotationMetaData()
   {
      super();
   }
   /**
    * Create a new annotation meta data
    * @param ann - the annotation declaration
    */
   public ServiceAnnotationMetaData(String ann)
   {
      super();
      setAnnotation(ann);
   }

   public String getAnnotation()
   {
      return annotation;
   }

   public void setAnnotation(String annotation)
   {
      this.annotation = annotation;
   }

   public boolean isReplace()
   {
      return replace;
   }

   public void setReplace(boolean replace)
   {
      this.replace = replace;
   }

   public Annotation getAnnotationInstance()
   {
      return getAnnotationInstance(null);
   }

   public Annotation getAnnotationInstance(ClassLoader cl)
   {
      try
      {
         String annString = annotation;
         if (replace)
         {
            annString = StringPropertyReplacer.replaceProperties(annString);
         }
         if (cl == null)
         {
            cl = Thread.currentThread().getContextClassLoader();
         }
         ann = (Annotation)AnnotationCreator.createAnnotation(annString, cl);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Error creating annotation for " + annotation, e);
      }
      catch(TokenMgrError e)
      {
         throw new RuntimeException("Error creating annotation for " + annotation, e);
      }

      return ann;
   }

   public void toString(JBossStringBuilder buffer)
   {
      buffer.append("expr=").append(ann);
   }

   public void toShortString(JBossStringBuilder buffer)
   {
      buffer.append(ann);
   }

   protected int getHashCode()
   {
      return annotation.hashCode();
   }

   public boolean equals(Object object)
   {
      if (object == null || object instanceof ServiceAnnotationMetaData == false)
         return false;

      ServiceAnnotationMetaData amd = (ServiceAnnotationMetaData)object;
      return (replace == amd.replace) && annotation.equals(amd.annotation);
   }

}
