/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.web.test;

import java.io.BufferedReader;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.jboss.test.JBossTestCase;

/**
 * @author bmaxwell
 * https://issues.jboss.org/browse/JBPAPP-6414
 */
public class JBPAPP6414UnitTestCase extends JBossTestCase
{ 
   private String host; 
   private static String TEST_EXT = ".war";
         
   public JBPAPP6414UnitTestCase(String name)
   {
      super(name);
   }
   
   @Override
   protected void setUp() throws Exception
   {    
      super.setUp();
      host = System.getProperty("jbosstest.server.host");
   }
   
   public void testPackagingXerces()
   {  
      // The test war jbpapp6414-xerces-2.9.0 has an index.jsp whose output should match the string below.  
      // This tests the implementation versions for several things, it tests the Xerces version via the Version Class
      // The last ClassLoader at the end tests to make sure the classloader is not null, if it were null that would mean JBoss is not using xerces classes packaged in the test war.
      
      String XERCES_TEST_VERSION = "org.apache.xerces.jaxp.datatype.DatatypeFactoryImpl|org.apache.xerces.jaxp.DocumentBuilderFactoryImpl|org.apache.xerces.jaxp.SAXParserFactoryImpl|Xerces-J 2.9.0|true";
      testDeployment("jbpapp6414-xerces-2.9.0", XERCES_TEST_VERSION);
   }
      
   private String readURL(URL url)
   {                
      BufferedReader in = null;
      try
      {
         URLConnection uc = url.openConnection();  
         in = new BufferedReader( new InputStreamReader(uc.getInputStream()));
         
         String inputLine = in.readLine();            
         return inputLine;
      }
      catch (Exception e)
      {
         e.printStackTrace();
         fail("Unable to read url: " + url);
         return null;
      }
      finally
      {
         if ( in != null )
         {
            try
            {
               in.close();   
            }
            catch (Exception e)
            {
               // eat it
            }
         }            
      }
   }
   
   // try to deploy the test file and undeploy after testing
   private void testDeployment(String filename, String validVersion)
   {
      String file = filename + TEST_EXT;
      try
      {
         deployFailOnError(file);
         String version = readURL(new URL("http://"+ host +":8080/"+ filename + "/index.jsp"));
                  
         if ( version == null || validVersion.compareTo(version) != 0 )
         {
            fail("Version: " + version + " did not match expected version: " + validVersion);
         }
      }
      catch (Exception e) 
      {
         e.printStackTrace();
         fail("Failed to deploy test deployment: " + filename);
      }
      finally
      {
         undeployFailOnError(file);
      }
   }
   
   // Use the main deployer to deploy the test file
   private void deployFailOnError(String file)
   {
      try
      {
         log.info("Deploying : " + file);
         super.deploy(file);
      }
      catch ( Exception e )
      {
         e.printStackTrace();
         fail("Failed to deploy: " + file);
      }
   }
   
   // Use the main deployer to undeploy the test file
   private void undeployFailOnError(String file)
   {
      try
      {
         log.info("Undeploying : " + file);
         super.undeploy(file);
      }
      catch ( Exception e )
      {
         fail("Failed to undeploy: " + file);
      }
   }
}
