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

public class SimpleEntityBean implements EntityBean {
	public Integer pk;
	public int field;
	
	public Integer ejbCreate(int pk) throws RemoteException, CreateException {
		this.pk = new Integer(pk);
		field = 0;
		return null;
	}
	
	public void ejbPostCreate(int pk) throws RemoteException, CreateException {}
	
	public int getField() throws RemoteException {
		return field;
	}
	
	public void setField(int field) throws RemoteException {
		this.field = field;
	}
	
	public void ejbStore() throws RemoteException {}

	public void ejbLoad() throws RemoteException {}
	
	public void ejbActivate() throws RemoteException {}
	
	public void ejbPassivate() throws RemoteException {}
	
	public void ejbRemove() throws RemoteException {}

	public void setEntityContext(EntityContext e) throws RemoteException {}

	public void unsetEntityContext() throws RemoteException {}
	

}

