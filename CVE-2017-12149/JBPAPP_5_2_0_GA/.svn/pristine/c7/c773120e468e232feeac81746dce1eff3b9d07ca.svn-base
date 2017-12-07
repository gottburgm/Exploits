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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.deployers.spi.annotations.AnnotationEnvironment;
import org.jboss.deployers.spi.annotations.Element;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.metadata.annotation.creator.AbstractCreator;
import org.jboss.metadata.annotation.creator.AnnotationContext;
import org.jboss.metadata.annotation.creator.client.ApplicationClient5MetaDataCreator;
import org.jboss.metadata.annotation.creator.ejb.jboss.JBoss50Creator;
import org.jboss.metadata.annotation.creator.web.Web25MetaDataCreator;
import org.jboss.metadata.annotation.finder.AnnotationFinder;
import org.jboss.metadata.annotation.finder.DefaultAnnotationFinder;
import org.jboss.metadata.client.spec.ApplicationClientMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.spec.EjbJarMetaData;
import org.jboss.metadata.web.spec.WebMetaData;
import org.jboss.virtual.VirtualFile;

/**
 * A POST_CLASSLOADER deployer which generates metadata from annotations.
 * Optimized option to its super class.
 *
 * @author Ales.Justin@jboss.org
 */
public class OptAnnotationMetaDataDeployer extends AnnotationMetaDataDeployer
{
   public OptAnnotationMetaDataDeployer()
   {
      super();
      setInput(AnnotationEnvironment.class);
   }

   protected void processMetaData(VFSDeploymentUnit unit, WebMetaData webMetaData, ApplicationClientMetaData clientMetaData, List<VirtualFile> classpath) throws Exception
   {
      AnnotationFinder<AnnotatedElement> finder = new DefaultAnnotationFinder<AnnotatedElement>();
      if(webMetaData != null)
      {
         processJBossWebMetaData(unit, finder);
      }
      else if (clientMetaData != null)
      {
         String mainClassName = getMainClassName(unit);
         if(mainClassName != null)
            processJBossClientMetaData(unit, finder, mainClassName);
      }
      else
      {
         String mainClassName = getMainClassName(unit);
         if (mainClassName != null && mainClassName.length() > 0)
            processJBossClientMetaData(unit, finder, mainClassName);
         else
            processJBossMetaData(unit, finder);
      }
   }

   /**
    * Process jboss web meta data.
    *
    * @param unit the deployment unit
    * @param finder the finder
    */
   protected void processJBossWebMetaData(VFSDeploymentUnit unit, AnnotationFinder<AnnotatedElement> finder)
   {
      Web25MetaDataCreator creator = new Web25MetaDataCreator(finder);
      final Collection<Class<?>> classes = getClasses(unit, creator);
      // initialize resource injection eligible classes to all classes
      Collection<Class<?>> resourceInjectionEligibleClasses = classes;
      if (this.isStrictServletSpecCompliance())
      {
         // The spec states that resource injection is eligible only on a certain set of classes, so filter out
         // the rest of the classes
         resourceInjectionEligibleClasses = this.getResourceInjectionEligibleWebAppClasses(unit, classes);
      }
      if (resourceInjectionEligibleClasses.isEmpty())
      {
         log.debug("No Java EE injection eligible classes found in web application: " + unit);
         return;
      }
      WebMetaData annotationMetaData = creator.create(resourceInjectionEligibleClasses);
      if(annotationMetaData != null)
      {
         unit.addAttachment(WEB_ANNOTATED_ATTACHMENT_NAME, annotationMetaData, WebMetaData.class);
      }
   }

   /**
    * Process jboss app client meta data.
    *
    * @param unit the deployment unit
    * @param finder the finder
    * @param mainClassName the main class name
    * @throws ClassNotFoundException for any error
    */
   protected void processJBossClientMetaData(VFSDeploymentUnit unit, AnnotationFinder<AnnotatedElement> finder, String mainClassName) throws ClassNotFoundException
   {
      ApplicationClient5MetaDataCreator creator = new ApplicationClient5MetaDataCreator(finder, mainClassName);
      Collection<Class<?>> classes = new ArrayList<Class<?>>(1);
      Class<?> mainClass = unit.getClassLoader().loadClass(mainClassName);
      classes.add(mainClass);
      ApplicationClientMetaData annotationMetaData = creator.create(classes);
      if(annotationMetaData != null)
         unit.addAttachment(CLIENT_ANNOTATED_ATTACHMENT_NAME, annotationMetaData, ApplicationClientMetaData.class);
   }

   /**
    * Process jboss meta data.
    *
    * @param unit the deployment unit
    * @param finder the finder
    */
   protected void processJBossMetaData(VFSDeploymentUnit unit, AnnotationFinder<AnnotatedElement> finder)
   {
      // Create the metadata model from the annotations
      EjbJarMetaData ejbJarMetaData = unit.getAttachment(EjbJarMetaData.class);
      JBoss50Creator creator = new JBoss50Creator(ejbJarMetaData, unit.getClassLoader(), finder);
      Collection<Class<?>> classes = getClasses(unit, creator);
      JBossMetaData annotationMetaData = creator.create(classes);
      if(annotationMetaData != null)
         unit.addAttachment(EJB_ANNOTATED_ATTACHMENT_NAME, annotationMetaData, JBossMetaData.class);
   }

   /**
    * Get the classes for creator to process.
    *
    * @param unit the deployment unit
    * @param creator the creator
    * @return classes to process
    */
   @SuppressWarnings("unchecked")
   protected Collection<Class<?>> getClasses(VFSDeploymentUnit unit, AbstractCreator creator)
   {
      boolean trace = log.isTraceEnabled();

      AnnotationEnvironment env = unit.getAttachment(AnnotationEnvironment.class);
      if (env == null)
      {
         if (trace)
            log.trace("Cannot scan classes, missing AnnotationEnvironment as attachment: " + unit.getName());

         return Collections.emptySet();
      }

      String creatorInfo = creator.toString();
      AnnotationContext context = creator.getAnnotationContext();      
      Set<Class<?>> classes = new HashSet<Class<?>>();

      Collection<Class<? extends Annotation>> typeAnnotations = context.getTypeAnnotations();
      if (trace)
         log.trace("Creator: " + creatorInfo + ", type annotations: " + typeAnnotations);
      for(Class<? extends Annotation> annotation : typeAnnotations)
      {
         Class<Annotation> annotationClass = (Class<Annotation>)annotation;
         Set<Element<Annotation, Class<?>>> elements = env.classIsAnnotatedWith(annotationClass);
         for(Element<Annotation, Class<?>> elt : elements)
               classes.add(elt.getOwner());
      }

      Collection<Class<? extends Annotation>> methodAnnotations = context.getMethodAnnotations();
      if (trace)
         log.trace("Creator: " + creatorInfo + ", method annotations: " + methodAnnotations);
      for(Class<? extends Annotation> annotation : methodAnnotations)
      {
         Class<Annotation> annotationClass = (Class<Annotation>)annotation;
         Set<Element<Annotation, Method>> elements = env.classHasMethodAnnotatedWith(annotationClass);
         for(Element<Annotation, Method> elt : elements)
            classes.add(elt.getOwner());
      }

      Collection<Class<? extends Annotation>> fieldAnnotations = context.getFieldAnnotations();
      if (trace)
         log.trace("Creator: " + creatorInfo + ", field annotations: " + fieldAnnotations);
      for(Class<? extends Annotation> annotation : fieldAnnotations)
      {
         Class<Annotation> annotationClass = (Class<Annotation>)annotation;
         Set<Element<Annotation, Field>> elements = env.classHasFieldAnnotatedWith(annotationClass);
         for(Element<Annotation, Field> elt : elements)
            classes.add(elt.getOwner());
      }

      if (trace)
         log.trace("Annotated classes [" + unit.getName() + ", " + creatorInfo + "]: " + classes);

      return classes;
   }
}