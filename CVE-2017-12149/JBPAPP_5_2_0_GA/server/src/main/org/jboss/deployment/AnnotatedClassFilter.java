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
package org.jboss.deployment;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.logging.Logger;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VirtualFileFilter;
import org.jboss.virtual.VirtualFileVisitor;
import org.jboss.virtual.VisitorAttributes;

/**
 * A VirtualFileVisitor that traverses unit root and determines the
 * class files that are annotated.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class AnnotatedClassFilter implements VirtualFileVisitor
{
   private static Logger log = Logger.getLogger(AnnotatedClassFilter.class);
   private ClassLoader loader;
   private int rootLength;
   private HashSet<String> childPaths = new HashSet<String>();
   private HashMap<VirtualFile, Class<?>> pathToClasses = new HashMap<VirtualFile, Class<?>>();
   private String clientClassName;

   public AnnotatedClassFilter(VFSDeploymentUnit unit, ClassLoader loader, VirtualFile classpathRoot)
   {
      this(unit, loader, classpathRoot, "");
   }
   public AnnotatedClassFilter(VFSDeploymentUnit unit, ClassLoader loader,
         VirtualFile classpathRoot, String clientClassName)
   {
      this.loader = loader;
      this.clientClassName = clientClassName;

      // Work out the root length. If there is a root, we need to add one to jump across the next /
      String rootName = classpathRoot.getPathName();
      rootLength = rootName.length();
      if (rootLength > 0)
         rootLength += 1;
      List<DeploymentUnit> children = unit.getChildren();
      if(children != null)
      {
         for(DeploymentUnit cu : children)
         {
            String path = cu.getName();
            childPaths.add(path);
         }
      }
   }

   public Map<VirtualFile, Class<?>> getAnnotatedClasses()
   {
      return pathToClasses;
   }

   public VisitorAttributes getAttributes()
   {
      VisitorAttributes attributes = new VisitorAttributes();
      attributes.setIncludeRoot(true);
      attributes.setRecurseFilter(new NoChildFilter());
      return attributes;
   }

   public void visit(VirtualFile file)
   {
      try
      {
         if(file.isLeaf())
         {
            accepts(file);
         }
      }
      catch (IOException e)
      {
         throw new Error("Error visiting " + file, e);
      }
   }

   public boolean accepts(VirtualFile file)
   {
      boolean accepts = file.getPathName().endsWith(".class");
      if(accepts)
      {
         accepts = false;
         String className = null;
         try
         {
            className = getClassName(file);
            Class<?> c = loader.loadClass(className);
            boolean hasAnnotations = hasAnnotations(c);
            boolean includeClass = false;
            if(clientClassName != null)
            {
               includeClass = className.equals(clientClassName) && hasAnnotations;
            }
            else
            {
               includeClass = hasAnnotations;
            }

            if(includeClass)
            {
               pathToClasses.put(file, c);
               accepts = true;
            }
         }
         catch(NoClassDefFoundError ignored)
         {
            log.debug("Incomplete class: "+className+", NCDFE: "+ignored);
         }
         catch(Exception ignored)
         {
            if(log.isTraceEnabled())
               log.trace("Failed to load class: "+className, ignored);
         }
      }
      return accepts;
   }

   protected String getFilePath(VirtualFile file)
   {
      String path = null;
      try
      {
         path = file.toURI().toString();
      }
      catch(Exception e)
      {
      }
      return path;
   }

   /**
    * Search the classpaths for the root of this file.
    *
    * @param classFile the class file
    * @return fqn class name
    * @throws IOException for any error
    */
   protected String getClassName(VirtualFile classFile) throws IOException
   {
      String pathName = classFile.getPathName();
      String name = pathName.substring(rootLength, pathName.length()-6);
      name = name.replace('/', '.');
      return name;
   }

   /**
    * Completely scan a class for annotations
    * @param cls
    * @return true if the class has annotations, false otherwise
    */
   protected boolean hasAnnotations(Class<?> cls)
   {
      if(cls == null)
         return false;
      
      // Note: this also returns true if super class has annotations
      if(cls.getAnnotations().length > 0)
         return true;
      
      for(Method m : cls.getDeclaredMethods())
      {
         if(m.getAnnotations().length > 0)
            return true;
      }
      
      for(Field f : cls.getDeclaredFields())
      {
         if(f.getAnnotations().length > 0)
            return true;
      }
      
      return hasAnnotations(cls.getSuperclass());
   }

   class NoChildFilter implements VirtualFileFilter
   {
      public boolean accepts(VirtualFile file)
      {
         String path = getFilePath(file);
         boolean accepts = false;
         try
         {
            accepts = file.isLeaf() == false && childPaths.contains(path) == false;
         }
         catch(Exception e)
         {
         }
         return accepts;
      }
      
   }
}
