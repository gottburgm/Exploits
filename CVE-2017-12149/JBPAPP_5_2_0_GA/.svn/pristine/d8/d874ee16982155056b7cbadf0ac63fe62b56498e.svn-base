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
package org.jboss.test.bench.ejb;

import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;

import org.jboss.test.bench.interfaces.AComplexPK;

public class ComplexEntityBean implements EntityBean {
    public boolean aBoolean;
    public int anInt;
    public long aLong;
    public double aDouble;
    public String aString;

	public String otherField;
   
	public AComplexPK ejbCreate(boolean aBoolean, int anInt, long aLong, double aDouble, String aString) throws RemoteException, CreateException {

        this.aBoolean = aBoolean;
        this.anInt = anInt;
        this.aLong = aLong;
        this.aDouble = aDouble;
        this.aString = aString;

		this.otherField = "";
		return null;
	}
   
	public void ejbPostCreate(boolean aBoolean, int anInt, long aLong, double aDouble, String aString) throws RemoteException, CreateException {}
      
	public String getOtherField() throws RemoteException {
		return otherField;
	}
   
	public void setOtherField(String otherField) throws RemoteException {
		this.otherField = otherField;
	}

	public void ejbStore() throws RemoteException {}

	public void ejbLoad() throws RemoteException {}
	
	public void ejbActivate() throws RemoteException {}
	
	public void ejbPassivate() throws RemoteException {}
	
	public void ejbRemove() throws RemoteException {}

	public void setEntityContext(EntityContext e) throws RemoteException {}

	public void unsetEntityContext() throws RemoteException {}

}
