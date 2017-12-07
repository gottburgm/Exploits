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
package org.jboss.ha.hasessionstate.server;

import org.jboss.ha.hasessionstate.interfaces.PackagedSession;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *   Default implementation of PackagedSession
 *
 *   @see PackagedSession
 *   @see HASessionStateImpl
 *   @author sacha.labourey@cogito-info.ch
 *   @version $Revision: 81001 $
 */
public class PackagedSessionImpl implements PackagedSession
{
   /** The serialVersionUID
    * @since 1.1.4.1
    */ 
   private static final long serialVersionUID = 4162160242862877223L;

   private Serializable key;
   
   private volatile byte[] state;
   private volatile long versionId;
   private volatile String owner;
   
   private transient volatile long lastModificationTimeInVM;
   private transient Lock lock;
   
   public PackagedSessionImpl(Serializable key, byte[] state, String owner)
   {
      this.key = key;
      this.setState(state);
      this.owner = owner;
      this.lock = new ReentrantLock();
   }
   
   public byte[] getState()
   {
      return this.state;
   }
   
   public boolean setState(byte[] state)
   {
      this.lastModificationTimeInVM = System.currentTimeMillis();
      if (isStateIdentical(state)) return true;

      this.state = state;
      this.versionId++;
      return false;
   }
   
   public boolean isStateIdentical(byte[] state)
   {
      return java.util.Arrays.equals(state, this.state);
   }
   
   public void update(PackagedSession clone)
   {
      this.state = clone.getState().clone();
      this.versionId = clone.getVersion();
      this.owner = clone.getOwner();    
      this.lastModificationTimeInVM = System.currentTimeMillis();
   }
   
   public String getOwner()
   {
      return this.owner;
   }
   
   public void setOwner(String owner)
   {
      this.owner = owner;
   }
   
   public long getVersion()
   {
      return this.versionId;
   }
   
   public Serializable getKey()
   {
      return this.key;
   }

   public long unmodifiedExistenceInVM()
   {
      return this.lastModificationTimeInVM;
   }

   /**
    * @see org.jboss.ha.hasessionstate.interfaces.PackagedSession#getLock()
    */
   public Lock getLock()
   {
      return this.lock;
   }
   
   // JBAS-3545 -- have to set the mod time after deserializing
   private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
   {
      in.defaultReadObject();
      this.lastModificationTimeInVM = System.currentTimeMillis();
      this.lock = new ReentrantLock();
   }
}
