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

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.ServiceMBean;
import org.opennms.protocols.snmp.SnmpObjectId;
import org.opennms.protocols.snmp.SnmpTimeTicks;

/**
 * MIB2SystemGroupService MBean interface
 * 
 * @author <a href="mailto:hwr@pilhuhn.de">Heiko W. Rupp</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 93935 $
 */
public interface MIB2SystemGroupServiceMBean extends ServiceMBean
{
   /** Default ObjectName */
   ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.jmx:name=SnmpAgent,service=MIB2SystemGroup");
   
   // Attributes ----------------------------------------------------

   /** The snmp agent */
   void setSnmpAgent(ObjectName agent);
   ObjectName getSnmpAgent();
   
   /** The description of the system (system.1) */
   void setSysDescr(String sysDescr);
   String getSysDescr();

   /** The oid of the system (system.2) -- not yet supported */
   SnmpObjectId getSysObjectId();

   /** The uptime of the system (system.3) */
   SnmpTimeTicks getSysUpTime();
   
   /** The system contact person (system.4) */   
   void setSysContact(String sysContact);
   String getSysContact();

   /** The (node)-name of the system (system.5) */
   void setSysName(String sysName);
   String getSysName();

	/** The location of the system (system.6) */
   void setSysLocation(String sysLocation);
	String getSysLocation();

	/** The services the system provides (system.7) */
	int getSysServices();

}