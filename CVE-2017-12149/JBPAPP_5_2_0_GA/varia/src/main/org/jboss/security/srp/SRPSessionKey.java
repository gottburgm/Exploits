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

import java.io.Serializable;

/* An encapsulation of an SRP username and session id.
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81038 $
 */
public class SRPSessionKey implements Serializable
{
   private static final long serialVersionUID = -7783783206948014409L;
   public static final Integer NO_SESSION_ID = new Integer(0);
   private String username;
   private int sessionID;

   public SRPSessionKey(String username)
   {
      this(username, NO_SESSION_ID);
   }
   public SRPSessionKey(String username, int sessionID)
   {
      this.username = username;
      this.sessionID = sessionID;
   }
   public SRPSessionKey(String username, Integer sessionID)
   {
      this.username = username;
      if( sessionID != null )
         this.sessionID = sessionID.intValue();
   }

   public boolean equals(Object obj)
   {
      SRPSessionKey key = (SRPSessionKey) obj;
      return this.username.equals(key.username) && this.sessionID == key.sessionID;
   }

   public int hashCode()
   {
      return this.username.hashCode() + this.sessionID;
   }

   public int getSessionID()
   {
      return sessionID;
   }

   public String getUsername()
   {
      return username;
   }

   public String toString()
   {
      return "{username="+username+", sessionID="+sessionID+"}";
   }
}
