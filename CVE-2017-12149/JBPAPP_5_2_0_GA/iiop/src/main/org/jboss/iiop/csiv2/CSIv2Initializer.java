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

import org.omg.CORBA.LocalObject;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfo;

import org.omg.IOP.Codec;
import org.omg.IOP.Encoding;
import org.omg.IOP.ENCODING_CDR_ENCAPS;

import org.jacorb.ssl.SSL_POLICY_TYPE;
import org.jacorb.security.ssl.SSLPolicyFactory;

/**
 * Implements an <code>org.omg.PortableInterceptor.ORBinitializer</code> that
 * installs a <code>CSIv2IORInterceptor</code> and a 
 * <code>CSIv2PolicyFactory</code>.
 *
 * @author  <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 * @version $Revision: 81018 $
 */
public class CSIv2Initializer 
      extends LocalObject
      implements ORBInitializer
{

   public CSIv2Initializer() 
   {
      // do nothing
   }

   // org.omg.PortableInterceptor.ORBInitializer operations -------------------

   public void pre_init(ORBInitInfo info) 
   {
      // do nothing
   }
   
   public void post_init(ORBInitInfo info) 
   {
      try {
         // Use CDR encapsulation with GIOP 1.0 encoding
         Encoding encoding = new Encoding(ENCODING_CDR_ENCAPS.value, 
                                          (byte)1, /* GIOP version */
                                          (byte)0  /* GIOP revision*/);
         Codec codec = info.codec_factory().create_codec(encoding);

         // Add IOR interceptor for CSIv2
         info.add_ior_interceptor(new CSIv2IORInterceptor(codec));

         // Register CSIv2-related policy factories
         info.register_policy_factory(CSIv2Policy.TYPE,
                                      new CSIv2PolicyFactory(codec));
         info.register_policy_factory(SSL_POLICY_TYPE.value,
                                      new SSLPolicyFactory());
      }
      catch (Exception e) {
         throw new RuntimeException("Unexpected " + e);
      }
   }
}
