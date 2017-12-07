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
package org.jboss.ha.hasessionstate.interfaces;


import java.io.Serializable;
import java.util.concurrent.locks.Lock;

/**
 *   Information about a session that is shared by nodes in the same subpartition
 *
 *   @see HASessionState
 *   @see org.jboss.ha.hasessionstate.server.PackagedSessionImpl
 * 
 *   @author sacha.labourey@cogito-info.ch
 *   @version $Revision: 81001 $
 *
 * <p><b>Revisions:</b><br>
 */

public interface PackagedSession extends Serializable
{
   /** The serialVersionUID
    * @since 1.2
    */
   static final long serialVersionUID = 689622988452110553L;
   /*
    * Stored state
    */
   public byte[] getState();
   public boolean setState(byte[] state);
   
   /*
    * Stored state
    */
   public boolean isStateIdentical(byte[] state);
   
   /*
    * Update the state and content of this PackagedSession from the content of another
    * PackagedSession.
    */
   public void update(PackagedSession clone);
   
   /*
    * Owner node of the state
    */
   public String getOwner();
   public void setOwner(String owner);
   
   /*
    * Version number of this state
    */
   public long getVersion();
   
   /*
    * Key identifier associated with this state
    */
   public Serializable getKey();
   
   /*
    * Number of miliseconds since when this state has not been modified in this VM
    */
   public long unmodifiedExistenceInVM();
   
   /**
    * Returns the lock used to prevent concurrent calls on this session.
    * @return a mutex
    */
   public Lock getLock();
}
