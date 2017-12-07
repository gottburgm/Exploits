/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws;

import junit.extensions.TestSetup;
import junit.framework.TestSuite;

import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
import java.net.URLClassLoader;
import java.net.URL;

/**
 * A test setup that deploys/undeploys archives
 *
 * @author Thomas.Diesler@jboss.org
 * @since 14-Oct-2004
 */
public class JBossWSTestSetup extends TestSetup
{
   private JBossWSTestHelper delegate = new JBossWSTestHelper();
   private String[] archives;

   protected JBossWSTestSetup(Class testClass, String archiveList)
   {
      super(new TestSuite(testClass));

      StringTokenizer st = new StringTokenizer(archiveList, ", ");
      archives = new String[st.countTokens()];

      for (int i = 0; i < archives.length; i++)
         archives[i] = st.nextToken();
   }

   public static JBossWSTestSetup newTestSetup(Class testClass, String archiveList)
   {
      return new JBossWSTestSetup(testClass, archiveList);
   }

   protected void setUp() throws Exception
   {
      List clientJars = new ArrayList();
      for (int i = 0; i < archives.length; i++)
      {
         String archive = archives[i];
         boolean isJ2EEClient = archive.endsWith("-client.jar");
         if (delegate.isTargetServerJBoss() || isJ2EEClient == false)
         {
            try
            {
               delegate.deploy(archive);
            }
            catch (Exception ex)
            {
               ex.printStackTrace();
               delegate.undeploy(archive);
            }
         }
         if (isJ2EEClient)
         {
            URL archiveURL = delegate.getArchiveURL(archive);
            clientJars.add(archiveURL);
         }
      }

      // add the client jars to the classloader
      if( !clientJars.isEmpty() )
      {
         ClassLoader parent = Thread.currentThread().getContextClassLoader();
         URL[] urls = new URL[clientJars.size()];
         for(int i=0; i<clientJars.size(); i++)
         {
            urls[i] = (URL)clientJars.get(i);
         }
         URLClassLoader cl = new URLClassLoader(urls, parent);
         Thread.currentThread().setContextClassLoader(cl);
      }
   }

   protected void tearDown() throws Exception
   {
      for (int i = 0; i < archives.length; i++)
      {
         String archive = archives[archives.length - i - 1];
         boolean isJ2EEClient = archive.endsWith("-client.jar");
         if (delegate.isTargetServerJBoss() || isJ2EEClient == false)
         {
            delegate.undeploy(archive);
         }
      }
   }
}
