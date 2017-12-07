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
package org.jboss.test.readahead.ejb;

import javax.ejb.EntityBean;
import javax.ejb.CreateException;
import javax.ejb.RemoveException;
import javax.ejb.EntityContext;
import org.jboss.test.readahead.interfaces.AddressPK;

/**
 * Implementation class for one of the entities used in read-ahead finder
 * tests
 * 
 * @author <a href="mailto:danch@nvisia.com">danch (Dan Christopherson</a>
 * @version $Id: Address.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $
 * 
 * Revision:
 */
public class Address implements EntityBean {
   EntityContext entityContext;
   public String key;
   public String addressId;
   public String address;
   public String city;
   public String state;
   public String zip;
   
   public AddressPK ejbCreate(String key, String addressId, String address, 
                              String city, String state, String zip) 
      throws CreateException 
   {
      this.key = key;
      this.addressId = addressId;
      this.address = address;
      this.city = city;
      this.state = state;
      this.zip = zip;
      return new AddressPK(key, addressId);
   }
   public void ejbPostCreate(String key, String addressId, String address, 
                             String city, String state, String zip) throws CreateException {
   }
   public void ejbRemove() throws RemoveException {
   }
   public void ejbActivate() {
   }
   public void ejbPassivate() {
   }
   public void ejbLoad() {
   }
   public void ejbStore() {
   }
   public void setEntityContext(EntityContext entityContext) {
      this.entityContext = entityContext;
   }
   public void unsetEntityContext() {
      entityContext = null;
   }
   public void setKey(String newKey) {
      key = newKey;
   }
   public String getKey() {
      return key;
   }
   public void setAddressId(String newAddressId) {
      addressId = newAddressId;
   }
   public String getAddressId() {
      return addressId;
   }
   public void setAddress(String newAddress) {
      address = newAddress;
   }
   public String getAddress() {
      return address;
   }
   public void setCity(String newCity) {
      city = newCity;
   }
   public String getCity() {
      return city;
   }
   public void setState(String newState) {
      state = newState;
   }
   public String getState() {
      return state;
   }
   public void setZip(String newZip) {
      zip = newZip;
   }
   public String getZip() {
      return zip;
   }
}