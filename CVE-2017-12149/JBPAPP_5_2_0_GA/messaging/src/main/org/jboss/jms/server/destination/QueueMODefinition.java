/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jms.server.destination;

import org.jboss.logging.Logger;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.factory.ManagedObjectFactory;
import org.jboss.managed.api.factory.ManagedObjectDefinition;
import org.jboss.managed.spi.factory.ManagedObjectBuilder;
import org.jboss.metadata.spi.MetaData;

/**
 * A ManagedObjectBuilder that maps the QueueService class to the QueueServiceMO
 * for its ManagedObject skeleton.
 *
 * @author Scott.Stark@jboss.org
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class QueueMODefinition extends ManagedObjectDefinition
{
   private static Logger log = Logger.getLogger(QueueMODefinition.class);

   public QueueMODefinition(ManagedObjectFactory factory)
   {
      super(QueueService.class, new QueueMOBuilder(factory));
   }

   private static class QueueMOBuilder implements ManagedObjectBuilder
   {
      private ManagedObjectFactory factory;

      private QueueMOBuilder(ManagedObjectFactory factory)
      {
         this.factory = factory;
      }

      /**
       * Create a ManagedObject from QueueService to QueueServiceMO.
       *
       * @param clazz - the mbean class to create the ManagedObject for
       * @param metaData - the MDR MetaData view
       */
      public ManagedObject buildManagedObject(Class<?> clazz, MetaData metaData)
      {
         ManagedObjectFactory mof = getMOFactory();
         log.debug("Creating QueueServiceMO template for: " + clazz);
         return mof.createManagedObject(QueueServiceMO.class, metaData);
      }

      /**
       * Get MO factory.
       *
       * @return the MO factory
       */
      protected ManagedObjectFactory getMOFactory()
      {
         if (factory == null)
            factory = ManagedObjectFactory.getInstance();

         return factory;
      }
   }
}
