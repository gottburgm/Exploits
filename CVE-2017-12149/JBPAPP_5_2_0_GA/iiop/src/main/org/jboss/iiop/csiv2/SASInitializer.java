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
import org.omg.IOP.Codec;
import org.omg.IOP.ENCODING_CDR_ENCAPS;
import org.omg.IOP.Encoding;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName;
import org.omg.PortableInterceptor.ORBInitializer;

import org.jboss.ejb.plugins.SecurityInterceptor;
import org.jboss.system.Registry;

/**
 * This is an <code>org.omg.PortableInterceptor.ORBInitializer</code> that
 * initializes the Security Attibute Service (SAS).
 *
 * @author  <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 * @version $Revision: 81018 $
 */
public class SASInitializer
   extends LocalObject
   implements ORBInitializer
{

   public SASInitializer()
   {
      // do nothing
   }
    
   // org.omg.PortableInterceptor.ORBInitializer operations ---------
    
   public void pre_init(ORBInitInfo info)
   {
      try
      {
         // Create and register the SASCurrent
         SASCurrent sasCurrent = new SASCurrentImpl();
         info.register_initial_reference("SASCurrent", sasCurrent);
      }
      catch(InvalidName e)
      {
         throw new RuntimeException("Could not register initial " +
            "reference for SASCurrent: " + e);
      }
   }

   public void post_init(ORBInitInfo info)
   {
      try
      {
         // Use CDR encapsulations with GIOP 1.0 encoding
         Encoding encoding = new Encoding(ENCODING_CDR_ENCAPS.value,
            (byte) 1, /* GIOP version */
            (byte) 0  /* GIOP revision*/);
         Codec codec = info.codec_factory().create_codec(encoding);
            
         // Create and register client interceptor
         SASClientIdentityInterceptor clientInterceptor =
            new SASClientIdentityInterceptor(codec);
         info.add_client_request_interceptor(clientInterceptor);

         // Create and register server interceptor
         SASTargetInterceptor serverInterceptor =
            new SASTargetInterceptor(codec);
         info.add_server_request_interceptor(serverInterceptor);
 
         // Initialize the SASCurrent implementation
         org.omg.CORBA.Object obj = 
            info.resolve_initial_references("SASCurrent");
         final SASCurrentImpl sasCurrentImpl = (SASCurrentImpl) obj;
         sasCurrentImpl.init(serverInterceptor);

         // Create and register an AuthenticationObserver to be called 
         // by the SecurityInterceptor
         Registry.bind(SecurityInterceptor.AuthenticationObserver.KEY,
                       new SecurityInterceptor.AuthenticationObserver() {
                          public void authenticationFailed()
                          {
                             sasCurrentImpl.reject_incoming_context();
                          }
                       });
      }
      catch(Exception e)
      {
         throw new RuntimeException("Unexpected " + e);
      }
   }

}
