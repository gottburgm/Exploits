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
package org.jboss.test.jca.ejb;

import java.rmi.RemoteException;
import java.sql.Connection;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.jboss.test.jca.interfaces.Reentrant;

/**
 * ReentrantBean.java tests if CachedConnectionManager works with reentrant ejbs.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version <tt>$Revision: 81036 $</tt>
 *
 * @ejb.bean
 *    jndi-name="ejb/jca/Reentrant"
 *    name="Reentrant"
 *    type="BMP"
 *    view-type="remote"
 *    reentrant="true"
 * @ejb.pk class="java.lang.Integer"
 * @ejb.transaction
 *    type="Required"
 *
 */

public class ReentrantBean implements EntityBean
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   private Integer id;

   private EntityContext ctx;

   public ReentrantBean()
   {

   }

   /**
    * Creates a new <code>ejbCreate</code> instance.
    *
    * @param id an <code>Integer</code> value
    * @param other a <code>Reentrant</code> value
    * @exception CreateException if an error occurs
    * @exception RemoteException if an error occurs
    *
    * @ejb.create-method 
    */
   public Integer ejbCreate(Integer id, Reentrant other) throws CreateException, RemoteException
   {
      this.id = id;
      return id;
   }

   /**
    * Creates a new <code>ejbPostCreate</code> instance.
    *
    * @param id an <code>Integer</code> value
    * @param other a <code>Reentrant</code> value
    * @exception CreateException if an error occurs
    * @exception RemoteException if an error occurs
    */
   public void ejbPostCreate(Integer id, Reentrant other) throws CreateException, RemoteException
   {
      this.id = id;
      Reentrant me = (Reentrant) ctx.getEJBObject();
      Connection c = null;
      try
      {
         try
         {
            DataSource ds = (DataSource) new InitialContext().lookup("java:/DefaultDS");
            c = ds.getConnection();
            if (other != null)
            {
               other.doSomething(me);
            }
         }
         finally
         {
            c.close();
         }
      }
      catch (Exception e)
      {
         throw new CreateException("could not get DataSource or Connection" + e.getMessage());
      }
   }

   /**
    * Describe <code>doSomething</code> method here.
    *
    * @param first a <code>Reentrant</code> value
    * @exception RemoteException if an error occurs
    *
    * @ejb.interface-method 
    */
   public void doSomething(Reentrant first) throws RemoteException
   {
      if (first != null)
      {
         first.doSomething(null);
      }
   }

   public Integer ejbFindByPrimaryKey(Integer id)
   {
      return id;
   }

   // implementation of javax.ejb.EntityBean interface

   public void ejbActivate()
   {
   }

   public void ejbLoad()
   {
      this.id = (Integer) ctx.getPrimaryKey();
   }

   public void ejbPassivate()
   {
   }

   public void ejbRemove() throws EJBException
   {
   }

   public void ejbStore() throws EJBException
   {
   }

   public void setEntityContext(EntityContext ctx)
   {
      this.ctx = ctx;
   }

   public void unsetEntityContext()
   {
      ctx = null;
   }

   public String toString()
   {
      if (id == null)
         return null;
      else
         return id.toString();
   }
}
