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

import java.util.List;

import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.loading.MLet;

import junit.framework.TestCase;

import org.jboss.mx.server.ServerConstants;
import org.jboss.mx.util.MBeanInstaller;

public class MLetVersionTEST extends TestCase
{
   public MLetVersionTEST(String s)
   {
      super(s);
   }

   public void testMLetFileLoadWithVersion() throws Exception
   {
      // NOTE: 
      // the urls used here are relative to the location of the build.xml
      
      final String MLET_URL1 = "file:./output/etc/test/implementation/loading/MLetVersion1.mlet";
      final String MLET_URL2 = "file:./output/etc/test/implementation/loading/MLetVersion2.mlet";

      ObjectName registry = new ObjectName(ServerConstants.MBEAN_REGISTRY);

      MBeanServer server = MBeanServerFactory.createMBeanServer();
      MLet mlet = new MLet();
      ObjectName name = new ObjectName("test:name=mlet");
      server.registerMBean(mlet, name);

      // Repeat to call the getMBeansFromURL method

      server.invoke(name, "getMBeansFromURL",
      new Object[] { MLET_URL1 },
      new String[] { String.class.getName() }
      );

      server.invoke(name, "getMBeansFromURL",
      new Object[] { MLET_URL2 },
      new String[] { String.class.getName() }
      );

      try
      {
         List versions1 =
               (List) server.invoke(registry, "getValue",
                                    new Object[]
                                    {
                                       new ObjectName("test:name=Trivial"),
                                       MBeanInstaller.VERSIONS
                                    },
                                    new String[]
                                    {
                                       ObjectName.class.getName(),
                                       String.class.getName()
                                    }
               );
         List versions2 =
               (List) server.invoke(registry, "getValue",
                                    new Object[]
                                    {
                                       new ObjectName("test:name=Trivial2"),
                                       MBeanInstaller.VERSIONS
                                    },
                                    new String[]
                                    {
                                       ObjectName.class.getName(),
                                       String.class.getName()
                                    }
               );

         assertTrue("Trivial1 version=" + versions1, ((String)versions1.get(0)).equals("1.1"));
         assertTrue("Trivial2 version=" + versions2, ((String)versions2.get(0)).equals("2.1"));
       }
      catch (MBeanException e)
      {
         e.printStackTrace();
         assertTrue(false);
      }

      assertTrue(server.isRegistered(new ObjectName("test:name=Trivial")));
      assertTrue(server.isRegistered(new ObjectName("test:name=Trivial2")));

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


   public void testMLetFileLoadWithVersion2() throws Exception
   {
      // NOTE: 
      // the urls used here are relative to the location of the build.xml
      
      final String MLET_URL1 = "file:./output/etc/test/implementation/loading/MLetVersion3.mlet";
      final String MLET_URL2 = "file:./output/etc/test/implementation/loading/MLetVersion4.mlet";

      ObjectName registry = new ObjectName(ServerConstants.MBEAN_REGISTRY);

      MBeanServer server = MBeanServerFactory.createMBeanServer();
      MLet mlet = new MLet();
      ObjectName name = new ObjectName("test:name=mlet");
      server.registerMBean(mlet, name);

      // Repeat to call the getMBeansFromURL method

      server.invoke(name, "getMBeansFromURL",
      new Object[] { MLET_URL1 },
      new String[] { String.class.getName() }
      );

      server.invoke(name, "getMBeansFromURL",
      new Object[] { MLET_URL2 },
      new String[] { String.class.getName() }
      );

      try
      {
         List versions1 =
               (List) server.invoke(registry, "getValue",
                                    new Object[]
                                    {
                                       new ObjectName("test:name=Trivial,round=2"),
                                       MBeanInstaller.VERSIONS
                                    },
                                    new String[]
                                    {
                                       ObjectName.class.getName(),
                                       String.class.getName()
                                    }
               );
         List versions2 =
               (List) server.invoke(registry, "getValue",
                                    new Object[]
                                    {
                                       new ObjectName("test:name=Trivial2,round=2"),
                                       MBeanInstaller.VERSIONS
                                    },
                                    new String[]
                                    {
                                       ObjectName.class.getName(),
                                       String.class.getName()
                                    }
               );

         assertTrue("Trivial1 version=" + versions1, versions1 == null);
         assertTrue("Trivial2 version=" + versions2, ((String)versions2.get(0)).equals("5.1"));
       }
      catch (MBeanException e)
      {
         e.printStackTrace();
         assertTrue(false);
      }

      assertTrue(server.isRegistered(new ObjectName("test:name=Trivial,round=2")));
      assertTrue(server.isRegistered(new ObjectName("test:name=Trivial2,round=2")));

      assertTrue(server.getMBeanInfo(new ObjectName("test:name=Trivial,round=2")) != null);
      assertTrue(server.getMBeanInfo(new ObjectName("test:name=Trivial2,round=2")) != null);

      assertTrue(server.getAttribute(new ObjectName("test:name=Trivial2,round=2"), "Something").equals("foo"));

      server.invoke(new ObjectName("test:name=Trivial,round=2"), "doOperation",
      new Object[] { "Test" },
      new String[] { String.class.getName() }
      );

      server.invoke(new ObjectName("test:name=Trivial2,round=2"), "doOperation",
      new Object[] { "Test" },
      new String[] { String.class.getName() }
      );

   }
   
}
