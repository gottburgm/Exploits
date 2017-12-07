/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.container.jboss50.deployment.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.ejb.EJB;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.ejb3.common.resolvers.spi.EjbReference;
import org.jboss.ejb3.common.resolvers.spi.EjbReferenceResolver;
import org.jboss.wsf.common.injection.resolvers.AbstractReferenceResolver;

/**
 * EJB reference resolver.
 *
 * @author <a href="mailto:richard.opalka@jboss.org">Richard Opalka</a>
 */
final class EJBBeanReferenceResolver extends AbstractReferenceResolver<EJB>
{

   /**
    * Deployment unit used for resolving process.
    */
   private final DeploymentUnit unit;

   /**
    * Delegate used to resolve JNDI names.
    */
   private final EjbReferenceResolver delegate;

   /**
    * Constructor.
    * 
    * @param unit deployment unit
    * @param delegate EJB reference resolver
    */
   EJBBeanReferenceResolver(final DeploymentUnit unit, final EjbReferenceResolver delegate)
   {
      super(EJB.class);

      if (unit == null)
      {
         throw new IllegalArgumentException("Deployment unit cannot be null");
      }
      if (delegate == null)
      {
         throw new IllegalArgumentException("Ejb reference resolver cannot be null");
      }

      this.unit = unit;
      this.delegate = delegate;
   }

   /**
    * @see org.jboss.wsf.common.injection.resolvers.AbstractReferenceResolver#resolveField(java.lang.reflect.Field)
    * 
    * @param field to be resolved
    * @return JNDI name of referenced EJB object
    */
   @Override
   protected String resolveField(final Field field)
   {
      final EJB ejbAnnotation = field.getAnnotation(EJB.class);
      final Class<?> type = field.getType();
      final EjbReference reference = this.getEjbReference(ejbAnnotation, type);

      return this.delegate.resolveEjb(this.unit, reference);
   }

   /**
    * @see org.jboss.wsf.common.injection.resolvers.AbstractReferenceResolver#resolveMethod(java.lang.reflect.Method)
    * 
    * @param method to be resolved
    * @return JNDI name of referenced EJB object
    */
   @Override
   protected String resolveMethod(final Method method)
   {
      final EJB ejbAnnotation = method.getAnnotation(EJB.class);
      final Class<?> type = method.getParameterTypes()[0];
      final EjbReference reference = this.getEjbReference(ejbAnnotation, type);

      return this.delegate.resolveEjb(this.unit, reference);
   }

   /**
    * Constructs EjbReference.
    *
    * @param ejbAnnotation ejb annotation
    * @param type fall back type
    * @return ejb reference instance
    */
   private EjbReference getEjbReference(final EJB ejbAnnotation, final Class<?> type)
   {
      String beanInterface = ejbAnnotation.beanInterface().getName();
      if (java.lang.Object.class.getName().equals(beanInterface))
      {
         beanInterface = type.getName();
      }
      return new EjbReference(ejbAnnotation.beanName(), beanInterface, ejbAnnotation.mappedName());
   }

}
