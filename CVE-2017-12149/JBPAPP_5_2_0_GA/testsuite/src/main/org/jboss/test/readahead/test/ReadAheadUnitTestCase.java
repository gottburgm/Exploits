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
package org.jboss.test.readahead.test;

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.readahead.interfaces.CMPFindTestSessionHome;
import org.jboss.test.readahead.interfaces.CMPFindTestSessionRemote;

import org.jboss.test.JBossTestCase;

/**
 * TestCase driver for the readahead finder tests
 * 
 * @author <a href="mailto:danch@nvisia.com">danch (Dan Christopherson</a>
 * @version $Id: ReadAheadUnitTestCase.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $
 * 
 * Revision:
 */
public class ReadAheadUnitTestCase extends JBossTestCase {

   CMPFindTestSessionRemote rem = null;
   
   public ReadAheadUnitTestCase(String name) {
      super(name);
   }

   protected void tearDown() throws Exception {
      if (rem != null) {
         getLog().debug("Removing test data");
         rem.removeTestData();
         
         rem.remove();
         
         rem = null;
      }
   }
      
   protected void setUp()
      throws Exception
   {
      super.setUp();
      CMPFindTestSessionHome home = 
         (CMPFindTestSessionHome)getInitialContext().lookup("CMPFindTestSession");
      rem = home.create();
      
      rem.createTestData();
   }
   
   public void testFindAll() throws Exception {
      rem.testFinder();
   }
   
   public void testFindByCity() throws Exception {
      rem.testByCity();
   }
   
   public void testAddressByCity() throws Exception {
      rem.addressByCity();
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(ReadAheadUnitTestCase.class, "readahead.jar");
   }

}
