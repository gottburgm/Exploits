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
package org.jboss.spring.deployment;

import javax.management.ObjectName;

import org.jboss.spring.loader.ApplicationContextLoaderImpl;
import org.jboss.spring.loader.BeanFactoryLoader;

/**
 * @author <a href="mailto:ales.justin@genera-lynx.com">Ales Justin</a>
 * @jmx:mbean name="jboss.spring:service=SpringApplicationContextDeployer"
 * extends="org.jboss.spring.deployment.SpringDeployer"
 */
@Deprecated
public class SpringApplicationContextDeployer extends SpringDeployer
{

   //default object name
   public static final javax.management.ObjectName OBJECT_NAME =
         org.jboss.mx.util.ObjectNameFactory.create("jboss.spring:service=SpringApplicationContextDeployer");

   public SpringApplicationContextDeployer()
   {
      super();
   }

   protected BeanFactoryLoader createBeanFactoryLoader()
   {
      return new ApplicationContextLoaderImpl();
   }

   protected ObjectName getDefaultObjectName()
   {
      return OBJECT_NAME;
   }

}
