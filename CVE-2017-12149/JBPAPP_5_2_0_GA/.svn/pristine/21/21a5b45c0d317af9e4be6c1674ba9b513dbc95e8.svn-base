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
package org.jboss.test.bench.interfaces;

import java.io.Serializable;
import java.io.IOException;

public class AComplexPK implements Serializable{

    public boolean aBoolean;
    public int anInt;
    public long aLong;
    public double aDouble;
    public String aString;

    public AComplexPK() {};
    

    public AComplexPK(boolean aBoolean, int anInt, long aLong, double aDouble, String aString) {

        this.aBoolean = aBoolean;
        this.anInt = anInt;
        this.aLong = aLong;
        this.aDouble = aDouble;
        this.aString = aString;
    }
	
	public boolean equals(Object other) {
		if (other != null && other instanceof AComplexPK) {
			AComplexPK otherPK = (AComplexPK)other;
			return ((aBoolean == otherPK.aBoolean) &&
				(anInt == otherPK.anInt) &&
				(aLong == otherPK.aLong) &&
				(aDouble == otherPK.aDouble) &&
				(aString == null ? otherPK.aString == null : aString.equals(otherPK.aString)));
		} else return false;
	}
				
	
	public int hashCode() {
		
		// Missing the double but ok for test
		
		return anInt*
				(new Long(aLong)).intValue()*
				(new Double(aDouble)).intValue()*
				aString.hashCode();
	}
} 

