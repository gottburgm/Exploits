/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.deployers.test.jbpapp9412;

import org.jboss.test.JBossTestCase;

/**
 * @author bmaxwell
 *
 */
public class JBPAPP9412UnitTestCase extends JBossTestCase
{

   public JBPAPP9412UnitTestCase(String name)
   {
      super(name);
   }
   
   public void testJBPAPP9412()
   {
      String jbpapp9412Test = "jbpapp-9412.ear";
      boolean deployedSuccessfully = false;
      try
      {
         // deploy the test ear which has an mbean that will try to load classes which should be available if to the sar if the MANIFEST.MF classpath is being added to the classpath 
         super.deploy(jbpapp9412Test);
         deployedSuccessfully = true;         
      }
      catch(Exception e)
      {
         e.printStackTrace();
         fail("Exception should not occur: " + e.getMessage());
      }
      finally
      {
         // undeploy the test archive when done, only log a warning if we were able to deploy successuly but unable to undeploy
         try
         {
            super.undeploy(jbpapp9412Test);
         }
         catch(Exception e)
         {
            if(deployedSuccessfully)
               log.warn("Unable to undeploy: " + jbpapp9412Test);            
         }
      }      
   }   
}
