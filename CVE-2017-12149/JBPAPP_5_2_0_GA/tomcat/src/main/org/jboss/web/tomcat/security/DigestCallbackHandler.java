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
package org.jboss.web.tomcat.security;

import java.io.IOException;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.jboss.security.auth.callback.MapCallback;
import org.jboss.security.auth.spi.RFC2617Digest;

/**
 A CallbackHandler that is used to pass the RFC2617 parameters to the login
 module DigestCallback.

 @author Scott.Stark@jboss.org
 @version $Revision: 81037 $
 */
public class DigestCallbackHandler implements CallbackHandler
{
   private String username;
   private String nonce;
   private String nc;
   private String cnonce;
   private String qop;
   private String realm;
   private String md5a2;

   DigestCallbackHandler(String username, String nonce,
      String nc, String cnonce, String qop, String realm, String md5a2)
   {
      this.username = username;
      this.nonce = nonce;
      this.nc = nc;
      this.cnonce = cnonce;
      this.qop = qop;
      this.realm = realm;
      this.md5a2 = md5a2;
   }

   public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
   {
      boolean foundCallback = false;
      Callback firstUnknown = null;
      int count = callbacks != null ? callbacks.length : 0;
      for(int n = 0; n < count; n ++)
      {
         Callback c = callbacks[n];
         if( c instanceof MapCallback )
         {
            MapCallback mc = (MapCallback) c;
            mc.setInfo(RFC2617Digest.USERNAME, username);
            mc.setInfo(RFC2617Digest.CNONCE, cnonce);
            mc.setInfo(RFC2617Digest.NONCE, nonce);
            mc.setInfo(RFC2617Digest.NONCE_COUNT, nc);
            mc.setInfo(RFC2617Digest.QOP, qop);
            mc.setInfo(RFC2617Digest.REALM, realm);
            mc.setInfo(RFC2617Digest.A2HASH, md5a2);
            foundCallback = true;
         }
         else if( firstUnknown == null )
         {
            firstUnknown = c;
         }
      }
      if( foundCallback == false )
         throw new UnsupportedCallbackException(firstUnknown, "Unrecognized Callback");         
   }
}
