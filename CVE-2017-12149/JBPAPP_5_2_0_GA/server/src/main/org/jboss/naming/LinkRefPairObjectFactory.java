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
package org.jboss.naming;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;

import org.jboss.logging.Logger;
import org.jboss.util.id.GUID;

/** 
 * An object factory that allows different objects to be used
 * in the local virtual machine versus remote virtual machines
 *  
 * @author Adrian Brock (adrian@jboss.com)
 * @version $Revision: 81030 $
 */
public class LinkRefPairObjectFactory implements ObjectFactory
{
   // Constants -----------------------------------------------------

   /** The logger */
   private static final Logger log = Logger.getLogger(LinkRefPairObjectFactory.class);
   
   /** Our class name */
   static final String className = LinkRefPairObjectFactory.class.getName();
   
   /** The guid used to determine whether we in the same VM */
   static final String guid = new GUID().asString();
   
   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   
   // ObjectFactory implementation ----------------------------------

   public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable environment) throws Exception
   {
      LinkRefPair pair = (LinkRefPair) obj;
      String jndiName;
      
      // Local or remote?
      boolean local = false;
      if (guid.equals(pair.getGUID()))
      {
         jndiName = pair.getLocalLinkName();
         local = true;
      }
      else
         jndiName = pair.getRemoteLinkName();
      
      InitialContext ctx;
      if (local || environment == null)
         ctx = new InitialContext();
      else
         ctx = new InitialContext(environment);
      
      return ctx.lookup(jndiName);
   }
   
   // Y overrides ---------------------------------------------------
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
