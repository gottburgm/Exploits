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

import org.jboss.ha.hasessionstate.interfaces.PackagedSession;
import org.jboss.ha.framework.interfaces.HAPartition;

/**
 *   Interface for services providing clustered state session availability
 *
 *   @see org.jboss.ha.hasessionstate.server.HASessionStateImpl
 *
 *   @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>
 *   @version $Revision: 81001 $
 *
 * <p><b>Revisions:</b><br>
 */

public interface HASessionState
{   
   
   public void init () throws Exception;
   public void start () throws Exception;
   
   /**
    * Listener used to indicate that a session's state has changed.
    * Information that can be used to clean a cache for example. It indicates that
    * another node has touched a session that we previously owned.
    *
    */   
   public interface HASessionStateListener
   {
      public void sessionExternallyModified (PackagedSession session);
   }
   /**
    * Subscribe to receive notifications when objects gets modified on another node.
    *
    */   
   public void subscribe (String appName, HASessionStateListener listener);
   public void unsubscribe (String appName, HASessionStateListener listener);
   
   public HAPartition getCurrentHAPartition ();
   
   /**
    * Return the name of this node as used in the computations
    */   
   public String getNodeName ();
   
   /**
    * Share a new session state in the sub-partition of this cluster
    * @param appName Application name for which is state is shared
    * @param keyId Key identifier of the state
    */   
   public void createSession (String appName, Object keyId);
   /**
    * Modifies a state already shared
    * @param appName Application name hosting this state
    * @param keyId Key identifier of the state to modify
    * @param state New state
    */   
   public void setState (String appName, Object keyId, byte[] state) 
      throws java.rmi.RemoteException;
   
   /**
    * Get a particular state
    * @param appName Application hosting the state
    * @param keyId Key identifier of the state
    * @return The state value
    */   
   public PackagedSession getState (String appName, Object keyId);
   /**
    * Get a state and, if it is not already the case, takes its ownership (a state is
    * always owned by a node)
    * @param appName Application hosting the state
    * @param keyId Key identifier of the state
    * @throws java.rmi.RemoteException Thrown if an exception occurs while getting the ownership of the state
    * @return The state value
    */   
   public PackagedSession getStateWithOwnership (String appName, Object keyId) throws java.rmi.RemoteException;
   
   /**
    * Take ownership of a state. Each state is owned by a node.
    * @param appName Application hosting the state
    * @param keyId Key identifier of the state
    * @throws java.rmi.RemoteException Trown if a communication exception occurs while asking other node to get the ownership
    */   
   public void takeOwnership (String appName, Object keyId) throws java.rmi.RemoteException;
      
   /**
    * Remove a session from the sub-partition
    * @param appName Application hosting the state
    * @param keyId Key identifier of the state
    */   
   public void removeSession (String appName, Object keyId);
   
}

