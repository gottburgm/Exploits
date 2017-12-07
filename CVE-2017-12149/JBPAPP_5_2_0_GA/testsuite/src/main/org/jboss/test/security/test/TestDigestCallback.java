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
package org.jboss.test.security.test;

import java.security.MessageDigest;
import java.util.Map;

import org.jboss.crypto.digest.DigestCallback;

/** A test implementation of the DigestCallback which includes the digest.preSalt
 * and digest.postSalt strings into the MessageDigest
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class TestDigestCallback
   implements DigestCallback
{
   private Map options;

   public void init(Map options)
   {
      this.options = options;
      if( options.get("javax.security.auth.login.name") == null )
         throw new SecurityException("Failed to find javax.security.auth.login.name");
      if( options.get("javax.security.auth.login.password") == null )
         throw new SecurityException("Failed to find javax.security.auth.login.password");
   }


   public void preDigest(MessageDigest digest)
   {
      String salt = (String) options.get("digest.preSalt");
      digest.update(salt.getBytes());
   }

   public void postDigest(MessageDigest digest)
   {
      String salt = (String) options.get("digest.postSalt");
      digest.update(salt.getBytes());
   }
}
