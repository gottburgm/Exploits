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

import java.security.Principal;

import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.NO_PERMISSION;
import org.omg.CORBA.ORB;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.LocalObject;
import org.omg.CSI.AuthorizationElement;
import org.omg.CSI.EstablishContext;
import org.omg.CSI.IdentityToken;
import org.omg.CSI.MTContextError;
import org.omg.CSI.SASContextBody;
import org.omg.CSI.SASContextBodyHelper;

import org.omg.CSIIOP.CompoundSecMech;
import org.omg.CSIIOP.EstablishTrustInClient;

import org.omg.GSSUP.InitialContextToken;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecPackage.FormatMismatch;
import org.omg.IOP.CodecPackage.TypeMismatch;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.jacorb.orb.MinorCodes;

import org.jboss.logging.Logger;
import org.jboss.security.SecurityAssociation;

/**
 * This implementation of 
 * <code>org.omg.PortableInterceptor.ClientRequestInterceptor</code> inserts 
 * the security attribute service (SAS) context into outgoing IIOP requests 
 * and handles the SAS messages received from the target security service 
 * in the SAS context of incoming IIOP replies.
 * 
 * @author  <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 * @version $Revision: 81018 $
 */
public class SASClientInterceptor
   extends LocalObject
   implements ClientRequestInterceptor
{
   // Constants ------------------------------------------------------
   private static final int sasContextId =
      org.omg.IOP.SecurityAttributeService.value;

   private static final IdentityToken absentIdentityToken;
   static {
      absentIdentityToken = new IdentityToken();
      absentIdentityToken.absent(true);
   }
   private static final AuthorizationElement[] noAuthorizationToken = {};

   private static final Logger log = 
      Logger.getLogger(SASTargetInterceptor.class);
   private static final boolean traceEnabled = log.isTraceEnabled();


   // Fields ---------------------------------------------------------
    
   private Codec codec;

   // Constructor ---------------------------------------------------
    
   public SASClientInterceptor(Codec codec)
   {
      this.codec = codec;
   }
    
   // Methods  -------------------------------------------------------

    
   // org.omg.PortableInterceptor.Interceptor operations ------------
    
   public String name()
   {
      return "SASClientInterceptor";
   }

   public void destroy()
   {
      // do nothing
   }    
    
   // ClientRequestInterceptor operations ---------------------------
    
   public void send_request(ClientRequestInfo ri)
   {
      try
      {
         CompoundSecMech secMech = 
            CSIv2Util.getMatchingSecurityMech(
               ri,
               codec,
               EstablishTrustInClient.value,  /* client supports */
               (short)0                       /* client requires */);
         if (secMech == null)
            return;

         if ((secMech.as_context_mech.target_supports 
              & EstablishTrustInClient.value) != 0)
         {
            Principal p = SecurityAssociation.getPrincipal();
            if (p != null)
            {
               byte[] encodedTargetName = secMech.as_context_mech.target_name;

               // The name scope needs to be externalized
               String name = p.getName();
               if (name.indexOf('@') < 0)
               {
                  byte[] decodedTargetName = 
                     CSIv2Util.decodeGssExportedName(encodedTargetName);
                  String targetName = new String(decodedTargetName, "UTF-8");
                  name += "@" + targetName; // "@default"
               }
               byte[] username = name.getBytes("UTF-8");
               // I don't know why there is not a better way 
               // to go from char[] -> byte[]
               Object credential = SecurityAssociation.getCredential();
               byte[] password = {};
               if (credential instanceof char[])
               {
                  String tmp = new String((char[]) credential);
                  password = tmp.getBytes("UTF-8");
               }
               else if (credential instanceof byte[])
                  password = (byte[])credential;
               else if (credential != null)
               {
                  String tmp = credential.toString();
                  password = tmp.getBytes("UTF-8");
               }

               // create authentication token
               InitialContextToken authenticationToken = 
                  new InitialContextToken(username,
                                          password,
                                          encodedTargetName);
               // ASN.1-encode it, as defined in RFC 2743
               byte[] encodedAuthenticationToken =
                  CSIv2Util.encodeInitialContextToken(authenticationToken, 
                                                      codec);

               // create EstablishContext message with the encoded token
               EstablishContext message = 
                  new EstablishContext(0, // stateless ctx id
                                       noAuthorizationToken,
                                       absentIdentityToken,
                                       encodedAuthenticationToken); 

               // create SAS context with the EstablishContext message
               SASContextBody contextBody = new SASContextBody();
               contextBody.establish_msg(message);

               // stuff the SAS context into the outgoing request
               Any any = ORB.init().create_any();
               SASContextBodyHelper.insert(any, contextBody);
               ServiceContext sc =
                  new ServiceContext(sasContextId, codec.encode_value(any));
               ri.add_request_service_context(sc,
                                              true /*replace existing context*/);
            }
         }
      }
      catch (java.io.UnsupportedEncodingException e)
      {
         throw new MARSHAL("Unexpected exception: " + e);
      }
      catch (org.omg.IOP.CodecPackage.InvalidTypeForEncoding e)
      {
         throw new MARSHAL("Unexpected exception: " + e);
      }
   }

   public void send_poll(ClientRequestInfo ri)
   {
      // do nothing
   }

   public void receive_reply(ClientRequestInfo ri)
   {
      try
      {
         ServiceContext sc = ri.get_reply_service_context(sasContextId);
         Any msg = codec.decode_value(sc.context_data,
            SASContextBodyHelper.type());
         SASContextBody contextBody = SASContextBodyHelper.extract(msg);

         // At this point contextBody should contain a 
         // CompleteEstablishContext message, which does not require any 
         // treatment. ContextError messages should arrive via 
         // receive_exception().

         if (traceEnabled)
            log.trace("receive_reply: got SAS reply, type " +
                      contextBody.discriminator());

         if (contextBody.discriminator() == MTContextError.value)
         {
            // should not happen
            log.warn("Unexpected ContextError in SAS reply");
            throw new NO_PERMISSION("Unexpected ContextError in SAS reply",
               MinorCodes.SAS_CSS_FAILURE,
               CompletionStatus.COMPLETED_YES);
         }
      }
      catch (BAD_PARAM e)
      {
         // no service context with sasContextId: do nothing
      }
      catch (FormatMismatch e)
      {
         throw new MARSHAL("Could not parse SAS reply: " + e,
            0,
            CompletionStatus.COMPLETED_YES);
      }
      catch (TypeMismatch e)
      {
         throw new MARSHAL("Could not parse SAS reply: " + e,
            0,
            CompletionStatus.COMPLETED_YES);
      }
   }

   public void receive_exception(ClientRequestInfo ri)
   {
      try
      {
         ServiceContext sc = ri.get_reply_service_context(sasContextId);
         Any msg = codec.decode_value(sc.context_data,
            SASContextBodyHelper.type());
         SASContextBody contextBody = SASContextBodyHelper.extract(msg);

         // At this point contextBody may contain a either a 
         // CompleteEstablishContext message or a ContextError message.
         // Neither message requires any treatment. We decoded the context
         // body just to check that it contains a well-formed message.

         if (traceEnabled)
            log.trace("receive_exception: got SAS reply, type " +
                      contextBody.discriminator());
      }
      catch (BAD_PARAM e)
      {
         // no service context with sasContextId: do nothing
      }
      catch (FormatMismatch e)
      {
         throw new MARSHAL("Could not parse SAS reply: " + e,
            MinorCodes.SAS_CSS_FAILURE,
            CompletionStatus.COMPLETED_MAYBE);
      }
      catch (TypeMismatch e)
      {
         throw new MARSHAL("Could not parse SAS reply: " + e,
            MinorCodes.SAS_CSS_FAILURE,
            CompletionStatus.COMPLETED_MAYBE);
      }
   }

   public void receive_other(ClientRequestInfo ri)
   {
      // do nothing
   }

}
