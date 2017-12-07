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
public class T0PassBasicTypesTestCase
   extends JBossTestCase
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   private java.util.Properties cosnamingJndiProps;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public T0PassBasicTypesTestCase(String name) 
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
    *   This tests the speed of JRMP sendReceiveNothing invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendReceiveNothing()
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
         session.sendReceiveNothing();
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendReceiveNothing: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendReceiveNothing invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendReceiveNothing()
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
         session.sendReceiveNothing();
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendReceiveNothing: " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendBoolean invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendBoolean()
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
         session.sendBoolean(true);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendBoolean: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendBoolean invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendBoolean()
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
         session.sendBoolean(true);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendBoolean: " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP receiveBoolean invocations
    *
    * @exception   Exception
    */
   public void testJRMPReceiveBoolean()
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
         session.receiveBoolean();
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP receiveBoolean: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP receiveBoolean invocations
    *
    * @exception   Exception
    */
   public void testIIOPReceiveBoolean()
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
         session.receiveBoolean();
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP receiveBoolean: " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendReceiveBoolean invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendReceiveBoolean()
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
         session.sendReceiveBoolean(true);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendReceiveBoolean: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendReceiveBoolean invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendReceiveBoolean()
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
         session.sendReceiveBoolean(true);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendReceiveBoolean: " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendChar invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendChar()
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
         session.sendChar(Character.MAX_VALUE);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendChar: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendChar invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendChar()
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
         session.sendChar(Character.MAX_VALUE);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendChar: " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP receiveChar invocations
    *
    * @exception   Exception
    */
   public void testJRMPReceiveChar()
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
         session.receiveChar();
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP receiveChar: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP receiveChar invocations
    *
    * @exception   Exception
    */
   public void testIIOPReceiveChar()
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
         session.receiveChar();
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP receiveChar: " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendReceiveChar invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendReceiveChar()
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
         session.sendReceiveChar(Character.MAX_VALUE);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendReceiveChar: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendReceiveChar invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendReceiveChar()
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
         session.sendReceiveChar(Character.MAX_VALUE);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendReceiveChar: " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendByte invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendByte()
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
         session.sendByte(Byte.MAX_VALUE);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendByte: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendByte invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendByte()
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
         session.sendByte(Byte.MAX_VALUE);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendByte: " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP receiveByte invocations
    *
    * @exception   Exception
    */
   public void testJRMPReceiveByte()
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
         session.receiveByte();
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP receiveByte: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP receiveByte invocations
    *
    * @exception   Exception
    */
   public void testIIOPReceiveByte()
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
         session.receiveByte();
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP receiveByte: " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendReceiveByte invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendReceiveByte()
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
         session.sendReceiveByte(Byte.MAX_VALUE);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendReceiveByte: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendReceiveByte invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendReceiveByte()
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
         session.sendReceiveByte(Byte.MAX_VALUE);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendReceiveByte: " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendChar invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendShort()
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
         session.sendShort(Short.MAX_VALUE);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendShort: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendShort invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendShort()
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
         session.sendShort(Short.MAX_VALUE);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendShort: " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP receiveShort invocations
    *
    * @exception   Exception
    */
   public void testJRMPReceiveShort()
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
         session.receiveShort();
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP receiveShort: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP receiveShort invocations
    *
    * @exception   Exception
    */
   public void testIIOPReceiveShort()
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
         session.receiveShort();
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP receiveShort: " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendReceiveShort invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendReceiveShort()
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
         session.sendReceiveShort(Short.MAX_VALUE);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendReceiveShort: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendReceiveShort invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendReceiveShort()
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
         session.sendReceiveShort(Short.MAX_VALUE);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendReceiveShort: " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendInt invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendInt()
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
         session.sendInt(Integer.MAX_VALUE);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendInt: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendInt invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendInt()
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
         session.sendInt(Integer.MAX_VALUE);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendInt: " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP receiveInt invocations
    *
    * @exception   Exception
    */
   public void testJRMPReceiveInt()
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
         session.receiveInt();
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP receiveInt: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP receiveInt invocations
    *
    * @exception   Exception
    */
   public void testIIOPReceiveInt()
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
         session.receiveInt();
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP receiveInt: " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendReceiveInt invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendReceiveInt()
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
         session.sendReceiveInt(Integer.MAX_VALUE);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendReceiveInt: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendReceiveInt invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendReceiveInt()
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
         session.sendReceiveInt(Integer.MAX_VALUE);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendReceiveInt: " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendLong invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendLong()
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
         session.sendLong(Long.MAX_VALUE);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendLong: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendLong invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendLong()
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
         session.sendLong(Long.MAX_VALUE);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendLong: " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP receiveLong invocations
    *
    * @exception   Exception
    */
   public void testJRMPReceiveLong()
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
         session.receiveLong();
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP receiveLong: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP receiveLong invocations
    *
    * @exception   Exception
    */
   public void testIIOPReceiveLong()
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
         session.receiveLong();
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP receiveLong: " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendReceiveLong invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendReceiveLong()
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
         session.sendReceiveLong(Long.MAX_VALUE);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendReceiveLong: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendReceiveLong invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendReceiveLong()
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
         session.sendReceiveLong(Long.MAX_VALUE);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendReceiveLong: " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendFloat invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendFloat()
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
         session.sendFloat(Float.MAX_VALUE);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendFloat: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendFloat invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendFloat()
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
         session.sendFloat(Float.MAX_VALUE);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendFloat: " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP receiveFloat invocations
    *
    * @exception   Exception
    */
   public void testJRMPReceiveFloat()
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
         session.receiveFloat();
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP receiveFloat: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP receiveFloat invocations
    *
    * @exception   Exception
    */
   public void testIIOPReceiveFloat()
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
         session.receiveFloat();
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP receiveFloat: " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendReceiveFloat invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendReceiveFloat()
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
         session.sendReceiveFloat(Float.MAX_VALUE);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendReceiveFloat: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendReceiveFloat invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendReceiveFloat()
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
         session.sendReceiveFloat(Float.MAX_VALUE);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendReceiveFloat: " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendDouble invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendDouble()
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
         session.sendDouble(Double.MAX_VALUE);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendDouble: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendDouble invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendDouble()
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
         session.sendDouble(Double.MAX_VALUE);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendDouble: " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP receiveDouble invocations
    *
    * @exception   Exception
    */
   public void testJRMPReceiveDouble()
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
         session.receiveDouble();
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP receiveDouble: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP receiveDouble invocations
    *
    * @exception   Exception
    */
   public void testIIOPReceiveDouble()
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
         session.receiveDouble();
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP receiveDouble: " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendReceiveDouble invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendReceiveDouble()
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
         session.sendReceiveDouble(Double.MAX_VALUE);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendReceiveDouble: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendReceiveDouble invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendReceiveDouble()
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
         session.sendReceiveDouble(Double.MAX_VALUE);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendReceiveDouble: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(T0PassBasicTypesTestCase.class, "iiopperf.jar");
   }

}
