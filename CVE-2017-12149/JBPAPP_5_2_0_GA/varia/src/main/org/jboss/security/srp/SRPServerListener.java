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

/** A callback interface for SRP session events.

@author  Scott.Stark@jboss.org
@version $Revision: 81038 $
*/
public interface SRPServerListener
{
   /** Called when a user has successfully completed the SRP handshake and any auxillary
    * challenge verification.
    * @param key, the {username, sessionID} pair
    * @param session, the server SRP session information
    */
   public void verifiedUser(SRPSessionKey key, SRPServerSession session);
   /** Called when a user requests that a session be closed
    *
    * @param key, the {username, sessionID} pair
    */
   public void closedUserSession(SRPSessionKey key);
}
