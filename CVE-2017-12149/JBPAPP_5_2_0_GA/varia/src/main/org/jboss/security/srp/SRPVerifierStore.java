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
package org.jboss.security.srp;

import java.io.IOException;
import java.io.Serializable;
import java.io.ObjectStreamField;
import java.security.KeyException;

/** An interface describing the requirements of a password verifier store.
This is an abstraction that allows the <username, verifier, salt> information
needed by the server to be plugged in from various sources. E.g., LDAP
servers, databases, files, etc.

 @author Scott.Stark@jboss.org
 @version $Revision: 81038 $
*/
public interface SRPVerifierStore
{
   public static class VerifierInfo implements Serializable
   {
      /** The serial version UID @since 1.2.4.1 */
      private static final long serialVersionUID = 7420301687504271098L;
      private static final ObjectStreamField[] serialPersistentFields = {
         new ObjectStreamField("username", String.class),
         new ObjectStreamField("verifier", byte[].class),
         new ObjectStreamField("salt", byte[].class),
         new ObjectStreamField("g", byte[].class),
         new ObjectStreamField("N", byte[].class),
         new ObjectStreamField("hashAlgorithm", String.class),
         new ObjectStreamField("cipherAlgorithm", String.class),
         new ObjectStreamField("cipherIV", byte[].class)
      };

      /** The username the information applies to. Perhaps redundant but it
       * makes the object self contained.
       * @serialField username String username
       */
      public String username;
      /** The SRP password verifier hash
       * @serialField verifier byte[] password verifier
       */
      public byte[] verifier;
      /** The random password salt originally used to verify the password
       * @serialField salt originally used to verify the password
       */
      public byte[] salt;
      /** The SRP algorithm primitive generator
       * @serialField g primitive generator
       */
      public byte[] g;
      /** The algorithm safe-prime modulus
       * @serialField N safe-prime modulus
       */
      public byte[] N;
      /** The algorithm to hash the session key to produce K. To be consistent
       with the RFC2945 description this must be SHA_Interleave as implemented
       by the JBossSX security provider. For compatibility with earlier JBossSX
       SRP releases the algorithm must be SHA_ReverseInterleave. This name is
       passed to java.security.MessageDigest.getInstance().
       * @serialField hashAlgorithm algorithm to hash the session key
       * @since 1.2.4.2
       */
      public String hashAlgorithm;
      /** The algorithm to use for any encryption of data.
       * @serialField cipherAlgorithm algorithm to use for any encryption
       * @since 1.2.4.2
       */
      public String cipherAlgorithm;
      /** The initialization vector to use for any encryption of data.
       * @serialField cipherIV initialization vector to use for any encryption
       * @since 1.6
       */
      public byte[] cipherIV;
   }

    /** Get the indicated user's password verifier information.
     */
    public VerifierInfo getUserVerifier(String username)
      throws KeyException, IOException;
    /** Set the indicated users' password verifier information. This is equivalent
     to changing a user's password and should generally invalidate any existing
     SRP sessions and caches.
     */
    public void setUserVerifier(String username, VerifierInfo info)
      throws IOException;

   /** Verify an optional auxillary challenge sent from the client to the server. The
    * auxChallenge object will have been decrypted if it was sent encrypted from the
    * client. An example of a auxillary challenge would be the validation of a hardware
    * token (SafeWord, SecureID, iButton) that the server validates to further strengthen
    * the SRP password exchange.
    */
   public void verifyUserChallenge(String username, Object auxChallenge)
         throws SecurityException;
}
