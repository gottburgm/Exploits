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
package org.jboss.test.aop.test;

import javax.management.ObjectName;
import javax.management.Attribute;

import junit.framework.TestSuite;
import org.jboss.test.JBossTestSetup;

/** A test setup wrapper that enables the AspectManager transformation mode
 * on setUp and disables it on tearDown.
 * 
 * For use with either the -javaagent or -Xbootclasspath switches
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class AOPClassLoaderHookTestSetup extends JBossTestSetup
{
   public static String ASPECT_MANAGER_NAME = "jboss.aop:service=AspectManager";

   private String[] jars;
   SetupHook hook;
   boolean useBaseXml;
   
   // Create an initializer for the test suite
   public AOPClassLoaderHookTestSetup(TestSuite suite) throws Exception
   {
      super(suite);
   }
   
   public AOPClassLoaderHookTestSetup(TestSuite suite, String jar) throws Exception
   {
      super(suite);
      this.jars = jar.split(",");
   }
   
   public AOPClassLoaderHookTestSetup(TestSuite suite, SetupHook hook, String jar) throws Exception
   {
      this(suite, jar);
      this.hook = hook;
   }
   
   /**
    * Sets whether to use base-aspects.xml for this test
    */
   public void setUseBaseXml(boolean useIt)
   {
      this.useBaseXml = useIt;
   }
   
   protected void setUp() throws Exception
   {
      super.setUp();
      ObjectName aspectManager = new ObjectName(ASPECT_MANAGER_NAME);
      Attribute enableTransformer = new Attribute("EnableLoadtimeWeaving", Boolean.TRUE);
      getServer().setAttribute(aspectManager, enableTransformer);
      if (useBaseXml)
      {
         Attribute useBaseXmlAttribute = new Attribute("UseBaseXml", useBaseXml);
         getServer().setAttribute(new ObjectName("jboss.aop:service=AspectManager"), useBaseXmlAttribute);
      }
      if (hook != null)
      {
         hook.setup(getServer());
      }
      try
      {
         if (jars != null)
         {
            for (int i = 0 ; i < jars.length ; i++)
            {
               String jar = jars[i].trim();
               redeploy(jar);
            }
         }
      }
      catch(Exception e)
      {
         // Reset the EnableTransformer to false
         try
         {
            enableTransformer = new Attribute("EnableLoadtimeWeaving", Boolean.FALSE);
            getServer().setAttribute(aspectManager, enableTransformer);
            if (hook != null)
            {
               hook.teardown(getServer());
            }
         }
         catch(Exception ex)
         {
            getLog().error("Failed to set EnableLoadtimeWeaving to false", ex);
         }
         throw e;
      }
   }
   protected void tearDown() throws Exception
   {
      Exception undeployException = null;
      if (jars != null)
      {
         for (int i = jars.length -1 ; i >= 0 ; i--)
         {
            try
            {
               String jar = jars[i].trim();
               undeploy(jar);
            }
            catch(Exception e)
            {
               undeployException = e;
            }
         }
      }
      ObjectName aspectManager = new ObjectName(ASPECT_MANAGER_NAME);
      Attribute enableTransformer = new Attribute("EnableLoadtimeWeaving", Boolean.FALSE);
      getServer().setAttribute(aspectManager, enableTransformer);
      if (useBaseXml)
      {
         Attribute useBaseXmlAttribute = new Attribute("UseBaseXml", false);
         getServer().setAttribute(new ObjectName("jboss.aop:service=AspectManager"), useBaseXmlAttribute);
      }
      if (hook != null)
      {
         hook.teardown(getServer());
      }
      if( undeployException != null )
         throw undeployException;
      super.tearDown();
   }
}
