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
package org.jboss.web.tomcat.security.jaspi.modules;

import java.io.IOException;
import java.security.Principal;

import javax.security.auth.Subject;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.util.Base64;
import org.apache.catalina.util.StringManager;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.CharChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import org.jboss.logging.Logger;

/**
 * Server auth module for Basic authentication
 * @author Anil.Saldhana@redhat.com
 * @since Oct 7, 2008
 */
public class HTTPBasicServerAuthModule extends TomcatServerAuthModule
{
   private static Logger log = Logger.getLogger(HTTPBasicServerAuthModule.class);

   protected Context context; 
   
   protected boolean cache = false;
   
   protected static final StringManager sm =
      StringManager.getManager(Constants.Package);
   
   /**
    * Authenticate bytes.
    */
   public static final byte[] AUTHENTICATE_BYTES = {
       (byte) 'W',
       (byte) 'W',
       (byte) 'W',
       (byte) '-',
       (byte) 'A',
       (byte) 'u',
       (byte) 't',
       (byte) 'h',
       (byte) 'e',
       (byte) 'n',
       (byte) 't',
       (byte) 'i',
       (byte) 'c',
       (byte) 'a',
       (byte) 't',
       (byte) 'e'
   };

   
   /**
    * The number of random bytes to include when generating a
    * session identifier.
    */
   protected static final int SESSION_ID_BYTES = 16;

   protected String delgatingLoginContextName = null;
   
   public HTTPBasicServerAuthModule()
   { 
   }
 
   public HTTPBasicServerAuthModule(String delgatingLoginContextName)
   {
      super();
      this.delgatingLoginContextName = delgatingLoginContextName;
   }

   public AuthStatus secureResponse(MessageInfo messageInfo, Subject serviceSubject)
   throws AuthException
   {
      throw new RuntimeException("Not Applicable");
   }

   public AuthStatus validateRequest(MessageInfo messageInfo, Subject clientSubject, 
         Subject serviceSubject) throws AuthException
   { 
      Request request = (Request) messageInfo.getRequestMessage();
      Response response = (Response) messageInfo.getResponseMessage();
     
      Principal principal;
      context = request.getContext();
      LoginConfig config = context.getLoginConfig(); 
      
      // Validate any credentials already included with this request
      String username = null;
      String password = null;

      MessageBytes authorization = 
          request.getCoyoteRequest().getMimeHeaders()
          .getValue("authorization");
      
      if (authorization != null) {
          authorization.toBytes();
          ByteChunk authorizationBC = authorization.getByteChunk();
          if (authorizationBC.startsWithIgnoreCase("basic ", 0)) {
              authorizationBC.setOffset(authorizationBC.getOffset() + 6);
              // FIXME: Add trimming
              // authorizationBC.trim();
              
              CharChunk authorizationCC = authorization.getCharChunk();
              Base64.decode(authorizationBC, authorizationCC);
              
              // Get username and password
              int colon = authorizationCC.indexOf(':');
              if (colon < 0) {
                  username = authorizationCC.toString();
              } else {
                  char[] buf = authorizationCC.getBuffer();
                  username = new String(buf, 0, colon);
                  password = new String(buf, colon + 1, 
                          authorizationCC.getEnd() - colon - 1);
              }
              
              authorizationBC.setOffset(authorizationBC.getOffset() - 6);
          }

          principal = context.getRealm().authenticate(username, password);
          if (principal != null) {
             registerWithCallbackHandler(principal, username, password);
             
              /*register(request, response, principal, Constants.BASIC_METHOD,
                       username, password);*/
             return AuthStatus.SUCCESS; 
          }
      } 

      // Send an "unauthorized" response and an appropriate challenge
      MessageBytes authenticate = 
          response.getCoyoteResponse().getMimeHeaders()
          .addValue(AUTHENTICATE_BYTES, 0, AUTHENTICATE_BYTES.length);
      CharChunk authenticateCC = authenticate.getCharChunk();
      try
      {
         authenticateCC.append("Basic realm=\"");
         if (config.getRealmName() == null) {
            authenticateCC.append(request.getServerName());
            authenticateCC.append(':');
            authenticateCC.append(Integer.toString(request.getServerPort()));
         } else {
            authenticateCC.append(config.getRealmName());
         }
         authenticateCC.append('\"');        
         authenticate.toChars();

         response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      }
      catch (IOException e)
      {
         log.error("IOException ", e); 
      }
      //response.flushBuffer();
      return AuthStatus.FAILURE;  
   } 
}