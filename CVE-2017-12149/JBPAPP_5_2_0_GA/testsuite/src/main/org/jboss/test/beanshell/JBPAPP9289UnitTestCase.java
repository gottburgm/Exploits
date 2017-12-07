/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.test.beanshell;

import java.io.File;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Date;

import org.jboss.test.JBossTestCase;

public class JBPAPP9289UnitTestCase extends JBossTestCase
{
   private static String jbossHome;
   private ClassLoader secondaryBSHClassLoader;
   private ClassLoader primaryBSHClassLoader;
   private static String bshInterpreterClassName = "bsh.Interpreter";
   private URL eapBshJar;
   private URL unpatchedBshJar;
   
   public JBPAPP9289UnitTestCase(String name)
   {
      super(name);
   }
   
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      // setup urls
      jbossHome = System.getProperty("jboss.dist");
      unpatchedBshJar = Thread.currentThread().getContextClassLoader().getResource("org/jboss/test/beanshell/jbpapp9289/bsh-2.0b4.jar");
      
      eapBshJar = new File(jbossHome + "/" + "common/lib/bsh.jar").toURI().toURL();
      primaryBSHClassLoader = new URLClassLoader(new URL[] { eapBshJar }, null);
      log.debug("Primary classloader: " + primaryBSHClassLoader);
      
      log.debug("EAP Bsh jar: " + eapBshJar);
      log.debug("Unpatched Bsh jar: " + unpatchedBshJar);
      
      // Create a classloader that has the bsh.jar from EAP 5.x      
      secondaryBSHClassLoader = new URLClassLoader(new URL[] { unpatchedBshJar }, null);
      
      log.debug("secondary classloader: " + secondaryBSHClassLoader);
   }
   
   public void testPerformance()
   {
      try
      {
         // test bsh. in the EAP which should be patched
    	 Class clazz = primaryBSHClassLoader.loadClass(bshInterpreterClassName); // Thread.currentThread().getContextClassLoader().loadClass(bshInterpreterClassName);
         long eapElapseTime = benchmarkTime(clazz, 100);
         log.debug("EAP Elapse: " + eapElapseTime);
         
         // test bsh.Interpreter in the EAP which is not patched
         clazz = secondaryBSHClassLoader.loadClass(bshInterpreterClassName);         
         long unpatchedElapseTime = benchmarkTime(clazz, 100);
         log.debug("Unpatched Elapse: " + unpatchedElapseTime);       
         
         // if eapElapseTime is not less than unpatchedElapseTime, then it appears the patch is missing
         if((eapElapseTime * 2) > unpatchedElapseTime)
         {
            fail("JBPAPP-9289 the EAP bsh.jar: " + eapBshJar + " does not appear to be patched. It should be at least twice as fast.  EAP bsh time: " + eapElapseTime + " unpatched bsh time: " + unpatchedElapseTime);
         }
      // use different classloaders, set the system property before accessing the second one
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }
   
   private long benchmarkTime(Class clazz, int times)
   {
	  log.debug("benchmarkTime on class: " + clazz  + " for " + times + " iterations");
      long start = new Date().getTime();
      long stop;
      
      for(int i=0; i<times; i++)
      {
         try
         {
            clazz.newInstance();
         }
         catch(Exception e)
         {
            e.printStackTrace();
         }
      }
      
      stop = new Date().getTime();
      return (stop - start);
   }
}
