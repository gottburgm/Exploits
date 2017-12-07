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
package org.jboss.jmx.adaptor.html;

import java.security.SecureRandom;

import javax.servlet.http.HttpSession;
import org.jboss.logging.Logger;
import org.jboss.util.Base64;

/**
 * Utility class for CSRF token management.
 * @author pskopek@redhat.com
 * 
 */
public class CSRFUtil {

   public static final String CSRF_TOKEN = "CSRFToken";

   private static Logger log = Logger.getLogger(CSRFUtil.class);
   
   /**
    * Create a CSFR token for given session. 
    * @param session - param not used in present, but might be useful if we decide to put some more randomness to the generated token. 
    * @return
    */
   public static String generateCSRFToken(HttpSession session) {
      SecureRandom rand = new SecureRandom();
      byte bytes[] = new byte[32];
      rand.nextBytes(bytes);
      
      String token = Base64.encodeBytes(bytes);
      if (log.isTraceEnabled()) {
         log.trace(CSRFUtil.CSRF_TOKEN + "=" + token);
      }
      
      return token;
   }

   /**
    * Validates whether given token is part of current session.
    * @param session - current session for a request
    * @param token
    * @return
    */
   public static boolean isCSRFTokenValid(HttpSession session, String token) {
      if (log.isTraceEnabled()) {
         log.trace("Validating CSRF token=" + token);
      }
      String sessionToken = getCSRFToken(session);
      if (sessionToken != null) {
         return sessionToken.equals(token);
      }
      else {
         return false;
      }
   }
   
   /**
    * Get CSRF token out of given HttpSession.
    * @param session
    * @return
    */
   public static String getCSRFToken(HttpSession session) {
      return (String)session.getAttribute(CSRFUtil.CSRF_TOKEN);
   }
}
