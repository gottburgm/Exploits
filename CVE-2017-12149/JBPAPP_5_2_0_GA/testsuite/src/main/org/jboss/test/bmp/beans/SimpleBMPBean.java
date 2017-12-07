/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.bmp.beans;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.Vector;

import javax.ejb.CreateException;
import javax.ejb.DuplicateKeyException;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.FinderException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.jboss.logging.Logger;
import org.jboss.test.bmp.interfaces.SimpleBMPHome;

public class SimpleBMPBean implements EntityBean
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   Logger log = Logger.getLogger(getClass());
   EntityContext ctx = null;
   DataSource ds = null;

   // bmp fields
   Integer id;
   String name;
   boolean ejbStoreInvoked = false;
   boolean tempEjbStoreInvoked = false;
   
   public Integer ejbCreate (int _id, String _name)
      throws CreateException, RemoteException
   {
      log.debug ("ejbCreate (int, String) called");
      
      id = new Integer (_id);
      
      boolean dublicate = false;
      
      Connection con = null;
      try
      {
         con = ds.getConnection ();
         Statement s = con.createStatement ();
         ResultSet rs = s.executeQuery ("SELECT id FROM simplebean WHERE id=" + id.toString ());
         dublicate = rs.next ();
         rs.close ();
         s.close ();
         
         if (!dublicate)
         {
            PreparedStatement ps = con.prepareStatement ("INSERT INTO simplebean VALUES (?,?)");
            ps.setInt (1, _id);
            ps.setString (2, _name);
            ps.execute ();
            ps.close ();

            name = _name;
         }
      }
      catch (Exception _e)
      {
         throw new EJBException ("couldnt create: "+_e.getMessage ());
      }
      finally 
      {
         try
         {
            if (con != null)
               con.close ();
         }
         catch (Exception _sqle)
         {
         }
      }
      
      if (dublicate)
         throw new DuplicateKeyException ("Bean with id="+_id+" already exists.");

      return id;      
   }
   
   public Integer ejbCreateMETHOD (int _id, String _name)
      throws CreateException, RemoteException
   {
      log.debug ("ejbCreateMETHOD (int, String) called");
      
      id = new Integer (_id);
      
      boolean dublicate = false;
      
      Connection con = null;
      try
      {
         con = ds.getConnection ();
         Statement s = con.createStatement ();
         ResultSet rs = s.executeQuery ("SELECT id FROM simplebean WHERE id=" + id.toString ());
         dublicate = rs.next ();
         rs.close ();
         s.close ();
         
         if (!dublicate)
         {
            PreparedStatement ps = con.prepareStatement ("INSERT INTO simplebean VALUES (?,?)");
            ps.setInt (1, _id);
            ps.setString (2, _name);
            ps.execute ();
            ps.close ();

            name = _name;
         }
      }
      catch (Exception _e)
      {
         throw new EJBException ("couldnt create: "+_e.getMessage ());
      }
      finally 
      {
         try
         {
            if (con != null)
               con.close ();
         }
         catch (Exception _sqle)
         {
         }
      }
      
      if (dublicate)
         throw new DuplicateKeyException ("Bean with id="+_id+" already exists.");

      return id;      
   }
   
   public void ejbPostCreate (int _id, String _name)
      throws CreateException, RemoteException
   {
      log.debug ("ejbPostCreate (int, String) called");
      
      tempEjbStoreInvoked = false;
      // Do a find all to see whether ejbStore gets invoked
      SimpleBMPHome home = (SimpleBMPHome) ctx.getEJBHome();
      try
      {
         home.findAll();
      }
      catch (FinderException e)
      {
         throw new RemoteException("Unexpected error invoking findAll", e);
      }
      ejbStoreInvoked = tempEjbStoreInvoked;
   }
   
   public void ejbPostCreateMETHOD (int _id, String _name)
      throws CreateException, RemoteException
   {
      log.debug ("ejbPostCreateMETHOD (int, String) called");
   }
   
   public void ejbLoad ()
   {
      log.debug ("ejbLoad () called " + this);
      
      Connection con = null;
      try
      {
         con = ds.getConnection ();
         PreparedStatement ps = con.prepareStatement ("SELECT id,name FROM simplebean WHERE id=?");
         ps.setInt (1, ((Integer)ctx.getPrimaryKey ()).intValue ());
         ResultSet rs = ps.executeQuery ();
         if (rs.next ())
         {
            id = new Integer (rs.getInt ("id"));
            name = rs.getString ("name");            
         }
         rs.close ();
         ps.close ();
      }
      catch (Exception _e)
      {
         throw new EJBException ("couldnt load: "+_e.getMessage ());
      }
      finally 
      {
         try
         {
            if (con != null)
               con.close ();
         }
         catch (Exception _sqle)
         {
         }
      }
   }
   
   public void ejbStore ()
   {
      log.debug ("ejbStore () called " + this);
      tempEjbStoreInvoked = true;

      Connection con = null;
      try
      {
         con = ds.getConnection ();
         PreparedStatement ps = con.prepareStatement ("UPDATE simplebean SET name=? WHERE id=?");
         ps.setString (1, name);
         ps.setInt (2, id.intValue ());
         ps.execute ();
         ps.close ();
      }
      catch (Exception _e)
      {
         throw new EJBException ("couldnt store: "+_e.getMessage ());
      }
      finally 
      {
         try
         {
            if (con != null)
               con.close ();
         }
         catch (Exception _sqle)
         {
         }
      }
   }
   
   public void ejbRemove ()
   {
      log.debug ("ejbRemove () called " + this);

      Connection con = null;
      try
      {
         con = ds.getConnection ();
         PreparedStatement ps = con.prepareStatement ("DELETE FROM simplebean WHERE id=?");
         ps.setInt (1, id.intValue ());
         ps.execute ();
         ps.close ();
      }
      catch (Exception _e)
      {
         throw new EJBException ("couldnt remove: "+_e.getMessage ());
      }
      finally 
      {
         try
         {
            if (con != null)
               con.close ();
         }
         catch (Exception _sqle)
         {
         }
      }
   }

   
   public Integer ejbFindByPrimaryKey (Integer _key) throws FinderException
   {
      log.debug ("ejbFindByPrimaryKey (Integer) called " + this);

      Connection con = null;
      boolean found = false;
      try
      {
         con = ds.getConnection ();
         PreparedStatement ps = con.prepareStatement ("SELECT id FROM simplebean WHERE id=?");
         ps.setInt (1, _key.intValue ());
         ResultSet rs = ps.executeQuery ();
         found = rs.next ();
         rs.close ();
         ps.close ();
      }
      catch (Exception _e)
      {
         throw new EJBException ("couldnt seek: "+_e.getMessage ()); 
      }
      finally 
      {   
         
         try
         {
            if (con != null)
               con.close ();
         }
         catch (Exception _sqle)
         {
         }
      }
      if (!found)
         throw new FinderException ("No bean with id="+_key+" found.");
      
      return _key;
   }

   public Collection ejbFindAll () throws FinderException
   {
      log.debug ("ejbFindAll () called");

      Connection con = null;
      Vector result = new Vector ();
      try
      {
         con = ds.getConnection ();
         Statement s = con.createStatement ();
         ResultSet rs = s.executeQuery ("SELECT id FROM simplebean");
         while (rs.next ())
         {
            result.add (new Integer (rs.getInt ("id")));
         }
         rs.close ();
         s.close ();
      }
      catch (Exception _e)
      {
         throw new EJBException ("couldnt seek: "+_e.getMessage ()); 
      }
      finally 
      {   
         
         try
         {
            if (con != null)
               con.close ();
         }
         catch (Exception _sqle)
         {
         }
      }
      
      return result;
   }


   
   public void ejbActivate ()
   {
      log.debug ("ejbActivate () called " + this);
   }
   
   public void ejbPassivate ()
   {
      log.debug ("ejbPassivate () called " + this, new Exception("ST"));
   }
   
   public void setEntityContext (EntityContext _ctx)
   {
      log.debug ("setEntityContext() called " + this);

      ctx = _ctx;
      // lookup the datasource
      try
      {
         ds = (DataSource)new InitialContext ().lookup ("java:comp/env/datasource");
      }
      catch (NamingException _ne)
      {
         throw new EJBException ("Datasource not found: "+_ne.getMessage ());
      }
   }
   
   public void unsetEntityContext ()
   {
      log.debug ("unsetEntityContext () called");

      ctx = null;
   }
   
   // business methods ---------------------------------------------------------------

   public Integer getIdViaEJBObject()
   {
      try
      {
         Integer result = (Integer) ctx.getEJBObject().getPrimaryKey();
         log.debug(result + " " + ctx.getPrimaryKey());
         return result;
      }
      catch (RemoteException e)
      {
         throw new EJBException(e);
      }
   }
   
   public void setName (String _name)
   {
      name = _name;
   }
   
   public String getName ()
   {
      return name;
   }
   
   public boolean isEjbStoreInvoked()
   {
      return ejbStoreInvoked;
   }
}
