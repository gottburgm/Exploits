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
package org.jboss.test.cmp2.batchcascadedelete.ejb;



public class ParentUtil
{
   /**
    * Cached remote home (EJBHome). Uses lazy loading to obtain its value (loaded by getHome() methods).
    */
   private static ParentHome cachedRemoteHome = null;

   /**
    * Cached local home (EJBLocalHome). Uses lazy loading to obtain its value (loaded by getLocalHome() methods).
    */
   private static ParentLocalHome cachedLocalHome = null;

   // Home interface lookup methods

   /**
    * Obtain remote home interface from default initial context
    *
    * @return Home interface for Parent. Lookup using JNDI_NAME
    */
   public static ParentHome getHome() throws javax.naming.NamingException
   {
      if(cachedRemoteHome == null)
      {
         // Obtain initial context
         javax.naming.InitialContext initialContext = new javax.naming.InitialContext();
         try
         {
            java.lang.Object objRef = initialContext.lookup(ParentHome.JNDI_NAME);
            cachedRemoteHome = (ParentHome)javax.rmi.PortableRemoteObject.narrow(objRef, ParentHome.class);
         }
         finally
         {
            initialContext.close();
         }
      }
      return cachedRemoteHome;
   }

   /**
    * Obtain remote home interface from parameterised initial context
    *
    * @param environment Parameters to use for creating initial context
    * @return Home interface for Parent. Lookup using JNDI_NAME
    */
   public static ParentHome getHome(java.util.Hashtable environment) throws javax.naming.NamingException
   {
      // Obtain initial context
      javax.naming.InitialContext initialContext = new javax.naming.InitialContext(environment);
      try
      {
         java.lang.Object objRef = initialContext.lookup(ParentHome.JNDI_NAME);
         return (ParentHome)javax.rmi.PortableRemoteObject.narrow(objRef, ParentHome.class);
      }
      finally
      {
         initialContext.close();
      }
   }

   /**
    * Obtain local home interface from default initial context
    *
    * @return Local home interface for Parent. Lookup using JNDI_NAME
    */
   public static ParentLocalHome getLocalHome() throws javax.naming.NamingException
   {
      // Local homes shouldn't be narrowed, as there is no RMI involved.
      if(cachedLocalHome == null)
      {
         // Obtain initial context
         javax.naming.InitialContext initialContext = new javax.naming.InitialContext();
         try
         {
            cachedLocalHome = (ParentLocalHome)initialContext.lookup(ParentLocalHome.JNDI_NAME);
         }
         finally
         {
            initialContext.close();
         }
      }
      return cachedLocalHome;
   }

   /**
    * Cached per JVM server IP.
    */
   private static String hexServerIP = null;

   // initialise the secure random instance
   private static final java.security.SecureRandom seeder = new java.security.SecureRandom();

   /**
    * A 32 byte GUID generator (Globally Unique ID). These artificial keys SHOULD <strong>NOT </strong> be seen by the user,
    * not even touched by the DBA but with very rare exceptions, just manipulated by the database and the programs.
    *
    * Usage: Add an id field (type java.lang.String) to your EJB, and add setId(XXXUtil.generateGUID(this)); to the ejbCreate method.
    */
   public static final String generateGUID(Object o)
   {
      StringBuffer tmpBuffer = new StringBuffer(16);
      if(hexServerIP == null)
      {
         java.net.InetAddress localInetAddress = null;
         try
         {
            // get the inet address
            localInetAddress = java.net.InetAddress.getLocalHost();
         }
         catch(java.net.UnknownHostException uhe)
         {
            System.err.println("ParentUtil: Could not get the local IP address using InetAddress.getLocalHost()!");
            // todo: find better way to get around this...
            uhe.printStackTrace();
            return null;
         }
         byte serverIP[] = localInetAddress.getAddress();
         hexServerIP = hexFormat(getInt(serverIP), 8);
      }
      String hashcode = hexFormat(System.identityHashCode(o), 8);
      tmpBuffer.append(hexServerIP);
      tmpBuffer.append(hashcode);

      long timeNow = System.currentTimeMillis();
      int timeLow = (int)timeNow & 0xFFFFFFFF;
      int node = seeder.nextInt();

      StringBuffer guid = new StringBuffer(32);
      guid.append(hexFormat(timeLow, 8));
      guid.append(tmpBuffer.toString());
      guid.append(hexFormat(node, 8));
      return guid.toString();
   }

   private static int getInt(byte bytes[])
   {
      int i = 0;
      int j = 24;
      for(int k = 0; j >= 0; k++)
      {
         int l = bytes[k] & 0xff;
         i += l << j;
         j -= 8;
      }
      return i;
   }

   private static String hexFormat(int i, int j)
   {
      String s = Integer.toHexString(i);
      return padHex(s, j) + s;
   }

   private static String padHex(String s, int i)
   {
      StringBuffer tmpBuffer = new StringBuffer();
      if(s.length() < i)
      {
         for(int j = 0; j < i - s.length(); j++)
         {
            tmpBuffer.append('0');
         }
      }
      return tmpBuffer.toString();
   }

}