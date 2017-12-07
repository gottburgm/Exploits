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
package org.jboss.naming.java;

import java.util.Hashtable;
import javax.naming.*;
import javax.naming.spi.*;

import org.jnp.interfaces.NamingContext;
import org.jnp.server.NamingServer;

/**
 *   Implementation of "java:" namespace factory. java: is a VM-local namespace.
 *     
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Oberg</a>
 *   @version $Revision: 81030 $
 */
public class javaURLContextFactory
   implements ObjectFactory
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
    
   // Static --------------------------------------------------------
   static NamingServer root;
   
   static
   {
      try
      {
         root = new NamingServer();
      } catch (NamingException e)
      {
         e.printStackTrace();
      }
   }
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------

   // ObjectFactory implementation ----------------------------------
   public Object getObjectInstance(Object obj,
                                Name name,
                                Context nameCtx,
                                Hashtable environment)
                         throws Exception
   {
      return new NamingContext(environment, name, root);
   }
}
