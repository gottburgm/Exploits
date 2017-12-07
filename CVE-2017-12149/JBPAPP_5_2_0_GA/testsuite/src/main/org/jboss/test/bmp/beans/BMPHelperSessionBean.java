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
package org.jboss.test.bmp.beans;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.jboss.logging.Logger;
import org.jboss.test.bmp.interfaces.SimpleBMP;
import org.jboss.test.bmp.interfaces.SimpleBMPHome;

public class BMPHelperSessionBean implements SessionBean
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   Logger log = Logger.getLogger(getClass());
   
   SessionContext ctx = null;
   private DataSource ds = null;
   
   public void ejbCreate () throws CreateException, RemoteException
   {
      try
      {
         ds = (DataSource)new InitialContext ().lookup ("java:comp/env/datasource");
      }
      catch (NamingException _ne)
      {
         throw new CreateException ("Datasource not found: "+_ne.getMessage ());
      }
   }
   
   public boolean existsSimpleBeanTable ()
   {
      return tableExists ("SIMPLEBEAN");
   }
   
   public void createSimpleBeanTable ()
   {
      createTable ("CREATE TABLE SIMPLEBEAN (id INTEGER, name VARCHAR(200))");
   }
   
   public void dropSimpleBeanTable ()
   {
      dropTable ("SIMPLEBEAN");
   }
   
   public String doTest () throws RemoteException
   {
      StringBuffer sb = new StringBuffer ();
      SimpleBMP b;
      try
      {
         SimpleBMPHome home = (SimpleBMPHome) new InitialContext ().lookup ("java:comp/env/bean");
         b = home.findByPrimaryKey(new Integer (1));
      }
      catch (Exception _ne)
      {
         throw new EJBException ("couldnt find entity: "+_ne.getMessage ());
      }
      sb.append ("found: "+b.getName ()+"\n");
      sb.append ("set name to \"Name for rollback\"\n");
      b.setName ("Name for rollback");
      sb.append ("current name is: "+b.getName ()+"\n");
      try
      {
         sb.append ("now rolling back...\n");
         
         ctx.setRollbackOnly();
      }
      catch (Exception _e)
      {
         sb.append ("Error on rolling back: "+_e.getMessage ()+"\n");
      }
      sb.append ("done.");
   
      return sb.toString ();
   }
   
   public String doTestAfterRollback () throws RemoteException
   {
      StringBuffer sb = new StringBuffer ();
      SimpleBMP b;
      try
      {
         SimpleBMPHome home = (SimpleBMPHome) new InitialContext ().lookup ("java:comp/env/bean");
         b = home.findByPrimaryKey(new Integer (1));
      }
      catch (Exception _ne)
      {
         throw new EJBException ("couldnt find entity: "+_ne.getMessage ());
      }
      sb.append ("found: "+b.getName ()+"\n");
      sb.append ("done.");
   
      return sb.toString ();
   }
   
   private boolean tableExists (String _tableName)
   {
      boolean result = false;
      Connection con = null;
      try
      {
         con = ds.getConnection ();
         DatabaseMetaData dmd = con.getMetaData ();
         ResultSet rs = dmd.getTables (con.getCatalog (), null, _tableName, null);
         if (rs.next ())
            result = true;
         
         rs.close ();
      }
      catch (Exception _e)
      {
         throw new EJBException ("Error while looking up table: "+_e.getMessage ());
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
   
   
   private void createTable (String _sql)
   {
      Connection con = null;
      try
      {
         con = ds.getConnection ();
         Statement s = con.createStatement ();
         s.executeUpdate (_sql);
         s.close ();
      }
      catch (Exception _e)
      {
         throw new EJBException ("Error while creating table: "+_e.getMessage ());
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
   
   private void dropTable (String _tableName)
   {
      Connection con = null;
      try
      {
         con = ds.getConnection ();
         Statement s = con.createStatement ();
         s.executeUpdate ("DROP TABLE "+_tableName);
         s.close ();
      }
      catch (Exception _e)
      {
         throw new EJBException ("Error while dropping table: "+_e.getMessage ());
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
   
   
   public void ejbActivate () {}
   public void ejbPassivate () {}
   public void ejbRemove () {}
   public void setSessionContext (SessionContext _ctx) {ctx = _ctx;}
}
