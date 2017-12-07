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
public class T2PassRemoteRefTestCase
   extends JBossTestCase
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   private java.util.Properties cosnamingJndiProps;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public T2PassRemoteRefTestCase(String name) 
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
    *   This tests the speed of JRMP sendRemote invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendRemote()
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
         session.sendRemote(session);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendRemote: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendRemote invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendRemote()
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
         session.sendRemote(session);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendRemote: " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP receiveRemote invocations
    *
    * @exception   Exception
    */
   public void testJRMPReceiveRemote()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext().lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendRemote(session);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveRemote();
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP receiveRemote: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP receiveRemote invocations
    *
    * @exception   Exception
    */
   public void testIIOPReceiveRemote()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext(cosnamingJndiProps).lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendRemote(session);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveRemote();
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP receiveRemote: " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendReceiveRemote invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendReceiveRemote()
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
         session.sendReceiveRemote(session);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendReceiveRemote: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendReceiveRemote invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendReceiveRemote()
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
         session.sendReceiveRemote(session);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendReceiveRemote: " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendSessionRef invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendSessionRef()
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
         session.sendSessionRef(session);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendSessionRef: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendSessionRef invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendSessionRef()
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
         session.sendSessionRef(session);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendSessionRef: " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP receiveSessionRef invocations
    *
    * @exception   Exception
    */
   public void testJRMPReceiveSessionRef()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext().lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendSessionRef(session);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveSessionRef();
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP receiveSessionRef: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP receiveSessionRef invocations
    *
    * @exception   Exception
    */
   public void testIIOPReceiveSessionRef()
      throws Exception
   {
      SessionHome home = (SessionHome)PortableRemoteObject.narrow(
            getInitialContext(cosnamingJndiProps).lookup(SessionHome.JNDI_NAME),
            SessionHome.class);
      Session session = home.create();
      session.sendSessionRef(session);
      int n = getIterationCount();
      long start = System.currentTimeMillis();
      for (int i = 0 ; i < n; i++)
      {
         session.receiveSessionRef();
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP receiveSessionRef: " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of JRMP sendReceiveSessionRef invocations
    *
    * @exception   Exception
    */
   public void testJRMPSendReceiveSessionRef()
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
         session.sendReceiveSessionRef(session);
      }
      long end = System.currentTimeMillis();
      getLog().info("JRMP sendReceiveSessionRef: " +
                    ((end - start) / (double)n) + " ms/call");
   }
   
   /**
    *   This tests the speed of IIOP sendReceiveSessionRef invocations
    *
    * @exception   Exception
    */
   public void testIIOPSendReceiveSessionRef()
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
         session.sendReceiveSessionRef(session);
      }
      long end = System.currentTimeMillis();
      getLog().info("IIOP sendReceiveSessionRef: " + 
                    ((end - start) / (double)n) + " ms/call");
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(T2PassRemoteRefTestCase.class, "iiopperf.jar");
   }

}
