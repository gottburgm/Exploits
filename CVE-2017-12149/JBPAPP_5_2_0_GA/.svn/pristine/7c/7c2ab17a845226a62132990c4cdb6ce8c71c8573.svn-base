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
package org.jboss.test.jmx.ejb;


import java.rmi.RemoteException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;
/**
 * EntityA.java
 *
 *
 * Created: Wed Mar  6 20:08:11 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 *
 *
 * @ejb:bean   name="EntityA"
 *             jndi-name="EntityA"
 *             view-type="remote"
 *             type="CMP"
 *             cmp-version="2.x"
 *             primkey-field="id"
 * @ejb:pk class="java.lang.Integer"
 * @ejb:finder signature="java.util.Collection findAll()"
 *             unchecked="yes"
 */

public abstract class EntityABean implements EntityBean  
{
   public EntityABean ()
   {
      
   }

   
   
   /**
    * Abstract cmp2 field get-set pair for field id
    * Get the value of id
    * @return value of id
    *
    * @ejb:interface-method
    * @ejb:persistent-field
    */
   public abstract Integer getId();
   
   /**
    * Set the value of id
    * @param id  Value to assign to id
    *
    * @ejb:interface-method view-type="remote"
    */
   public abstract void setId(Integer id);
   
   
   
   /**
    * Abstract cmp2 field get-set pair for field value
    * Get the value of value
    * @return value of value
    *
    * @ejb:interface-method
    * @ejb:persistent-field
    */
   public abstract String getValue();
   
   /**
    * Set the value of value
    * @param value  Value to assign to value
    *
    * @ejb:interface-method view-type="remote"
    */
   public abstract void setValue(String value);
   

   
   public void ejbActivate() throws RemoteException 
   {
   }
   
   public void ejbPassivate() throws RemoteException 
   {
   }
   
   public void ejbLoad() throws RemoteException 
   {
   }
   
   public void ejbStore() throws RemoteException 
   {
   }
   
   public void ejbRemove() throws RemoteException, RemoveException 
   {
   }
   
   public void setEntityContext(EntityContext ctx) throws RemoteException 
   {
   }
   
   public void unsetEntityContext() throws RemoteException 
   {
   }
   
}// EntityA
