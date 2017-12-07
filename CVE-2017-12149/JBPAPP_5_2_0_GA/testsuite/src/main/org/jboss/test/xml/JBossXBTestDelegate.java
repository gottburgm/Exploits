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
package org.jboss.test.xml;

import java.lang.reflect.Method;
import java.net.URL;

import org.jboss.net.protocol.URLStreamHandlerFactory;
import org.jboss.test.AbstractTestDelegate;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.sunday.unmarshalling.DefaultSchemaResolver;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBindingResolver;

/**
 * JBossXBTestDelegate.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81036 $
 */
public class JBossXBTestDelegate extends AbstractTestDelegate
{
   /** Whether initialization has been done */
   private static boolean done = false;

   /** The unmarshaller factory */
   protected UnmarshallerFactory unmarshallerFactory;

   /** The resolver */
   protected SchemaBindingResolver defaultResolver;
   
   /**
    * Initialize
    */
   public synchronized static void init()
   {
      if (done)
         return;
      done = true;
      URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory());
      URLStreamHandlerFactory.preload();
      String handlerPkgs = System.getProperty("java.protocol.handler.pkgs");
      if (handlerPkgs != null)
         handlerPkgs += "|org.jboss.net.protocol";
      else
         handlerPkgs = "org.jboss.net.protocol";
      System.setProperty("java.protocol.handler.pkgs", handlerPkgs);
   }

   /**
    * Create a new JBossXBTestDelegate.
    * 
    * @param clazz the test class
    */
   public JBossXBTestDelegate(Class clazz)
   {
      super(clazz);
   }

   public void setUp() throws Exception
   {
      super.setUp();
      init();
      unmarshallerFactory = UnmarshallerFactory.newInstance();
      initResolver();
   }
   
   protected void initResolver() throws Exception
   {
      try
      {
         Method method = clazz.getMethod("initResolver", null);
         defaultResolver = (SchemaBindingResolver) method.invoke(null, null);
      }
      catch (NoSuchMethodException ignored)
      {
         defaultResolver = new DefaultSchemaResolver();
      }
   }
   
   /**
    * Unmarshal an object
    * 
    * @param url the url
    * @param resolver the resolver
    * @return the object
    * @throws Exception for any error
    */
   public Object unmarshal(String url, SchemaBindingResolver resolver) throws Exception
   {
      if (resolver == null)
         resolver = defaultResolver;
      
      long start = System.currentTimeMillis();
      Unmarshaller unmarshaller = unmarshallerFactory.newUnmarshaller();
      log.debug("Initialized parsing in " + (System.currentTimeMillis() - start) + "ms");
      try
      {
         Object result = unmarshaller.unmarshal(url, resolver);
         log.debug("Total parse for " + url + " took " + (System.currentTimeMillis() - start) + "ms");
         return result;
      }
      catch (Exception e)
      {
         log.debug("Error during parsing: " + url, e);
         throw e;
      }
   }
}
