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
package org.jboss.test.deployers.seam.test;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;
import org.jboss.test.deployers.AbstractDeploymentTest;
import org.jboss.test.util.web.HttpUtils;

/**
 * Test Seam example.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class SeamExampleTest extends AbstractDeploymentTest
{
   // Example libs
   public static final String JBoss = "jboss-";
   public static final String simpleName = "seam-%1$s";
   public static final String exampleName = JBoss + simpleName;
   public static final String exampleEar = exampleName + ".ear";
   public static final String exampleJar = exampleName + ".jar";
   public static final String exampleWar = exampleName + ".war";
   public static final String exampleDS = exampleName + "-ds.xml";
   // Seam libs
   public static final String seamJar = "jboss-seam.jar";

   protected enum Type
   {
      EAR(exampleEar),
      JAR(exampleJar),
      WAR(exampleWar);

      private String format;

      Type(String format)
      {
         this.format = format;
      }

      public String getFormat()
      {
         return format;
      }
   }

   private boolean testExpected;
   private boolean useAuthentification;
   private String username;
   private String password;

   protected SeamExampleTest(String test)
   {
      super(test);
   }

   protected static Test deploy(Class clazz) throws Exception
   {
      return deploy(clazz, true);
   }

   protected static Test deploy(Class clazz, boolean includeDS) throws Exception
   {
      return deploy(clazz, includeDS, Type.EAR);
   }

   protected static Test deploy(Class clazz, boolean includeDS, Type type) throws Exception
   {
      String name = getExampleName(clazz);
      String deployments = String.format(type.getFormat(), name);
      if (includeDS)
         deployments = String.format(exampleDS, name) + "," + deployments;
      return getManagedDeployment(clazz, deployments);
   }

   protected static String getExampleName(Class clazz)
   {
      String className = clazz.getSimpleName();
      int start = "Seam".length();
      int end = "ExampleUnitTestCase".length();
      int length = className.length();
      return className.substring(start, length - end).toLowerCase();
   }

   /**
    * Return type.
    * Use EAR by default.
    *
    * @return
    */
   protected Type getType()
   {
      return Type.EAR;
   }

   protected String getExampleName()
   {
      return getExampleName(getClass());
   }

   protected String getTopLevelDeployment(String exampleName)
   {
      return String.format(getType().getFormat(), exampleName);
   }

   public void testExample() throws Exception
   {
      String exampleName = getExampleName();
      log.info("Testing Seam " + exampleName + " example.");
      String topLevelDeployment = getTopLevelDeployment(exampleName);

      assertTrue(isDeployed(topLevelDeployment));

      if (testExpected)
      {
         final Set<String> expected = getExpectedDeployments(topLevelDeployment, exampleName);
         assertDeployed(topLevelDeployment, expected);
      }

      URL testURL = getBaseURL();
      log.info("Accessing test URL: " + testURL);
      HttpUtils.accessURL(testURL);
   }

   protected String getBaseURLString()
   {
      if (useAuthentification)
      {
         if (username != null && password != null)
            return HttpUtils.getBaseURL(username, password);
         else
            return HttpUtils.getBaseURL();
      }
      return HttpUtils.getBaseURLNoAuth();
   }

   protected String getWebContextFormat()
   {
      return simpleName;
   }

   protected String getWebContextName()
   {
      return String.format(getWebContextFormat(), getExampleName());
   }

   protected URL getBaseURL() throws Exception
   {
      String example = getWebContextName();
      return new URL(getBaseURLString() + example);
   }

   protected Set<String> getExpectedDeployments(String topLevelDeployment, String exampleName)
   {
      final Set<String> expected = new HashSet<String>();
      expected.add(topLevelDeployment);
      expected.add(String.format(exampleJar, exampleName));
      expected.add(String.format(exampleWar, exampleName));
      expected.add(seamJar);
      return expected;
   }

   public void setTestExpected(boolean testExpected)
   {
      this.testExpected = testExpected;
   }

   public void setUseAuthentification(boolean useAuthentification)
   {
      this.useAuthentification = useAuthentification;
   }

   public void setUsername(String username)
   {
      this.username = username;
   }

   public void setPassword(String password)
   {
      this.password = password;
   }
}
