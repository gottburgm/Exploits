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
package org.jboss.test.jmx.compliance.loading;

import java.net.URL;
import java.util.Iterator;
import java.util.Set;

import javax.management.Attribute;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.ServiceNotFoundException;
import javax.management.loading.MLet;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/**
 * MLet tests.
 *
 * @author <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 */
public class MLetTEST extends TestCase
{
   private MBeanServer server;

   private URL location;
   
   public MLetTEST(String s) throws Exception
   {
      super(s);
      
      // Workout the output location for the dynamically loaded files
      location = getClass().getResource("/org/jboss/test/jmx/compliance/loading/MLetTEST.class");
      String jarPath = location.getPath();
      int i = jarPath.indexOf('!');
      if (i != -1)
      {
         jarPath = jarPath.substring(0, i);
         location = new URL(jarPath);
         location = new URL(location, "file:../");
      }
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      server = MBeanServerFactory.createMBeanServer();
   }

   public void testCreateAndRegister() throws Exception
   {

      MLet mlet = new MLet();
      ObjectName name = new ObjectName("test:name=mlet");
   
      try
      {
         server.registerMBean(mlet, name);
      }
      finally
      {
         server.unregisterMBean(name);
      }
   }
   
   public void testMLetLoadClassFromURLInConstructor() throws Exception
   {
      // NOTE: 
      // the urls used here are relative to the location of the build.xml
      
      final URL MBEANS_URL = new URL(location, "lib/jmxcompliance-MyMBeans.jar");
      
      // make sure the class is not available
      try
      {
         server.getClassLoaderRepository().loadClass("org.jboss.test.jmx.compliance.loading.support.Trivial");
         
         fail("class org.jboss.test.jmx.compliance.loading.support.Trivial was already found in CL repository.");
      }
      catch (ClassNotFoundException e)
      {
         // expected
      }

      MBeanServer server = MBeanServerFactory.createMBeanServer();
      ObjectName name = new ObjectName("test:name=mlet");
      MLet mlet = new MLet(new URL[] { MBEANS_URL });
      
      // make sure the class is not available
      try
      {
         server.getClassLoaderRepository().loadClass("org.jboss.test.jmx.compliance.loading.support.Trivial");
         
         fail("class org.jboss.test.jmx.compliance.loading.support.Trivial found in CL repository after MLet construction.");
      }
      catch (ClassNotFoundException e)
      {
         // expected
      }

      try
      {      
         server.registerMBean(mlet, name);
         server.getClassLoaderRepository().loadClass("org.jboss.test.jmx.compliance.loading.support.Trivial");
      }
      finally
      {
         server.unregisterMBean(name); 
      }
      // make sure the class is not available
      try
      {
         server.getClassLoaderRepository().loadClass("org.jboss.test.jmx.compliance.loading.support.Trivial");
         
         fail("class org.jboss.test.jmx.compliance.loading.support.Trivial was still found in CL repository.");
      }
      catch (ClassNotFoundException e)
      {
         // expected
      }
   }
   
   public void testBasicMLetFileLoad() throws Exception
   {
      // NOTE: 
      // the urls used here are relative to the location of the build.xml
      
      final URL MLET_URL = new URL(location, "etc/tests/BasicConfig.mlet");
      
      // make sure the classes are loaded from mlet, not system cl
      try
      {
         server.getClassLoaderRepository().loadClass("org.jboss.test.jmx.compliance.loading.support.Trivial");
         
         fail("class org.jboss.test.jmx.compliance.loading.support.Trivial was already found in CL repository.");
      }
      catch (ClassNotFoundException e)
      {
         // expected
      }
      // make sure the classes are loaded from mlet, not system cl
      try
      {
         server.getClassLoaderRepository().loadClass("org.jboss.test.jmx.compliance.loading.support.Trivial2");
         
         fail("class org.jboss.test.jmx.compliance.loading.support.Trivial2 was already found in CL repository.");
      }
      catch (ClassNotFoundException e)
      {
         // expected
      }
     
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      MLet mlet = new MLet();
      ObjectName name = new ObjectName("test:name=mlet");
      server.registerMBean(mlet, name);
      
      server.invoke(name, "getMBeansFromURL", 
                                      new Object[] { MLET_URL.toString() },
                                      new String[] { String.class.getName() }
      );

      try 
      {         
         assertTrue(server.isRegistered(new ObjectName("test:name=Trivial")));
         assertTrue(server.isRegistered(new ObjectName("test:name=Trivial2")));
      }
      catch (AssertionFailedError e)
      {
         URL[] urls = mlet.getURLs();
         URL url = null;
         
         if (urls != null && urls.length > 0)
            url = urls[0];
         
         fail("FAILS IN RI: SUN JMX RI builds a malformed URL from an MLet text file URL '" +
              MLET_URL + "' resulting into MLET codebase URL '" + url + "' and therefore fails " +
              "to load the required classes from the Java archive (MyMBeans.jar)");
      }

      assertTrue(server.getMBeanInfo(new ObjectName("test:name=Trivial")) != null);
      assertTrue(server.getMBeanInfo(new ObjectName("test:name=Trivial2")) != null);
      
      assertTrue(server.getAttribute(new ObjectName("test:name=Trivial2"), "Something").equals("foo"));

      server.invoke(new ObjectName("test:name=Trivial"), "doOperation",
      new Object[] { "Test" },
      new String[] { String.class.getName() }
      );
      
      server.invoke(new ObjectName("test:name=Trivial2"), "doOperation",
      new Object[] { "Test" },
      new String[] { String.class.getName() }
      );
   }

   /**
    * Make sure the versioning MLet installer won't replace MBeans that were
    * not registered with version information.
    */
   public void testConflictingMLetFileLoad() throws Exception
   {
      // NOTE: 
      // the urls used here are relative to the location of the build.xml
      
      final URL MLET_URL1 = new URL(location, "etc/tests/BasicConfig2.mlet");
      final URL MLET_URL2 = new URL(location, "etc/tests/BasicConfig2.mlet");
      
      // make sure the classes are loaded from mlet, not system cl
      try
      {
         server.getClassLoaderRepository().loadClass("org.jboss.test.jmx.compliance.loading.support.Trivial3");
         
         fail("class org.jboss.test.jmx.compliance.loading.support.Trivial was already found in CL repository.");
      }
      catch (ClassNotFoundException e)
      {
         // expected
      }
      // make sure the classes are loaded from mlet, not system cl
      try
      {
         server.getClassLoaderRepository().loadClass("org.jboss.test.jmx.compliance.loading.support.Trivial4");
         
         fail("class org.jboss.test.jmx.compliance.loading.support.Trivial2 was already found in CL repository.");
      }
      catch (ClassNotFoundException e)
      {
         // expected
      }
     
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      MLet mlet = new MLet();
      ObjectName name = new ObjectName("test:name=mlet");
      server.registerMBean(mlet, name);
      
      Set result = (Set) server.invoke(name, "getMBeansFromURL", 
                                      new Object[] { MLET_URL1.toString() },
                                      new String[] { String.class.getName() }
      );
      checkResult(result);

      ObjectName oname = new ObjectName("test:name=Trivial2"); 
      server.setAttribute(oname, new Attribute("Something", "Something"));
      
      mlet = new MLet();
      name = new ObjectName("test:name=mlet2");
      server.registerMBean(mlet, name);
      
      server.invoke(name, "getMBeansFromURL", 
                               new Object[] { MLET_URL2.toString() },
                               new String[] { String.class.getName() }
      );
      
      oname = new ObjectName("test:name=Trivial2");
      String value = (String)server.getAttribute(oname, "Something");

      assertTrue(value.equals("Something")); 
   }

      
   public void testMalformedURLLoad() throws Exception
   {
      // NOTE:
      // the urls used here are relative to the location of the build.xml

      final URL MLET_URL = new URL(location, "etc/tests/BasicConfig.mlet");
      
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      MLet mlet = new MLet();
      ObjectName name = new ObjectName("test:name=mlet");
      try
      {
         server.registerMBean(mlet, name);
         
         server.invoke(name, "getMBeansFromURL",
         new Object[] { MLET_URL.getPath() },
         new String[] { String.class.getName() }
         );
         
         // should not reach here
         fail("FAILS IN RI: Malformed URL in getMBeansURL() should result in ServiceNotFoundException thrown.");
      }
      catch (AssertionFailedError e)
      {
         // defensive: in case assertXXX() or fail() are later added
         throw e;
      }
      catch (MBeanException e)
      {
         assertTrue(e.getTargetException() instanceof ServiceNotFoundException);
      }
      finally
      {
         try
         {
            server.unregisterMBean(name);
         }
         catch (Exception ignored) {}
      }
   }
   
   public void testMissingMLetTagInLoad() throws Exception
   {
      // NOTE:
      // the urls used here are relative to the location of the build.xml

      final URL MLET_URL = new URL(location, "etc/tests/MissingMLET.mlet");

      MBeanServer server = MBeanServerFactory.createMBeanServer();
      MLet mlet = new MLet();
      ObjectName name = new ObjectName("test:name=mlet");
      try
      {
         server.registerMBean(mlet, name);
         
         server.invoke(name, "getMBeansFromURL",
         new Object[] { MLET_URL.toString() },
         new String[] { String.class.getName() }
         );
         
         // should not reach here
         fail("MLet text file missing the MLET tag should result in ServiceNotFoundException thrown.");
      }
      catch (AssertionFailedError e)
      {
         // defensive: in case assertXXX() or fail() are added later
         throw e;
      }
      catch (MBeanException e)
      {
         assertTrue(e.getTargetException() instanceof ServiceNotFoundException);
      }
      finally
      {
         try
         {
            server.unregisterMBean(name);
         }
         catch (Exception ignored) {}
      }
   }

   public void testMissingMandatoryArchiveTagInLoad() throws Exception
   {
      // NOTE:
      // the urls used here are relative to the location of the build.xml

      final URL MLET_URL = new URL(location, "etc/tests/MissingMandatoryArchive.mlet");

      MBeanServer server = MBeanServerFactory.createMBeanServer();
      MLet mlet = new MLet();
      ObjectName name = new ObjectName("test:name=mlet");
      try
      {
         server.registerMBean(mlet, name);
         
         server.invoke(name, "getMBeansFromURL",
         new Object[] { MLET_URL.toString() },
         new String[] { String.class.getName() }
         );
         
         // should not reach here
         fail("MLet text file missing mandatory ARCHIVE attribute should result in ServiceNotFoundException thrown.");
      }
      catch (AssertionFailedError e)
      {
         // defensive: in case assertXXX() or fail() are added later
         throw e;
      }
      catch (MBeanException e)
      {
         assertTrue(e.getTargetException() instanceof ServiceNotFoundException);
      }
      finally
      {
         try
         {
            server.unregisterMBean(name);
         }
         catch (Exception ignored) {}
      }
   }
   
   public void testMissingMandatoryCodeTagInLoad() throws Exception
   {
      // NOTE:
      // the urls used here are relative to the location of the build.xml

      final URL MLET_URL = new URL(location, "etc/tests/MissingMandatoryCode.mlet");
      
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      MLet mlet = new MLet();
      ObjectName name = new ObjectName("test:name=mlet");
      try
      {
         server.registerMBean(mlet, name);
         
         server.invoke(name, "getMBeansFromURL",
         new Object[] { MLET_URL.toString() },
         new String[] { String.class.getName() }
         );
         
         // should not reach here
         fail("MLet text file missing mandatory CODE attribute should result in ServiceNotFoundException thrown.");
      }
      catch (AssertionFailedError e)
      {
         // defensive: in case assertXXX() or fail() are added later
         throw e;
      }
      catch (MBeanException e)
      {
         assertTrue(e.getTargetException() instanceof ServiceNotFoundException);
      }
      finally
      {
         try
         {
            server.unregisterMBean(name);
         }
         catch (Exception ignored) {}
      }
   }         
   
   public void testArchiveListInMLet() throws Exception
   {
      // NOTE:
      // the urls used here are relative to the location of the build.xml

      final URL MLET_URL = new URL(location, "etc/tests/ArchiveList.mlet");
   
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      MLet mlet = new MLet();
      ObjectName name = new ObjectName("test:name=mlet");

      try
      {
         server.registerMBean(mlet, name);
      
         server.invoke(name, "getMBeansFromURL",
         new Object[] { MLET_URL.toString() },
         new String[] { String.class.getName() }
         );
      
         Class c = null;
      
         try
         {
            c  = mlet.loadClass("org.jboss.test.jmx.compliance.loading.support.AClass");
         }
         catch (ClassNotFoundException e)
         {
            URL[] urls = mlet.getURLs();
            fail("FAILS IN RI: SUN JMX RI builds a malformed URL from an MLet text file URL '" +
                 MLET_URL + "' resulting into MLET codebase URL '" + urls[0] + "' and therefore fails " +
                 "to load the required classes from the Java archive.");            
         }
      
         Object o = c.newInstance();
      
         server.setAttribute(new ObjectName("test:name=AnotherTrivial"), new Attribute("Something", o));
         o = server.getAttribute(new ObjectName("test:name=AnotherTrivial"), "Something");
      
         assertTrue(o.getClass().isAssignableFrom(c));
      }
      finally
      {
         try
         {
            server.unregisterMBean(name);
         }
         catch (Exception ignored) {}
      }
   }
   
   public void testUnexpectedEndOfFile() throws Exception
   {
      // NOTE:
      // the urls used here are relative to the location of the build.xml

      final URL MLET_URL = new URL(location, "etc/tests/UnexpectedEnd.mlet");

      MBeanServer server = MBeanServerFactory.createMBeanServer();
      MLet mlet = new MLet();
      ObjectName name = new ObjectName("test:name=mlet");
      try
      {
         server.registerMBean(mlet, name);
         
         server.invoke(name, "getMBeansFromURL",
         new Object[] { MLET_URL.toString() },
         new String[] { String.class.getName() }
         );
         
         // should not reach here
         fail("Unexpected end of file from mlet text file did not cause an exception.");
      }
      catch (AssertionFailedError e)
      {
         throw e;
      }
      catch (MBeanException e)
      {
         assertTrue(e.getTargetException() instanceof ServiceNotFoundException);
      }
      finally
      {
         try
         {
            server.unregisterMBean(name);
         }
         catch (Exception ignored) {}
      }
   }
   
   public void testMissingEndMLetTag() throws Exception
   {
      // NOTE:
      // the urls used here are relative to the location of the build.xml

      final URL MLET_URL = new URL(location, "etc/tests/MissingEndTag.mlet");

      MBeanServer server = MBeanServerFactory.createMBeanServer();
      MLet mlet = new MLet();
      ObjectName name = new ObjectName("test:name=mlet");
      try
      {
         server.registerMBean(mlet, name);
         
         server.invoke(name, "getMBeansFromURL",
         new Object[] { MLET_URL.toString() },
         new String[] { String.class.getName() }
         );
      
         assertTrue(!server.isRegistered(new ObjectName("test:name=Trivial")));
      }
      catch (AssertionFailedError e)
      {
         throw e;
      }
      catch (MBeanException e)
      {
         assertTrue(e.getTargetException() instanceof ServiceNotFoundException);
      }
      finally
      {
         try
         {
            server.unregisterMBean(name);
         }
         catch (Exception ignored) {}
      }
   }
   
   
   protected void checkResult(Set result)
   {
      for (Iterator i = result.iterator(); i.hasNext();)
      {
         Object mbean = i.next();
         if (mbean instanceof Throwable)
            throw new RuntimeException((Throwable) mbean);
      }
   }
}
