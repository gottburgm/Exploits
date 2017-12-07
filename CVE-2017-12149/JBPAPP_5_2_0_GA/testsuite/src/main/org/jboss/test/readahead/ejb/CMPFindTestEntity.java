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

/**
 * Implementation class for one of the entities used in read-ahead finder
 * tests
 * 
 * @author <a href="mailto:danch@nvisia.com">danch (Dan Christopherson</a>
 * @version $Id: CMPFindTestEntity.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $
 * 
 * Revision:
 */
public class CMPFindTestEntity implements EntityBean {
   org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(getClass());
   
   EntityContext entityContext;
   public String key;
   public String name;
   public String rank;
   public String serialNumber;
   private boolean modified;
   
   public String ejbCreate(String key) throws CreateException {
      this.key = key;
      return key;
   }
   public boolean isModified() {
      return modified;
   }
   public void ejbPostCreate(String key) throws CreateException {
   }
   public void ejbRemove() throws RemoveException {
   }
   public void ejbActivate() {
   }
   public void ejbPassivate() {
   }
   public void ejbLoad() {
      modified = false;
   }
   public void ejbStore() {
      modified = false;
   }
   public void setEntityContext(EntityContext entityContext) {
      this.entityContext = entityContext;
   }
   public void unsetEntityContext() {
      entityContext = null;
   }
   public String getKey() {
      return key;
   }
   public void setName(String newName) {
      name = newName;
   }
   public String getName() {
      return name;
   }
   public void setRank(String newRank) {
      rank = newRank;
      modified = true;
   }
   public String getRank() {
      return rank;
   }
   public void setSerialNumber(String newSerialNumber) {
      serialNumber = newSerialNumber;
      modified = true;
   }
   public String getSerialNumber() {
      return serialNumber;
   }
}
