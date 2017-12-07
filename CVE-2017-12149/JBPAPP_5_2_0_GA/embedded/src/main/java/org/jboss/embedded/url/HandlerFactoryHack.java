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
package org.jboss.embedded.url;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.util.List;

/**
 * Tries to set URL.setURLStreamHandlerFactory() with the provided factory.
 * If it fails, it tries to reset handler by creating a BridgeHandlerFactory
 * and hacking the JDK to overcome URL.setURLStreamHandlerFactory()'s limitation
 * of only being able to be set up once.
 *
 * If there is already an existing handler factory then this factory will be added to the bridge list
 *
 * @author <a href="bill@jboss.com">Bill Burke</a>
 * @version $Revision: 85945 $
 */
public class HandlerFactoryHack
{
   private List<URLStreamHandlerFactory> factories;

   public List<URLStreamHandlerFactory> getFactories()
   {
      return factories;
   }

   public void setFactories(List<URLStreamHandlerFactory> factories)
   {
      this.factories = factories;
   }

   public void start() throws Exception
   {
      hack();
   }

   protected void hack() throws Exception
   {
      BridgeHandlerFactory bridge = new BridgeHandlerFactory(factories);
      try
      {
         URL.setURLStreamHandlerFactory(bridge);
         return;
      }
      catch (Error ignored)
      {
         // already set
      }
      try
      {
         Field factoryField = URL.class.getDeclaredField("factory");
         factoryField.setAccessible(true);
         URLStreamHandlerFactory old = (URLStreamHandlerFactory)factoryField.get(null);
         if (old != null) factories.add(0, old);
         factoryField.set(null, bridge);
      }
      catch (Exception e)
      {
         throw e;
      }
   }
}
