/*
 * Copyright (c) 2003,  Intracom S.A. - www.intracom.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * This package and its source code is available at www.jboss.org
**/ 
package org.jboss.jmx.adaptor.snmp.trapd;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.jboss.bootstrap.spi.ServerConfig;
import org.jboss.system.ServiceMBeanSupport;
import org.opennms.protocols.snmp.SnmpTrapSession;

/**
 * MBean wrapper class that acts as an SNMP trap receiver/logger.
 * It logs traps as INFO messages - change log4j configuration to
 * redirect logging output. To reconfigure the listening port
 * the MBean needs to be stopped and re-started.
 *
 * @jmx:mbean
 *    extends="org.jboss.system.ServiceMBean"
 *
 * @version $Revision: 80636 $
 *
 * @author  <a href="mailto:spol@intracom.gr">Spyros Pollatos</a>
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
**/
public class TrapdService 
   extends ServiceMBeanSupport
   implements TrapdServiceMBean
{
   /** The listening port */
   private int port;

   /** The interface to bind, useful for multi-homed hosts */
   private InetAddress bindAddress;
   
   /** The snmp session used to receive the traps*/
   protected SnmpTrapSession trapSession;
    
   /**
    * Empty CTOR
   **/
   public TrapdService()
   {
       // empty
   }
        
   /**
    * Sets the port that will be used to receive traps
    *
    * @param port the port to listen for traps
    *
    * @jmx:managed-attribute
   **/
   public void setPort(int port)
   {
      this.port = port;
   }

   /**
    * Gets the port that will be used to receive traps
    *
    * @return the port to listen for traps
    *
    * @jmx:managed-attribute
   **/    
   public int getPort()
   {
      return this.port;
   }

   /**
    * Sets the interface that will be bound
    *
    * @param host the interface to bind
    *
    * @jmx:managed-attribute
   **/   
   public void setBindAddress(String host)
      throws UnknownHostException
   {
      this.bindAddress = toInetAddress(host);
   }

   /**
    * Gets the interface that will be bound
    *
    * @return the interface to bind
    * 
    * @jmx:managed-attribute
   **/      
   public String getBindAddress()
   {
      String address = null;
      
      if (this.bindAddress != null)
         address = this.bindAddress.getHostAddress();
      
      return address;
   }
   
   /**
    * Performs service start-up by instantiating an SnmpTrapSession
   **/
   protected void startService()
      throws Exception
   {
      // Create the SNMP trap receiving session with the logging handler,
      // using Logger inherited from ServiceMBeanSupport
      try {
         // cater for possible global -b option, if no override has been specified
         InetAddress address = this.bindAddress != null ? this.bindAddress :
               toInetAddress(System.getProperty(ServerConfig.SERVER_BIND_ADDRESS));
         
         this.trapSession =
            new SnmpTrapSession(new TrapReceiver(this.log), this.port, address);
      }
      catch (Exception e) {
         log.error("Cannot instantiate trap session");
            
         throw e; // ServiceMBeanSupport will log this
      }
   }
    
   /**
    * Performs service shutdown by stopping SnmpTrapSession
   **/
   protected void stopService()
      throws Exception
   {
      this.trapSession.close();
      this.trapSession = null; // gc
   }
   
   /**
    * Safely convert a host string to InetAddress or null
    */
   private InetAddress toInetAddress(String host)
      throws UnknownHostException
   {
      if (host == null || host.length() == 0)
         return null;
      else
         return InetAddress.getByName(host);
   }
    
} // class TrapdService
