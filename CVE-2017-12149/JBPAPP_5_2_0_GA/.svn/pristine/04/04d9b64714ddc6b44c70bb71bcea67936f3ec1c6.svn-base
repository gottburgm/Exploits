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
package org.jboss.test.excepiiop.test;


import javax.ejb.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import org.jboss.test.excepiiop.interfaces.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossIIOPTestCase;


public class ExceptionTimingStressTestCase
   extends JBossIIOPTestCase
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public ExceptionTimingStressTestCase(String name)
   {
      super(name);
   }

   // Public --------------------------------------------------------

   public void testNoException()
      throws Exception
   {
      ExceptionThrowerHome home =
         (ExceptionThrowerHome)PortableRemoteObject.narrow(
               getInitialContext().lookup(ExceptionThrowerHome.JNDI_NAME),
               ExceptionThrowerHome.class);
      ExceptionThrower exceptionThrower = home.create();
      exceptionThrower.throwException(0);
      exceptionThrower.remove();
   }

   public void testJavaException()
      throws Exception
   {
      ExceptionThrowerHome home =
         (ExceptionThrowerHome)PortableRemoteObject.narrow(
               getInitialContext().lookup(ExceptionThrowerHome.JNDI_NAME),
               ExceptionThrowerHome.class);
      ExceptionThrower exceptionThrower = home.create();
      try
      {
         exceptionThrower.throwException(1);
      }
      catch (JavaException e)
      {
         System.out.println("JavaException: " + e.i + ", " + e.s);
      }
      exceptionThrower.remove();
   }

   public void testIdlException()
      throws Exception
   {
      ExceptionThrowerHome home =
         (ExceptionThrowerHome)PortableRemoteObject.narrow(
               getInitialContext().lookup(ExceptionThrowerHome.JNDI_NAME),
               ExceptionThrowerHome.class);
      ExceptionThrower exceptionThrower = home.create();
      try
      {
         exceptionThrower.throwException(-1);
      }
      catch (IdlException e)
      {
         System.out.println("IdlException: " + e.i + ", " + e.s);
      }
      exceptionThrower.remove();
   }

   /**
    *   This tests the speed of invocations
    *
    * @exception   Exception
    */
   public void testSpeedNoException()
      throws Exception
   {
      long start = System.currentTimeMillis();
      ExceptionThrowerHome home =
         (ExceptionThrowerHome)PortableRemoteObject.narrow(
               getInitialContext().lookup(ExceptionThrowerHome.JNDI_NAME),
               ExceptionThrowerHome.class);
      ExceptionThrower exceptionThrower = home.create();
      for (int i = 0 ; i < getIterationCount(); i++)
      {
         exceptionThrower.throwException(0);
      }
      long end = System.currentTimeMillis();
      getLog().debug("Avg. time/call(ms):"+((end-start)/getIterationCount()));
      exceptionThrower.remove();
   }

   /**
    *   This tests the speed of invocations
    *
    * @exception   Exception
    */
   public void testSpeedJavaException()
      throws Exception
   {
      long start = System.currentTimeMillis();
      ExceptionThrowerHome home =
         (ExceptionThrowerHome)PortableRemoteObject.narrow(
               getInitialContext().lookup(ExceptionThrowerHome.JNDI_NAME),
               ExceptionThrowerHome.class);
      ExceptionThrower exceptionThrower = home.create();
      for (int i = 0 ; i < getIterationCount(); i++)
      {
         try
         {
            exceptionThrower.throwException(i + 1);
         }
         catch (JavaException e)
         {
            System.out.println("JavaException: " + e.i + ", " + e.s);
         }
      }
      long end = System.currentTimeMillis();
      getLog().debug("Avg. time/call(ms):"+((end-start)/getIterationCount()));
      exceptionThrower.remove();
   }

   /**
    *   This tests the speed of invocations
    *
    * @exception   Exception
    */
   public void testSpeedIdlException()
      throws Exception
   {
      long start = System.currentTimeMillis();
      ExceptionThrowerHome home =
         (ExceptionThrowerHome)PortableRemoteObject.narrow(
               getInitialContext().lookup(ExceptionThrowerHome.JNDI_NAME),
               ExceptionThrowerHome.class);
      ExceptionThrower exceptionThrower = home.create();
      for (int i = 0 ; i < getIterationCount(); i++)
      {
         try
         {
            exceptionThrower.throwException(-1 - i);
         }
         catch (IdlException e)
         {
            System.out.println("IdlException: " + e.i + ", " + e.s);
         }
      }
      long end = System.currentTimeMillis();
      getLog().debug("Avg. time/call(ms):"+((end-start)/getIterationCount()));
      exceptionThrower.remove();
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(ExceptionTimingStressTestCase.class, "excepiiop.jar");
   }

}
