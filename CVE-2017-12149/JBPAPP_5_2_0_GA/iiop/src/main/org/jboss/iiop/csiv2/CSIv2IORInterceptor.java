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
import org.omg.CORBA.ORB;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CSIIOP.Integrity;
import org.omg.CSIIOP.DetectReplay;
import org.omg.CSIIOP.DetectMisordering;
import org.omg.PortableInterceptor.IORInfo;
import org.omg.PortableInterceptor.IORInterceptor;

import org.omg.IOP.Codec;
import org.omg.IOP.CodecPackage.InvalidTypeForEncoding;
import org.omg.IOP.TAG_INTERNET_IOP;
import org.omg.IOP.TaggedComponent;

import org.omg.SSLIOP.SSL;
import org.omg.SSLIOP.SSLHelper;
import org.omg.SSLIOP.TAG_SSL_SEC_TRANS;

import org.jboss.iiop.CorbaORBService;
import org.jboss.logging.Logger;
import org.jboss.metadata.IorSecurityConfigMetaData;

/**
 * Implements an <code>org.omg.PortableInterceptor.IORInterceptor</code>
 * that CSIv2 info to an IOR.
 *
 * @author Dimitris.Andreadis@jboss.org
 * @version $Revision: 108276 $
 */
public class CSIv2IORInterceptor
   extends LocalObject
   implements IORInterceptor
{
   private static final Logger log = Logger.getLogger(CSIv2IORInterceptor.class);
   /**
    * The minimum set of security options supported by the SSL mechanism
    * (These options cannot be turned off, so they are always supported.)
    */
   private static final int MIN_SSL_OPTIONS = Integrity.value |
      DetectReplay.value |
      DetectMisordering.value;

   private TaggedComponent defaultSSLComponent;
   private TaggedComponent defaultCSIComponent;

   public CSIv2IORInterceptor(Codec codec)
   {
      int sslPort = CorbaORBService.getTheActualSSLPort();
      try
      {
         // Build default SSL component with minimum SSL options
         SSL ssl = new SSL((short) MIN_SSL_OPTIONS, /* supported options */
            (short) 0, /* required options  */
            (short) sslPort);
         ORB orb = ORB.init();
         Any any = orb.create_any();
         SSLHelper.insert(any, ssl);
         byte[] componentData = codec.encode_value(any);
         defaultSSLComponent = new TaggedComponent(TAG_SSL_SEC_TRANS.value,
            componentData);

         IorSecurityConfigMetaData metadata = CorbaORBService.getDefaultIORSecurityMetaData();
         defaultCSIComponent = CSIv2Util.createSecurityTaggedComponent(metadata,
            codec, sslPort, orb);
      }
      catch (InvalidTypeForEncoding e)
      {
         log.warn("Caught unexcepted exception while encoding SSL component", e);
         throw new RuntimeException(e);
      }
   }

   // org.omg.PortableInterceptor.IORInterceptor operations -------------------

   public String name()
   {
      return CSIv2IORInterceptor.class.getName();
   }

   public void destroy()
   {
   }

   // called for all IORs created from this ORB
   public void establish_components(IORInfo info)
   {
      // check if CSIv2 policy is in effect for this IOR
      CSIv2Policy csiv2Policy = null;

      try
      {
         csiv2Policy = (CSIv2Policy) info.get_effective_policy(CSIv2Policy.TYPE);
      }
      catch (BAD_PARAM e)
      {
         log.debug("No CSIv2Policy");
      }
      catch (Exception e)
      {
         log.debug("Error fetching CSIv2Policy", e);
      }

      if (csiv2Policy != null)
      {
         // if csiv2Policy effective, stuff a copy of the TaggedComponents
         // already created by the CSIv2Policy into the IOR's IIOP profile
         TaggedComponent sslComponent =
            csiv2Policy.getSSLTaggedComponent();
         if (sslComponent != null &&
             CorbaORBService.getSSLComponentsEnabledFlag() == true)
         {
            info.add_ior_component_to_profile(sslComponent,
                                              TAG_INTERNET_IOP.value);
         }
         TaggedComponent csiv2Component =
            csiv2Policy.getSecurityTaggedComponent();
         if (csiv2Component != null)
         {
            info.add_ior_component_to_profile(csiv2Component,
               TAG_INTERNET_IOP.value);
         }
      }
      else 
      {
         if (defaultSSLComponent != null &&
             CorbaORBService.getSSLComponentsEnabledFlag() == true)
         {
            // otherwise stuff the default SSL component (with the minimum
            // set of SSL options) into the IOR's IIOP profile
            info.add_ior_component_to_profile(defaultSSLComponent,
                                              TAG_INTERNET_IOP.value);
         }
         if (defaultCSIComponent != null) 
         {
            // and stuff the default CSI component (with the minimum
            // set of CSI options) into the IOR's IIOP profile
            info.add_ior_component_to_profile(defaultCSIComponent,
                                              TAG_INTERNET_IOP.value);
         }
      }

      return;
   }
}
