/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.console.jbas3861; 

import javax.management.Attribute;
import javax.management.ObjectName;
import javax.management.RuntimeMBeanException;

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.test.JBossTestCase;

/**
 * Test JBAS-3861 (DeploymentFileRepository service)
 * 
 *  @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 *  @version $Revision: 85945 $
 */
public class JBAS3861UnitTestCase extends JBossTestCase
{
   ObjectName target= ObjectNameFactory.create("jboss.admin:service=DeploymentFileRepository");
   
   public JBAS3861UnitTestCase(String name)
   {
      super(name); 
   }
   
   /**
    * Check if BaseDir can be set outside the server home directory
    */
   public void testSetBaseDirOutsideServerHomeDir() throws Exception
   {
      // remember original BaseDir
      String basedir = (String)getServer().getAttribute(target, "BaseDir");
      try
      {
         // Should throw an IllegalArgumentException
         getServer().setAttribute(target, new Attribute("BaseDir", ".."));
         // Should throw an IllegalArgumentException
         getServer().setAttribute(target, new Attribute("BaseDir", "/"));
         
         // Restore the original dir and fail the test
         getServer().setAttribute(target, new Attribute("BaseDir", basedir));
         fail("Managed to set BaseDir outside ServerHomeDir for service: " + target);
      }
      catch (RuntimeMBeanException e)
      {
         // expected
      }
   }   
   
   /**
    * Check if we can write a file outside the server home directory
    */
   public void testStoreFileOutsideServerHomeDir() throws Exception
   {
      try
      {
         // Should throw an exception
         getServer().invoke(
               target,
               "store",
               new Object[] { "..", "jbas3861", ".tmp", "file content", Boolean.TRUE },
               new String[] { "java.lang.String", "java.lang.String", "java.lang.String", "java.lang.String", Boolean.TYPE.toString() });

         // Should throw an exception
         getServer().invoke(
               target,
               "store",
               new Object[] { ".", "../jbas3861", ".tmp", "file content", Boolean.TRUE },
               new String[] { "java.lang.String", "java.lang.String", "java.lang.String", "java.lang.String", Boolean.TYPE.toString() });         
         
         // Remove the stored file and fail the test - normally it should throw an exception, too
         getServer().invoke(
               target,
               "remove",
               new Object[] { ".", "../jbas3861", ".tmp" },
               new String[] { "java.lang.String", "java.lang.String", "java.lang.String" });  
         
         fail("Managed to create/remove a file outside ServerHomeDir for service: " + target);
      }
      catch (RuntimeMBeanException e)
      {
         // expected
      }
   } 
   
}
