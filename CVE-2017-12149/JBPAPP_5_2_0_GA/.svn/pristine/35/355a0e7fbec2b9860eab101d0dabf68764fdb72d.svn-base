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
package test.implementation.loading;

import java.net.URL;

import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.loading.DefaultLoaderRepository;
import javax.management.loading.MLet;

import junit.framework.TestCase;

import org.jboss.mx.loading.LoaderRepository;
import org.jboss.mx.loading.RepositoryClassLoader;
import org.jboss.mx.server.ServerConstants;
import org.jboss.mx.util.AgentID;

public class LoaderRepositoryTEST extends TestCase implements ServerConstants
{
   public LoaderRepositoryTEST(String s)
   {
      super(s);
   }

   public void testRemoveDuplicateURL() throws Exception
   {
      // NOTE:
      // the urls used here are relative to the location of the build.xml
      final URL url = new URL("file:./output/etc/test/implementation/loading/MyMBeans.jar");

      // Retrieve the loader repository
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      LoaderRepository lr = (LoaderRepository)server.getClassLoaderRepository();

      // Should not be able to load the class
      try
      {
         lr.loadClass("test.implementation.loading.support.Trivial");
         fail("test.implementation.loading.support.Trivial is already visible");
      }
      catch (ClassNotFoundException expected) {}

      // Add the URL to the repository twice
      RepositoryClassLoader ucl1 = lr.newClassLoader(url, true);
      RepositoryClassLoader ucl2 = lr.newClassLoader(url, true);

      // Should be able to load the class
      lr.loadClass("test.implementation.loading.support.Trivial");

      // Remove one
      ucl1.unregister();

      // Should still be able to load the class
      lr.loadClass("test.implementation.loading.support.Trivial");

      // Remove the other
      ucl2.unregister();
   }

   public void testClassConflictBetweenMLets() throws Exception
   {
      // NOTE:
      // the urls used here are relative to the location of the build.xml
 
      // make sure the classes are loaded from mlet, not system cl
      String[] classes = { "test.implementation.loading.support.Start",
                           "test.implementation.loading.support.StartMBean",
                           "test.implementation.loading.support.Target",
                           "test.implementation.loading.support.TargetMBean",
                           "test.implementation.loading.support.AClass"
      };
            
      for (int i = 0; i < classes.length; ++i)
      {
         try
         {
            DefaultLoaderRepository.loadClass(classes[i]);

            fail("class " + classes[i] + " was already found in CL repository.");
         }
         catch (ClassNotFoundException e)
         {
            // expected
         }
      }
      
      try
      {
         MBeanServer server = MBeanServerFactory.createMBeanServer();
         MLet mlet1 = new MLet();
         MLet mlet2 = new MLet();
         ObjectName m1Name = new ObjectName(":name=mlet1");
         ObjectName m2Name = new ObjectName(":name=mlet2");
         
         server.registerMBean(mlet1, m1Name);
         server.registerMBean(mlet2, m2Name);
         
         server.invoke(m1Name, "getMBeansFromURL",
         new Object[] { "file:./output/etc/test/implementation/loading/CCTest1.mlet" },
         new String[] { String.class.getName() }
         );
         
         server.invoke(m2Name, "getMBeansFromURL",
         new Object[] { "file:./output/etc/test/implementation/loading/CCTest2.mlet" },
         new String[] { String.class.getName() }
         );
         
         server.invoke(new ObjectName(":name=Start"), "startOp", 
         new Object[] { AgentID.get(server) },
         new String[] { String.class.getName() }
         );
         
         //fail("Expected to fail due to two different mlet loaders having a class mismatch.");
      }
      catch (MBeanException e)
      {
         if (e.getTargetException() instanceof ReflectionException)
         {
            // expected, argument type mismatch error since the arg of type AClass is
            // loaded by diff mlet loader than the target MBean with AClass in its sign.
            if (System.getProperty(LOADER_REPOSITORY_CLASS_PROPERTY).equals(UNIFIED_LOADER_REPOSITORY_CLASS))
               throw e;
         }
         else throw e;
      }
   }

}
