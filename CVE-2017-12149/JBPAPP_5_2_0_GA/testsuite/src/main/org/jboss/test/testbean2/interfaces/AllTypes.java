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
package org.jboss.test.testbean2.interfaces;


import javax.ejb.EJBObject;
import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import java.util.Collection;
import javax.ejb.Handle;
import java.sql.Date;
import java.sql.Timestamp;

public interface AllTypes extends EJBObject {

	// business methods
	public void updateAllValues(boolean aBoolean, byte aByte, short aShort, int anInt, 
		long aLong, float aFloat, double aDouble, /*char aChar,*/ String aString, 
		Date aDate, Timestamp aTimestamp, MyObject anObject ) throws RemoteException;
        
	public void addObjectToList(Object anObject) throws RemoteException;
	public void removeObjectFromList(Object anObject) throws RemoteException;
	public Collection getObjectList() throws RemoteException;
	                                   	
	public String callBusinessMethodA() throws RemoteException;
	
	public boolean getBoolean() throws RemoteException;
    public byte getByte() throws RemoteException;
	public short getShort() throws RemoteException;
	public int getInt() throws RemoteException;
	public long getLong() throws RemoteException;
	public float getFloat() throws RemoteException;
	public double getDouble() throws RemoteException;
	//public char getChar() throws RemoteException;
	public String getString() throws RemoteException;
	public Date getDate() throws RemoteException;
	public Timestamp getTimestamp() throws RemoteException;
	
	public MyObject getObject() throws RemoteException;
	
	public Handle getStateful() throws RemoteException;
	public Handle getStateless() throws RemoteException;
	public Handle getEntity() throws RemoteException;
	
	
}
