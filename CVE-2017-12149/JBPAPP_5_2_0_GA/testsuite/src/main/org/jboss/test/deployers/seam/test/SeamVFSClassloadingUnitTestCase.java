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
package org.jboss.test.deployers.seam.test;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Map;

import junit.framework.Test;

import org.jboss.classloader.spi.ClassLoaderPolicy;
import org.jboss.classloading.spi.vfs.policy.VFSClassLoaderPolicy;
import org.jboss.mx.loading.UnifiedLoaderRepository3;
import org.jboss.test.JBossTestCase;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;

/**
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85526 $
 */
public class SeamVFSClassloadingUnitTestCase extends JBossTestCase
{
   public SeamVFSClassloadingUnitTestCase(String string)
   {
      super(string);
   }

   public static Test suite()
   {
      return suite(SeamVFSClassloadingUnitTestCase.class);
   }

   /*
   jboss-seam-booking.ear contents
   META-INF/application.xml
   META-INF/jboss-app.xml
   lib/commons-beanutils.jar
   lib/commons-digester.jar
   lib/jboss-el.jar
   lib/richfaces-api.jar
   jboss-seam.jar
   jboss-seam.jar/META-INF/MANIFEST.MF
   jboss-seam.jar/META-INF/components.xml
   jboss-seam.jar/META-INF/ejb-jar.xml
   jboss-seam.jar/META-INF/faces-config.xml
   jboss-seam.jar/META-INF/javamail.providers
   jboss-seam-booking.jar
   jboss-seam-booking.jar/META-INF/ejb-jar.xml
   jboss-seam-booking.jar/META-INF/persistence.xml
   jboss-seam-booking.war
   jboss-seam-booking.war/WEB-INF/classes/
   jboss-seam-booking.war/WEB-INF/components.xml
   jboss-seam-booking.war/WEB-INF/faces-config.xml
   jboss-seam-booking.war/WEB-INF/lib/jboss-seam-debug.jar
   jboss-seam-booking.war/WEB-INF/lib/jboss-seam-ui.jar
   jboss-seam-booking.war/WEB-INF/lib/jsf-facelets.jar
   jboss-seam-booking.war/WEB-INF/lib/richfaces-impl.jar
   jboss-seam-booking.war/WEB-INF/lib/richfaces-ui.jar
   jboss-seam-booking.war/WEB-INF/pages.xml
   jboss-seam-booking.war/WEB-INF/web.xml
   */
   protected VirtualFile getRoot(boolean noCopy) throws IOException
   {
      URL url = getDeployURL("jboss-seam-booking.ear");
      if(noCopy)
         url = new URL(url.toExternalForm() + "?useNoCopyJarHandler=true");
      assertNotNull(url);
      VFS vfs = VFS.getVFS(url);
      VirtualFile vf = vfs.getRoot();
      assertNotNull(vf);
      return vf;
   }
   protected URL[] getEarClassPath(VirtualFile ear)
      throws Exception
   {
      URL[] cp = {
         // ear
         ear.toURL(),
         ear.findChild("lib/commons-beanutils.jar").toURL(),
         ear.findChild("lib/commons-digester.jar").toURL(),
         ear.findChild("lib/commons-digester.jar").toURL(),
         ear.findChild("lib/jboss-el.jar").toURL(),
         ear.findChild("lib/richfaces-api.jar").toURL(),
         ear.findChild("jboss-seam.jar").toURL(),
         ear.findChild("jboss-seam-booking.jar").toURL(),
         ear.findChild("jboss-seam-booking.war/WEB-INF/classes/").toURL(),
         ear.findChild("jboss-seam-booking.war/WEB-INF/lib/jboss-seam-debug.jar").toURL(),
         ear.findChild("jboss-seam-booking.war/WEB-INF/lib/jboss-seam-ui.jar").toURL(),
         ear.findChild("jboss-seam-booking.war/WEB-INF/lib/jsf-facelets.jar").toURL(),
         ear.findChild("jboss-seam-booking.war/WEB-INF/lib/richfaces-impl.jar").toURL(),
         ear.findChild("jboss-seam-booking.war/WEB-INF/lib/richfaces-ui.jar").toURL(),
      };
      return cp;
   }

   public void testURLClassLoader() throws Exception
   {
      VirtualFile ear = getRoot(false);
      testURLClassLoader(ear);
   }
   public void testURLClassLoaderNoCopy() throws Exception
   {
      VirtualFile ear = getRoot(true);
      testURLClassLoader(ear);
   }
   protected void testURLClassLoader(VirtualFile ear) throws Exception
   {
      URL[] cp = getEarClassPath(ear);
      log.debug("ear classpath: "+Arrays.asList(cp));
      URLClassLoader loader = new URLClassLoader(cp);
      loader.loadClass("org.jboss.seam.example.booking.Hotel");
      loader.loadClass("org.jboss.seam.debug.Contexts");      
   }

   public void testULRClassloading() throws Exception
   {
      VirtualFile ear = getRoot(false);
      testULRClassloading(ear);
   }
   public void testULRClassloadingNoCopy() throws Exception
   {
      VirtualFile ear = getRoot(true);
      testULRClassloading(ear);
   }
   public void testULRClassloading(VirtualFile ear) throws Exception
   {
      URL[] cp = getEarClassPath(ear);
      UnifiedLoaderRepository3 repository = new UnifiedLoaderRepository3();
      for(URL url : cp)
         repository.newClassLoader(url, true);
      log.debug("ear classpath: "+Arrays.asList(cp));
      repository.loadClass("org.jboss.seam.example.booking.Hotel");
      repository.loadClass("org.jboss.seam.debug.Contexts");
   }

   public void testVFSPolicy() throws Exception
   {
      VirtualFile vf = getRoot(false);
      VirtualFile child = vf.getChild("jboss-seam-booking.war/WEB-INF/lib/jboss-seam-debug.jar");
      assertNotNull(child);
      VirtualFile[] roots = {child};
      ClassLoaderPolicy policy = new VFSClassLoaderPolicy(roots);
      URL url = policy.getResource("org/jboss/seam/debug/Contexts.class");
      log.info(url);
      assertNotNull(url);
   }
}
