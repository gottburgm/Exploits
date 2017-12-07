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
package org.jboss.iiop;

import javax.naming.InitialContext;

import org.jboss.corba.ORBFactory;
import org.omg.CORBA.ORB;

/**
 * Singleton class to ensure that all code running in the JBoss VM uses the 
 * same ORB instance. The CorbaORBService MBean calls CorbaORB.setInstance()
 * at service creation time, after it creates an ORB instance. Code that runs
 * both in the server VM and in client VM calls CorbaORB.getInstance() to get 
 * an ORB.
 *
 * @author  <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 * @version $Revision: 81018 $
 */
public class CorbaORB
{
   /** The ORB instance in this VM. */
   private static ORB instance;

   /** Enforce non-instantiability. */
   private CorbaORB()
   {
   }
   
   /** 
    * This method is called only by the CorbaORBService MBean, so it has
    * package visibility. 
    */
   static void setInstance(ORB orb)
   {
      if (instance == null)
         instance = orb;
      else
         throw new RuntimeException(CorbaORB.class.getName() 
                                    + ".setInstance() called more than once");
   }
   
   /**
    * This method is called by classes that are used both at the server and at 
    * the client side: the handle impl (org.jboss.proxy.ejb.HandleImplIIOP),
    * the home handle impl (org.jboss.proxy.ejb.HomeHandleImplIIOP),
    * and the home factory (org.jboss.proxy.ejb.IIOPHomeFactory).
    * When called by code running in the same VM as the the JBoss server,
    * getInstance() returns the ORB instance used by the CorbaORBService MBean 
    * (which previously issued a setInstance() call). Otherwise getInstance() 
    * returns an ORB instance obtained with an ORB.init() call.
    */
   public static ORB getInstance() 
   {
      // No instance established, we must be on the client
      // Use the orb consistent with java:comp/ORB (if any).
      if (instance == null) 
      {
         try
         {
            InitialContext ctx = new InitialContext();
            instance = (ORB) ctx.lookup("java:comp/ORB");
         }
         catch (Exception ignored)
         {
         }
         if (instance == null)
            instance = ORBFactory.getORB();
      }
      return instance;
   }
}
