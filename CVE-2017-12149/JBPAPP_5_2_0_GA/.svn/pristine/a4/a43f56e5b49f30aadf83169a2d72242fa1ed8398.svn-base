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
package org.jboss.test.dbtest.bean;

import java.rmi.*;
import javax.ejb.*;

public class RecordBean implements EntityBean {
	private EntityContext entityContext;
	public String name;
	public String address;
	
	
	public String ejbCreate(String name) throws RemoteException, CreateException {
		
		this.name = name;
		this.address = "";
		return null;
	}

   /** what is this for?! testing NoSuchEntityException?
	public String ejbFindByPrimaryKey(String name) throws RemoteException, FinderException {
		
		return name;
	}
    */

	public void ejbPostCreate(String name) throws RemoteException, CreateException {
	}
	
	public void ejbActivate() throws RemoteException {
	}
	
	public void ejbLoad() throws RemoteException {
	}
	
	public void ejbPassivate() throws RemoteException {
	}
	
	public void ejbRemove() throws RemoteException, RemoveException {
	}
	
	public void ejbStore() throws RemoteException {
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getAddress() {
		return address;
	}
	
	public String getName() {
		return name;
	}
	
	
	public void setEntityContext(EntityContext context) throws RemoteException {
		entityContext = context;
	}
	
	public void unsetEntityContext() throws RemoteException {
		entityContext = null;
	}

}
