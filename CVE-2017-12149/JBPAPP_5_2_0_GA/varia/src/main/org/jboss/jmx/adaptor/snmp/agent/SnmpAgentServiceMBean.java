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
package org.jboss.jmx.adaptor.snmp.agent;

import java.net.UnknownHostException;

import javax.management.ObjectName;

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.ListenerServiceMBean;

/**
 * SnmpAgentService MBean interface.
 * 
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81038 $
 */
public interface SnmpAgentServiceMBean extends ListenerServiceMBean
{
   /** Default ObjectName */
   ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.jmx:name=SnmpAgent,service=snmp,type=adaptor");
   
   // Attributes ----------------------------------------------------

   /** The name of the file containing SNMP manager specifications */
   void setManagersResName(String managersResName);
   String getManagersResName();

   /** The name of the file containing the notification/trap mappings */
   void setNotificationMapResName(String notificationMapResName);
   String getNotificationMapResName();

   /** The resource file name containing get/set mappings */
   void setRequestHandlerResName(String requestHandlerResName);
   String getRequestHandlerResName();
   
   /** The utilised trap factory name */
   void setTrapFactoryClassName(String name);
   String getTrapFactoryClassName();

   /** The RequestHandler implementation class */
   void setRequestHandlerClassName(String requestHandlerClassName);
   String getRequestHandlerClassName();
   
   /** The utilised timer MBean name */
   void setTimerName(ObjectName timerName);
   ObjectName getTimerName();

   /** Enables/disables dynamic subscriptions */
   void setDynamicSubscriptions(boolean dynamicSubscriptions);
   boolean getDynamicSubscriptions();
   
   /** The agent bind address */
   void setBindAddress(String bindAddress) throws UnknownHostException;
   String getBindAddress();
   
   /** The heartbeat period (in seconds) - 0 disables heartbeat */
   void setHeartBeatPeriod(int heartBeatPeriod);   
   int getHeartBeatPeriod();

   /** The agent listening port number */
   void setPort(int port);
   int getPort();
   
   /** The number of threads in the agent request processing thread pool */
   void setNumberOfThreads(int numberOfThreads);
   int getNumberOfThreads();

   /** The snmp protocol version */
   void setSnmpVersion(int snmpVersion);
   int getSnmpVersion();

   /** The read community (no getter) */
   void setReadCommunity(String readCommunity);

   /** The write community (no getter) */
   void setWriteCommunity(String writeCommunity);
   
   /** The time difference (in msecs) between instantiation time and epoch (midnight, January 1, 1970 UTC) */
   long getInstantiationTime();

   /** The up-time, in msecs. */
   long getUptime();

   /** The current trap counter reading */
   long getTrapCount();

   // Operations ----------------------------------------------------

   /**
    * Reconfigures the RequestHandler, that is 
    * reponsible for handling get requests etc.
    */
   void reconfigureRequestHandler() throws Exception;
   
}
