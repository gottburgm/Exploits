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
public class T5PassStringArraysTestCase
   extends JBossTestCase
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   private java.util.Properties cosnamingJndiProps;
   String[] sa10;
   String[] sa100;
   String[] sa1000;
   String[] sa10000;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public T5PassStringArraysTestCase(String name) 
       throws java.io.IOException
   {
      super(name);
      java.net.URL url;

      url = ClassLoader.getSystemResource("cosnaming.jndi.properties");
      cosnamingJndiProps = new java.util.Properties();
      cosnamingJndiProps.load(url.openStream());

      sa10 = new String[10];
      for (int i = 0; i < sa10.length; i++)
         sa10[i] = "" + i;

      sa100 = new String[100];
      for (int i = 0; i < sa100.length; i++)
         sa100[i] = "" + i;

      sa1000 = new String[1000];
      for (int i = 0; i < sa1000.length; i++)
         sa1000[i] = "" + i;

      sa10000 = new String[10000];
      for (int i = 0; i < sa10000.length; i++)
         sa10000[i] = "" + i;
   }
   
   // Package --------------------------------------------------------

   InitialContext getInitialContext(java.util.Properties jndiProps) 
       throws Exception
   {
      return new InitialContext(jndiProps);
   }

   // Public --------------------------------------------------------
   
   /**
    *   This tests the speed of JRMP sendStringArray invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendStringArray10()
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
         session.sendStringArray(sa10);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendStringArray(10): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendStringArray invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendStringArray10()
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
         session.sendStringArray(sa10);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendStringArray(10): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP receiveStringArray invocations
    *
    * @exception   Exception
    */
   public void testJRMPReceiveStringArray10()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext().lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendStringArray(sa10);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveStringArray();
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP receiveStringArray(10): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP receiveStringArray invocations
    *
    * @exception   Exception
    */
   public void testIIOPReceiveStringArray10()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext(cosnamingJndiProps).lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendStringArray(sa10);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveStringArray();
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP receiveStringArray(10): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendReceiveStringArray invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendReceiveStringArray10()
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
         session.sendReceiveStringArray(sa10);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendReceiveStringArray(10): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendReceiveStringArray invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendReceiveStringArray10()
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
         session.sendReceiveStringArray(sa10);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendReceiveStringArray(10): " + 
                    ((end - start) / (double)n) + " ms/call");
   }

   /**
    *   This tests the speed of JRMP sendStringArray invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendStringArray100()
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
         session.sendStringArray(sa100);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendStringArray(100): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendStringArray invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendStringArray100()
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
         session.sendStringArray(sa100);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendStringArray(100): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP receiveStringArray invocations
    *
    * @exception   Exception
    */
   public void testJRMPReceiveStringArray100()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext().lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendStringArray(sa100);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveStringArray();
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP receiveStringArray(100): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP receiveStringArray invocations
    *
    * @exception   Exception
    */
   public void testIIOPReceiveStringArray100()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext(cosnamingJndiProps).lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendStringArray(sa100);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveStringArray();
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP receiveStringArray(100): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendReceiveStringArray invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendReceiveStringArray100()
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
         session.sendReceiveStringArray(sa100);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendReceiveStringArray(100): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendReceiveStringArray invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendReceiveStringArray100()
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
         session.sendReceiveStringArray(sa100);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendReceiveStringArray(100): " + 
                    ((end - start) / (double)n) + " ms/call");
   }

   /**
    *   This tests the speed of JRMP sendStringArray invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendStringArray1000()
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
         session.sendStringArray(sa1000);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendStringArray(1000): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendStringArray invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendStringArray1000()
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
         session.sendStringArray(sa1000);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendStringArray(1000): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP receiveStringArray invocations
    *
    * @exception   Exception
    */
   public void testJRMPReceiveStringArray1000()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext().lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendStringArray(sa1000);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveStringArray();
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP receiveStringArray(1000): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP receiveStringArray invocations
    *
    * @exception   Exception
    */
   public void testIIOPReceiveStringArray1000()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext(cosnamingJndiProps).lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendStringArray(sa1000);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveStringArray();
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP receiveStringArray(1000): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendReceiveStringArray invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendReceiveStringArray1000()
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
         session.sendReceiveStringArray(sa1000);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendReceiveStringArray(1000): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendReceiveStringArray invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendReceiveStringArray1000()
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
         session.sendReceiveStringArray(sa1000);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendReceiveStringArray(1000): " + 
                    ((end - start) / (double)n) + " ms/call");
   }

   /**
    *   This tests the speed of JRMP sendStringArray invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendStringArray10000()
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
         session.sendStringArray(sa10000);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendStringArray(10000): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendStringArray invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendStringArray10000()
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
         session.sendStringArray(sa10000);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendStringArray(10000): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP receiveStringArray invocations
    *
    * @exception   Exception
    */
   public void testJRMPReceiveStringArray10000()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext().lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendStringArray(sa10000);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveStringArray();
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP receiveStringArray(10000): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP receiveStringArray invocations
    *
    * @exception   Exception
    */
   public void testIIOPReceiveStringArray10000()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext(cosnamingJndiProps).lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendStringArray(sa10000);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveStringArray();
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP receiveStringArray(10000): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendReceiveStringArray invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendReceiveStringArray10000()
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
         session.sendReceiveStringArray(sa10000);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendReceiveStringArray(10000): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendReceiveStringArray invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendReceiveStringArray10000()
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
         session.sendReceiveStringArray(sa10000);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendReceiveStringArray(10000): " + 
                    ((end - start) / (double)n) + " ms/call");
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(T5PassStringArraysTestCase.class, "iiopperf.jar");
   }

}
