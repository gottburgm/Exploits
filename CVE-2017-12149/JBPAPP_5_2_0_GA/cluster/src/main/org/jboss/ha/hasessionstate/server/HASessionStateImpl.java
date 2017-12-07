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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.Lock;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.ha.hasessionstate.interfaces.HASessionState;
import org.jboss.ha.hasessionstate.interfaces.PackagedSession;
import org.jboss.logging.Logger;
import org.jboss.metadata.ClusterConfigMetaData;
import org.jboss.naming.NonSerializableFactory;

/**
 *   Default implementation of HASessionState
 *
 *   @see org.jboss.ha.hasessionstate.interfaces.HASessionState
 *   @author sacha.labourey@cogito-info.ch
 *   @author <a href="bill@burkecentral.com">Bill Burke</a>
 *   @author Paul Ferraro
 *   @version $Revision: 81001 $
 *
 * <p><b>Revisions:</b><br>
 * <p><b>2002/01/09: billb</b>
 * <ol>
 *   <li>ripped out sub partitioning stuff.  It really belongs as a subclass of HAPartition
 * </ol>
 * 
 */

public class HASessionStateImpl
   implements HASessionState, HAPartition.HAPartitionStateTransfer
{
   private static final long MAX_DELAY_BEFORE_CLEANING_UNRECLAIMED_STATE = 30L * 60L * 1000L; // 30 minutes... should be set externally or use cache settings
   private static final String HA_SESSION_STATE_STATE_TRANSFER = "HASessionStateTransfer";
   
   private static final Class<?>[] SET_OWNERSHIP_TYPES = new Class[] { String.class, Object.class, String.class, Long.class };
   private static final Class<?>[] REMOVE_SESSION_TYPES = new Class[] { String.class, Object.class };
   private static final Class<?>[] SET_STATE_TYPES = new Class[] { String.class, PackagedSession.class };
   
   protected Map<String, ConcurrentMap<Object, PackagedSession>> appSessionMap = new HashMap<String, ConcurrentMap<Object, PackagedSession>>();
   protected ConcurrentMap<String, Set<HASessionStateListener>> appListenerMap = new ConcurrentHashMap<String, Set<HASessionStateListener>>();
      
   long beanCleaningDelay;
   
   private String _sessionStateName;
   private Logger log;
   private HAPartition partition;
   private String sessionStateIdentifier;
   private String myNodeName;
   
   public HASessionStateImpl(String sessionStateName, HAPartition partition, long beanCleaningDelay)
   {
      if (partition == null)
      {
         throw new IllegalArgumentException("HAPartition must not be null when constructing HASessionImpl");
      }
      
      this.partition = partition;
      
      this._sessionStateName = (sessionStateName != null) ? sessionStateName : ClusterConfigMetaData.DEFAULT_SESSION_STATE_NAME;
      
      this.sessionStateIdentifier = "SessionState-'" + this._sessionStateName + "'";

      this.beanCleaningDelay = (beanCleaningDelay > 0) ? beanCleaningDelay : MAX_DELAY_BEFORE_CLEANING_UNRECLAIMED_STATE;
      
      this.log = Logger.getLogger(HASessionStateImpl.class.getName() + "." + this._sessionStateName);
   }
   
   public void init() throws Exception
   {
      this.partition.registerRPCHandler(this.sessionStateIdentifier, this);
      this.partition.subscribeToStateTransferEvents(HA_SESSION_STATE_STATE_TRANSFER, this);
   }
   
   public void start() throws Exception
   {
      this.myNodeName = this.partition.getNodeName();
      this.log.debug("HASessionState node name : " + this.myNodeName );
      
      // BES 4/7/06 clean up lifecycle; move this to start, as it can't be
      // called until startService due to JNDI dependency
      Context ctx = new InitialContext();
      this.bind(this._sessionStateName, this, HASessionStateImpl.class, ctx);
   }
   
   protected void bind(String jndiName, Object who, Class<?> classType, Context ctx) throws Exception
   {
      // Ah ! This service isn't serializable, so we use a helper class
      //
      org.jboss.util.naming.NonSerializableFactory.bind(jndiName, who);
      Name n = ctx.getNameParser("").parse(jndiName);
      while (n.size() > 1)
      {
         String ctxName = n.get(0);
         try
         {
            ctx = (Context) ctx.lookup(ctxName);
         }
         catch (NameNotFoundException e)
         {
            this.log.debug("creating Subcontext " + ctxName);
            ctx = ctx.createSubcontext(ctxName);
         }
         n = n.getSuffix(1);
      }
      
      // The helper class NonSerializableFactory uses address type nns, we go on to
      // use the helper class to bind the service object in JNDI
      //
      StringRefAddr addr = new StringRefAddr("nns", jndiName);
      Reference ref = new Reference(classType.getName(), addr, NonSerializableFactory.class.getName(), null);
      ctx.bind(n.get(0), ref);
   }
   
   public void stop()
   {
      this.purgeState();
      
      // Unbind so we can rebind if restarted
      try
      {
         Context ctx = new InitialContext();
         ctx.unbind(this._sessionStateName);
         org.jboss.util.naming.NonSerializableFactory.unbind(this._sessionStateName);
      }
      catch (NamingException e)
      {
         // Ignore
      }
   }
   
   public void destroy() throws Exception
   {
      // Remove ref to ourself from HAPartition
      this.partition.unregisterRPCHandler(this.sessionStateIdentifier, this);
      this.partition.unsubscribeFromStateTransferEvents(HA_SESSION_STATE_STATE_TRANSFER, this);
   }
   
   public String getNodeName()
   {
      return this.myNodeName;
   }
   
   // Used for Session state transfer
   //
   public Serializable getCurrentState()
   {
      this.log.debug ("Building and returning state of HASessionState");
      
      synchronized (this.appSessionMap)
      {
         this.purgeState();
         
         try
         {
            return this.deflate(this.appSessionMap);
         }
         catch (IOException e)
         {
            this.log.error("operation failed", e);
            return null;
         }
      }
   }
   
   @SuppressWarnings("unchecked")
   public void setCurrentState(Serializable newState)
   {
      this.log.debug("Receiving state of HASessionState");
      
      try
      {
         Map<String, ConcurrentMap<Object, PackagedSession>> map = (Map) this.inflate((byte[]) newState);
         
         synchronized (this.appSessionMap)
         {
            this.appSessionMap.clear();
            this.appSessionMap.putAll(map);
         }
      }
      catch (IOException e)
      {
         this.log.error("operation failed", e);
      }
   }
   
   public void purgeState()
   {
      synchronized (this.appSessionMap)
      {
         long currentTime = System.currentTimeMillis();
         
         for (Map<Object, PackagedSession> map: this.appSessionMap.values())
         {
            for (PackagedSession session: map.values())
            {
               if ((currentTime - session.unmodifiedExistenceInVM()) > this.beanCleaningDelay)
               {
                  map.remove(session.getKey());
               }
            }
         }
      }
   }
   
   protected byte[] deflate(Object object) throws IOException
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      Deflater def = new Deflater(java.util.zip.Deflater.BEST_COMPRESSION);
      DeflaterOutputStream dos = new DeflaterOutputStream(baos, def);
      
      ObjectOutputStream out = new ObjectOutputStream(dos);
      out.writeObject(object);
      out.close();
      dos.finish();
      dos.close();
      
      return baos.toByteArray();
   }
   
   protected Object inflate(byte[] compressedContent) throws IOException
   {
      if (compressedContent == null) return null;
      
      try
      {
         ObjectInputStream in = new ObjectInputStream(new InflaterInputStream(new ByteArrayInputStream(compressedContent)));
         
         Object object = in.readObject();
         in.close();
         return object;
      }
      catch (Exception e)
      {
         throw new IOException(e.toString());
      }
   }

   protected ConcurrentMap<Object, PackagedSession> getAppMap(String appName)
   {
      synchronized (this.appSessionMap)
      {
         ConcurrentMap<Object, PackagedSession> map = this.appSessionMap.get(appName);
         
         if (map == null)
         {
            map = new ConcurrentHashMap<Object, PackagedSession>();
            
            this.appSessionMap.put(appName, map);
         }
         
         return map;
      }
   }

   public void createSession(String appName, Object keyId)
   {
      this._createSession(appName, keyId);
   }
   
   public PackagedSession _createSession(String appName, Object keyId)
   {
      PackagedSession session = this.createSession(keyId);
      
      this.getAppMap(appName).put(keyId, session);
      
      return session;
   }
   
   private PackagedSession createSession(Object keyId)
   {
      return new PackagedSessionImpl((Serializable) keyId, null, this.myNodeName);
   }
   
   public void setState(String appName, Object keyId, byte[] state)
      throws java.rmi.RemoteException
   {
      PackagedSession session = this.createSession(keyId);
      PackagedSession existing = this.getAppMap(appName).putIfAbsent(keyId, session);
      
      if (existing != null)
      {
         session = existing;
      }
      
      Lock lock = session.getLock();

      if (!lock.tryLock())
      {
         throw new java.rmi.RemoteException("Concurent calls on session object.");
      }
      
      try
      {
         boolean isStateIdentical = session.setState(state);
         
         if (!isStateIdentical)
         {
            Object[] args = { appName, session };
            
            this.partition.callMethodOnCluster(this.sessionStateIdentifier, "_setState", args, SET_STATE_TYPES, true);
         }
      }
      catch (Exception e)
      {
         this.log.error("operation failed", e);
      }
      finally
      {
         lock.unlock();
      }
   }
   
   public void _setState(String appName, PackagedSession session)
   {
      PackagedSession existing = this.getAppMap(appName).putIfAbsent(session.getKey(), session);
      
      if (existing != null)
      {
         Lock lock = existing.getLock();
         
         try
         {
            lock.lockInterruptibly();
         }
         catch (InterruptedException ie)
         {
            this.log.info(ie);
            return;
         }
         
         try
         {
            if (existing.getOwner().equals(this.myNodeName))
            {
               // a modification has occured externally while we were the owner
               //
               this.ownedObjectExternallyModified(appName, session.getKey(), existing, session);
            }
            
            existing.update(session);
         }
         finally
         {
            lock.unlock();
         }
      }
   }
   
   public PackagedSession getState(String appName, Object keyId)
   {
      return this.getAppMap(appName).get(keyId);
   }
   
   public PackagedSession getStateWithOwnership(String appName, Object keyId) throws java.rmi.RemoteException
   {
      return this.localTakeOwnership(appName, keyId);
   }
   
   public PackagedSession localTakeOwnership(String appName, Object keyId) throws java.rmi.RemoteException
   {
      PackagedSession session = this.getAppMap(appName).get(keyId);
      
      // if the session is not yet available, we simply return null. The persistence manager
      // will have to take an action accordingly
      //
      if (session == null)
      {
         return null;
      }
      
      Lock lock = session.getLock();
      
      if (!lock.tryLock())
      {
         throw new java.rmi.RemoteException("Concurent calls on session object.");
      }
      
      try
      {
         if (!session.getOwner().equals(this.myNodeName))
         {
            Object[] args = { appName, keyId, this.myNodeName, new Long(session.getVersion()) };
            ArrayList<?> answers = null;
            try
            {
               answers = this.partition.callMethodOnCluster(this.sessionStateIdentifier, "_setOwnership", args, SET_OWNERSHIP_TYPES, true);
            }
            catch (Exception e)
            {
               this.log.error("operation failed", e);
            }
            
            if ((answers != null) && answers.contains(Boolean.FALSE))
            {
               throw new java.rmi.RemoteException("Concurent calls on session object.");
            }

            session.setOwner(this.myNodeName);
            return session;
         }

         return session;
      }
      finally
      {
         lock.unlock();
      }
   }
   
   public Boolean _setOwnership(String appName, Object keyId, String newOwner, Long remoteVersion)
   {
      PackagedSession session = this.getAppMap(appName).get(keyId);
      
      Lock lock = session.getLock();
      
      if (!lock.tryLock())
      {
         return Boolean.FALSE;
      }

      try
      {
         if (!session.getOwner().equals(this.myNodeName))
         {
            // this is not our business... we don't care
            // we do not update the owner of ps as another host may refuse the _setOwnership call
            // anyway, the update will be sent to us later if state is modified
            //
            return Boolean.TRUE;
         }
         else if (session.getVersion() > remoteVersion.longValue())
         {
            // we are concerned and our version is more recent than the one of the remote host!
            // it means that we have concurrent calls on the same state that has not yet been updated
            // this means we will need to raise a java.rmi.RemoteException
            //
            return Boolean.FALSE;
         }

         // the remote host has the same version as us (or more recent? possible?)
         // we need to update the ownership. We can do this because we know that no other
         // node can refuse the _setOwnership call
         session.setOwner(newOwner);

         this.ownedObjectExternallyModified(appName, keyId, session, session);
         
         return Boolean.TRUE;
      }
      finally
      {
         lock.unlock();
      }
   }
   
   public void takeOwnership(String appName, Object keyId) throws java.rmi.RemoteException
   {
      this.localTakeOwnership(appName, keyId);
   }
   
   public void removeSession(String appName, Object keyId)
   {
      if (this.getAppMap(appName).remove(keyId) != null)
      {
         Object[] args = { appName, keyId };
         
         try
         {
            this.partition.callMethodOnCluster(this.sessionStateIdentifier, "_removeSession", args, REMOVE_SESSION_TYPES, true);
         }
         catch (Exception e)
         {
            this.log.error("operation failed", e);
         }
      }
   }
   
   public void _removeSession(String appName, Object keyId)
   {
      PackagedSession session = this.getAppMap(appName).remove(keyId);
      
      if ((session != null) && session.getOwner().equals(this.myNodeName))
      {
         this.ownedObjectExternallyModified(appName, keyId, session, session);
      }
   }
   
   public void subscribe(String appName, HASessionStateListener listener)
   {
      Set<HASessionStateListener> set = new CopyOnWriteArraySet<HASessionStateListener>();
      
      Set<HASessionStateListener> existing = this.appListenerMap.putIfAbsent(appName, set);
      
      ((existing != null) ? existing : set).add(listener);
   }
   
   public void unsubscribe(String appName, HASessionStateListener listener)
   {
      Set<HASessionStateListener> listeners = this.appListenerMap.get(appName);
      
      if (listeners != null)
      {
         listeners.remove(listener);
      }
   }
   
   public void ownedObjectExternallyModified(String appName, Object key, PackagedSession oldSession, PackagedSession newSession)
   {
      Set<HASessionStateListener> listeners = this.appListenerMap.get(appName);
      
      if (listeners != null)
      {
         for (HASessionStateListener listener: listeners)
         {
            try
            {
               listener.sessionExternallyModified(newSession);
            }
            catch (Throwable e)
            {
               this.log.debug(e);
            }
         }
      }
   }
   
   public HAPartition getCurrentHAPartition()
   {
      return this.partition;
   }
}
