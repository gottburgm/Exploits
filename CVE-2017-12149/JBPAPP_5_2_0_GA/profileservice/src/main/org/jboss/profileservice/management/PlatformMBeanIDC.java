/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.profileservice.management;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.dependency.spi.ControllerState;
import org.jboss.dependency.spi.dispatch.InvokeDispatchContext;
import org.jboss.kernel.plugins.registry.AbstractKernelRegistryEntry;

/**
 * An extension of AbstractKernelRegistryEntry that implements InvokeDispatchContext
 * to handle access to the platform mbeans.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class PlatformMBeanIDC extends AbstractKernelRegistryEntry
   implements InvokeDispatchContext
{
   private MBeanServer server;
   private ObjectName objectName;

   
   public PlatformMBeanIDC(MBeanServer server, ObjectName objectName,
         Object mbean)
   {
      super(objectName.getCanonicalName(), mbean);
      this.server = server;
      this.objectName = objectName;
   }

   public ClassLoader getClassLoader() throws Throwable
   {
      return server.getClassLoaderFor(objectName);
   }

   public Object invoke(String operationName, Object[] parameters, String[] signature)
         throws Throwable
   {
      return server.invoke(objectName, operationName, parameters, signature);
   }

   public Object get(String name) throws Throwable
   {
      return server.getAttribute(objectName, name);
   }

   public void set(String name, Object value) throws Throwable
   {
      Attribute attribute = new Attribute(name, value);
      server.setAttribute(objectName, attribute);
   }

   @Override
   public ControllerState getState()
   {
      return ControllerState.INSTALLED;
   }
   
}
