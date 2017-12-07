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
package org.jboss.test.iiopperf.test;


import javax.ejb.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import org.jboss.test.iiopperf.interfaces.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jboss.test.JBossTestCase;


/** 
 *   @author Francisco.Reverbel@jboss.org
 *   @version $Revision: 81036 $
 */
public class T1PassStringsTestCase
   extends JBossTestCase
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   private java.util.Properties cosnamingJndiProps;
   String str10 = "0123456789";
   String str100;
   String str1000;
   String str10000;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public T1PassStringsTestCase(String name) 
       throws java.io.IOException
   {
      super(name);
      java.net.URL url;

      url = ClassLoader.getSystemResource("cosnaming.jndi.properties");
      cosnamingJndiProps = new java.util.Properties();
      cosnamingJndiProps.load(url.openStream());

      str100 = "";
      for (int i = 0; i < 10; i++)
         str100 = str100 + str10;

      str1000 = "";
      for (int i = 0; i < 10; i++)
         str1000 = str1000 + str100;

      str10000 = "";
      for (int i = 0; i < 10; i++)
         str10000 = str10000 + str1000;

   }
   
   // Package --------------------------------------------------------

   InitialContext getInitialContext(java.util.Properties jndiProps) 
       throws Exception
   {
      return new InitialContext(jndiProps);
   }

   // Public --------------------------------------------------------
   
   /**
    *   This tests the speed of JRMP sendString invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendString10()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext().lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.sendString(str10);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendString(10): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendString invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendString10()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext(cosnamingJndiProps).lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.sendString(str10);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendString(10): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP receiveString invocations
    *
    * @exception   Exception
    */
   public void testJRMPReceiveString10()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext().lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendString(str10);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveString();
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP receiveString(10): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP receiveString invocations
    *
    * @exception   Exception
    */
   public void testIIOPReceiveString10()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext(cosnamingJndiProps).lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendString(str10);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveString();
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP receiveString(10): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendReceiveString invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendReceiveString10()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext().lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.sendReceiveString(str10);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendReceiveString(10): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendReceiveString invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendReceiveString10()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext(cosnamingJndiProps).lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.sendReceiveString(str10);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendReceiveString(10): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendString invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendString100()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext().lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.sendString(str100);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendString(100): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendString invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendString100()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext(cosnamingJndiProps).lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.sendString(str100);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendString(100): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP receiveString invocations
    *
    * @exception   Exception
    */
   public void testJRMPReceiveString100()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext().lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendString(str100);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveString();
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP receiveString(100): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP receiveString invocations
    *
    * @exception   Exception
    */
   public void testIIOPReceiveString100()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext(cosnamingJndiProps).lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendString(str100);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveString();
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP receiveString(100): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendReceiveString invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendReceiveString100()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext().lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.sendReceiveString(str100);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendReceiveString(100): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendReceiveString invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendReceiveString100()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext(cosnamingJndiProps).lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.sendReceiveString(str100);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendReceiveString(100): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendString invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendString1000()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext().lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.sendString(str1000);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendString(1000): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendString invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendString1000()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext(cosnamingJndiProps).lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.sendString(str1000);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendString(1000): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP receiveString invocations
    *
    * @exception   Exception
    */
   public void testJRMPReceiveString1000()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext().lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendString(str1000);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveString();
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP receiveString(1000): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP receiveString invocations
    *
    * @exception   Exception
    */
   public void testIIOPReceiveString1000()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext(cosnamingJndiProps).lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendString(str1000);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveString();
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP receiveString(1000): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendReceiveString invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendReceiveString1000()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext().lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.sendReceiveString(str1000);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendReceiveString(1000): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendReceiveString invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendReceiveString1000()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext(cosnamingJndiProps).lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.sendReceiveString(str1000);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendReceiveString(1000): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendString invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendString10000()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext().lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.sendString(str10000);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendString(10000): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendString invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendString10000()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext(cosnamingJndiProps).lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.sendString(str10000);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendString(10000): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP receiveString invocations
    *
    * @exception   Exception
    */
   public void testJRMPReceiveString10000()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext().lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendString(str10000);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveString();
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP receiveString(10000): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP receiveString invocations
    *
    * @exception   Exception
    */
   public void testIIOPReceiveString10000()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext(cosnamingJndiProps).lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendString(str10000);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveString();
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP receiveString(10000): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendReceiveString invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendReceiveString10000()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext().lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.sendReceiveString(str10000);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendReceiveString(10000): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendReceiveString invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendReceiveString10000()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext(cosnamingJndiProps).lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.sendReceiveString(str10000);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendReceiveString(10000): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(T1PassStringsTestCase.class, "iiopperf.jar");
   }

}
