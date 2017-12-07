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
public class T4PassIntArraysTestCase
   extends JBossTestCase
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   private java.util.Properties cosnamingJndiProps;
   int[] ia10;
   int[] ia100;
   int[] ia1000;
   int[] ia10000;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public T4PassIntArraysTestCase(String name) 
       throws java.io.IOException
   {
      super(name);
      java.net.URL url;

      url = ClassLoader.getSystemResource("cosnaming.jndi.properties");
      cosnamingJndiProps = new java.util.Properties();
      cosnamingJndiProps.load(url.openStream());

      ia10 = new int[10];
      for (int i = 0; i < ia10.length; i++)
         ia10[i] = i;

      ia100 = new int[100];
      for (int i = 0; i < ia100.length; i++)
         ia100[i] = i;

      ia1000 = new int[1000];
      for (int i = 0; i < ia1000.length; i++)
         ia1000[i] = i;

      ia10000 = new int[10000];
      for (int i = 0; i < ia10000.length; i++)
         ia10000[i] = i;
   }
   
   // Package --------------------------------------------------------

   InitialContext getInitialContext(java.util.Properties jndiProps) 
       throws Exception
   {
      return new InitialContext(jndiProps);
   }

   // Public --------------------------------------------------------
   
   /**
    *   This tests the speed of JRMP sendIntArray invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendIntArray10()
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
         session.sendIntArray(ia10);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendIntArray(10): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendIntArray invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendIntArray10()
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
         session.sendIntArray(ia10);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendIntArray(10): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP receiveIntArray invocations
    *
    * @exception   Exception
    */
   public void testJRMPReceiveIntArray10()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext().lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendIntArray(ia10);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveIntArray();
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP receiveIntArray(10): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP receiveIntArray invocations
    *
    * @exception   Exception
    */
   public void testIIOPReceiveIntArray10()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext(cosnamingJndiProps).lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendIntArray(ia10);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveIntArray();
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP receiveIntArray(10): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendReceiveIntArray invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendReceiveIntArray10()
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
         session.sendReceiveIntArray(ia10);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendReceiveIntArray(10): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendReceiveIntArray invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendReceiveIntArray10()
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
         session.sendReceiveIntArray(ia10);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendReceiveIntArray(10): " + 
                    ((end - start) / (double)n) + " ms/call");
   }

   /**
    *   This tests the speed of JRMP sendIntArray invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendIntArray100()
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
         session.sendIntArray(ia100);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendIntArray(100): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendIntArray invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendIntArray100()
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
         session.sendIntArray(ia100);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendIntArray(100): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP receiveIntArray invocations
    *
    * @exception   Exception
    */
   public void testJRMPReceiveIntArray100()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext().lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendIntArray(ia100);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveIntArray();
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP receiveIntArray(100): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP receiveIntArray invocations
    *
    * @exception   Exception
    */
   public void testIIOPReceiveIntArray100()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext(cosnamingJndiProps).lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendIntArray(ia100);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveIntArray();
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP receiveIntArray(100): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendReceiveIntArray invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendReceiveIntArray100()
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
         session.sendReceiveIntArray(ia100);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendReceiveIntArray(100): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendReceiveIntArray invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendReceiveIntArray100()
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
         session.sendReceiveIntArray(ia100);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendReceiveIntArray(100): " + 
                    ((end - start) / (double)n) + " ms/call");
   }

   /**
    *   This tests the speed of JRMP sendIntArray invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendIntArray1000()
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
         session.sendIntArray(ia1000);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendIntArray(1000): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendIntArray invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendIntArray1000()
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
         session.sendIntArray(ia1000);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendIntArray(1000): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP receiveIntArray invocations
    *
    * @exception   Exception
    */
   public void testJRMPReceiveIntArray1000()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext().lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendIntArray(ia1000);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveIntArray();
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP receiveIntArray(1000): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP receiveIntArray invocations
    *
    * @exception   Exception
    */
   public void testIIOPReceiveIntArray1000()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext(cosnamingJndiProps).lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendIntArray(ia1000);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveIntArray();
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP receiveIntArray(1000): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendReceiveIntArray invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendReceiveIntArray1000()
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
         session.sendReceiveIntArray(ia1000);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendReceiveIntArray(1000): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendReceiveIntArray invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendReceiveIntArray1000()
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
         session.sendReceiveIntArray(ia1000);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendReceiveIntArray(1000): " + 
                    ((end - start) / (double)n) + " ms/call");
   }

   /**
    *   This tests the speed of JRMP sendIntArray invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendIntArray10000()
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
         session.sendIntArray(ia10000);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendIntArray(10000): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendIntArray invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendIntArray10000()
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
         session.sendIntArray(ia10000);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendIntArray(10000): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP receiveIntArray invocations
    *
    * @exception   Exception
    */
   public void testJRMPReceiveIntArray10000()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext().lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendIntArray(ia10000);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveIntArray();
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP receiveIntArray(10000): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP receiveIntArray invocations
    *
    * @exception   Exception
    */
   public void testIIOPReceiveIntArray10000()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext(cosnamingJndiProps).lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendIntArray(ia10000);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveIntArray();
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP receiveIntArray(10000): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendReceiveIntArray invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendReceiveIntArray10000()
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
         session.sendReceiveIntArray(ia10000);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendReceiveIntArray(10000): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendReceiveIntArray invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendReceiveIntArray10000()
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
         session.sendReceiveIntArray(ia10000);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendReceiveIntArray(10000): " + 
                    ((end - start) / (double)n) + " ms/call");
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(T4PassIntArraysTestCase.class, "iiopperf.jar");
   }

}
