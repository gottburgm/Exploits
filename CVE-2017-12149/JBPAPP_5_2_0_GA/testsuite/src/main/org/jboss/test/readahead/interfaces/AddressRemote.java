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
package org.jboss.test.readahead.interfaces;

import java.rmi.RemoteException;
import javax.ejb.EJBObject;

/**
 * Remote interface for one of the entities used in read-ahead finder tests
 * 
 * @author <a href="mailto:danch@nvisia.com">danch (Dan Christopherson</a>
 * @version $Id: AddressRemote.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $
 * 
 * Revision:
 */
public interface AddressRemote extends EJBObject {
   public java.lang.String getZip() throws RemoteException;
   public void setZip(java.lang.String newZip) throws RemoteException;
   public java.lang.String getState() throws RemoteException;
   public void setState(java.lang.String newState) throws RemoteException;
   public java.lang.String getCity() throws RemoteException;
   public void setCity(java.lang.String newCity) throws RemoteException;
   public void setAddress(java.lang.String newAddress) throws RemoteException;
   public java.lang.String getAddress() throws RemoteException;
   public java.lang.String getAddressId() throws RemoteException;
   public java.lang.String getKey() throws RemoteException;
   public void setAddressId(java.lang.String newAddressId) throws RemoteException;
}