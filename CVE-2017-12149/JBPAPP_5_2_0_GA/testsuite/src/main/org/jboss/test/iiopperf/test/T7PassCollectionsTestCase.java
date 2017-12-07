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

import java.util.ArrayList;
import java.util.Collection;
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
public class T7PassCollectionsTestCase
   extends JBossTestCase
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   private java.util.Properties cosnamingJndiProps;
   Collection c10;
   Collection c100;
   Collection c1000;
   Collection c10000;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public T7PassCollectionsTestCase(String name) 
       throws java.io.IOException
   {
      super(name);
      java.net.URL url;

      url = ClassLoader.getSystemResource("cosnaming.jndi.properties");
      cosnamingJndiProps = new java.util.Properties();
      cosnamingJndiProps.load(url.openStream());

      c10 = new ArrayList(10);
      for (int i = 0; i < 10; i++)
         c10.add(new Foo(i, "This is serializable #" + i));

      c100 = new ArrayList(100);
      for (int i = 0; i < 100; i++)
         c100.add(new Foo(i, "This is serializable #" + i));

      c1000 = new ArrayList(1000);
      for (int i = 0; i < 1000; i++)
         c1000.add(new Foo(i, "This is serializable #" + i));

      c10000 = new ArrayList(10000);
      for (int i = 0; i < 10000; i++)
         c10000.add(new Foo(i, "This is serializable #" + i));

   }
   
   // Package --------------------------------------------------------

   InitialContext getInitialContext(java.util.Properties jndiProps) 
       throws Exception
   {
      return new InitialContext(jndiProps);
   }

   // Public --------------------------------------------------------
   
   /**
    *   This tests the speed of JRMP sendCollection invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendCollection10()
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
         session.sendCollection(c10);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendCollection(10): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendCollection invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendCollection10()
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
         session.sendCollection(c10);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendCollection(10): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP receiveCollection invocations
    *
    * @exception   Exception
    */
   public void testJRMPReceiveCollection10()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext().lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendCollection(c10);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveCollection();
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP receiveCollection(10): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP receiveCollection invocations
    *
    * @exception   Exception
    */
   public void testIIOPReceiveCollection10()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext(cosnamingJndiProps).lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendCollection(c10);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveCollection();
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP receiveCollection(10): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendReceiveCollection invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendReceiveCollection10()
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
         session.sendReceiveCollection(c10);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendReceiveCollection(10): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendReceiveCollection invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendReceiveCollection10()
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
         session.sendReceiveCollection(c10);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendReceiveCollection(10): " + 
                    ((end - start) / (double)n) + " ms/call");
   }

   /**
    *   This tests the speed of JRMP sendCollection invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendCollection100()
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
         session.sendCollection(c100);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendCollection(100): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendCollection invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendCollection100()
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
         session.sendCollection(c100);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendCollection(100): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP receiveCollection invocations
    *
    * @exception   Exception
    */
   public void testJRMPReceiveCollection100()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext().lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendCollection(c100);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveCollection();
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP receiveCollection(100): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP receiveCollection invocations
    *
    * @exception   Exception
    */
   public void testIIOPReceiveCollection100()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext(cosnamingJndiProps).lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendCollection(c100);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveCollection();
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP receiveCollection(100): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendReceiveCollection invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendReceiveCollection100()
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
         session.sendReceiveCollection(c100);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendReceiveCollection(100): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendReceiveCollection invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendReceiveCollection100()
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
         session.sendReceiveCollection(c100);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendReceiveCollection(100): " + 
                    ((end - start) / (double)n) + " ms/call");
   }

   /**
    *   This tests the speed of JRMP sendCollection invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendCollection1000()
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
         session.sendCollection(c1000);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendCollection(1000): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendCollection invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendCollection1000()
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
         session.sendCollection(c1000);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendCollection(1000): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP receiveCollection invocations
    *
    * @exception   Exception
    */
   public void testJRMPReceiveCollection1000()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext().lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendCollection(c1000);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveCollection();
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP receiveCollection(1000): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP receiveCollection invocations
    *
    * @exception   Exception
    */
   public void testIIOPReceiveCollection1000()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext(cosnamingJndiProps).lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendCollection(c1000);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveCollection();
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP receiveCollection(1000): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendReceiveCollection invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendReceiveCollection1000()
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
         session.sendReceiveCollection(c1000);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendReceiveCollection(1000): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendReceiveCollection invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendReceiveCollection1000()
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
         session.sendReceiveCollection(c1000);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendReceiveCollection(1000): " + 
                    ((end - start) / (double)n) + " ms/call");
   }

   /**
    *   This tests the speed of JRMP sendCollection invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendCollection10000()
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
         session.sendCollection(c10000);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendCollection(10000): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendCollection invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendCollection10000()
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
         session.sendCollection(c10000);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendCollection(10000): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP receiveCollection invocations
    *
    * @exception   Exception
    */
   public void testJRMPReceiveCollection10000()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext().lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendCollection(c10000);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveCollection();
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP receiveCollection(10000): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP receiveCollection invocations
    *
    * @exception   Exception
    */
   public void testIIOPReceiveCollection10000()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext(cosnamingJndiProps).lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendCollection(c10000);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveCollection();
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP receiveCollection(10000): " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendReceiveCollection invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendReceiveCollection10000()
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
         session.sendReceiveCollection(c10000);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendReceiveCollection(10000): " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendReceiveCollection invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendReceiveCollection10000()
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
         session.sendReceiveCollection(c10000);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendReceiveCollection(10000): " + 
                    ((end - start) / (double)n) + " ms/call");
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(T7PassCollectionsTestCase.class, "iiopperf.jar");
   }

}
