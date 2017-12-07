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
package org.jboss.corba;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Hashtable;
import java.util.Properties;

import org.jboss.logging.Logger;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;

/** 
 * An object factory that creates an ORB on the client
 *  
 * @author Adrian Brock (adrian@jboss.com)
 * @version $Revision: 81030 $
 */
public class ORBFactory
{
   // Constants -----------------------------------------------------

   /** The logger */
   private static final Logger log = Logger.getLogger(ORBFactory.class);
   
   /** The orb */
   private static ORB orb;
   
   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------
   
   public static ORB getORB()
   {
      synchronized (ORBFactory.class)
      {
         if (orb == null)
         {
            Properties properties;
            try
            {
               properties = (Properties) AccessController.doPrivileged(new PrivilegedAction()
               {
                  public Object run()
                  {
                     return System.getProperties();
                  }
               });
            }
            catch (SecurityException ignored)
            {
               log.trace("Unable to retrieve system properties", ignored);
               properties = null;
            }

            // Create the singleton ORB
            orb = ORB.init(new String[0], properties);

            // Activate the root POA
            try 
            {
                POA rootPOA = (POA) orb.resolve_initial_references("RootPOA");
                rootPOA.the_POAManager().activate();
            }
            catch (Throwable t)
            {
                log.warn("Unable to activate POA", t);
            }
         }
         return orb;
      }
   }
   
   public static void setORB(ORB orb)
   {
      if (ORBFactory.orb != null)
         throw new IllegalStateException("ORB has already been set");
      ORBFactory.orb = orb;
   }
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------

   // Y overrides ---------------------------------------------------
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
