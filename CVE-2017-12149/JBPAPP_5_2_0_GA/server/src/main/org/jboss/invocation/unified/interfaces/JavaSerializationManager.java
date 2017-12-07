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
package org.jboss.invocation.unified.interfaces;

import java.io.IOException;

import org.jboss.invocation.MarshalledValueEX;
import org.jboss.logging.Logger;
import org.jboss.remoting.serialization.IMarshalledValue;
import org.jboss.remoting.serialization.SerializationStreamFactory;

/**
 * JavaSerializationmanager from JBossRemoting doesn't use the same
 * MarshalledValue specified by org.jboss.invocation. As
 * org.jboss.invocation.MarshalledValue could use caching features from JBossAS,
 * we will need to use that MarshalledValue.
 * 
 * @author <a href="mailto:clebert.suconic@jboss.com">Clebert Suconic</a>
 */
public class JavaSerializationManager extends
   org.jboss.remoting.serialization.impl.java.JavaSerializationManager
{
   protected static final Logger log = Logger.getLogger(JavaSerializationManager.class);

   static
   {
      register();
   }

   /** Register yourself as Java manager into SerializationStreamFactory */
   private static void register()
   {
      register("compatible");
      register(SerializationStreamFactory.JAVA);

      try
      {
         if (SerializationStreamFactory.getManagerInstance().getClass() 
            == org.jboss.remoting.serialization.impl.java.JavaSerializationManager.class)
         {
            register(SerializationStreamFactory.DEFAULT);
         }
      } catch (Exception e)
      {
         log.error(e);
      }
   }

   private static void register(String provider)
   {
      try
      {
         SerializationStreamFactory.setManagerClassName(
            provider, JavaSerializationManager.class.getName());
      }
      catch (ClassNotFoundException e)
      {
         log.error(e);
      }
      catch (IllegalAccessException e)
      {
         log.error(e);
      }
      catch (InstantiationException e)
      {
         log.error(e);
      }
   }

   /**
    * Creates a MarshalledValue that does lazy serialization.
    */
   public IMarshalledValue createdMarshalledValue(Object source) throws IOException
   {
      if (source instanceof IMarshalledValue)
      {
         return (IMarshalledValue) source;
      }
      else
      {
         return new MarshalledValueEX(source);
      }
   }
}
