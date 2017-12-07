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
package org.jboss.iiop.csiv2;

/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

import org.omg.CORBA.LocalObject;
import org.omg.CSI.IdentityToken;

/**
 * This class implements <code>SASCurrent</code>.
 *
 * @author  <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 * @version $Revision: 81018 $
 */

public class SASCurrentImpl
   extends LocalObject
   implements SASCurrent
{
   // Fields --------------------------------------------------------

   private SASTargetInterceptor serverInterceptor;
 
   // Initializer ---------------------------------------------------

   public void init(SASTargetInterceptor serverInterceptor)
   {
      this.serverInterceptor = serverInterceptor;
   }

   // SASCurrent operations -----------------------------------------
    
   public boolean context_received()
   {
      return serverInterceptor.sasContextReceived();
   }

   public boolean client_authentication_info_received()
   {
      return serverInterceptor.authenticationTokenReceived();
   }

   public byte[] get_incoming_username()
   {
      return serverInterceptor.getIncomingUsername();
   }

   public byte[] get_incoming_password()
   {
      return serverInterceptor.getIncomingPassword();
   }

   public byte[] get_incoming_target_name()
   {
      return serverInterceptor.getIncomingTargetName();
   }

   public IdentityToken get_incoming_identity()
   {
      return serverInterceptor.getIncomingIdentity();
   }


   public int get_incoming_identity_token_type()
   {
      return serverInterceptor.getIncomingIdentity().discriminator();
   }

   public byte[] get_incoming_principal_name()
   {
      return serverInterceptor.getIncomingPrincipalName();
   }

   public void reject_incoming_context()
   {
      serverInterceptor.rejectIncomingContext();
   }

}
