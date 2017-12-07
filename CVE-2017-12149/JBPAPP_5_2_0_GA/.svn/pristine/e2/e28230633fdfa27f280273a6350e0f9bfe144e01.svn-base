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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.annotation.factory.AnnotationProxy;
import org.jboss.aop.microcontainer.aspects.jmx.JMX;
import org.jboss.beans.metadata.plugins.AbstractDependencyValueMetaData;
import org.jboss.beans.metadata.spi.MetaDataVisitorNode;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.dependency.spi.Controller;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.kernel.plugins.annotations.AbstractAnnotationPlugin;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.metadata.spi.MetaData;
import org.jboss.reflect.spi.AnnotatedInfo;

/**
 * Supporting @JMX on attributes.
 *
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 * @param <T> exact info type
 */
public abstract class JMXAnnotationPlugin<T extends AnnotatedInfo> extends AbstractAnnotationPlugin<T, JMX>
{
   protected JMXAnnotationPlugin()
   {
      super(JMX.class);
   }

   protected boolean isCleanup()
   {
      return true;
   }

   protected List<? extends MetaDataVisitorNode> internalApplyAnnotation(T info, MetaData metaData, JMX jmx, KernelControllerContext context) throws Throwable
   {
      Class<?> exposedInterface = jmx.exposedInterface();
      if (exposedInterface == null || void.class.equals(exposedInterface))
         exposedInterface = getExposedInterface(info);
      if (exposedInterface == null || exposedInterface.isInterface() == false)
         throw new IllegalArgumentException("Illegal exposed interface: " + exposedInterface);

      String name = createObjectName(context, info, jmx);
      String property = getName(info);
      if (log.isTraceEnabled())
         log.trace("Exposing " + context.getName() + "." + property + " (" + exposedInterface.getName() + ") via objectName: " + name);

      String id = createId(context, info, jmx);
      BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder(id, exposedInterface.getName());
      builder.addAnnotation(wrapJMX(jmx, name, exposedInterface));
      builder.setConstructorValue(new AbstractDependencyValueMetaData(context.getName(), property));

      KernelController controller = (KernelController)context.getController();
      controller.install(builder.getBeanMetaData());

      // no change directly on context
      return null;
   }

   /**
    * Create unique id for the exposed pojo attribute.
    *
    * @param context the context
    * @param info the info
    * @param jmx the annotation
    * @return generated id
    */
   protected String createId(KernelControllerContext context, T info, JMX jmx)
   {
      return context.getName() + "." + getName(info) + "." + jmx.hashCode();
   }

   /**
    * Should we wrap the original
    * in order to get the name we created.
    *
    * @param original the original
    * @param name the new created name
    * @param exposedInterface the exposed interface
    * @return jmx annoation instance
    * @throws Throwable for any error
    */
   protected JMX wrapJMX(JMX original, String name, Class<?> exposedInterface) throws Throwable
   {
      Map<String, Object> attributes = new HashMap<String, Object>();
      attributes.put("exposedInterface", exposedInterface);
      attributes.put("name", name);
      attributes.put("registerDirectly", original.registerDirectly());
      return (JMX)AnnotationProxy.createProxy(attributes, JMX.class);
   }

   protected void internalCleanAnnotation(T info, MetaData metaData, JMX jmx, KernelControllerContext context) throws Throwable
   {
      String id = createId(context, info, jmx);
      if (log.isTraceEnabled())
         log.trace("Removing " + context.getName() + "." + getName(info) + " via id: " + id);

      Controller controller = context.getController();
      controller.uninstall(id);
   }

   /**
    * Get exposed interface from info.
    *
    * @param info the info
    * @return exposed interface
    */
   protected abstract Class<?> getExposedInterface(T info);

   /**
    * Get name from info.
    *
    * @param info the info
    * @return info's name
    */
   protected abstract String getName(T info);

   /**
    * Get jmx name.
    *
    * @param jmx the JMX annotation
    * @return valid jmx name
    */
   protected static String getJmxName(JMX jmx)
   {
      String jmxName = jmx.name();
      if (jmxName != null && jmxName.length() > 0)
         return jmxName;

      return null;
   }

   /**
    * Create object name.
    *
    * @param context the context
    * @param info the info
    * @param jmx the annotation
    * @return object name
    * @throws Exception for any error
    */
   protected String createObjectName(ControllerContext context, T info, JMX jmx) throws Exception
   {
      String jmxName = getJmxName(jmx);
      if (jmxName != null)
         return jmxName;

      // try to build one from the bean name and  info param
      String name = context.getName().toString();
      String objectName = name;
      if (name.contains(":") == false)
      {
         objectName = "jboss.pojo:name='" + name + "'";
      }
      return objectName + ",attribute=" + getName(info);
   }
}