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
public class T6PassArraysOfSerializablesTestCase
   extends JBossTestCase
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   private java.util.Properties cosnamingJndiProps;
   Foo[] sa10;
   Foo[] sa100;
   Foo[] sa1000;
   Foo[] sa10000;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public T6PassArraysOfSerializablesTestCase(String name) 
       throws java.io.IOException
   {
      super(name);
      java.net.URL url;

      url = ClassLoader.getSystemResource("cosnaming.jndi.properties");
      cosnamingJndiProps = new java.util.Properties();
      cosnamingJndiProps.load(url.openStream());

      sa10 = new Foo[10];
      for (int i = 0; i < sa10.length; i++)
         sa10[i] = new Foo(i, "This is serializable #" + i);

      sa100 = new Foo[100];
      for (int i = 0; i < sa100.length; i++)
         sa100[i] = new Foo(i, "This is serializable #" + i);

      sa1000 = new Foo[1000];
      for (int i = 0; i < sa1000.length; i++)
         sa1000[i] = new Foo(i, "This is serializable #" + i);

      sa10000 = new Foo[10000];
      for (int i = 0; i < sa10000.length; i++)
         sa10000[i] = new Foo(i, "This is serializable #" + i);
   }
   
   // Package --------------------------------------------------------

   InitialContext getInitialContext(java.util.Properties jndiProps) 
       throws Exception
   {
      return new InitialContext(jndiProps);
   }

   // Public --------------------------------------------------------
   
   /**
    *   This tests the speed of JRMP sendArrayOfSerializables invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendArrayOf10Serializables()
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
         session.sendArrayOfSerializables(sa10);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendArrayOfSerializables(10): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendArrayOfSerializables invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendArrayOf10Serializables()
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
         session.sendArrayOfSerializables(sa10);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendArrayOfSerializables(10): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP receiveArrayOfSerializables invocations
    *
    * @exception   Exception
    */
   public void testJRMPReceiveArrayOf10Serializables()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext().lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendArrayOfSerializables(sa10);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveArrayOfSerializables();
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP receiveArrayOfSerializables(10): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP receiveArrayOfSerializables invocations
    *
    * @exception   Exception
    */
   public void testIIOPReceiveArrayOf10Serializables()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext(cosnamingJndiProps).lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendArrayOfSerializables(sa10);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveArrayOfSerializables();
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP receiveArrayOfSerializables(10): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendReceiveArrayOfSerializables invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendReceiveArrayOf10Serializables()
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
         session.sendReceiveArrayOfSerializables(sa10);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendReceiveArrayOfSerializables(10): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendReceiveArrayOfSerializables invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendReceiveArrayOf10Serializables()
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
         session.sendReceiveArrayOfSerializables(sa10);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendReceiveArrayOfSerializables(10): " + 
                    ((end - start) / (double)n) + " ms/call");
   }

   /**
    *   This tests the speed of JRMP sendArrayOfSerializables invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendArrayOf100Serializables()
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
         session.sendArrayOfSerializables(sa100);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendArrayOfSerializables(100): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendArrayOfSerializables invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendArrayOf100Serializables()
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
         session.sendArrayOfSerializables(sa100);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendArrayOfSerializables(100): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP receiveArrayOfSerializables invocations
    *
    * @exception   Exception
    */
   public void testJRMPReceiveArrayOf100Serializables()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext().lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendArrayOfSerializables(sa100);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveArrayOfSerializables();
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP receiveArrayOfSerializables(100): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP receiveArrayOfSerializables invocations
    *
    * @exception   Exception
    */
   public void testIIOPReceiveArrayOf100Serializables()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext(cosnamingJndiProps).lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendArrayOfSerializables(sa100);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveArrayOfSerializables();
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP receiveArrayOfSerializables(100): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendReceiveArrayOfSerializables invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendReceiveArrayOf100Serializables()
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
         session.sendReceiveArrayOfSerializables(sa100);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendReceiveArrayOfSerializables(100): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendReceiveArrayOfSerializables invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendReceiveArrayOf100Serializables()
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
         session.sendReceiveArrayOfSerializables(sa100);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendReceiveArrayOfSerializables(100): " + 
                    ((end - start) / (double)n) + " ms/call");
   }

   /**
    *   This tests the speed of JRMP sendArrayOfSerializables invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendArrayOf1000Serializables0()
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
         session.sendArrayOfSerializables(sa1000);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendArrayOfSerializables(1000): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendArrayOfSerializables invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendArrayOf1000Serializables()
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
         session.sendArrayOfSerializables(sa1000);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendArrayOfSerializables(1000): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP receiveArrayOfSerializables invocations
    *
    * @exception   Exception
    */
   public void testJRMPReceiveArrayOf1000Serializables()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext().lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendArrayOfSerializables(sa1000);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveArrayOfSerializables();
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP receiveArrayOfSerializables(1000): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP receiveArrayOfSerializables invocations
    *
    * @exception   Exception
    */
   public void testIIOPReceiveArrayOf1000Serializables()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext(cosnamingJndiProps).lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendArrayOfSerializables(sa1000);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveArrayOfSerializables();
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP receiveArrayOfSerializables(1000): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendReceiveArrayOfSerializables invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendReceiveArrayOf1000Serializables()
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
         session.sendReceiveArrayOfSerializables(sa1000);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendReceiveArrayOfSerializables(1000): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendReceiveArrayOfSerializables invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendReceiveArrayOf1000Serializables()
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
         session.sendReceiveArrayOfSerializables(sa1000);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendReceiveArrayOfSerializables(1000): " + 
                    ((end - start) / (double)n) + " ms/call");
   }

   /**
    *   This tests the speed of JRMP sendArrayOfSerializables invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendArrayOf10000Serializables()
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
         session.sendArrayOfSerializables(sa10000);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendArrayOfSerializables(10000): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendArrayOfSerializables invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendArrayOf10000Serializables()
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
         session.sendArrayOfSerializables(sa10000);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendArrayOfSerializables(10000): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP receiveArrayOfSerializables invocations
    *
    * @exception   Exception
    */
   public void testJRMPReceiveArrayOf10000Serializables()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext().lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendArrayOfSerializables(sa10000);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveArrayOfSerializables();
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP receiveArrayOfSerializables(10000): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP receiveArrayOfSerializables invocations
    *
    * @exception   Exception
    */
   public void testIIOPReceiveArrayOf10000Serializables()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext(cosnamingJndiProps).lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendArrayOfSerializables(sa10000);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveArrayOfSerializables();
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP receiveArrayOfSerializables(10000): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendReceiveArrayOfSerializables invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendReceiveArrayOf10000Serializables()
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
         session.sendReceiveArrayOfSerializables(sa10000);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendReceiveArrayOfSerializables(10000): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendReceiveArrayOfSerializables invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendReceiveArrayOf10000Serializables()
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
         session.sendReceiveArrayOfSerializables(sa10000);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendReceiveArrayOfSerializables(10000): " + 
                    ((end - start) / (double)n) + " ms/call");
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(T6PassArraysOfSerializablesTestCase.class, "iiopperf.jar");
   }

}
