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
package org.jboss.web.tomcat.service.session;

import java.util.Random;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.jboss.logging.Logger;

/**
 * Unique session id generator
 *
 * @author Ben Wang
 */
public class SessionIDGenerator
{
   protected final static int SESSION_ID_BYTES = 16; // We want 16 Bytes for the session-id
   protected final static String SESSION_ID_HASH_ALGORITHM = "MD5";
   protected final static String SESSION_ID_RANDOM_ALGORITHM = "SHA1PRNG";
   protected final static String SESSION_ID_RANDOM_ALGORITHM_ALT = "IBMSecureRandom";
   protected Logger log = Logger.getLogger(SessionIDGenerator.class);

   protected MessageDigest digest = null;
   protected Random random = null;
   protected static final SessionIDGenerator s_ = new SessionIDGenerator();
   
   protected String sessionIdAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+-_";

   public static SessionIDGenerator getInstance()
   {
      return s_;
   }

   /**
    * The SessionIdAlphabet is the set of characters used to create a session Id
    */
   public void setSessionIdAlphabet(String sessionIdAlphabet) 
   {
      if (sessionIdAlphabet.length() != 65) {
         throw new IllegalArgumentException("SessionIdAlphabet must be exactly 65 characters long");
      }

      checkDuplicateChars(sessionIdAlphabet);

      this.sessionIdAlphabet = sessionIdAlphabet;
   }

   protected void checkDuplicateChars(String sessionIdAlphabet) {
      char[] alphabet = sessionIdAlphabet.toCharArray();
      for (int i=0; i < alphabet.length; i++) {
          if (!uniqueChar(alphabet[i], sessionIdAlphabet)) {
              throw new IllegalArgumentException("All chars in SessionIdAlphabet must be unique");
          }
      }
   }
      
   // does a character appear in the String once and only once?
   protected boolean uniqueChar(char c, String s) {
       int firstIndex = s.indexOf(c);
       if (firstIndex == -1) return false;
       return s.indexOf(c, firstIndex + 1) == -1;
   }

   /**
    * The SessionIdAlphabet is the set of characters used to create a session Id
    */
   public String getSessionIdAlphabet() {
      return this.sessionIdAlphabet;
   }
   
   public synchronized String getSessionId()
   {
      String id = generateSessionId();
      if (log.isTraceEnabled())
         log.trace("getSessionId() called: " + id);
      return id;
   }


   /**
    * Generate a session-id that is not guessable
    *
    * @return generated session-id
    */
   protected synchronized String generateSessionId()
   {
      if (this.digest == null)
      {
         this.digest = getDigest();
      }

      if (this.random == null)
      {
         this.random = getRandom();
      }

      byte[] bytes = new byte[SESSION_ID_BYTES];

      // get random bytes
      this.random.nextBytes(bytes);

      // Hash the random bytes
      bytes = this.digest.digest(bytes);

      // Render the result as a String of hexadecimal digits
      return encode(bytes);
   }

   /**
    * Encode the bytes into a String with a slightly modified Base64-algorithm
    * This code was written by Kevin Kelley <kelley@ruralnet.net>
    * and adapted by Thomas Peuss <jboss@peuss.de>
    *
    * @param data The bytes you want to encode
    * @return the encoded String
    */
   protected String encode(byte[] data)
   {
      char[] out = new char[((data.length + 2) / 3) * 4];
      char[] alphabet = this.sessionIdAlphabet.toCharArray();

      //
      // 3 bytes encode to 4 chars.  Output is always an even
      // multiple of 4 characters.
      //
      for (int i = 0, index = 0; i < data.length; i += 3, index += 4)
      {
         boolean quad = false;
         boolean trip = false;

         int val = (0xFF & (int) data[i]);
         val <<= 8;
         if ((i + 1) < data.length)
         {
            val |= (0xFF & (int) data[i + 1]);
            trip = true;
         }
         val <<= 8;
         if ((i + 2) < data.length)
         {
            val |= (0xFF & (int) data[i + 2]);
            quad = true;
         }
         out[index + 3] = alphabet[(quad ? (val & 0x3F) : 64)];
         val >>= 6;
         out[index + 2] = alphabet[(trip ? (val & 0x3F) : 64)];
         val >>= 6;
         out[index + 1] = alphabet[val & 0x3F];
         val >>= 6;
         out[index + 0] = alphabet[val & 0x3F];
      }
      return new String(out);
   }

   /**
    * get a random-number generator
    *
    * @return a random-number generator
    */
   protected synchronized Random getRandom()
   {
      long seed;
      Random random = null;

      // Mix up the seed a bit
      seed = System.currentTimeMillis();
      seed ^= Runtime.getRuntime().freeMemory();

      try
      {
         random = SecureRandom.getInstance(SESSION_ID_RANDOM_ALGORITHM);
      }
      catch (NoSuchAlgorithmException e)
      {
         try
         {
            random = SecureRandom.getInstance(SESSION_ID_RANDOM_ALGORITHM_ALT);
         }
         catch (NoSuchAlgorithmException e_alt)
         {
            log.error("Could not generate SecureRandom for session-id randomness", e);
            log.error("Could not generate SecureRandom for session-id randomness", e_alt);
            return null;
         }
      }

      // set the generated seed for this PRNG
      random.setSeed(seed);

      return random;
   }

   /**
    * get a MessageDigest hash-generator
    *
    * @return a hash generator
    */
   protected synchronized MessageDigest getDigest()
   {
      MessageDigest digest = null;

      try
      {
         digest = MessageDigest.getInstance(SESSION_ID_HASH_ALGORITHM);
      }
      catch (NoSuchAlgorithmException e)
      {
         log.error("Could not generate MessageDigest for session-id hashing", e);
         return null;
      }

      return digest;
   }

}
