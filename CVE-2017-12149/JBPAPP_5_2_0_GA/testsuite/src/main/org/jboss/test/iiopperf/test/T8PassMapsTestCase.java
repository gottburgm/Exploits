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

import java.util.HashMap;
import java.util.Map;
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
public class T8PassMapsTestCase
   extends JBossTestCase
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   private java.util.Properties cosnamingJndiProps;
   Map m10;
   Map m100;
   Map m1000;
   Map m10000;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public T8PassMapsTestCase(String name) 
       throws java.io.IOException
   {
      super(name);
      java.net.URL url;

      url = ClassLoader.getSystemResource("cosnaming.jndi.properties");
      cosnamingJndiProps = new java.util.Properties();
      cosnamingJndiProps.load(url.openStream());

      m10 = new HashMap(10);
      for (int i = 0; i < 10; i++)
         m10.put(new Integer(i), new Foo(i, "This is serializable #" + i));

      m100 = new HashMap(100);
      for (int i = 0; i < 100; i++)
         m100.put(new Integer(i), new Foo(i, "This is serializable #" + i));

      m1000 = new HashMap(1000);
      for (int i = 0; i < 1000; i++)
         m1000.put(new Integer(i), new Foo(i, "This is serializable #" + i));

      m10000 = new HashMap(10000);
      for (int i = 0; i < 10000; i++)
         m10000.put(new Integer(i), new Foo(i, "This is serializable #" + i));

   }
   
   // Package --------------------------------------------------------

   InitialContext getInitialContext(java.util.Properties jndiProps) 
       throws Exception
   {
      return new InitialContext(jndiProps);
   }

   // Public --------------------------------------------------------
   
   /**
    *   This tests the speed of JRMP sendMap invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendMap10()
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
         session.sendMap(m10);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendMap(10): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendMap invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendMap10()
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
         session.sendMap(m10);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendMap(10): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP receiveMap invocations
    *
    * @exception   Exception
    */
   public void testJRMPReceiveMap10()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext().lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendMap(m10);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveMap();
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP receiveMap(10): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP receiveMap invocations
    *
    * @exception   Exception
    */
   public void testIIOPReceiveMap10()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext(cosnamingJndiProps).lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendMap(m10);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveMap();
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP receiveMap(10): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendReceiveMap invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendReceiveMap10()
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
         session.sendReceiveMap(m10);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendReceiveMap(10): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendReceiveMap invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendReceiveMap10()
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
         session.sendReceiveMap(m10);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendReceiveMap(10): " + 
                    ((end - start) / (double)n) + " ms/call");
   }

   /**
    *   This tests the speed of JRMP sendMap invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendMap100()
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
         session.sendMap(m100);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendMap(100): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendMap invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendMap100()
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
         session.sendMap(m100);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendMap(100): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP receiveMap invocations
    *
    * @exception   Exception
    */
   public void testJRMPReceiveMap100()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext().lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendMap(m100);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveMap();
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP receiveMap(100): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP receiveMap invocations
    *
    * @exception   Exception
    */
   public void testIIOPReceiveMap100()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext(cosnamingJndiProps).lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendMap(m100);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveMap();
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP receiveMap(100): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendReceiveMap invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendReceiveMap100()
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
         session.sendReceiveMap(m100);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendReceiveMap(100): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendReceiveMap invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendReceiveMap100()
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
         session.sendReceiveMap(m100);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendReceiveMap(100): " + 
                    ((end - start) / (double)n) + " ms/call");
   }

   /**
    *   This tests the speed of JRMP sendMap invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendMap1000()
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
         session.sendMap(m1000);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendMap(1000): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendMap invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendMap1000()
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
         session.sendMap(m1000);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendMap(1000): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP receiveMap invocations
    *
    * @exception   Exception
    */
   public void testJRMPReceiveMap1000()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext().lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendMap(m1000);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveMap();
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP receiveMap(1000): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP receiveMap invocations
    *
    * @exception   Exception
    */
   public void testIIOPReceiveMap1000()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext(cosnamingJndiProps).lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendMap(m1000);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveMap();
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP receiveMap(1000): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendReceiveMap invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendReceiveMap1000()
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
         session.sendReceiveMap(m1000);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendReceiveMap(1000): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendReceiveMap invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendReceiveMap1000()
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
         session.sendReceiveMap(m1000);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendReceiveMap(1000): " + 
                    ((end - start) / (double)n) + " ms/call");
   }

   /**
    *   This tests the speed of JRMP sendMap invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendMap10000()
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
         session.sendMap(m10000);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendMap(10000): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendMap invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendMap10000()
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
         session.sendMap(m10000);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendMap(10000): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP receiveMap invocations
    *
    * @exception   Exception
    */
   public void testJRMPReceiveMap10000()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext().lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendMap(m10000);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveMap();
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP receiveMap(10000): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP receiveMap invocations
    *
    * @exception   Exception
    */
   public void testIIOPReceiveMap10000()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext(cosnamingJndiProps).lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendMap(m10000);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveMap();
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP receiveMap(10000): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendReceiveMap invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendReceiveMap10000()
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
         session.sendReceiveMap(m10000);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendReceiveMap(10000): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendReceiveMap invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendReceiveMap10000()
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
         session.sendReceiveMap(m10000);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendReceiveMap(10000): " + 
                    ((end - start) / (double)n) + " ms/call");
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(T8PassMapsTestCase.class, "iiopperf.jar");
   }

}
