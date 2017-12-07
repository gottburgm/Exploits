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
package org.jboss.jmx.adaptor.snmp.config.attribute;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * List of mbean attribute mappings to snmp oids.
 * 
 * @author Heiko W. Rupp <pilhuhn@user.sf.net>
 * @version $Revision: 81038 $
 */
public class AttributeMappings
{
   private List mbeans = new ArrayList();
	
	public void addMonitoredMBean(ManagedBean mbean)
   {
	   mbeans.add(mbean);
	}
	
	public Iterator iterator()
   {
		return mbeans.iterator();
	}
	
	public int size()
   {
		return mbeans.size();
	}
}
