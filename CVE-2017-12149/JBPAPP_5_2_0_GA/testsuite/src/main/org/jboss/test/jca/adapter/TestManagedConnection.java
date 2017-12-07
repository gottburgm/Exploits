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
package org.jboss.test.jca.adapter;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.security.auth.Subject;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.logging.Logger;
import org.jboss.tm.TxUtils;

/**
 * TestManagedConnection.java
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version <tt>$Revision: 113438 $</tt>
 */
public class TestManagedConnection  implements ManagedConnection, XAResource, LocalTransaction
{
   public static final String STARTED = "STARTED";
   public static final String SUSPENDED = "SUSPENDED";
   public static final String ENDED = "ENDED";
   public static final String PREPARED = "PREPARED";

   public static final String LOCAL_NONE = "LOCAL_NONE";
   public static final String LOCAL_TRANSACTION = "LOCAL_TRANSACTION";
   public static final String LOCAL_COMMITTED = "LOCAL_COMMITTED";
   public static final String LOCAL_ROLLEDBACK = "LOCAL_ROLLEDBACK";

   private final int id;

   private Logger log = Logger.getLogger(getClass());
   private TestManagedConnectionFactory mcf;
   private HashSet handles = new HashSet();
   private HashSet listeners = new HashSet();

   private GlobalXID currentXid;

   private AtomicBoolean destroyed = new AtomicBoolean(false);

   private boolean failInPrepare = false;
   private boolean failInCommit = false;
   private static boolean failInStart = false;
   private static boolean failInEnd = false;

   private static int xaCode;

   private String localState = LOCAL_NONE;

   public static void setFailInStart(boolean fis, int xa)
   {
      failInStart = fis;
      xaCode = xa;
   }
   public static void setFailInEnd(boolean fie, int xa)
   {
     failInEnd = fie;
     xaCode = xa;

   }

   public TestManagedConnection (final TestManagedConnectionFactory mcf, final Subject subject, final TestConnectionRequestInfo cri, final int id)
   {
      this.mcf = mcf;
      this.id = id;
   }

   void setFailInPrepare(final boolean fail, final int xaCode)
   {
      this.failInPrepare = fail;
      this.xaCode = xaCode;
   }

   void setFailInCommit(final boolean fail, final int xaCode)
   {
      this.failInCommit = fail;
      this.xaCode = xaCode;
   }

   // implementation of javax.resource.spi.ManagedConnection interface

   public synchronized void destroy() throws ResourceException
   {
      log.info("Destroying connection: " + this);
      if (destroyed.get())
         return;
      cleanup();
      destroyed.set(true);
      currentXid = null;
   }

   public synchronized void cleanup() throws ResourceException
   {
      log.info("cleanup: " + this +" handles=" + handles);

      checkDestroyedResourceException();
      for (Iterator i = handles.iterator(); i.hasNext(); )
      {
         TestConnection c = (TestConnection)i.next();
         c.setMc(null);
         i.remove();
      }
   }

   public synchronized Object getConnection(Subject param1, ConnectionRequestInfo param2) throws ResourceException
   {
      log.info("getConnection " + this);

      checkDestroyedResourceException();

      if (param2 != null && ((TestConnectionRequestInfo) param2).failure.equals("getConnectionResource"))
         throw new ResourceException(this.toString());
      if (param2 != null && ((TestConnectionRequestInfo) param2).failure.equals("getConnectionRuntime"))
         throw new RuntimeException(this.toString());
      TestConnection c =  new TestConnection(this);
      handles.add(c);
      return c;
   }

   public synchronized void associateConnection(Object p) throws ResourceException
   {
      log.info("associateConnecton " + this + " connection=" + p);

      checkDestroyedResourceException();

      if (p instanceof TestConnection)
      {
         ((TestConnection)p).setMc(this);
         handles.add(p);
      }
      else
      {
         throw new ResourceException("wrong kind of Connection " + p);
      }
   }

   public synchronized void addConnectionEventListener(ConnectionEventListener cel)
   {
      log.info("addCEL: " + this + " " + cel);
      listeners.add(cel);
   }

   public synchronized void removeConnectionEventListener(ConnectionEventListener cel)
   {
      log.info("removeCEL: " + this + " " + cel);
      listeners.remove(cel);
   }

   public synchronized XAResource getXAResource() throws ResourceException
   {
      checkDestroyedResourceException();
      return this;
   }

   public LocalTransaction getLocalTransaction() throws ResourceException
   {
      return this;
   }

   public ManagedConnectionMetaData getMetaData() throws ResourceException
   {
      return null;
   }

   public void setLogWriter(PrintWriter param1) throws ResourceException
   {
   }

   public PrintWriter getLogWriter() throws ResourceException
   {
     return null;
   }

   // implementation of javax.transaction.xa.XAResource interface

   public List getListeners()
   {
      List result = null;

      synchronized (listeners)
      {
         result = new ArrayList(listeners);
      }

      return result;

   }
   public void start(Xid xid, int flags) throws XAException
   {
      long sleepInStart = mcf.getSleepInStart();
      if (flags == TMNOFLAGS && sleepInStart != 0)
         doSleep(sleepInStart);

      synchronized (this)
      {
         if(failInStart)
         {
            XAException xaex = new XAException(xaCode + "for" + this);
            xaex.errorCode = xaCode;
            broadcastConnectionError(xaex);
            resetFlags();
            throw xaex;
         }

         GlobalXID gid = new GlobalXID(xid);
         String flagString = TxUtils.getXAResourceFlagsAsString(flags);
         log.info("start with xid=" + gid + " flags=" + flagString + " for " + this);
         checkDestroyedXAException();
         Map xids = getXids();
         synchronized (xids)
         {
            String state = (String) xids.get(gid);
            if (state == null && flags != TMNOFLAGS)
            {
                XAException xaex = new XAException("Invalid start state=" + state + " xid=" + gid + " flags=" + flagString + " for " + this);
                xaex.errorCode = XAException.XAER_PROTO;
                throw xaex;
            }
            if (state != null && state != SUSPENDED && state != ENDED
                  && (state != STARTED || ((flags & TMJOIN) == 0))
                  && (state != STARTED || ((flags & TMRESUME) == 0))
               )
            {
                XAException xaex = new XAException("Invalid start state=" + state + " xid=" + gid + " flags=" + flagString + " for " + this);
                xaex.errorCode = XAException.XAER_PROTO;
                throw xaex;
            }

            if ((flags & TMJOIN) != 0 && mcf.failJoin)
            {
                XAException xaex = new XAException("Join is not allowed " + state + " xid=" + gid + " flags=" + flagString + " for " + this);
                xaex.errorCode = XAException.XAER_PROTO;
                throw xaex;
            }
            xids.put(gid, STARTED);
         }

         this.currentXid = gid;
      }
   }

   public void end(final Xid xid, final int flags) throws XAException
   {

      if(failInEnd)
      {
          XAException xaex = new XAException(xaCode + "for" + this);
          xaex.errorCode = xaCode;
          broadcastConnectionError(xaex);
          resetFlags();
          throw xaex;
      }

      long sleepInEnd = mcf.getSleepInEnd();
      if (flags != TMSUCCESS && sleepInEnd != 0)
         doSleep(sleepInEnd);

      synchronized (this)
      {
         GlobalXID gid = new GlobalXID(xid);
         String flagString = TxUtils.getXAResourceFlagsAsString(flags);
         log.info("end with xid=" + gid + " flags=" + flagString + " for " + this);
         // checkDestroyedXAException(); (check is broken, don't use with JBossTS)
         Map xids = getXids();
         synchronized (xids)
         {
            String state = (String) xids.get(gid);
            if (state != STARTED && state != SUSPENDED && state != ENDED)
            {
                XAException xaex = new XAException("Invalid end state=" + state + " xid=" + gid + " " + this);
                xaex.errorCode = XAException.XAER_PROTO;
                throw xaex;
            }
            if ((flags & TMSUSPEND) == 0)
               xids.put(gid, ENDED);
            else
               xids.put(gid, SUSPENDED);
         }

         this.currentXid = null;
      }
   }

   public synchronized void commit(Xid xid, boolean onePhase) throws XAException
   {
      GlobalXID gid = new GlobalXID(xid);
      log.info("commit with xid=" + gid + " onePhase=" + onePhase + " for " + this);
      checkDestroyedXAException();
      if (failInCommit)
      {
          XAException xaex = new XAException(xaCode + " for " + this);
          xaex.errorCode = xaCode;
          resetFlags();
          throw xaex;
      }
      Map xids = getXids();
      synchronized (xids)
      {
         String state = (String) xids.get(gid);
         if (onePhase)
         {
            if (state != SUSPENDED && state != ENDED)
            {
               XAException xaex = new XAException("Invalid one phase commit state=" + state + " xid=" + gid + " " + this);
               xaex.errorCode = XAException.XAER_PROTO;
               throw xaex;
            }
         }
         else
         {
            if (state != PREPARED)
            {
               XAException xaex = new XAException("Invalid two phase commit state=" + state + " xid=" + gid + " " + this);
               xaex.errorCode = XAException.XAER_PROTO;
               throw xaex;
            }
         }
         xids.remove(gid);
      }
   }

   public synchronized void rollback(Xid xid) throws XAException
   {
      GlobalXID gid = new GlobalXID(xid);
      log.info("rollback with xid=" + gid + " for " + this);
      checkDestroyedXAException();
      Map xids = getXids();
      synchronized (xids)
      {
         String state = (String) xids.get(gid);
         if (state != SUSPENDED && state != ENDED && state != PREPARED)
         {
            XAException xaex = new XAException("Invalid rollback state=" + state + " xid=" + gid + " " + this);
            xaex.errorCode = XAException.XAER_PROTO;
            throw xaex;
         }
         xids.remove(gid);
      }
   }

   public synchronized int prepare(Xid xid) throws XAException
   {
      GlobalXID gid = new GlobalXID(xid);
      log.info("prepare with xid=" + gid + " for " + this);
      checkDestroyedXAException();
      Map xids = getXids();
      synchronized (xids)
      {
         String state = (String) xids.get(gid);
         if (state != SUSPENDED && state != ENDED) {
            XAException xaex = new XAException("Invalid prepare state=" + state + " xid=" + gid + " " + this);
            xaex.errorCode = XAException.XAER_PROTO;
            throw xaex;
         }
         if (failInPrepare)
         {
            XAException xae = new XAException(xaCode + " for " + this);
            xae.errorCode = xaCode;
            resetFlags();
            throw xae;
         }
         xids.put(gid, PREPARED);
         return XA_OK;
      }
   }

   public synchronized void forget(Xid xid) throws XAException
   {
      GlobalXID gid = new GlobalXID(xid);
      log.info("forget with xid=" + gid + " for " + this);
      checkDestroyedXAException();
      Map xids = getXids();
      synchronized (xids)
      {
         xids.remove(gid);
      }
   }

   public Xid[] recover(int param1) throws XAException
   {
      return null;
   }

   public boolean isSameRM(XAResource xar) throws XAException
   {
      if (xar == null || xar instanceof TestManagedConnection == false)
         return false;
      TestManagedConnection other = (TestManagedConnection) xar;
      return (mcf == other.mcf);
   }

   public int getTransactionTimeout() throws XAException
   {
      return 0;
   }

   public boolean setTransactionTimeout(int param1) throws XAException
   {
      return false;
   }

   public String getLocalState()
   {
      return localState;
   }

   public void begin() throws ResourceException
   {
      localState = LOCAL_TRANSACTION;
   }

   public void sendBegin() throws ResourceException
   {
      begin();
      ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.LOCAL_TRANSACTION_STARTED);
      Collection copy = new ArrayList(listeners);
      for (Iterator i = copy.iterator(); i.hasNext(); )
      {
         ConnectionEventListener cel = (ConnectionEventListener)i.next();
         try
         {
            cel.localTransactionStarted(event);
         }
         catch (Throwable ignored)
         {
            log.warn("Ignored", ignored);
         }
      }
   }

   public void commit() throws ResourceException
   {
      localState = LOCAL_COMMITTED;
   }

   public void sendCommit() throws ResourceException
   {
      commit();

      ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.LOCAL_TRANSACTION_COMMITTED);
      Collection copy = new ArrayList(listeners);
      for (Iterator i = copy.iterator(); i.hasNext(); )
      {
         ConnectionEventListener cel = (ConnectionEventListener)i.next();
         try
         {
            cel.localTransactionCommitted(event);
         }
         catch (Throwable ignored)
         {
            log.warn("Ignored", ignored);
         }
      }
   }

   public void rollback() throws ResourceException
   {
      localState = LOCAL_ROLLEDBACK;
   }

   public void sendRollback() throws ResourceException
   {
      rollback();

      ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK);
      Collection copy = new ArrayList(listeners);
      for (Iterator i = copy.iterator(); i.hasNext(); )
      {
         ConnectionEventListener cel = (ConnectionEventListener)i.next();
         try
         {
            cel.localTransactionRolledback(event);
         }
         catch (Throwable ignored)
         {
            log.warn("Ignored", ignored);
         }
      }
   }
   /**
    * This method should be called after flag does its job to avoid leaks...
    */
   void resetFlags(){

       failInPrepare = false;
       failInCommit = false;
       failInStart = false;
       failInEnd = false;

       xaCode = 0;
   }
   
   synchronized boolean isInTx()
   {
      log.info("isInTx: " + this);
      return currentXid != null;
   }

   Map getXids()
   {
      return mcf.getXids();
   }

   void connectionClosed(TestConnection handle)
   {
      if (destroyed.get())
         return;

      log.info("Connetion closed handle=" + handle + " for " + this);
      
      ConnectionEvent ce = new ConnectionEvent(this ,ConnectionEvent.CONNECTION_CLOSED);
      ce.setConnectionHandle(handle);
      Collection copy = new ArrayList(listeners);
      for (Iterator i = copy.iterator(); i.hasNext(); )
      {
         log.info("notifying 1 cel connectionClosed");
         ConnectionEventListener cel = (ConnectionEventListener)i.next();
         try
         {
            cel.connectionClosed(ce);
         }
         catch (Throwable ignored)
         {
            log.warn("Ignored", ignored);
         }
      }
      synchronized (this)
      {
         handles.remove(handle);
      }
   }

   protected void broadcastConnectionError(Throwable e)
   {
      if(destroyed.get())
         return;

      Exception ex = null;
      if (e instanceof Exception)
         ex = (Exception) e;
      else
         ex = new ResourceAdapterInternalException("Unexpected error", e);
      ConnectionEvent ce = new ConnectionEvent(this, ConnectionEvent.CONNECTION_ERROR_OCCURRED, ex);
      Collection copy = null;
      synchronized(listeners)
      {
         copy = new ArrayList(listeners);
      }
      for (Iterator i = copy.iterator(); i.hasNext(); )
      {
         ConnectionEventListener cel = (ConnectionEventListener)i.next();
         try
         {
            cel.connectionErrorOccurred(ce);
         }
         catch (Throwable t)
         {
         }
      }
   }

   void connectionError(TestConnection handle, Exception e)
   {
      if (destroyed.get())
         return;

      log.info("Connetion error handle=" + handle + " for " + this, e);

      ConnectionEvent ce = new ConnectionEvent(this, ConnectionEvent.CONNECTION_ERROR_OCCURRED, e);
      ce.setConnectionHandle(handle);
      Collection copy = new ArrayList(listeners);
      for (Iterator i = copy.iterator(); i.hasNext(); )
      {
         ConnectionEventListener cel = (ConnectionEventListener)i.next();
         try
         {
            cel.connectionErrorOccurred(ce);
         }
         catch (Throwable ignored)
         {
         }
      }
   }

   void checkDestroyedResourceException() throws ResourceException
   {
      if (destroyed.get())
         throw new ResourceException("Already destroyed " + this);
   }

   void checkDestroyedXAException() throws XAException
   {
      if (destroyed.get()) {
          XAException xaex = new XAException("Already destroyed " + this);
          xaex.errorCode = XAException.XAER_PROTO;
          throw xaex;
      }
   }

   public synchronized String toString()
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append("TestManagedConnection#").append(id);
      buffer.append("{");
      buffer.append("xid=").append(currentXid);
      buffer.append(" destroyed=").append(destroyed.get());
      buffer.append("}");
      return buffer.toString();
   }

   public void doSleep(long sleep)
   {
      boolean interrupted = false;
      try
      {
         Thread.sleep(sleep);
      }
      catch (InterruptedException e)
      {
         interrupted = true;
      }
      if (interrupted)
         Thread.currentThread().interrupt();
   }

   public class GlobalXID
   {
      byte[] gid;
      int hashCode;
      String toString;

      public GlobalXID(Xid xid)
      {
         gid = xid.getGlobalTransactionId();

         for (int i = 0; i < gid.length; ++i)
            hashCode += 37 * gid[i];
         toString = new String(gid).trim();
      }

      public int hashCode()
      {
         return hashCode;
      }

      public String toString()
      {
         return toString;
      }

      public boolean equals(Object obj)
      {
         if (obj == null || obj instanceof GlobalXID == false)
            return false;
         GlobalXID other = (GlobalXID) obj;
         return toString.equals(other.toString);
      }
   }
}
