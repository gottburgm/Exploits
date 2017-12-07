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
package org.jboss.test.proxycompiler.beans.ejb; // Generated package name

import java.rmi.RemoteException;

import javax.ejb.EntityBean;

import javax.ejb.EntityContext;
import javax.ejb.CreateException;

import org.jboss.test.proxycompiler.Util;

/**
 * ReadOnlyBean.java
 *
 *
 * Created: Tue Jan 22 17:13:36 2002
 *
 * @author <a href="mailto:neale@isismanor.co.uk">Neale Swinnerton</a>
 * @version
 *
 *
 * @ejb:bean   name="ProxyCompilerTest"
 *             jndi-name="ProxyCompilerTest"
 *             local-jndi-name="LocalProxyCompilerTest"
 *             view-type="both"
 *             type="CMP"
 *             cmp-version="2.x"
 *             primkey-field="pk"
 * @ejb:pk class="java.lang.Integer"
 * @ejb:finder signature="java.util.Collection findAll()"
 */

public abstract class ProxyCompilerTestBean implements EntityBean  
{
   public ProxyCompilerTestBean ()
   {
      
   }

   /**
    * @ejb:create-method
    */
   public Integer ejbCreate(Integer pk)
      throws CreateException
   {
      setPk(pk);
      return pk;
   }

   public void ejbPostCreate(Integer pk)
   {
   }

   /**
    * @ejb:persistent-field
    * @ejb:interface-method
    */
   public abstract Integer getPk();

   /**
    * @ejb:interface-method
    */
   public abstract void setPk(Integer pk);

   /**
    * @ejb:persistent-field
    * @ejb:interface-method
    */
   public abstract boolean getBool();

   /**
    * @ejb:interface-method
    */
   public abstract void setBool(boolean arg);

   /**
    * @ejb:persistent-field
    * @ejb:interface-method
    */
   public abstract byte getByte();

   /**
    * @ejb:interface-method
    */
   public abstract void setByte(byte arg);

   /**
    * @ejb:persistent-field
    * @ejb:interface-method
    */
   public abstract char getChar();

   /**
    * @ejb:interface-method
    */
   public abstract void setChar(char arg);

   /**
    * @ejb:persistent-field
    * @ejb:interface-method
    */
   public abstract double getDouble();

   /**
    * @ejb:interface-method
    */
   public abstract void setDouble(double arg);

   /**
    * @ejb:persistent-field
    * @ejb:interface-method
    */
   public abstract float getFloat();

   /**
    * @ejb:interface-method
    */
   public abstract void setFloat(float arg);

   /**
    * @ejb:persistent-field
    * @ejb:interface-method
    */
   public abstract int getInt();

   /**
    * @ejb:interface-method
    */
   public abstract void setInt(int arg);

   /**
    * @ejb:persistent-field
    * @ejb:interface-method
    */
   public abstract long getLong();

   /**
    * @ejb:interface-method
    */
   public abstract void setLong(long arg);

   /**
    * @ejb:persistent-field
    * @ejb:interface-method
    */
   public abstract short getShort();

   /**
    * @ejb:interface-method
    */
   public abstract void setShort(short arg);

   /**
    * @ejb:persistent-field
    * @ejb:interface-method
    */
   public abstract Object[] getObjectArray();

   /**
    * @ejb:interface-method
    */
   public abstract void setObjectArray(Object[] arg);

   /**
    * @ejb:persistent-field
    * @ejb:interface-method
    */
   public abstract int[] getIntArray();

   /**
    * @ejb:interface-method
    */
   public abstract void setIntArray(int[] arg);

   /**
    * @ejb:interface-method
    */
   public  boolean noArgsMethod() {
      return true;
   }

   /**
    * @ejb:interface-method
    */
   public String complexSignatureMethod(int i, Object ref, int[] ints, Object[] objectRefs) {
      return Util.getStringRepresentation(i, ref, ints, objectRefs);
   }
   
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
   
   public void ejbRemove() throws RemoteException 
   {
   }
   
   public void setEntityContext(EntityContext ctx) throws RemoteException 
   {
   }
   
   public void unsetEntityContext() throws RemoteException 
   {
   }
   
}// ReadOnlyBean
