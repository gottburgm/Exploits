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
package org.jboss.test.idgen.test;
import java.lang.reflect.*;

import java.util.*;
import javax.ejb.*;
import javax.naming.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;

import org.jboss.test.idgen.interfaces.*;

/**
 * @see       <related>
 * @author    Author: d_jencks only added JBossTestCase and logging
 * @version   $Revision: 81036 $
 */
public class IdGenUnitTestCase
       extends JBossTestCase
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------


   // Constructors --------------------------------------------------
   /**
    * Constructor for the IdGenUnitTestCase object
    *
    * @param name  Description of Parameter
    */
   public IdGenUnitTestCase(String name)
   {
      super(name);
   }

   // Public --------------------------------------------------------
   /**
    * A unit test for JUnit
    *
    * @exception Exception  Description of Exception
    */
   public void testGenerator()
          throws Exception
   {
      IdGeneratorHome home = (IdGeneratorHome)getInitialContext().lookup(IdGeneratorHome.JNDI_NAME);
      IdGenerator generator = home.create();

      generator.getNewId("Account");
      generator.getNewId("Account");
      generator.getNewId("Account");

      generator.getNewId("Customer");
      generator.getNewId("Customer");
      generator.getNewId("Customer");

      generator.remove();
   }

   /**
    * The JUnit setup method
    *
    * @exception Exception  Description of Exception
    */
   protected void setUp()
          throws Exception
   {
      super.setUp();
      getLog().debug("Remove id counters");
      {
         IdCounterHome home = (IdCounterHome)new InitialContext().lookup(IdCounterHome.JNDI_NAME);
         Collection counters = home.findAll();
         Iterator i = counters.iterator();
         while (i.hasNext())
         {
            EJBObject obj = (EJBObject)i.next();
            getLog().debug("Removing " + obj.getPrimaryKey());
            obj.remove();
         }
      }
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(IdGenUnitTestCase.class, "idgen.jar");
   }

}
