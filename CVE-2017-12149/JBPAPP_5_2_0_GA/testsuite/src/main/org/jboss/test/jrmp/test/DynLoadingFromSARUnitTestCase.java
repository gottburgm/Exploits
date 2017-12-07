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
package org.jboss.test.jrmp.test;

import java.io.File;
import java.net.URL;
import java.security.CodeSource;

import javax.naming.InitialContext;

import org.jboss.test.JBossTestCase;
import org.jboss.test.jrmp.interfaces.IString;

/**
 * Test of RMI dynamic class loading from a sar.
 *
 * @author    adrian@jboss.com
 * @version   $Revision: 81036 $
 */
public class DynLoadingFromSARUnitTestCase extends JBossTestCase
{
   public DynLoadingFromSARUnitTestCase(String name)
   {
      super(name);
   }

   /**
    * A unit test for JUnit
    *
    * @exception Exception  Description of Exception
    */
   public void testAccess() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      getLog().debug("Lookup IString");
      Object obj = jndiContext.lookup("test/jrmp/IString");
      IString echo = (IString) obj;
      getLog().debug("found = " + echo);
      Class clazz = echo.getClass();
      CodeSource cs = clazz.getProtectionDomain().getCodeSource();
      URL location = cs.getLocation();
      getLog().debug("IString.class = " + clazz);
      getLog().debug("IString.class location = " + location);
      assertTrue("CodeSource URL.protocol != file", location.getProtocol().equals("file") == false);
   }

   /**
    * Remove any local IString implementation so that we test RMI class loading.
    *
    * @exception Exception  Description of Exception
    */
   protected void setUp() throws Exception
   {
      super.setUp();
      URL istringImpl = getClass().getResource("/org/jboss/test/jrmp/ejb/AString.class");
      if (istringImpl != null)
      {
         getLog().debug("Found IString impl at: " + istringImpl);
         File implFile = new File(istringImpl.getFile());
         getLog().debug("Removed: " + implFile.delete());
      }
      deploy("jrmp-dl.sar");
   }

   /**
    * The teardown method for JUnit
    *
    * @exception Exception  Description of Exception
    */
   protected void tearDown() throws Exception
   {
      undeploy("jrmp-dl.sar");
      super.tearDown();
   }

}
