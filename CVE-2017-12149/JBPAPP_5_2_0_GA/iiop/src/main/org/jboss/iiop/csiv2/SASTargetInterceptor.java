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

import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.NO_PERMISSION;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.ORB;
import org.omg.CSI.CompleteEstablishContext;
import org.omg.CSI.ContextError;
import org.omg.CSI.EstablishContext;
import org.omg.CSI.GSS_NT_ExportedNameHelper;
import org.omg.CSI.ITTPrincipalName;
import org.omg.CSI.IdentityToken;
import org.omg.CSI.MTEstablishContext;
import org.omg.CSI.MTMessageInContext;
import org.omg.CSI.SASContextBody;
import org.omg.CSI.SASContextBodyHelper;
import org.omg.GSSUP.ErrorToken;
import org.omg.GSSUP.ErrorTokenHelper;
import org.omg.GSSUP.GSS_UP_S_G_UNSPECIFIED;
import org.omg.GSSUP.InitialContextToken;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecPackage.FormatMismatch;
import org.omg.IOP.CodecPackage.TypeMismatch;
import org.omg.IOP.CodecPackage.InvalidTypeForEncoding;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

import org.jboss.iiop.CorbaORBService;
import org.jboss.logging.Logger;

/**
 * This implementation of 
 * <code>org.omg.PortableInterceptor.ServerRequestInterceptor</code>
 * extracts the security attribute service (SAS) context from incoming IIOP
 * and inserts SAS messages into the SAS context of outgoing IIOP replies.
 *
 * @author <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 * @version $Revision: 81018 $
 */
public class SASTargetInterceptor
      extends LocalObject
      implements ServerRequestInterceptor 
{

   // Static fields and initializers ---------------------------------
   
   private static final Logger log = 
      Logger.getLogger(SASTargetInterceptor.class);
   private static final boolean traceEnabled = log.isTraceEnabled();

   private static final int sasContextId = 
      org.omg.IOP.SecurityAttributeService.value;
   
   private static final byte[] empty = new byte[0];
   private static final IdentityToken absent;
   
   /** Scratch field for <code>CompleteEstablishContext<code> messages */
   private static final SASContextBody msgBodyCtxAccepted;
   
   /** Ready-to-go <code>CompleteEstablishContext<code> message 
       with context id set to zero */
   private static final Any msgCtx0Accepted;
   
   static 
   {
      // initialize absent
      absent = new IdentityToken();
      absent.absent(true);
      
      // initialize msgBodyCtxAccepted
      // (Note that "context stateful" is always set to false. Even if the 
      // client wants a stateful context, we negotiate the context down to
      // stateless.)
      CompleteEstablishContext ctxAccepted =
         new CompleteEstablishContext(0,          /* context id       */
                                      false,      /* context stateful */
                                      new byte[0] /* no final token   */);
      
      msgBodyCtxAccepted = new SASContextBody();
      msgBodyCtxAccepted.complete_msg(ctxAccepted);
      
      // initialize msgCtx0Accepted
      msgCtx0Accepted = createMsgCtxAccepted(0);
   }
   
   // Static methods  ------------------------------------------------
   
   private static Any createMsgCtxAccepted(long contextId)
   {
      Any any = ORB.init().create_any();
      synchronized (msgBodyCtxAccepted)
      {
         msgBodyCtxAccepted.complete_msg().client_context_id = contextId;
         SASContextBodyHelper.insert(any, msgBodyCtxAccepted);
      }
      return any;
   }
   
   // Fields ---------------------------------------------------------
   
   private final Codec codec;
   
   /** Scratch field for <code>ContextError<code> messages */
   private final SASContextBody msgBodyCtxError;
   
   /** Ready-to-go <code>ContextError<code> message with context id set to 
       zero and major status "invalid evidence" */
   private final Any msgCtx0Rejected;
   
   private ThreadLocal threadLocalData = new ThreadLocal() {
         protected synchronized Object initialValue() 
         {
            return new CurrentRequestInfo(); // see nested class below
         }
      };
   
   // Nested class  -------------------------------------------------

   /**
    * The <code>CurrentRequestInfo</code> class holds SAS information
    * associated with IIOP request handled by the current thread. 
    */
   private static class CurrentRequestInfo
   {
      boolean sasContextReceived;
      boolean authenticationTokenReceived;
      byte[] incomingUsername;
      byte[] incomingPassword;
      byte[] incomingTargetName;
      IdentityToken incomingIdentity;
      byte[] incomingPrincipalName;
      long contextId;
      Any sasReply;
      boolean sasReplyIsAccept;   // true if sasReply is 
                                  // CompleteEstablishContext (for 
                                  // interoperability with IONA's ASP 6.0)
      CurrentRequestInfo()
      {
      }
   }
   
   // Private method ------------------------------------------------
   
   private Any createMsgCtxError(long contextId, int majorStatus)
   {
      Any any = ORB.init().create_any();
      synchronized (msgBodyCtxError)
      {
         msgBodyCtxError.error_msg().client_context_id = contextId;
         msgBodyCtxError.error_msg().major_status = majorStatus;
         SASContextBodyHelper.insert(any, msgBodyCtxError);
      }
      return any;
   }

   // Constructor ---------------------------------------------------
   
   public SASTargetInterceptor(Codec codec)
   {
      this.codec = codec;
      
      // build encapsulated GSSUP error token for ContextError messages
      // (the error code within the error token is GSS_UP_S_G_UNSPECIFIED,
      // which says nothing about the cause of the error)
      ErrorToken errorToken = new ErrorToken(GSS_UP_S_G_UNSPECIFIED.value);
      Any any = ORB.init().create_any();
      byte[] encapsulatedErrorToken;
      
      ErrorTokenHelper.insert(any, errorToken);
      try
      {
         encapsulatedErrorToken = codec.encode_value(any);
      }
      catch (InvalidTypeForEncoding e)
      {
         throw new RuntimeException("Unexpected exception: " + e);
      }
      
      // initialize msgBodyCtxError
      ContextError ctxError =
         new ContextError(0,          /* context id                     */
                          1,          /* major status: invalid evidence */
                          1,          /* minor status (always 1)        */
                          encapsulatedErrorToken);
      
      msgBodyCtxError = new SASContextBody();
      msgBodyCtxError.error_msg(ctxError);
      
      // initialize msgCtx0Rejected (major status: invalid evidence)
      msgCtx0Rejected = createMsgCtxError(0, 1);
      
   }
    
   // Methods  -------------------------------------------------------
   
   /**
    * Returns true if an SAS context arrived with the current IIOP request.
    */
   boolean sasContextReceived() 
   {
      CurrentRequestInfo threadLocal = 
            (CurrentRequestInfo)threadLocalData.get();
      return threadLocal.sasContextReceived;
   }
   
   /**
    * Returns true if a client authentication token arrived with the 
    * current IIOP request.
    */
   boolean authenticationTokenReceived()
   {
      CurrentRequestInfo threadLocal = 
         (CurrentRequestInfo)threadLocalData.get();
      return threadLocal.authenticationTokenReceived;
   }
   
   /**
    * Returns the username that arrived in the current IIOP request.
    */
   byte[] getIncomingUsername() 
   {
      CurrentRequestInfo threadLocal = 
            (CurrentRequestInfo)threadLocalData.get();
      return threadLocal.incomingUsername;
   }
   
   /**
    * Returns the password that arrived in the current IIOP request.
    */
   byte[] getIncomingPassword() 
   {
      CurrentRequestInfo threadLocal = 
            (CurrentRequestInfo)threadLocalData.get();
      return threadLocal.incomingPassword;
   }
   
   /**
    * Returns the target name that arrived in the current IIOP request.
    */
   byte[] getIncomingTargetName() 
   {
      CurrentRequestInfo threadLocal = 
            (CurrentRequestInfo)threadLocalData.get();
      return threadLocal.incomingTargetName;
   }
   
   /**
    * Returns the <code>org.omg.CSI.IdentityToken<code> that arrived in 
    * the current IIOP request.
    */
   IdentityToken getIncomingIdentity() 
   {
      CurrentRequestInfo threadLocal = 
            (CurrentRequestInfo)threadLocalData.get();
      return threadLocal.incomingIdentity;
   }

   /**
    * Returns the principal name that arrived in the current IIOP request.
    */
   byte[] getIncomingPrincipalName() 
   {
      CurrentRequestInfo threadLocal = 
            (CurrentRequestInfo)threadLocalData.get();
      return threadLocal.incomingPrincipalName;
   }
   
   /**
    * Sets the outgoing SAS reply to <code>ContextError</code>, with major 
    * status "invalid evidence".
    */
   void rejectIncomingContext() 
   {
      CurrentRequestInfo threadLocal = 
         (CurrentRequestInfo)threadLocalData.get();
      
      if (threadLocal.sasContextReceived)
      {
         threadLocal.sasReply =  
            (threadLocal.contextId == 0) 
            ? msgCtx0Rejected 
            : createMsgCtxError(threadLocal.contextId, 
                                1 /* major status: invalid evidence */);
         threadLocal.sasReplyIsAccept = false;
      }
   }
   
   // org.omg.PortableInterceptor.Interceptor operations ------------
   
   public String name()
   {
      return "SASTargetInterceptor";
   }
   
   public void destroy()
   {
      // do nothing
   }    
   
   // ServerRequestInterceptor operations ---------------------------
   
   public void receive_request_service_contexts(ServerRequestInfo ri) 
   {
      // do nothing
   }
   
   // ServerRequestInterceptor operations ---------------------------
   
   public void receive_request(ServerRequestInfo ri) 
   {
      if (traceEnabled)
         log.trace("receive_request " + ri.operation());
      CurrentRequestInfo threadLocal =
            (CurrentRequestInfo)threadLocalData.get();
      
      threadLocal.sasContextReceived = false;
      threadLocal.authenticationTokenReceived = false;
      threadLocal.incomingUsername = empty;
      threadLocal.incomingPassword = empty;
      threadLocal.incomingTargetName = empty;
      threadLocal.incomingIdentity = absent;
      threadLocal.incomingPrincipalName = empty;
      threadLocal.sasReply = null;
      threadLocal.sasReplyIsAccept = false;
      
      try 
      {
         ServiceContext sc = ri.get_request_service_context(sasContextId);
         Any any = codec.decode_value(sc.context_data, 
                                      SASContextBodyHelper.type());
         SASContextBody contextBody = SASContextBodyHelper.extract(any);
         
         if (contextBody == null)
         {
            // we're done
            return;
         }
         else if (contextBody.discriminator() == MTMessageInContext.value)
         {
            // should not happen, as stateful context requests are always
            // negotiated down to stateless in this implementation
            long contextId = 
               contextBody.in_context_msg().client_context_id;
            threadLocal.sasReply =  
               createMsgCtxError(contextId,
                                 4 /* major status: no context */);
            throw new NO_PERMISSION("SAS context does not exist.");
         }
         else if (contextBody.discriminator() == MTEstablishContext.value)
         {
            EstablishContext message = contextBody.establish_msg();
            threadLocal.contextId = message.client_context_id;
            threadLocal.sasContextReceived = true;
            
            if (message.client_authentication_token != null
                && message.client_authentication_token.length > 0)
            {
               if (traceEnabled)
                  log.trace("received client authentication token");
               InitialContextToken authToken =
                  CSIv2Util.decodeInitialContextToken(
                                          message.client_authentication_token, 
                                          codec);
               if (authToken == null) 
               {
                  threadLocal.sasReply =  
                     createMsgCtxError(message.client_context_id,
                                       2 /* major status:
                                            invalid mechanism */);
                  throw new NO_PERMISSION("Could not decode " +
                                          "initial context token.");
               }
               threadLocal.incomingUsername = authToken.username;
               threadLocal.incomingPassword = authToken.password;
               threadLocal.incomingTargetName = 
                  CSIv2Util.decodeGssExportedName(authToken.target_name);
               if (threadLocal.incomingTargetName == null) 
               {
                  threadLocal.sasReply =  
                     createMsgCtxError(message.client_context_id,
                                       2 /* major status:
                                            invalid mechanism */);
                  throw new NO_PERMISSION("Could not decode target name " +
                                          "in initial context token.");
               }
               
               
               threadLocal.authenticationTokenReceived = true;
            }
            if (message.identity_token != null)
            {
               if (traceEnabled)
                  log.trace("received identity token");
               threadLocal.incomingIdentity = message.identity_token;
               if (message.identity_token.discriminator() == ITTPrincipalName.value)
               {
                  // Extract the RFC2743-encoded name 
                  // from CDR encapsulation
                  Any a = codec.decode_value(
                                       message.identity_token.principal_name(),
                                       GSS_NT_ExportedNameHelper.type());
                  byte[] encodedName = GSS_NT_ExportedNameHelper.extract(a); 
                  
                  // Decode the principal name
                  threadLocal.incomingPrincipalName = 
                     CSIv2Util.decodeGssExportedName(encodedName);
                  
                  if (threadLocal.incomingPrincipalName == null) 
                  {
                     threadLocal.sasReply =  
                        createMsgCtxError(message.client_context_id,
                                          2 /* major status:
                                               invalid mechanism */);
                     throw new NO_PERMISSION("Could not decode " +
                                             "incoming principal name.");
                  }
               }
            }
            threadLocal.sasReply = (threadLocal.contextId == 0) ? 
                                   msgCtx0Accepted : 
                                   createMsgCtxAccepted(threadLocal.contextId);
            threadLocal.sasReplyIsAccept = true;
         }
      }
      catch (BAD_PARAM e) 
      {
         // no service context with sasContextId: do nothing
      } 
      catch (FormatMismatch e) 
      {
         throw new MARSHAL("Exception decoding context data in " +
                           "SASTargetInterceptor: " + e);
      }
      catch (TypeMismatch e) 
      {
         throw new MARSHAL("Exception decoding context data in " +
                           "SASTargetInterceptor: " + e);
      }
   }
    
   public void send_reply(ServerRequestInfo ri) 
   {
      if (traceEnabled)
         log.trace("send_reply " + ri.operation());
      CurrentRequestInfo threadLocal =
         (CurrentRequestInfo)threadLocalData.get();
      
      if (threadLocal.sasReply != null)
      {
            try
            {
               ServiceContext sc = 
                  new ServiceContext(sasContextId, 
                                     codec.encode_value(threadLocal.sasReply));
               ri.add_reply_service_context(sc, true);
            }
            catch (InvalidTypeForEncoding e)
            {
               throw new MARSHAL("Unexpected exception: " + e);
            }
      }
   }
   
   public void send_exception(ServerRequestInfo ri) 
   {
      if (traceEnabled)
         log.trace("send_exception " + ri.operation() + ": ");
      CurrentRequestInfo threadLocal =
            (CurrentRequestInfo)threadLocalData.get();

      // The check for sasReplyIsAccept below was added for interoperability 
      // with IONA's ASP 6.0, which throws an ArrayIndexOutOfBoundsException 
      // when it receives an IIOP reply carrying both an application exception 
      // and a SAS reply CompleteEstablishContext. The sasReplyIsAccept flag 
      // serves the purpose of refraining from sending an SAS accept 
      // (CompleteEstablishContext) reply together with an exception. 
      // 
      // The CSIv2 spec does not explicitly disallow an SAS accept in an
      // IIOP exception reply.
      //
      if (threadLocal.sasReply != null && 
          (!threadLocal.sasReplyIsAccept ||
           CorbaORBService.getSendSASAcceptWithExceptionEnabledFlag() == true))
      {
         try
         {
            ServiceContext sc = 
               new ServiceContext(sasContextId, 
                                  codec.encode_value(threadLocal.sasReply));
            ri.add_reply_service_context(sc, true);
         }
         catch (InvalidTypeForEncoding e)
         {
            throw new MARSHAL("Unexpected exception: " + e);
         }
      }
   }
   
   public void send_other(ServerRequestInfo ri) 
   {
      // Do nothing. According to the SAS spec, LOCATION_FORWARD reply
      // carries no SAS message.
   }
}
