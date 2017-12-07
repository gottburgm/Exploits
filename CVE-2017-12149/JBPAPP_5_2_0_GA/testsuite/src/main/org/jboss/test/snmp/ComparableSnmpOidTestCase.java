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
package org.jboss.test.snmp;

import java.util.SortedSet;
import java.util.TreeSet;

import org.jboss.jmx.adaptor.snmp.agent.ComparableSnmpObjectId;

import junit.framework.TestCase;

/**
 * Tests for the ComparableSnmpObjectId, a Subclass of SnmpObjectId from
 * the joesnmp package. Most tests are trivial.
 * @author <a href="mailto:hwr@pilhuhn.de">Heiko W. Rupp</a>
 * @version $Revision: 81036 $
 */
public class ComparableSnmpOidTestCase extends TestCase
{
	
	/**
	 * Make sure, that the passed oid which does not end in .0
	 * is not detected as leaf.
	 */
	public void testIsNotLeaf()
	{
		ComparableSnmpObjectId coid = new ComparableSnmpObjectId(".1.2.3.4");
		boolean res = coid.isLeaf();
		assertFalse(res);
	}
	
	/**
	 * Make sure that the passed oid ending in .0 is detected as leaf.
	 */
	public void testIsLeaf()
	{
		ComparableSnmpObjectId coid = new ComparableSnmpObjectId(".1.2.3.4.0");
		boolean res = coid.isLeaf();
		assertTrue(res);
	}

	/**
	 * Make sure that the passed oid ending in .0 is detected as leaf.
	 */
	public void testIsLeaf2()
	{
		ComparableSnmpObjectId coid = new ComparableSnmpObjectId("1.2.3.4.0");
		boolean res = coid.isLeaf();
		assertTrue(res);
	}

	
	/**
	 * See if the last part of an oid is correctly chopped of.
	 *
	 */
	public void testRemoveLastPart()
	{
		ComparableSnmpObjectId coid = new ComparableSnmpObjectId("1.2.3.4.0");
		ComparableSnmpObjectId res = coid.removeLastPart();
		assertEquals(".1.2.3.4",res.toString());
	}
	
	/**
	 * See if compareTo from Comparable works as expected.
	 * This is needed for use of the ComparableSnmpObjectId in SortedSets etc.
	 * @see java.lang.Comparable
	 */
	public void testCompareTo1()
	{
		ComparableSnmpObjectId coid = new ComparableSnmpObjectId("1.2.3.4.0");
		ComparableSnmpObjectId coid2 = new ComparableSnmpObjectId(".1.2.3.4.0");
		int res = coid.compareTo(coid2);
		assertEquals(0,res);
	}

	/**
	 * See if compareTo from Comparable works as expected.
	 * This is needed for use of the ComparableSnmpObjectId in SortedSets etc.
	 * @see java.lang.Comparable
	 */
	public void testCompareTo2()
	{
		ComparableSnmpObjectId coid = new ComparableSnmpObjectId("1.2.3.4");
		ComparableSnmpObjectId coid2 = new ComparableSnmpObjectId("1.2.3.4.0");
		int res = coid.compareTo(coid2);
		assertTrue(res!=0);
	}

	/**
	 * See if compareTo from Comparable works as expected.
	 * This is needed for use of the ComparableSnmpObjectId in SortedSets etc.
	 * @see java.lang.Comparable
	 */
	public void testCompareTo3()
	{
		ComparableSnmpObjectId coid  = new ComparableSnmpObjectId("1.2.3.4.1");
		ComparableSnmpObjectId coid2 = new ComparableSnmpObjectId("1.2.3.4.2");
		int res = coid.compareTo(coid2);
		assertTrue(res<0);
	}

	/**
	 * See if compareTo from Comparable works as expected.
	 * This is needed for use of the ComparableSnmpObjectId in SortedSets etc.
	 * @see java.lang.Comparable
	 */
	public void testCompareTo4()
	{
		ComparableSnmpObjectId coid  = new ComparableSnmpObjectId("1.2.3.4.2");
		ComparableSnmpObjectId coid2 = new ComparableSnmpObjectId("1.2.3.4.1");
		int res = coid.compareTo(coid2);
		assertTrue(res>0);
	}

	/**
	 * See if compareTo from Comparable works as expected.
	 * This is needed for use of the ComparableSnmpObjectId in SortedSets etc.
	 * @see java.lang.Comparable
	 */
	public void testCompareTo5()
	{
		ComparableSnmpObjectId coid  = new ComparableSnmpObjectId("1.2.3.4.2");
		Object coid2 = new Object();
		int res = coid.compareTo(coid2);
		assertTrue(res == -1);
	}

	
	public void testGetNextArc() 
	{
		ComparableSnmpObjectId coid = new ComparableSnmpObjectId("1.2.3.4");
		ComparableSnmpObjectId res = coid.getNextArc();
		assertEquals(".1.2.4",res.toString());
	}

	public void testGetNextArc2() 
	{
		ComparableSnmpObjectId coid = new ComparableSnmpObjectId(".1.2.3.4.5");
		ComparableSnmpObjectId res = coid.getNextArc();
		assertEquals(".1.2.3.5",res.toString());
	}

	public void testGetNextArc3() 
	{
		ComparableSnmpObjectId coid = new ComparableSnmpObjectId(".1.2.3.4.0");
		ComparableSnmpObjectId res = coid.getNextArc();
		assertEquals(".1.2.4",res.toString()); 
	}

	
	public void testGetSubtree()
	{
		SortedSet s = new TreeSet();
		ComparableSnmpObjectId coid = new ComparableSnmpObjectId("1.2.3.4.0");
		s.add(coid);
		coid = new ComparableSnmpObjectId("1.2.3.5.0");
		s.add(coid);
		coid = new ComparableSnmpObjectId("1.2.3.6.0");
		s.add(coid);
		
		ComparableSnmpObjectId c2 = new ComparableSnmpObjectId("1.2.3.4.1");
		SortedSet subset = s.tailSet(c2);
		assertEquals(2,subset.size());
		
		subset = s.headSet(c2);
		assertEquals(1,subset.size());
	}

}