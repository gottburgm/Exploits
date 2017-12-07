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
package org.jboss.test.security.interceptors;

import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.util.Iterator;
import java.util.Set;
import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.security.auth.Subject;

import org.jboss.logging.Logger;

import org.jboss.invocation.Invocation;
import org.jboss.proxy.Interceptor;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.srp.SRPParameters;

/** A client side interceptor that encrypts

@author Scott.Stark@jboss.org
@version $Revision: 81036 $
*/
public class ClientEncryptionInterceptor
   extends Interceptor
{
   /** The is initialized the first time */
   private Cipher encryptCipher;
   private Cipher decryptCipher;
   private Logger log = Logger.getLogger(ClientEncryptionInterceptor.class);

   /** Creates a new instance of EncryptionInterceptor */
   public ClientEncryptionInterceptor()
   {
   }
   
   public Object invoke(Invocation mi) throws Throwable
   {
      if( encryptCipher == null )
      {
         Subject subject = SecurityAssociation.getSubject();
         initCipher(subject);
      }

      log.debug("invoke mi="+mi.getMethod());
      // Check for arguments to encrypt
      Object[] args = mi.getArguments();
      int length = args != null ? args.length : 0;
      for(int a = 0; a < length; a ++)
      {
         if( (args[a] instanceof Serializable) == false )
            continue;
         Serializable arg = (Serializable) args[a];
         SealedObject sarg = new SealedObject(arg, encryptCipher);
         args[a] = sarg;
         log.debug(" Sealed arg("+a+"): "+arg);
      }

      Interceptor next = getNext();
      Object value = next.invoke(mi);
      if( value instanceof SealedObject )
      {
         SealedObject svalue = (SealedObject) value;
         value = svalue.getObject(decryptCipher);
      }
      return value;
   }

   private void initCipher(Subject subject) throws GeneralSecurityException
   {
      Set credentials = subject.getPrivateCredentials(SecretKey.class);
      Iterator iter = credentials.iterator();
      SecretKey key = null;
      while( iter.hasNext() )
      {
         key = (SecretKey) iter.next();
      }
      if( key == null )
      {
         System.out.println("Subject: "+subject);
         throw new GeneralSecurityException("Failed to find SecretKey in Subject.PrivateCredentials");
      }

      credentials = subject.getPrivateCredentials(SRPParameters.class);
      iter = credentials.iterator();
      SRPParameters params = null;
      while( iter.hasNext() )
      {
         params = (SRPParameters) iter.next();
      }
      if( params == null )
         throw new GeneralSecurityException("Failed to find SRPParameters in Subject.PrivateCredentials");

      encryptCipher = Cipher.getInstance(key.getAlgorithm());
      encryptCipher.init(Cipher.ENCRYPT_MODE, key);
      decryptCipher = Cipher.getInstance(key.getAlgorithm());
      decryptCipher.init(Cipher.DECRYPT_MODE, key);         
   }
}
