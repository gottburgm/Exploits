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
package org.jboss.jmx.adaptor.snmp.system;

import javax.management.ObjectName;

import org.jboss.jmx.adaptor.snmp.agent.SnmpAgentServiceMBean;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigLocator;
import org.jboss.system.server.ServerInfoMBean;
import org.opennms.protocols.snmp.SnmpObjectId;
import org.opennms.protocols.snmp.SnmpTimeTicks;

/**
 * An MBean service that defines the MIB-2 system group an agent
 * is supposed to implement under the oid
 * iso.org.dod.internet.mgmt.mib-2.system (.1.3.6.1.2.1.1)
 * See rfc-1213
 * 
 * @author <a href="mailto:hwr@pilhuhn.de">Heiko W. Rupp</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 93935 $
 */
public class MIB2SystemGroupService extends ServiceMBeanSupport
   implements MIB2SystemGroupServiceMBean
{
   // Constants -----------------------------------------------------
   
   /** JBoss OID Prefix */
   public static final String JBOSS_PREFIX = "1.3.6.1.4.1.18016";
   
   /** Product JBossAS */
	public static final String PRODUCT = ".1.1";
   
   /** Version 4.0.x */
	public static final String VERSION = ".2";
   
   // Private Data --------------------------------------------------
   
	private String sysDescr;            // system.1
	private SnmpObjectId sysObjectId;   // system.2
	// private long sysUpTime;          // system.3
	private String sysContact;          // system.4
	private String sysName;             // system.5  usually fqdn
	private String sysLocation;         // system.6  where is the system located 
	private final int sysServices = 64; // system.7  (2^(L-1) with L=Layer 7 services)
   
	private ObjectName snmpAgent = SnmpAgentServiceMBean.OBJECT_NAME;
	
	/**
    * CTOR
	 *
	 */
	public MIB2SystemGroupService()
   {
      // empty
   }
   
   // Attributes ----------------------------------------------------
   
   /**
    * @jmx:managed-attribute
    */
	public void setSnmpAgent(ObjectName agent)
   {
      this.snmpAgent = agent;
   }
   
   /**
    * @jmx:managed-attribute
    */
   public ObjectName getSnmpAgent()
   {
      return snmpAgent;
   }
   
   /**
    * @jmx:managed-attribute
    */
   public void setSysDescr(String sysDescr)
   {
      this.sysDescr = sysDescr;
   }
   
   /**
    * @jmx:managed-attribute
    */
   public String getSysDescr()
   {
      return sysDescr;
   }
   
   /**
    * @jmx:managed-attribute
    */
   public SnmpObjectId getSysObjectId()
   {
      return sysObjectId;
   }   

   /**
    * The system uptime in hundreth of a second (TimeTicks)
    * @jmx:managed-attribute
    */
   public SnmpTimeTicks getSysUpTime()
   {
      if (snmpAgent !=null)
      {
         try
         {
            Long upTime = (Long)server.getAttribute(snmpAgent, "Uptime");
            return new SnmpTimeTicks(upTime.longValue() / 10);
         }
         catch (Exception e)
         {
            log.debug("Can't get uptime value from agent");
         }
      }
      // fallback
      return new SnmpTimeTicks(System.currentTimeMillis() / 10);
   }   
   
   /**
    * @jmx:managed-attribute
    */
   public void setSysContact(String sysContact)
   {
      this.sysContact = sysContact;
   } 
   
   /**
    * @jmx:managed-attribute
    */
   public String getSysContact()
   {
      return sysContact;
   }
  
   /**
    * @jmx:managed-attribute
    */
   public void setSysName(String sysName)
   {
      this.sysName = sysName;
   }
   
   /**
    * @jmx:managed-attribute
    */
   public String getSysName()
   {
      return sysName;
   }
   
   /**
    * @jmx:managed-attribute
    */
   public void setSysLocation(String sysLocation)
   {
      this.sysLocation = sysLocation;
   }
   
   /**
    * @jmx:managed-attribute
    */
   public String getSysLocation()
   {
      return sysLocation;
   }
   
   /**
    * @jmx:managed-attribute
    */
   public int getSysServices()
   {
      return sysServices; 
   }   

   // Lifecycle -----------------------------------------------------
   
   protected void createService() throws Exception
   {
      // Determine the sysName from the running server config and the host name.
      if (this.sysName == null)
      {
         String serverConfig = ServerConfigLocator.locate().getServerName();
         ObjectName name = new ObjectName(ServerInfoMBean.OBJECT_NAME_STR);
         String hostAddress = (String)server.getAttribute(name, "HostAddress");

         this.sysName = serverConfig + "@" + hostAddress;
         log.debug("Setting sysName name to " + sysName);
      }
      this.sysObjectId = new SnmpObjectId(JBOSS_PREFIX + PRODUCT + VERSION);   
   }
   
}
