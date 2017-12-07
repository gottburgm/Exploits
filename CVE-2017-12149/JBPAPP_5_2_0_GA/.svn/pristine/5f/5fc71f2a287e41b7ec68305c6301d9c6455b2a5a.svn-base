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
package org.jboss.test.banknew.ejb;

/**
 * CMP layer for bank/Transaction.
 */
public abstract class TransactionCMP
   extends org.jboss.test.banknew.ejb.TransactionBean
   implements javax.ejb.EntityBean
{

   public org.jboss.test.banknew.interfaces.TransactionData getData()
   {
      org.jboss.test.banknew.interfaces.TransactionData dataHolder = null;
      try
      {
         dataHolder = new org.jboss.test.banknew.interfaces.TransactionData();

         dataHolder.setId( getId() );
         dataHolder.setAcountId( getAcountId() );
         dataHolder.setType( getType() );
         dataHolder.setAmount( getAmount() );
         dataHolder.setDate( getDate() );
         dataHolder.setDescription( getDescription() );

      }
      catch (RuntimeException e)
      {
         throw new javax.ejb.EJBException(e);
      }

      return dataHolder;
   }

   public void setData( org.jboss.test.banknew.interfaces.TransactionData dataHolder )
   {
      try
      {
         setAcountId( dataHolder.getAcountId() );
         setType( dataHolder.getType() );
         setAmount( dataHolder.getAmount() );
         setDate( dataHolder.getDate() );
         setDescription( dataHolder.getDescription() );

      }
      catch (Exception e)
      {
         throw new javax.ejb.EJBException(e);
      }
   }

   public void ejbLoad() throws java.rmi.RemoteException
   {
      super.ejbLoad();
   }

   public void ejbStore() throws java.rmi.RemoteException
   {
         super.ejbStore();
   }

   public void ejbActivate() throws java.rmi.RemoteException
   {
      super.ejbActivate();
   }

   public void ejbPassivate() throws java.rmi.RemoteException
   {
      super.ejbPassivate();

   }

   public void setEntityContext(javax.ejb.EntityContext ctx) throws java.rmi.RemoteException
   {
      super.setEntityContext(ctx);
   }

   public void unsetEntityContext() throws java.rmi.RemoteException
   {
      super.unsetEntityContext();
   }

   public void ejbRemove() throws java.rmi.RemoteException, javax.ejb.RemoveException
   {
      super.ejbRemove();

   }

   public abstract java.lang.String getId() ;

   public abstract void setId( java.lang.String id ) ;

   public abstract java.lang.String getAcountId() ;

   public abstract void setAcountId( java.lang.String acountId ) ;

   public abstract int getType() ;

   public abstract void setType( int type ) ;

   public abstract float getAmount() ;

   public abstract void setAmount( float amount ) ;

   public abstract java.util.Date getDate() ;

   public abstract void setDate( java.util.Date date ) ;

   public abstract java.lang.String getDescription() ;

   public abstract void setDescription( java.lang.String description ) ;

}
