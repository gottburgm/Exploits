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


import javax.ejb.EJBHome;
import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import java.util.Collection;
import javax.ejb.Handle;
import java.sql.Date;
import java.sql.Timestamp;

import org.jboss.test.testbean.interfaces.EnterpriseEntity;
import org.jboss.test.testbean.interfaces.StatefulSession;
import org.jboss.test.testbean.interfaces.StatelessSession;

public interface AllTypesHome extends EJBHome {

    public AllTypes create(String pk) throws RemoteException, CreateException; 
	
	public AllTypes create(boolean aBoolean, byte aByte, short aShort, int anInt, 
		long aLong, float aFloat, double aDouble, /*char aChar,*/ String aString, 
		Date aDate, Timestamp aTimestamp, MyObject anObject )
		
        throws RemoteException, CreateException;


	// automatically generated finders
	public AllTypes findByPrimaryKey(String name)
        throws RemoteException, FinderException;

    public Collection findAll()
        throws RemoteException, FinderException;
	    
	public Collection findByABoolean(boolean b)
        throws RemoteException, FinderException;
	    
	public Collection findByAByte(byte b)
        throws RemoteException, FinderException;
	    
	public Collection findByAShort(short s)
        throws RemoteException, FinderException;
	    
	public Collection findByAnInt(int i)
        throws RemoteException, FinderException;
	    
	public Collection findByALong(long l)
        throws RemoteException, FinderException;
	    
	public Collection findByAFloat(float f)
        throws RemoteException, FinderException;
	    
	public Collection findByADouble(double d)
        throws RemoteException, FinderException;
	    
//	public Collection findByAChar(char c)
//        throws RemoteException, FinderException;
	    
	public Collection findByAString(String s)
        throws RemoteException, FinderException;
	    
	public Collection findByADate(Date d)
        throws RemoteException, FinderException;
	    
	public Collection findByATimestamp(Timestamp t)
        throws RemoteException, FinderException;
	    
	public Collection findByAnObject(MyObject o)
        throws RemoteException, FinderException;
	    
	public Collection findByEnterpriseEntity(EnterpriseEntity e)
        throws RemoteException, FinderException;
	
	public Collection findByStatefulSession(StatefulSession s)
        throws RemoteException, FinderException;

	public Collection findByStatelessSession(StatelessSession s)
        throws RemoteException, FinderException;
	    
	
	// finders defined in jaws.xml
	public Collection findByMinInt(int min)
        throws RemoteException, FinderException;
	    
	public Collection findByIntAndDouble(int i, double d)
        throws RemoteException, FinderException;
	    
	
}
