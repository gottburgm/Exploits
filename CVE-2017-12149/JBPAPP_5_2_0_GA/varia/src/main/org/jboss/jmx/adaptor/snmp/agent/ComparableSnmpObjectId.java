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

import org.opennms.protocols.snmp.SnmpObjectId;

/**
 * Provide SnmpObjectIds that are Comparable to be used
 * in SortedSets etc.
 * @author <a href="mailto:hwr@pilhuhn.de">Heiko W. Rupp</a>
 * @version $Revision: 81038 $
 */
public class ComparableSnmpObjectId extends SnmpObjectId implements Comparable 
{

	public ComparableSnmpObjectId(String oid) 
	{
		super(oid);
	}
	
	public ComparableSnmpObjectId(SnmpObjectId oid) 
	{
		super(oid);
	}
	
	public ComparableSnmpObjectId(int[] identifiers)
	{
		super(identifiers);
	}

	/**
	 * Compare to the passed object. Uses compare()
	 * from the underlying snmp-library
	 * @see SnmpObjectId.compare()
	 * @param o Object to compare with (Usually a ComparableSnmpObjectId)
	 * @return -1, if no SnmpObjectId passed in, the result of the underlying compare otherwise.
	 */
	public int compareTo(Object o) 
	{
		
		if (o==null)
			return -1;
		
		if (!(o instanceof SnmpObjectId))
			return -1;
		
		return this.compare((SnmpObjectId)o);
	}
	
	/**
	 * This object is a leaf if the last part of the oid parts is a 0 (Zero)
	 * @return true if the oid ends in 0 
	 */
	public boolean isLeaf() 
	{
		int[] ids = getIdentifiers();
		if (ids.length==0) { // no data present (should not happen)
			return false;
		}
		if (ids[ids.length-1]==0)
		{
			return true;
		}
		return false;
	}

	/**
	 * Removes the last oid-component.
	 * Example  .1.2.3.4.0 as input yields .1.2.3.4 as output
	 * @return an oid with the last part removed.
	 */
	public ComparableSnmpObjectId removeLastPart()
	{
		int[] ids = getIdentifiers();
		
		int[] outs = new int[ids.length-1];
		int len = ids.length-1;
		for (int i = 0; i<len ; i++)
		{
			outs[i]=ids[i];
		}
		ComparableSnmpObjectId out = new ComparableSnmpObjectId(outs);
		return out;
	}
	
	/**
	 * Build an oid where the second last component 
	 * (after removing a  .0 of a leaf) is increased by 1.
	 * The last component is removed, to the result actually forms
	 * the root of a subtree, that is adjacent to the subtree this
	 * object is in.
	 * Example .1.2.3.4.0 -> .1.2.4 
	 * Example .1.2.3.4.5 -> .1.2.4
	 * @return
	 */
	public ComparableSnmpObjectId getNextArc()
	{
		ComparableSnmpObjectId cid = this;
		if (isLeaf())
		{
			cid = removeLastPart();
		}
		cid = cid.removeLastPart(); 
		
		int[] ids = cid.getIdentifiers();
		int[] ods = new int[ids.length];
		System.arraycopy(ids,
						0,
						ods,
						0,
						ids.length);
		
		int len = ods.length-1;
		ods[len]++;
		
		ComparableSnmpObjectId ret = new ComparableSnmpObjectId(ods);
		return ret;
	}
	
}
