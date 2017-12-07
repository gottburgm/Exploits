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

import org.omg.CORBA.Any;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Policy;
import org.omg.CORBA.PolicyError;
import org.omg.IOP.Codec;
import org.omg.PortableInterceptor.PolicyFactory;

import org.jboss.metadata.IorSecurityConfigMetaData;

/**
 * Factory of <code>org.omg.CORBA.Policy</code> objects containing 
 * csiv2 ior security config info
 *
 * @author  Dimitris.Andreadis@jboss.org
 * @version $Revision: 81018 $
 */
class CSIv2PolicyFactory
   extends LocalObject
   implements PolicyFactory 
{
   private Codec codec;
   
   // Constructor -------------------------------------------------------------
   public CSIv2PolicyFactory(Codec codec)
   {
      // cache the codec
      this.codec = codec;
   }

   // org.omg.PortableInterceptor.PolicyFactory operations --------------------

   public Policy create_policy(int type, Any value)
      throws PolicyError
   {
      if (type != CSIv2Policy.TYPE) {
         throw new PolicyError();
      }
      
      // stored as java.io.Serializable - is this a hack?
      IorSecurityConfigMetaData metadata =
         (IorSecurityConfigMetaData)value.extract_Value();
         
      return new CSIv2Policy(metadata, codec);
   }
}
