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
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.annotation.Resources;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.ejb.ApplicationException;
import javax.ejb.EJB;
import javax.ejb.EJBs;
import javax.ejb.Init;
import javax.ejb.Local;
import javax.ejb.LocalHome;
import javax.ejb.MessageDriven;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remote;
import javax.ejb.RemoteHome;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionManagement;
import javax.interceptor.AroundInvoke;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.ExcludeDefaultInterceptors;
import javax.interceptor.Interceptors;
import javax.persistence.Entity;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContexts;
import javax.xml.ws.WebServiceRef;
import javax.xml.ws.WebServiceRefs;

import org.jboss.deployers.spi.annotations.AnnotationEnvironment;
import org.jboss.deployers.spi.annotations.Element;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.virtual.VirtualFile;

/**
 * A POST_CLASSLOADER deployer which generates metadata from annotations.
 * Alternative option to its super class.
 *
 * @author Ales.Justin@jboss.org
 */
public class AltAnnotationMetaDataDeployer extends AnnotationMetaDataDeployer
{
   private Set<Class<? extends Annotation>> annotationOnClass = new HashSet<Class<? extends Annotation>>();
   private Set<Class<? extends Annotation>> annotationOnMethod = new HashSet<Class<? extends Annotation>>();
   private Set<Class<? extends Annotation>> annotationOnField = new HashSet<Class<? extends Annotation>>();

   public AltAnnotationMetaDataDeployer()
   {
      super();
      setInput(AnnotationEnvironment.class);
      // add annotations AnnotationMetaDataDeployer scans
      addAnnotationClass(Stateful.class);
      addAnnotationClass(Remove.class);
      addAnnotationClass(PostActivate.class);
      addAnnotationClass(PrePassivate.class);
      addAnnotationClass(Local.class);
      addAnnotationClass(LocalHome.class);
      addAnnotationClass(Remote.class);
      addAnnotationClass(RemoteHome.class);
      addAnnotationClass(Init.class);
      addAnnotationClass(Timeout.class);
      addAnnotationClass(AroundInvoke.class);
      addAnnotationClass(TransactionManagement.class);
      addAnnotationClass(TransactionAttribute.class);
      addAnnotationClass(RunAs.class);
      addAnnotationClass(DeclareRoles.class);
      addAnnotationClass(DenyAll.class);
      addAnnotationClass(RolesAllowed.class);
      addAnnotationClass(PermitAll.class);
      addAnnotationClass(Interceptors.class);
      addAnnotationClass(ExcludeClassInterceptors.class);
      addAnnotationClass(ExcludeDefaultInterceptors.class);
      addAnnotationClass(Resource.class);
      addAnnotationClass(Resources.class);
      addAnnotationClass(EJB.class);
      addAnnotationClass(EJBs.class);
      addAnnotationClass(PersistenceContext.class);
      addAnnotationClass(PersistenceContexts.class);
      addAnnotationClass(PostConstruct.class);
      addAnnotationClass(PreDestroy.class);
      addAnnotationClass(WebServiceRef.class);
      addAnnotationClass(WebServiceRefs.class);
      addAnnotationClass(Stateless.class);
      addAnnotationClass(MessageDriven.class);
      addAnnotationClass(Entity.class);
      addAnnotationClass(ApplicationException.class);
   }

   @SuppressWarnings("unchecked")
   protected Collection<Class<?>> getClasses(VFSDeploymentUnit unit, String mainClassName, List<VirtualFile> classpath) throws IOException
   {
      AnnotationEnvironment env = unit.getAttachment(AnnotationEnvironment.class);
      if (env == null)
      {
         if (log.isTraceEnabled())
            log.trace("Cannot scan classes, missing AnnotationEnvironment as attachment: " + unit.getName());
         return Collections.emptySet();
      }

      Set<Class<?>> classes = new HashSet<Class<?>>();
      for(Class<? extends Annotation> annotation : annotationOnClass)
      {
         Class<Annotation> annotationClass = (Class<Annotation>)annotation;
         Set<Element<Annotation, Class<?>>> elements = env.classIsAnnotatedWith(annotationClass);
         for(Element<Annotation, Class<?>> elt : elements)
            classes.add(elt.getOwner());
      }
      for(Class<? extends Annotation> annotation : annotationOnMethod)
      {
         Class<Annotation> annotationClass = (Class<Annotation>)annotation;
         Set<Element<Annotation, Method>> elements = env.classHasMethodAnnotatedWith(annotationClass);
         for(Element<Annotation, Method> elt : elements)
            classes.add(elt.getOwner());
      }
      for(Class<? extends Annotation> annotation : annotationOnField)
      {
         Class<Annotation> annotationClass = (Class<Annotation>)annotation;
         Set<Element<Annotation, Field>> elements = env.classHasFieldAnnotatedWith(annotationClass);
         for(Element<Annotation, Field> elt : elements)
            classes.add(elt.getOwner());
      }

      if(log.isTraceEnabled() && classes.isEmpty() == false)
         log.trace("Annotated classes: " + classes);

      return classes;
   }

   /**
    * Cleanup annotation classes.
    */
   public void stop()
   {
      annotationOnClass.clear();
      annotationOnMethod.clear();
      annotationOnField.clear();
   }

   /**
    * Add annotation to matching set.
    *
    * @param annotation the annotation class
    */
   public void addAnnotationClass(Class<? extends Annotation> annotation)
   {
      Target target = annotation.getAnnotation(Target.class);
      if (target == null)
      {
         log.info("Annotation " + annotation + " has not @Target.");
         return;
      }
      ElementType[] types = target.value();
      if (types == null || types.length == 0)
      {
         log.info("Null or empty types on annotation's @Target: " + annotation);
         return;
      }
      for(ElementType type : types)
      {
         boolean used = false; // no need for duplicates
         if (type == ElementType.TYPE)
         {
            annotationOnClass.add(annotation);
            used = true;
         }
         if (used == false && type == ElementType.METHOD)
         {
            annotationOnMethod.add(annotation);
            used = true;
         }
         if (used == false && type == ElementType.FIELD)
         {
            annotationOnField.add(annotation);
         }
      }
   }
}