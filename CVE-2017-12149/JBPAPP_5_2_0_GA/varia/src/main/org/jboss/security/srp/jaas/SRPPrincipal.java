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
package org.jboss.security.srp.jaas;

import org.jboss.security.SimplePrincipal;

/** An extension of SimplePrincipal that adds the SRP session ID
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81038 $
 */
public class SRPPrincipal extends SimplePrincipal
{
   /** Serial Version */
   static final long serialVersionUID = -7123071794402068344L;

   /** The SRP session ID, 0 == no session */
   private int sessionID;

   /** Creates a new instance of SRPPrincipal */
   public SRPPrincipal(String name)
   {
      this(name, 0);
   }

   public SRPPrincipal(String name, int sessionID)
   {
      super(name);
      this.sessionID = sessionID;
   }

   public SRPPrincipal(String name, Integer sessionID)
   {
      super(name);
      this.sessionID = sessionID != null ? sessionID.intValue() : 0;
   }

   public int getSessionID()
   {
      return sessionID;
   }

   /**
    * Override to include the sessionID in the equality check
    * @param obj a SRPPrincipal
    * @return true of name and sessionID are equal
    */
   public boolean equals(Object obj)
   {
      if (!(obj instanceof SRPPrincipal))
         return false;
      SRPPrincipal p = (SRPPrincipal) obj;
      return getName().equals(p.getName()) && sessionID == p.getSessionID();
   }

   /**
    * Override to include sessionID in the hash
    * @return name.hashCode() + sessionID
    */
   public int hashCode()
   {
      return getName().hashCode() + sessionID;
   }
}
