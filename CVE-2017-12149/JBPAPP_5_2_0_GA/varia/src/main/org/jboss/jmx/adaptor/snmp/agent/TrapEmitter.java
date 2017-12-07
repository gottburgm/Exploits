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
package org.jboss.jmx.adaptor.snmp.agent;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.management.Notification;

import org.jboss.bootstrap.spi.ServerConfig;
import org.jboss.jmx.adaptor.snmp.config.manager.Manager;
import org.jboss.logging.Logger;
import org.jboss.xb.binding.MappingObjectModelFactory;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.opennms.protocols.snmp.SnmpIPAddress;
import org.opennms.protocols.snmp.SnmpPduPacket;
import org.opennms.protocols.snmp.SnmpPduTrap;

/**
 * <tt>TrapEmitter</tt> is a class that manages SNMP trap emission.
 *
 * Currently, it allows to send V1 or V2 traps to one or more subscribed SNMP
 * managers defined by their IP address, listening port number and expected
 * SNMP version.
 *
 * @version $Revision: 80636 $
 *
 * @author  <a href="mailto:spol@intracom.gr">Spyros Pollatos</a>
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
**/
public class TrapEmitter
{
   /** The logger object */
   private static final Logger log = Logger.getLogger(TrapEmitter.class);
   
   /** Reference to the utilised trap factory*/
   private TrapFactory trapFactory = null;
   
   /** The actual trap factory to instantiate */
   private String trapFactoryClassName = null;

   /** The managers resource name */
   private String managersResName = null;
   
   /** The notification map resource name */
   private String notificationMapResName = null;
   
   /** Provides trap count */
   private Counter trapCount = null;
   
   /** Uptime clock */
   private Clock uptime = null;
   
   /** Holds the manager subscriptions. Accessed through synch'd wrapper */
   private Set managers = Collections.synchronizedSet(new HashSet());  
    
   /**
    * Builds a TrapEmitter object for sending SNMP V1 or V2 traps. <P>
   **/
   public TrapEmitter(String trapFactoryClassName,
                      Counter trapCount,
                      Clock uptime,
                      String managersResName,
                      String notificationMapResName)
   {
      this.trapFactoryClassName = trapFactoryClassName;
      this.trapCount = trapCount;
      this.uptime = uptime;
      this.managersResName = managersResName;
      this.notificationMapResName = notificationMapResName;
   }
    
   /**
    * Complete emitter initialisation
   **/               
   public void start()
      throws Exception
   {
      // Load persisted manager subscriptions
      load();
      
      // Instantiate the trap factory
      this.trapFactory = (TrapFactory) Class.forName(this.trapFactoryClassName,
                                                     true,
                                                     this.getClass().getClassLoader()).newInstance();
      
      // Initialise
      this.trapFactory.set(this.notificationMapResName,
                           this.uptime,
                           this.trapCount);
      
      // Start the trap factory
      this.trapFactory.start();
   }
    
   /**
    * Perform shutdown
   **/
   public void stop()
      throws Exception
   {
      synchronized(this.managers) {

         // Recycle open sessions to managers
         Iterator i = this.managers.iterator();
         
         while (i.hasNext()) {
            ManagerRecord s = (ManagerRecord)i.next();
            s.closeSession();    
         }
            
         // Drop all held manager records
         this.managers.clear();
      }
   }
    
   /**
    * Intercepts the notification and after translating it to a trap sends it
    * along.
    *
    * @param n notification to be sent
    * @throws Exception if an error occurs during the preparation or
    * sending of the trap
   **/    
   public void send(Notification n)
      throws Exception
   {
      // Beeing paranoid
      synchronized(this.trapFactory) {
         if(this.trapFactory == null) {
            log.error("Received notifications before trap factory set. Discarding.");
            return;     
         }
      }
           
      // Cache the translated notification
      SnmpPduTrap v1TrapPdu = null; 
      SnmpPduPacket v2TrapPdu = null; 
       
      // Send trap. Synchronise on the subscription collection while 
      // iterating 
      synchronized(this.managers) {
            
         // Iterate over sessions and emit the trap on each one
         Iterator i = this.managers.iterator();
         while (i.hasNext()) {
            ManagerRecord s = (ManagerRecord)i.next();       

            try {
               switch (s.getVersion()) {
                  case SnmpAgentService.SNMPV1:
                     if (v1TrapPdu == null)
                        v1TrapPdu = this.trapFactory.generateV1Trap(n);
                     
                     // fix the agent ip in the trap depending on which local address is bound
                     v1TrapPdu.setAgentAddress(new SnmpIPAddress(s.getLocalAddress()));
                            
                     // Advance the trap counter
                     this.trapCount.advance();
                            
                     // Send
                     s.getSession().send(v1TrapPdu);
                     break;
                  
                  case SnmpAgentService.SNMPV2:
                     if (v2TrapPdu == null)
                        v2TrapPdu = this.trapFactory.generateV2Trap(n);
                     
                     // Advance the trap counter
                     this.trapCount.advance();
                            
                     // Send
                     s.getSession().send(v2TrapPdu);
                     break;
                     
                  default:    
                     log.error("Skipping session: Unknown SNMP version found");    
               }            
            } 
            catch(MappingFailedException e) {
              log.error("Translating notification - " + e.getMessage());
            }    
            catch(Exception e) {
              log.error("SNMP send error for " + 
                        s.getAddress().toString() + ":" +
                        s.getPort() + ": <" + e +
                        ">");                    
            }
         }    
      }
   }

   /**
    * Load manager subscriptions
   **/ 
   private void load() throws Exception
   {
      log.debug("Reading resource: '" + this.managersResName + "'");
      
      // configure ObjectModelFactory for mapping XML to POJOs
      // we'll be simply getting an ArrayList of Manager objects
      MappingObjectModelFactory momf = new MappingObjectModelFactory();
      momf.mapElementToClass("manager-list", ArrayList.class);
      momf.mapElementToClass("manager", Manager.class);

      ArrayList managerList = null;
      InputStream is = null;
      try
      {
         // locate managers.xml
         is = this.getClass().getResourceAsStream(this.managersResName);
         
         // create unmarshaller
         Unmarshaller unmarshaller = UnmarshallerFactory.newInstance()
               .newUnmarshaller();
         
         // let JBossXB do it's magic using the MappingObjectModelFactory
         managerList = (ArrayList)unmarshaller.unmarshal(is, momf, null);         
      }
      catch (Exception e)
      {
         log.error("Accessing resource '" + managersResName + "'");
         throw e;
      }
      finally
      {
         if (is != null)
         {
            // close the XML stream
            is.close();            
         }
      }
      log.debug("Found " + managerList.size() + " monitoring managers");        
        
      for (Iterator i = managerList.iterator(); i.hasNext(); )
      {
         // Read the monitoring manager's particulars
         Manager m = (Manager)i.next();

         try
         {
            // Create a record of the manager's interest 
            ManagerRecord mr = new ManagerRecord(
                    InetAddress.getByName(m.getAddress()),
                    m.getPort(),
                    toInetAddressWithDefaultBinding(m.getLocalAddress()),
                    m.getLocalPort(),
                    m.getVersion()
                );
                
            // Add the record to the list of monitoring managers. If 
            // successfull open the session to the manager as well.
            if (this.managers.add(mr) == false)
            {
               log.warn("Ignoring duplicate manager: " + m);  
            }
            else
            {            
               // Open the session to the manager
               mr.openSession();
            }                
         }
         catch (Exception e)
         {
            log.warn("Error enabling monitoring manager: " + m, e);                
         } 
      }
   }

   /**
    * cater for possible global -b option, if no override has been specified
    */
   private InetAddress toInetAddressWithDefaultBinding(String host)
      throws UnknownHostException
   {
      if (host == null || host.length() == 0) {
         
         String defaultBindAddress = System.getProperty(ServerConfig.SERVER_BIND_ADDRESS);
         if (defaultBindAddress != null && !defaultBindAddress.equals("0.0.0.0"))
            return InetAddress.getByName(defaultBindAddress);
         else
            return InetAddress.getLocalHost();
      }
      else
         return InetAddress.getByName(host);
   }
   
} // class TrapEmitter
