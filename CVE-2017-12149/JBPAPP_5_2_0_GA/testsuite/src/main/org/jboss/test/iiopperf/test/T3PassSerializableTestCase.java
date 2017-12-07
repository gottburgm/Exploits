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
public class T3PassSerializableTestCase
   extends JBossTestCase
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   private java.util.Properties cosnamingJndiProps;
   Foo foo = new Foo(7, "This is a serializable object.");
   CMFoo cmfoo = new CMFoo(7, "This is a serializable object.");
   Zoo zoo = new Zoo("Outer", "This is the outer serializable object.",
                     new Zoo("Inner", "This is the inner serializable object."));
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public T3PassSerializableTestCase(String name) 
       throws java.io.IOException
   {
      super(name);
      java.net.URL url;

      url = ClassLoader.getSystemResource("cosnaming.jndi.properties");
      cosnamingJndiProps = new java.util.Properties();
      cosnamingJndiProps.load(url.openStream());
   }
   
   // Package --------------------------------------------------------

   InitialContext getInitialContext(java.util.Properties jndiProps) 
       throws Exception
   {
      return new InitialContext(jndiProps);
   }

   // Public --------------------------------------------------------
   
   /**
    *   This tests the speed of JRMP sendSimpleSerializable invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendSimpleSerializable()
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
         session.sendSimpleSerializable(foo);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendSimpleSerializable: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendSimpleSerializable invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendSimpleSerializable()
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
         session.sendSimpleSerializable(foo);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendSimpleSerializable: " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP receiveSimpleSerializable invocations
    *
    * @exception   Exception
    */
   public void testJRMPReceiveSimpleSerializable()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext().lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendSimpleSerializable(foo);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveSimpleSerializable();
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP receiveSimpleSerializable: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP receiveSimpleSerializable invocations
    *
    * @exception   Exception
    */
   public void testIIOPReceiveSimpleSerializable()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext(cosnamingJndiProps).lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendSimpleSerializable(foo);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveSimpleSerializable();
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP receiveSimpleSerializable: " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendReceiveSimpleSerializable invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendReceiveSimpleSerializable()
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
         session.sendReceiveSimpleSerializable(foo);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendReceiveSimpleSerializable: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendReceiveSimpleSerializable invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendReceiveSimpleSerializable()
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
         session.sendReceiveSimpleSerializable(foo);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendReceiveSimpleSerializable: " + 
                    ((end - start) / (double)n) + " ms/call");
   }

   /**
    *   This tests the speed of JRMP sendSimpleCustomMarshalledSerializable invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendSimpleCustomMarshalledSerializable()
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
         session.sendSimpleCustomMarshalledSerializable(cmfoo);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendSimpleCustomMarshalledSerializable: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendSimpleCustomMarshalledSerializable invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendSimpleCustomMarshalledSerializable()
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
         session.sendSimpleCustomMarshalledSerializable(cmfoo);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendSimpleCustomMarshalledSerializable: " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP receiveSimpleCustomMarshalledSerializable invocations
    *
    * @exception   Exception
    */
   public void testJRMPReceiveSimpleCustomMarshalledSerializable()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext().lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendSimpleCustomMarshalledSerializable(cmfoo);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveSimpleCustomMarshalledSerializable();
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP receiveSimpleCustomMarshalledSerializable: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP receiveSimpleCustomMarshalledSerializable invocations
    *
    * @exception   Exception
    */
   public void testIIOPReceiveSimpleCustomMarshalledSerializable()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext(cosnamingJndiProps).lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendSimpleCustomMarshalledSerializable(cmfoo);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveSimpleCustomMarshalledSerializable();
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP receiveSimpleCustomMarshalledSerializable: " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendReceiveSimpleCustomMarshalledSerializable invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendReceiveSimpleCustomMarshalledSerializable()
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
         session.sendReceiveSimpleCustomMarshalledSerializable(cmfoo);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendReceiveSimpleCustomMarshalledSerializable: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendReceiveSimpleCustomMarshalledSerializable invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendReceiveSimpleCustomMarshalledSerializable()
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
         session.sendReceiveSimpleCustomMarshalledSerializable(cmfoo);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendReceiveSimpleCustomMarshalledSerializable: " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendNestedSerializable invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendNestedSerializable()
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
         session.sendNestedSerializable(zoo);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendNestedSerializable: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendNestedSerializable invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendNestedSerializable()
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
         session.sendNestedSerializable(zoo);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendNestedSerializable: " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP receiveNestedSerializable invocations
    *
    * @exception   Exception
    */
   public void testJRMPReceiveNestedSerializable()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext().lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendNestedSerializable(zoo);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveNestedSerializable();
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP receiveNestedSerializable: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP receiveNestedSerializable invocations
    *
    * @exception   Exception
    */
   public void testIIOPReceiveNestedSerializable()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext(cosnamingJndiProps).lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendNestedSerializable(zoo);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveNestedSerializable();
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP receiveNestedSerializable: " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendReceiveNestedSerializable invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendReceiveNestedSerializable()
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
         session.sendReceiveNestedSerializable(zoo);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendReceiveNestedSerializable: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendReceiveNestedSerializable invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendReceiveNestedSerializable()
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
         session.sendReceiveNestedSerializable(zoo);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendReceiveNestedSerializable: " + 
                    ((end - start) / (double)n) + " ms/call");
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(T3PassSerializableTestCase.class, "iiopperf.jar");
   }

}
