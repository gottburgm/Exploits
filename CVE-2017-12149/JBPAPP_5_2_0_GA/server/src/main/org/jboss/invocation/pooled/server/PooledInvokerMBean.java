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
package org.jboss.invocation.pooled.server;

import java.net.ServerSocket;
import javax.management.ObjectName;
import javax.net.ServerSocketFactory;

import org.jboss.invocation.pooled.interfaces.PooledInvokerProxy;

/**
 * The PooledInvoker standard MBean interface.
 * @author Bill Burke
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81030 $
 */
public interface PooledInvokerMBean extends org.jboss.system.ServiceMBean
{

   /**
    * Getter for property numAcceptThreads
    * @return Value of property numAcceptThreads
    */
   int getNumAcceptThreads();

   /**
    * Setter for property numAcceptThreads
    * @param size New value of property numAcceptThreads.
    */
   void setNumAcceptThreads(int size);

   /**
    * Getter for property maxPoolSize;
    * @return Value of property maxPoolSize.
    */
   int getMaxPoolSize();

   /**
    * Setter for property maxPoolSize.
    * @param maxPoolSize New value of property serverBindPort.
    */
   void setMaxPoolSize(int maxPoolSize);

   /**
    * Getter for property maxPoolSize;
    * @return Value of property maxPoolSize.
    */
   int getClientMaxPoolSize();

   /**
    * Setter for property maxPoolSize.
    * @param clientMaxPoolSize New value of property serverBindPort.
    */
   void setClientMaxPoolSize(int clientMaxPoolSize);

   /**
    * Getter for property timeout
    * @return Value of property timeout
    */
   int getSocketTimeout();

   /**
    * Setter for property timeout
    * @param time New value of property timeout
    */
   void setSocketTimeout(int time);

   /**
    * @return Current client connection pool size
    */ 
   int getCurrentClientPoolSize();

   /**
    * @return current connection thread pool size
    */ 
   int getCurrentThreadPoolSize();

   /**
    * Getter for property serverBindPort.
    * @return Value of property serverBindPort.
    */
   int getServerBindPort();

   /**
    * Setter for property serverBindPort.
    * @param serverBindPort New value of property serverBindPort.
    */
   void setServerBindPort(int serverBindPort);

   /**
    * @return the address the client proxy connects to 
    */ 
   String getClientConnectAddress();
   /**
    * Set the clientConnectAddress
    * @param clientConnectAddress - address the client proxy connects to 
    */ 
   void setClientConnectAddress(java.lang.String clientConnectAddress);

   /**
    * @return the client local bind port
    */ 
   int getClientConnectPort();
   /**
    * @param clientConnectPort - the client local bind port
    */ 
   void setClientConnectPort(int clientConnectPort);

   /**
    * @return the number of connect retries
    */ 
   public int getClientRetryCount();
   /**
    * @param clientRetryCount - the number of connect retries
    */ 
   public void setClientRetryCount(int clientRetryCount);

   /**
    * @return the server accept backlog
    */ 
   int getBacklog();
   /**
    * @param backlog - the server accept backlog
    */ 
   void setBacklog(int backlog);

   /**
    * @return Socket.setTcpNoDelay flag
    */ 
   boolean isEnableTcpNoDelay();
   /**
    * Socket.setTcpNoDelay flag
    * @param enableTcpNoDelay
    */ 
   void setEnableTcpNoDelay(boolean enableTcpNoDelay);

   String getServerBindAddress();

   void setServerBindAddress(String serverBindAddress);

   /**
    * mbean get-set pair for field transactionManagerService Get the value of
    * transactionManagerService
    * @return value of transactionManagerService
    */
   ObjectName getTransactionManagerService();

   /**
    * Set the value of transactionManagerService
    * @param transactionManagerService Value to assign to transactionManagerService
    */
   void setTransactionManagerService(ObjectName transactionManagerService);

   PooledInvokerProxy getOptimizedInvokerProxy();

   /**
    * Set the client socket factory implementation
    * @return the javax.net.SocketFactory implementation class name
    */ 
   public String getClientSocketFactoryName();
   /**
    * Set the client factory implementation
    * @param factoryName - the javax.net.SocketFactory implementation class name
    */ 
   public void setClientSocketFactoryName(String factoryName);

   /**
    * Set the server socket factory implementation
    * @return the javax.net.ServerSocketFactory implementation class name
    */ 
   public String getServerSocketFactoryName();
   /**
    * Set the server factory implementation
    * @param factoryName - the javax.net.ServerSocketFactory implementation class name
    */ 
   public void setServerSocketFactoryName(String factoryName);

   public ServerSocketFactory getServerSocketFactory();
   public void setServerSocketFactory(ServerSocketFactory factory);

   public String getSslDomain();
   public void setSslDomain(String sslDomain);
   
}
