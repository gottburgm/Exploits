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
package org.jboss.security.auth.spi;

import java.util.Map;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.security.auth.callback.Callback;

import org.jboss.crypto.digest.DigestCallback;
import org.jboss.security.auth.callback.MapCallback;

/**
 An implementation of the DigestCallback that support the http digest auth as
 described in RFC2617 (http://www.ietf.org/rfc/rfc2617.txt).

 3.2.2.1 Request-Digest

 If the "qop" value is "auth" or "auth-int":

 request-digest  = <"> < KD ( H(A1),     unq(nonce-value) ":" nc-value ":"
 unq(cnonce-value) ":" unq(qop-value) ":" H(A2) ) <">

 If the "qop" directive is not present (this construction is for compatibility
 with RFC 2069):

 request-digest  = <"> < KD ( H(A1), unq(nonce-value) ":" H(A2) ) > <">

 See below for the definitions for A1 and A2.

 3.2.2.2 A1

 If the "algorithm" directive's value is "MD5" or is unspecified, then A1 is:

 A1       = unq(username-value) ":" unq(realm-value) ":" passwd

 where

 passwd   = < user's password >

 If the "algorithm" directive's value is "MD5-sess", then A1 is calculated only
 once - on the first request by the client following receipt of a
 WWW-Authenticate challenge from the server.  It uses the server nonce from that
 challenge, and the first client nonce value to construct A1 as follows:

 A1       = H( unq(username-value) ":" unq(realm-value) ":" passwd ) ":"
 unq(nonce-value) ":" unq(cnonce-value)

 This creates a 'session key' for the authentication of subsequent requests and
 responses which is different for each "authentication session", thus limiting
 the amount of material hashed with any one key.  (Note: see further discussion
 of the authentication session in section 3.3.) Because the server need only use
 the hash of the user credentials in order to create the A1 value, this
 construction could be used in conjunction with a third party authentication
 service so that the web server would not need the actual password value.  The
 specification of such a protocol is beyond the scope of this specification.

 3.2.2.3 A2

 If the "qop" directive's value is "auth" or is unspecified, then A2 is:

 A2       = Method ":" digest-uri-value

 If the "qop" value is "auth-int", then A2 is:

 A2       = Method ":" digest-uri-value ":" H(entity-body)

 3.2.2.4 Directive values and quoted-string

 Note that the value of many of the directives, such as "username- value", are
 defined as a "quoted-string". However, the "unq" notation indicates that
 surrounding quotation marks are removed in forming the string A1. Thus if the
 Authorization header includes the fields

 username="Mufasa", realm=myhost@testrealm.com

 and the user Mufasa has password "Circle Of Life" then H(A1) would be
 H(Mufasa:myhost@testrealm.com:Circle Of Life) with no quotation marks in the
 digested string.

 No white space is allowed in any of the strings to which the digest function H()
 is applied unless that white space exists in the quoted strings or entity body
 whose contents make up the string to be digested. For example, the string A1
 illustrated above must be

 Mufasa:myhost@testrealm.com:Circle Of Life

 with no white space on either side of the colons, but with the white space
 between the words used in the password value.  Likewise, the other strings
 digested by H() must not have white space on either side of the colons which
 delimit their fields unless that white space was in the quoted strings or entity
 body being digested.

 Also note that if integrity protection is applied (qop=auth-int), the
 H(entity-body) is the hash of the entity body, not the message body - it is
 computed before any transfer encoding is applied by the sender and after it has
 been removed by the recipient. Note that this includes multipart boundaries and
 embedded headers in each part of any multipart content-type.

 @author Scott.Stark@jboss.org
 @version $Revision: 85945 $
 */
public class RFC2617Digest implements DigestCallback
{
   /**
    String which can enable users to know which username and password to use, in
    case they might have different ones for different servers.
    */
   public static final String REALM = "realm";

   /**
    The user's name in the specified realm.
    */
   public static final String USERNAME = "username";

   /**
    The URI from Request-URI of the Request-Line; duplicated here because proxies
    are allowed to change the Request-Line in transit.
    */
   public static final String DIGEST_URI = "digest-uri";

   /**
    A server-specified data string which MUST be different each time a
    digest-challenge is sent as part of initial authentication.  It is
    recommended that this string be base64 or hexadecimal data. Note that since
    the string is passed as a quoted string, the double-quote character is not
    allowed unless escaped (see section 7.2). The contents of the nonce are
    implementation dependent. The

    security of the implementation depends on a good choice. It is RECOMMENDED
    that it contain at least 64 bits of entropy. The nonce is opaque to the
    client. This directive is required and MUST appear exactly once; if not
    present, or if multiple instances are present, the client should abort the
    authentication exchange.
    */
   public static final String NONCE = "nonce";

   /**
    This MUST be specified if a qop directive is sent (see above), and MUST NOT
    be specified if the server did not send a qop directive in the
    WWW-Authenticate header field.  The cnonce-value is an opaque quoted string
    value provided by the client and used by both client and server to avoid
    chosen plaintext attacks, to provide mutual authentication, and to provide
    some message integrity protection. See the descriptions below of the
    calculation of the response- digest and request-digest values.
    */
   public static final String CNONCE = "cnonce";

   /**
    This MUST be specified if a qop directive is sent (see above), and MUST NOT
    be specified if the server did not send a qop directive in the
    WWW-Authenticate header field.  The nc-value is the hexadecimal count of the
    number of requests (including the current request) that the client has sent
    with the nonce value in this request.  For example, in the first request sent
    in response to a given nonce value, the client sends "nc=00000001".  The
    purpose of this directive is to allow the server to detect request replays by
    maintaining its own copy of this count - if the same nc-value is seen twice,
    then the request is a replay.   See the description below of the construction
    of the request-digest value.
    */
   public static final String NONCE_COUNT = "nc";

   /**
    Indicates what "quality of protection" the client has applied to the message.
    If present, its value MUST be one of the alternatives the server indicated it
    supports in the WWW-Authenticate header. These values affect the computation
    of the request-digest. Note that this is a single token, not a quoted list of
    alternatives as in WWW- Authenticate.  This directive is optional in order to
    preserve backward compatibility with a minimal implementation of RFC 2069
    [6], but SHOULD be used if the server indicated that qop is supported by
    providing a qop directive in the WWW-Authenticate header field.
    */
   public static final String QOP = "qop";

   /**
    A string indicating a pair of algorithms used to produce the digest
     and a checksum. If this is not present it is assumed to be "MD5".
     If the algorithm is not understood, the challenge should be ignored
     (and a different one used, if there is more than one).

     In this document the string obtained by applying the digest
     algorithm to the data "data" with secret "secret" will be denoted
     by KD(secret, data), and the string obtained by applying the
     checksum algorithm to the data "data" will be denoted H(data). The
     notation unq(X) means the value of the quoted-string X without the
     surrounding quotes.
    */
   public static final String ALGORITHM = "algorithm";

   /**
    This directive allows for future extensions. Any unrecognized directive MUST
    be ignored.
    */
   public static final String AUTH_PARAM = "auth-param";

   /**
    The http method type
    */
   public static final String METHOD = "method";

   /**
    An explicit A2 digest
    */
   public static final String A2HASH = "a2hash";

   /**
    The ASCII printable characters the MD5 digest maps to
    */
   private static char[] MD5_HEX = "0123456789abcdef".toCharArray();

   private MapCallback info;

   private String username;

   private String password;

   private boolean passwordIsA1Hash;

   String rfc2617;

   public void init(Map options)
   {
      username = (String) options.get("javax.security.auth.login.name");
      password = (String) options.get("javax.security.auth.login.password");
      String flag = (String) options.get("passwordIsA1Hash");
      if (flag != null)
         passwordIsA1Hash = Boolean.valueOf(flag).booleanValue();

      // Ask for MapCallback to obtain the digest parameters
      info = new MapCallback();
      Callback[] callbacks = {info};
      options.put("callbacks", callbacks);
   }

   public void preDigest(MessageDigest digest)
   {
   }

   public void postDigest(MessageDigest digest)
   {
      String qop = (String) info.getInfo(QOP);
      String realm = (String) info.getInfo(REALM);
      String algorithm = (String) info.getInfo(ALGORITHM);
      String nonce = (String) info.getInfo(NONCE);
      String cnonce = (String) info.getInfo(CNONCE);
      String method = (String) info.getInfo(METHOD);
      String nc = (String) info.getInfo(NONCE_COUNT);
      String digestURI = (String) info.getInfo(DIGEST_URI);

      if (algorithm == null)
         algorithm = digest.getAlgorithm();
      // This replaces the existing hash, it does not add to it
      digest.reset();

      String hA1 = null;
      // 3.2.2.2 A1
      if (algorithm == null || algorithm.equals("MD5"))
      {
         if (passwordIsA1Hash)
            hA1 = password;
         else
         {
            String A1 = username + ":" + realm + ":" + password;
            hA1 = H(A1, digest);
         }
      }
      else if (algorithm.equals("MD5-sess"))
      {
         if (passwordIsA1Hash)
         {
            hA1 = password + ":" + nonce + ":" + cnonce;
         }
         else
         {
            String A1 = username + ":" + realm + ":" + password;
            hA1 = H(A1, digest) + ":" + nonce + ":" + cnonce;
         }
      }
      else
      {
         throw new IllegalArgumentException("Unsupported algorigthm: " + algorithm);
      }

      // 3.2.2.3 A2. First check to see if the A2 hash has been precomputed
      String hA2 = (String) info.getInfo(A2HASH);
      if (hA2 == null)
      {
         // No, compute it based on qop
         String A2 = null;
         if (qop == null || qop.equals("auth"))
         {
            A2 = method + ":" + digestURI;
         }
         else
         {
            throw new IllegalArgumentException("Unsupported qop=" + qop);
         }
         hA2 = H(A2, digest);
      }

      // 3.2.2.1 Request-Digest
      if (qop == null)
      {
         String extra = nonce + ":" + hA2;
         KD(hA1, extra, digest);
      }
      else if (qop.equals("auth"))
      {
         String extra = nonce +
            ":" + nc +
            ":" + cnonce +
            ":" + qop +
            ":" + hA2;
         KD(hA1, extra, digest);
      }
   }

   public String getInfoDigest(MessageDigest digest)
   {
      if (rfc2617 == null)
      {
         byte[] data = digest.digest();
         rfc2617 = cvtHex(data);
      }
      return rfc2617;
   }

   static private String H(String data, MessageDigest digest)
   {
      digest.reset();
      byte[] x = digest.digest(data.getBytes());
      return cvtHex(x);
   }

   static private void KD(String secret, String data, MessageDigest digest)
   {
      String x = secret + ":" + data;
      digest.reset();
      digest.update(x.getBytes());
   }

   /**
    3.1.3 Representation of digest values

    An optional header allows the server to specify the algorithm used to create
    the checksum or digest. By default the MD5 algorithm is used and that is the
    only algorithm described in this document.

    For the purposes of this document, an MD5 digest of 128 bits is represented
    as 32 ASCII printable characters. The bits in the 128 bit digest are
    converted from most significant to least significant bit, four bits at a time
    to their ASCII presentation as follows. Each four bits is represented by its
    familiar hexadecimal notation from the characters 0123456789abcdef. That is,
    binary 0000 getInfos represented by the character '0', 0001, by '1', and so on up
    to the representation of 1111 as 'f'.
    
    @param data - the raw MD5 hash data
    @return the encoded MD5 representation
    */
   static String cvtHex(byte[] data)
   {
      char[] hash = new char[32];
      for (int i = 0; i < 16; i++)
      {
         int j = (data[i] >> 4) & 0xf;
         hash[i * 2] = MD5_HEX[j];
         j = data[i] & 0xf;
         hash[i * 2 + 1] = MD5_HEX[j];
      }
      return new String(hash);
   }

   /**
    Compute the 
    @param args
    */
   public static void main(String[] args) throws NoSuchAlgorithmException
   {
      if (args.length != 3)
      {
         System.err.println("Usage: RFC2617Digest username realm password");
         System.err.println(" - username : the username");
         System.err.println(" - realm : the web app realm name");
         System.err.println(" - password : the plain text password");
         System.exit(1);
      }
      String username = args[0];
      String realm = args[1];
      String password = args[2];
      String A1 = username + ":" + realm + ":" + password;
      MessageDigest digest = MessageDigest.getInstance("MD5");
      String hA1 = H(A1, digest);
      System.out.println("RFC2617 A1 hash: " + hA1);
   }
}
