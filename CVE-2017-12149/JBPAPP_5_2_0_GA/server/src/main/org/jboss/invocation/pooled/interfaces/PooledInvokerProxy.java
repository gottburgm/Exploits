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
package org.jboss.invocation.pooled.interfaces;

import java.io.IOException;
import java.io.Externalizable;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.EOFException;
import java.io.OptionalDataException;
import java.io.UnsupportedEncodingException;
import java.io.InterruptedIOException;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.MarshalledObject;
import java.rmi.NoSuchObjectException;
import java.rmi.ServerException;
import java.rmi.ConnectException;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;

import javax.transaction.TransactionRolledbackException;
import javax.transaction.SystemException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.SSLException;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.Invoker;
import org.jboss.tm.TransactionPropagationContextFactory;
import org.jboss.tm.TransactionPropagationContextUtil;
import org.jboss.logging.Logger;
import EDU.oswego.cs.dl.util.concurrent.ConcurrentReaderHashMap;


/**
 * Client socket connections are pooled to avoid the overhead of
 * making a connection.  RMI seems to do a new connection with each
 * request.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81030 $
 */
public class PooledInvokerProxy
   implements Invoker, Externalizable
{
   // Attributes ----------------------------------------------------
   private static final Logger log = Logger.getLogger(PooledInvokerProxy.class);
   /** The serialVersionUID @since 1.1.4.3 */
   private static final long serialVersionUID = -1456509931095566410L;
   /** The current wire format we write */
   private static final int WIRE_VERSION = 1;

   // Simple performance measurements, not thread safe
   public static long getSocketTime = 0;
   public static long readTime = 0;
   public static long writeTime = 0;
   public static long serializeTime = 0;
   public static long deserializeTime = 0;
   /** The number of times a connection has been obtained from a pool */
   public static long usedPooled = 0;
   /** The number of connections in use */
   private static int inUseCount = 0;
   /** The number of socket connections made */
   private static long socketConnectCount = 0;
   /** The number of socket close calls made */
   private static long socketCloseCount = 0;

   /**
    * Set number of retries in getSocket method
    */
   public static int MAX_RETRIES = 10;

   /** A class wide pool Map<ServerAddres, LinkedList<ClientSocket>> */
   protected static final Map connectionPools = new ConcurrentReaderHashMap();

   /**
    * connection information
    */
   protected ServerAddress address;

   /**
    * Pool for this invoker.  This is shared between all
    * instances of proxies attached to a specific invoker
    * This should not be serializable, but is for backward compatibility.
    */
   protected LinkedList pool = null;
   /** */
   protected int maxPoolSize;
   /** The number of times to retry after seeing a ConnectionException */
   protected int retryCount = 1;
   /** The logging trace flag */
   private transient boolean trace;

   /**
    * An encapsulation of a client connection
    */
   protected static class ClientSocket
      implements HandshakeCompletedListener
   {
      public ObjectOutputStream out;
      public ObjectInputStream in;
      public Socket socket;
      public int timeout;
      public String sessionID;
      private boolean handshakeComplete = false;
      private boolean trace;

      public ClientSocket(Socket socket, int timeout) throws Exception
      {
         this.socket = socket;
         trace = log.isTraceEnabled();
         boolean needHandshake = false;

         if( socket instanceof SSLSocket )
         {
            SSLSocket ssl = (SSLSocket) socket;
            ssl.addHandshakeCompletedListener(this);
            if( trace )
               log.trace("Starting SSL handshake");
            needHandshake = true;
            handshakeComplete = false;
            ssl.startHandshake();
         }
         socket.setSoTimeout(timeout);
         this.timeout = timeout;
         out = new OptimizedObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
         out.flush();
         in = new OptimizedObjectInputStream(new BufferedInputStream(socket.getInputStream()));
         if( needHandshake )
         {
            // Loop waiting for the handshake to complete
            socket.setSoTimeout(1000);
            for(int n = 0; handshakeComplete == false && n < 60; n ++)
            {
               try
               {
                  int b = in.read();
               }
               catch(SSLException e)
               {
                  if( trace )
                     log.trace("Error while waiting for handshake to complete", e);
                  throw e;
               }
               catch(IOException e)
               {
                  if( trace )
                     log.trace("Handshaked read()", e);
               }
            }
            if( handshakeComplete == false )
               throw new SSLException("Handshaked failed to complete in 60 seconds");
            // Restore the original timeout
            socket.setSoTimeout(timeout);
         }

      }

      public void handshakeCompleted(HandshakeCompletedEvent event)
      {
         handshakeComplete = true;
         byte[] id = event.getSession().getId();
         try
         {
            sessionID = new String(id, "UTF-8");
         }
         catch (UnsupportedEncodingException e)
         {
            log.warn("Failed to create session id using UTF-8, using default", e);
            sessionID = new String(id);
         }
         if( trace )
         {
            log.trace("handshakeCompleted, event="+event+", sessionID="+sessionID);
         }
      }

      public String toString()
      {
         StringBuffer tmp = new StringBuffer("ClientSocket@");
         tmp.append(System.identityHashCode(this));
         tmp.append('[');
         tmp.append("socket=");
         tmp.append(socket.toString());
         tmp.append(']');
         return tmp.toString();
      }

      /**
       * @todo should this be handled with weak references as this should
       * work better with gc
       */
      protected void finalize()
      {
         if (socket != null)
         {
            if( trace )
               log.trace("Closing socket in finalize: "+socket);
            try
            {
               socketCloseCount --;
               socket.close();
            }
            catch (Exception ignored) {}
            finally
            {
               socket = null;
            }
         }
      }
   }

   /**
    * Clear all class level stats
    */
   public static void clearStats()
   {
      getSocketTime = 0;
      readTime = 0;
      writeTime = 0;
      serializeTime = 0;
      deserializeTime = 0;
      usedPooled = 0;      
   }

   /**
    * @return the active number of client connections
    */
   public static long getInUseCount()
   {
      return inUseCount;
   }

   /**
    * @return the number of times a connection was returned from a pool
    */
   public static long getUsedPooled()
   {
      return usedPooled;
   }
   public static long getSocketConnectCount()
   {
      return socketConnectCount;
   }
   public static long getSocketCloseCount()
   {
      return socketCloseCount;
   }

   /**
    * @return the total number of pooled connections across all ServerAddresses
    */
   public static int getTotalPoolCount()
   {
      int count = 0;
      Iterator iter = connectionPools.values().iterator();
      while( iter.hasNext() )
      {
         List pool = (List) iter.next();
         if( pool != null )
            count += pool.size();
      }
      return count;
   }

   /**
    * @return the proxy local pool count
    */
   public long getPoolCount()
   {
      return pool.size();
   }

   /**
    * Exposed for externalization.
    */
   public PooledInvokerProxy()
   {
      super();
      trace = log.isTraceEnabled();
   }

   /**
    * Create a new Proxy.
    *
    */
   public PooledInvokerProxy(ServerAddress sa, int maxPoolSize)
   {
      this(sa, maxPoolSize, MAX_RETRIES);
   }
   public PooledInvokerProxy(ServerAddress sa, int maxPoolSize, int retryCount)
   {
      this.address = sa;
      this.maxPoolSize = maxPoolSize;
      this.retryCount = retryCount;
   }

   /**
    * Close all sockets in a specific pool.
    */
   public static void clearPool(ServerAddress sa)
   {
      boolean trace = log.isTraceEnabled();
      if( trace )
         log.trace("clearPool, sa: "+sa);
      try
      {
         LinkedList thepool = (LinkedList)connectionPools.get(sa);
         if (thepool == null) return;
         synchronized (thepool)
         {
            int size = thepool.size();
            for (int i = 0; i < size; i++)
            {
               ClientSocket cs = null;
               try
               {
                  ClientSocket socket = (ClientSocket)thepool.removeFirst();
                  cs = socket;
                  if( trace )
                     log.trace("Closing, ClientSocket: "+socket);
                  socketCloseCount --;
                  socket.socket.close();
               }
               catch (Exception ignored)
               {
               }
               finally
               {
                  if( cs != null )
                     cs.socket = null;
               }
            }
         }
      }
      catch (Exception ex)
      {
         // ignored
      }
   }
   /**
    * Close all sockets in all pools
    */
   public static void clearPools()
   {
      synchronized (connectionPools)
      {
         Iterator it = connectionPools.keySet().iterator();
         while (it.hasNext())
         {
            ServerAddress sa = (ServerAddress)it.next();
            clearPool(sa);
         }
      }
   }

   public boolean equals(Object other)
   {
      if(! (other instanceof PooledInvokerProxy))
        return false;
      return (address.equals( ((PooledInvokerProxy)other).address ));
   }

   public int hashCode()
   {
      return address.hashCode();
   }

   protected void initPool()
   {
      synchronized (connectionPools)
      {
         pool = (LinkedList)connectionPools.get(address);
         if (pool == null)
         {
            pool = new LinkedList();
            connectionPools.put(address, pool);
         }
      }
   }

   protected ClientSocket getConnection() throws Exception
   {
      Socket socket = null;
      ClientSocket cs = null;

      //
      // Need to retry a few times
      // on socket connection because, at least on Windoze,
      // if too many concurrent threads try to connect
      // at same time, you get ConnectionRefused
      //
      // Retrying seems to be the most performant.
      //
      // This problem always happens with RMI and seems to
      // have nothing to do with backlog or number of threads
      // waiting in accept() on the server.
      // 
      for (int i = 0; i < retryCount; i++)
      {
         ClientSocket pooled = getPooledConnection();
         if (pooled != null)
         {
            usedPooled++;
            inUseCount ++;
            return pooled;
         }

         try
         {
            if( trace)
            {
               log.trace("Connecting to addr: "+address.address
                  +", port: "+address.port
                  +",clientSocketFactory: "+address.clientSocketFactory
                  +",enableTcpNoDelay: "+address.enableTcpNoDelay
                  +",timeout: "+address.timeout);
            }
            if( address.clientSocketFactory != null )
               socket = address.clientSocketFactory.createSocket(address.address, address.port);
            else
               socket = new Socket(address.address, address.port);
            socketConnectCount ++;
            if( trace )
               log.trace("Connected, socket="+socket);

            socket.setTcpNoDelay(address.enableTcpNoDelay);
            cs = new ClientSocket(socket, address.timeout);
            inUseCount ++;
            if( trace )
            {
               log.trace("New ClientSocket: "+cs
                  +", usedPooled="+ usedPooled
                  +", inUseCount="+ inUseCount
                  +", socketConnectCount="+ socketConnectCount
                  +", socketCloseCount="+ socketCloseCount
               );
            }
            break;
         }
         catch (Exception ex)
         {
            if( ex instanceof InterruptedIOException || ex instanceof SocketException )
            {
               if( trace )
                  log.trace("Connect failed", ex);
               if (i + 1 < retryCount)
               {
                  Thread.sleep(1);
                  continue;
               }
            }
            throw ex;
         }
      }
      // Should not happen
      if( cs == null )
         throw new ConnectException("Failed to obtain a socket, tries="+retryCount);
      return cs;
   }

   protected ClientSocket firstConnection()
   {
      synchronized (pool)
      {
         if(pool.size() > 0)
            return (ClientSocket)pool.removeFirst();
      }
       return null;
   }

   protected ClientSocket getPooledConnection()
   {
      ClientSocket socket = null;
      while ((socket = firstConnection()) != null)
      {
         try
         {
            // Test to see if socket is alive by send ACK message
            if( trace )
               log.trace("Checking pooled socket: "+socket+", address: "+socket.socket.getLocalSocketAddress());
            final byte ACK = 1;
            socket.out.writeByte(ACK);
            socket.out.flush();
            socket.in.readByte();
            if( trace )
            {
               log.trace("Using pooled ClientSocket: "+socket
                  +", usedPooled="+ usedPooled
                  +", inUseCount="+ inUseCount
                  +", socketConnectCount="+ socketConnectCount
                  +", socketCloseCount="+ socketCloseCount
               );
            }
            return socket;
         }
         catch (Exception ex)
         {
            if( trace )
               log.trace("Failed to validate pooled socket: "+socket, ex);
            try
            {
               if( socket != null )
               {
                  socketCloseCount --;
                  socket.socket.close();
               }
            }
            catch (Exception ignored)
            {
            }
            finally
            {
               if( socket != null )
                  socket.socket = null;
            }
         }
      }
      return null;
   }

   /**
    * Return a socket to the pool
    * @param socket
    * @return true if socket was added to the pool, false if the pool
    *    was full
    */
   protected boolean returnConnection(ClientSocket socket)
   {
      boolean pooled = false;
      synchronized( pool )
      {
         if (pool.size() < maxPoolSize)
         {
            pool.add(socket);
            inUseCount --;
            pooled = true;
         }
      }
      return pooled;
   }

   /**
    * The name of of the server.
    */
   public String getServerHostName() throws Exception
   {
      return address.address;
   }

   /**
    * ???
    *
    * @todo MOVE TO TRANSACTION
    *  
    * @return the transaction propagation context of the transaction
    *         associated with the current thread.
    *         Returns <code>null</code> if the transaction manager was never
    *         set, or if no transaction is associated with the current thread.
    */
   public Object getTransactionPropagationContext()
      throws SystemException
   {
      TransactionPropagationContextFactory tpcFactory = TransactionPropagationContextUtil.getTPCFactoryClientSide();
      return (tpcFactory == null) ? null : tpcFactory.getTransactionPropagationContext();
   }


   /**
    * The invocation on the delegate, calls the right invoker.  Remote if we are remote, 
    * local if we are local. 
    */
   public Object invoke(Invocation invocation)
      throws Exception
   {
      boolean trace = log.isTraceEnabled();
      // We are going to go through a Remote invocation, switch to a Marshalled Invocation
      PooledMarshalledInvocation mi = new PooledMarshalledInvocation(invocation);

      // Set the transaction propagation context
      //  @todo: MOVE TO TRANSACTION
      mi.setTransactionPropagationContext(getTransactionPropagationContext());
      
      Object response = null;
      long start = System.currentTimeMillis();
      ClientSocket socket = getConnection();
      long end = System.currentTimeMillis() - start;
      getSocketTime += end;
      // Add the socket session if it exists
      if( socket.sessionID != null )
      {
         mi.setValue("SESSION_ID", socket.sessionID);
         if( trace )
            log.trace("Added SESSION_ID to invocation");
      }

      try
      {
         if( trace )
            log.trace("Sending invocation to: "+mi.getObjectName());
         socket.out.writeObject(mi);
         socket.out.reset();
         socket.out.writeObject(Boolean.TRUE); // for stupid ObjectInputStream reset
         socket.out.flush();
         socket.out.reset();
         end = System.currentTimeMillis() - start;
         writeTime += end;
         start = System.currentTimeMillis();
         response = socket.in.readObject();
         // to make sure stream gets reset
         // Stupid ObjectInputStream holds object graph
         // can only be set by the client/server sending a TC_RESET
         socket.in.readObject();
         end = System.currentTimeMillis() - start;
         readTime += end;
      }
      catch (Exception ex)
      {
         if( trace )
            log.trace("Failure during invoke", ex);
         try
         {
            socketCloseCount --;
            socket.socket.close();
         }
         catch (Exception ignored) {}
         finally
         {
            socket.socket = null;
         }
         throw new java.rmi.ConnectException("Failure during invoke", ex);
      }

      // Put socket back in pool for reuse
      if( returnConnection(socket) == false )
      {
         // Failed, close the socket
         if( trace )
            log.trace("Closing unpooled socket: "+socket);
         try
         {
            socketCloseCount --;
            socket.socket.close();
         }
         catch (Exception ignored) {}
         finally
         {
            socket.socket = null;
         }
      }

      // Return response

      try
      {
         if (response instanceof Exception)
         {
            throw ((Exception)response);
         }
         if (response instanceof MarshalledObject)
         {
            return ((MarshalledObject)response).get();
         }
         return response;
      }
      catch (ServerException ex)
      {
         // Suns RMI implementation wraps NoSuchObjectException in
         // a ServerException. We cannot have that if we want
         // to comply with the spec, so we unwrap here.
         if (ex.detail instanceof NoSuchObjectException)
         {
            throw (NoSuchObjectException) ex.detail;
         }
         //likewise
         if (ex.detail instanceof TransactionRolledbackException)
         {
            throw (TransactionRolledbackException) ex.detail;
         }
         throw ex;
      }
   }

   /**
    * Write out the serializable data
    * @serialData address ServerAddress
    * @serialData maxPoolSize int 
    * @serialData WIRE_VERSION int version
    * @serialData retryCount int
    * @param out
    * @throws IOException
    */
   public void writeExternal(final ObjectOutput out)
      throws IOException
   {
      // The legacy wire format is address, maxPoolSize
      out.writeObject(address);
      out.writeInt(maxPoolSize);
      // Write out the current version format and its data
      out.writeInt(WIRE_VERSION);
      out.writeInt(retryCount);
   }

   public void readExternal(final ObjectInput in)
      throws IOException, ClassNotFoundException
   {
      trace = log.isTraceEnabled();
      address = (ServerAddress)in.readObject();
      maxPoolSize = in.readInt();
      int version = 0;
      try
      {
         version = in.readInt();
      }
      catch(EOFException e)
      {
         // No version written and there is no more data
      }
      catch(OptionalDataException e)
      {
         // No version written and there is data from other objects
      }

      switch( version )
      {
         case 0:
            // This has no retryCount, default it to the hard-coded value
            retryCount = MAX_RETRIES;
            break;
         case 1:
            readVersion1(in);
            break;
         default:
            /* Assume a newer version that only adds defaultable values.
            The alternative would be to thrown an exception
            */
            break;
      }
      initPool();
   }

   private void readVersion1(final ObjectInput in)
      throws IOException
   {
      retryCount = in.readInt();
   }
}
