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

import java.io.Serializable;

/**
 * Primary key class for one of the entities used in read-ahead finder tests.
 * 
 * @author <a href="mailto:danch@nvisia.com">danch (Dan Christopherson</a>
 * @version $Id: AddressPK.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $
 * 
 * Revision:
 */
public class AddressPK implements Serializable {

   public String key = "";
   public String addressId = "";

   public AddressPK() {
   }

   public AddressPK(String key, String addressId) {
      this.key = key;
      this.addressId = addressId;
   }
   public boolean equals(Object obj) {
      if (this.getClass().equals(obj.getClass())) {
         AddressPK that = (AddressPK) obj;
         return this.key.equals(that.key) && this.addressId.equals(that.addressId);
      }
      return false;
   }
   public int hashCode() {
      return key.hashCode()+addressId.hashCode();
   }
}