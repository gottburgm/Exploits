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
package org.jboss.test.cmp2.idxandusersql.ejb;

import java.util.Collection;
import javax.ejb.EntityBean;

/**
 * Test the dbindex feature
 * @author heiko.rupp@cellent.de
 * @version $Revision: 81036 $
 * 
 * @ejb.bean name="CMR2"
 * 	type="CMP"
 * 	cmp-version="2.x"
 * 	view-type="local"
 *  primkey-field="pKey2"
 * 
 * @ejb.util generate="false"
 * 	
 * @ejb.persistence table-name = "CMR2"
 * 
 * @jboss.persistence
 * 	create-table = "true"
 * 	remove-table = "true"
 * 	
 */
public abstract class CMR2Bean implements EntityBean
{

   /**
    * We don't call them, just have them here to 
    * satisfy the cmp-engine
    * @ejb.create-method
    */
   public String ejbCreate() throws javax.ejb.CreateException
   {
      return null;
   }

   public void ejbPostCreate()
   {

   }

   /**
    * @ejb.interface-method 
    * @param pKey2
    */
   public abstract void setPKey2(String pKey2);

   /** 
    * @ejb.interface-method
    * @ejb.persistent-field 
    */
   public abstract String getPKey2();

   /**
    * This field gets a <dbindex/> that we want to
    * look up in the database to see if the index
    * was really created on the file. 
    * @ejb.interface-method
    * @ejb.persistent-field
    * @todo set the dbindex property here with a modern xdoclet*  
    */
   public abstract String getFoo2();

   /**
    * This one is not indexed 
    * @ejb.interface-method
    * @ejb.persistent-field
    */
   public abstract String getBar2();

   // 
   // many-many relation to CMR2
   // 
   /**
    * @ejb.interface-method
    */
   public abstract Collection getIdxs();

   /**
    * @ejb.interface-method
    */
   public abstract void setIdxs(Collection Idxs);

}
