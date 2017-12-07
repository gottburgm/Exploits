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
package org.jboss.naming.client.java;

import javax.ejb.spi.HandleDelegate;

import org.jboss.proxy.ejb.handle.HandleDelegateImpl;

/**
 * A simple factory for the HandleDelegate implementation object
 * along the lines of ORBFactory
 *  
 * @author Dimitris.Andreadis@jboss.org
 * @version $Revision: 81030 $
 */
public class HandleDelegateFactory
{
   /** The HandleDelegateImpl */
   private static HandleDelegate hd;
   
   public static HandleDelegate getHandleDelegateSingleton()
   {
      synchronized (HandleDelegateFactory.class)
      {
         if (hd == null)
         {
            // Create the singleton
            hd = new HandleDelegateImpl();
         }
         return hd;
      }
   }
}
