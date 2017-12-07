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
package org.jboss.system.server.profileservice.persistence;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.api.factory.ManagedObjectFactory;

/**
 * A recreation helper.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class ManagedObjectRecreationHelper
{

   /** The loader. */
   private final ThreadLocal<ClassLoader> loader = new ThreadLocal<ClassLoader>();
   
   /** The managed object factory. */
   private final ManagedObjectFactory mangedObjectFactory;
   
   /** The attachment property populator. */
   private final AttachmentPropertyPopulator attachmentPopulator;
   
   public ManagedObjectRecreationHelper(ManagedObjectFactory mangedObjectFactory)
   {
      if(mangedObjectFactory == null)
         throw new IllegalArgumentException("null managed object factory");
      
      this.mangedObjectFactory = mangedObjectFactory;
      this.attachmentPopulator =  new AttachmentPropertyPopulator(mangedObjectFactory, this);
   }
   
   public ClassLoader getLoader()
   {
      return this.loader.get();
   }
   
   public void setLoader(ClassLoader loader)
   {
      this.loader.set(loader);
   }
   
   /**
    * Set a value to a managed property. This delegates
    * to the AttachmentPropertyPopulator.
    * 
    * @param name the property name
    * @param property the managed property
    * @param attachment the attachment
    * @throws Throwable for any erro
    */
   public void setValue(String name, ManagedProperty property, Object attachment) throws Throwable
   {
      attachmentPopulator.processManagedProperty(name, property, attachment);
   }
   
   /**
    * Create a ManagedObject skeleton based on a class name.
    * 
    * @param className the class name
    * @return the ManagedObject, null if the class has no ManagementObject annotations
    * @throws ClassNotFoundException 
    */
   protected ManagedObject createManagedObjectSkeleton(String className) throws ClassNotFoundException
   {
      Class<?> clazz = loadClass(className);
      return mangedObjectFactory.createManagedObject(clazz);
   }
   
   /**
    * Load class.
    * 
    * @param className the class name
    * @return the class
    * @throws ClassNotFoundException
    */
   protected Class<?> loadClass(String className) throws ClassNotFoundException
   {
      ClassLoader cl = getLoader();
      if(cl == null)
         cl = SecurityActions.getContextClassLoader();
      return cl.loadClass(className); 
   }
   
   private static final class SecurityActions
   {
      public static ClassLoader getContextClassLoader()
      {
         if (System.getSecurityManager() == null)
         {
            return Thread.currentThread().getContextClassLoader();
         }
         else
         {
            return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>()
            {
                public ClassLoader run()
                {
                   return Thread.currentThread().getContextClassLoader();
                }
            });
         }  
      }
   }
   
}

