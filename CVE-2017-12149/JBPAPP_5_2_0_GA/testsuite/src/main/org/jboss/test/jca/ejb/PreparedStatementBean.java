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
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.ejb.CreateException;
import javax.ejb.DuplicateKeyException;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.FinderException;
import javax.ejb.NoSuchEntityException;
import javax.ejb.ObjectNotFoundException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.jboss.ejb.plugins.cmp.jdbc.WrappedStatement;
import org.jboss.logging.Logger;

/** A BMP bean which exercises the prepared statement cache.
 * 
 * @author Scott.Stark@jboss.org
 * @author Adrian.Brock@jboss.org
 * @version $Revision: 81036 $
 */
public class PreparedStatementBean
   implements EntityBean
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 6204314647869034863L;
   static Logger log = Logger.getLogger(PreparedStatementBean.class);
   private EntityContext ctx = null;
   private DataSource ds;
   private String key;
   private String name;

   public void ejbActivate()
   {
   }
   public void ejbPassivate()
   {
   }

   public void ejbLoad()
   {
      key = (String) ctx.getPrimaryKey();
      log.debug("ejbLoad("+key+")");
      try
      {
         Connection con = ds.getConnection();
         try
         {
            PreparedStatement ps = con.prepareStatement("SELECT name FROM BMPTABLE WHERE pk=?");
            try
            {
               ps.setString(1, key);
               ResultSet rs = ps.executeQuery();

               if (rs.next() == false)
                  throw new NoSuchEntityException("Instance " +key +" not found in database.");
               name = rs.getString(1);
               rs.close();
            }
            finally
            {
               ps.close();
            }
         }
         finally
         {
            con.close();
         }
      }
      catch (SQLException e)
      {
         throw new EJBException(e);
      }
   }
   public void ejbStore()
   {
      log.debug("ejbStore(" + key + ")");
      try
      {
         Connection con = ds.getConnection();
         try
         {
            PreparedStatement ps = con.prepareStatement("UPDATE BMPTABLE SET name=? WHERE pk=?");
            try
            {
               ps.setString(1, key);
               ps.setString(2, name);
               ps.executeUpdate();
            }
            finally
            {
               ps.close();
            }
         }
         finally
         {
            con.close();
         }
      }
      catch (SQLException e)
      {
         throw new EJBException(e);
      }
   }
   public void ejbRemove()
   {
      log.debug("ejbRemove(" + key + ") called");
      try
      {
         Connection con = ds.getConnection();
         try
         {
            PreparedStatement ps = con.prepareStatement("DELETE FROM BMPTABLE WHERE pk=?");
            try
            {
               ps.setString(1, key);
               ps.executeUpdate();
            }
            finally
            {
               ps.close();
            }
         }
         finally
         {
            con.close();
         }
      }
      catch (SQLException e)
      {
         throw new EJBException(e);
      }

      log.debug("Removed "+key);
   }

   public void setEntityContext(EntityContext ctx)
   {
      log.debug("setEntityContext() called");
      this.ctx = ctx;
      try
      {
         InitialContext ic = new InitialContext();
         ds = (DataSource) ic.lookup("java:comp/env/jdbc/DataSource");
      }
      catch (NamingException e)
      {
         throw new EJBException("DataSource init failed", e);
      }
   }
   public void unsetEntityContext() throws EJBException, RemoteException
   {
      ds = null;
      
   }

   public String ejbCreate(String pk, String name)
      throws CreateException, DuplicateKeyException
   {
      log.debug("entry ejbCreate("+ pk + ", " + name + ")");
      ensureTableExists();
      try
      {
         Connection con = ds.getConnection();
         try
         {
            PreparedStatement ps = con.prepareStatement("INSERT INTO BMPTABLE VALUES (?,?)");
            try
            {
               ps.setString(1, pk);
               ps.setString(2, name);
               ps.execute();
            }
            finally
            {
               ps.close();
            }
         }
         finally
         {
            con.close();
         }
      }
      catch (SQLException e)
      {
         log.debug("failed", e);

         throw new CreateException("Entity bean creation failure: " +
            e.getMessage());
      }

      this.key = pk;
      this.name = name;
      log.debug("Created BMP: "+pk);
      return pk;
   }

   public void ejbPostCreate(String pk, String name)
   {
      log.debug("entry ejbCreate("+ pk + ", " + name + ")");
   }

  public String ejbFindByPrimaryKey(String pk)
     throws FinderException
  {
     log.debug("ejbFindByPrimaryKey, pk="+pk);
     ensureTableExists();
     try
     {
        Connection con = ds.getConnection();
        try
        {
           PreparedStatement ps;

           ps = con.prepareStatement("SELECT pk FROM BMPTABLE WHERE pk=?");
           try
           {
              ps.setString(1, pk);
              ResultSet rs = ps.executeQuery();
              if (!rs.next())
                 throw new ObjectNotFoundException("No bean with " +"pk=" +pk+ " found.");
              rs.close();
              return pk;
           }
           finally
           {
              ps.close();
           }
        }
        finally
        {
           con.close();
        }
     }
     catch (SQLException e)
     {
        throw new FinderException("Could not find pk="+pk+", msg=" + e.getMessage());
     }
  }

   public long hashEntityTable()
      throws RemoteException
   {
      long hash = 0;
      try
      {
         Connection con = ds.getConnection();
         try
         {
            PreparedStatement ps = con.prepareStatement("SELECT pk FROM BMPTABLE");
            try
            {
               ResultSet rs = ps.executeQuery();
               while( rs.next() )
               {
                  String pk = rs.getString(1);
                  PreparedStatement ps2 = con.prepareStatement("SELECT name FROM BMPTABLE WHERE pk=?");
                  ps2.setString(1, pk);
                  ResultSet rs2 = ps2.executeQuery();
                  if( rs2.next() )
                  {
                     String pkName = rs2.getString(1);
                     hash += pkName.hashCode();
                  }
                  rs2.close();
                  ps2.close();
               }
               rs.close();
            }
            finally
            {
               ps.close();
            }
         }
         finally
         {
            con.close();
         }
      }
      catch (SQLException e)
      {
         throw new RemoteException("Failed to calculate hash", e);
      }
      return hash;
   }

   public void testPreparedStatementCache() throws RemoteException
   {
      try
      {
         Connection con = ds.getConnection();
         try
         {
            PreparedStatement ps = con.prepareStatement("SELECT pk FROM BMPTABLE");
            Statement ps1 = getWrappedStatement(ps); 
            ps.close();
            ps = con.prepareStatement("SELECT pk FROM BMPTABLE");
            Statement ps2 = getWrappedStatement(ps); 
            if (ps1 !=  ps2)
               throw new EJBException("Statement " + ps1 + " was not cached: got " + ps2);
            ps.close();
            ps = con.prepareStatement("SELECT pk FROM BMPTABLE", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ps2 = getWrappedStatement(ps); 
            if (ps1 !=  ps2)
               throw new EJBException("Statement " + ps1 + " was not cached against default result set parameters: got " + ps2);
            ps.close();
            ps = con.prepareStatement("SELECT pk FROM BMPTABLE", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            ps2 = getWrappedStatement(ps); 
            if (ps1 ==  ps2)
               throw new EJBException("Statement " + ps1 + " should be different with different result set parameters: got " + ps2);
            ps.close();
         }
         finally
         {
            con.close();
         }
      }
      catch (SQLException e)
      {
         throw new RemoteException("Unexpected sql exception", e);
      }
   }

   public void testPreparedStatementCacheDoubleClose() throws RemoteException
   {
      try
      {
         Connection con = ds.getConnection();
         try
         {
            PreparedStatement ps = con.prepareStatement("SELECT pk FROM BMPTABLE");
            ps.close();
            try
            {
               ps.close();
            }
            catch (SQLException e)
            {
               log.debug("Got expected double close exception", e);
            }
         }
         finally
         {
            con.close();
         }
      }
      catch (SQLException e)
      {
         throw new RemoteException("Unexpected sql exception", e);
      }
   }

   public void testCallableStatementCache(String name) throws RemoteException
   {
      try
      {
         InitialContext ctx = new InitialContext();
         String query = (String) ctx.lookup("java:comp/env/"+name);

         Connection con = ds.getConnection();
         try
         {
            CallableStatement ps = con.prepareCall(query);
            Statement ps1 = getWrappedStatement(ps); 
            ps.close();
            ps = con.prepareCall(query);
            Statement ps2 = getWrappedStatement(ps); 
            if (ps1 !=  ps2)
               throw new EJBException("Statement " + ps1 + " was not cached: got " + ps2);
            ps.close();
            ps = con.prepareCall(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ps2 = getWrappedStatement(ps); 
            if (ps1 !=  ps2)
               throw new EJBException("Statement " + ps1 + " was not cached against default result set parameters: got " + ps2);
            ps.close();
            ps = con.prepareCall(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            ps2 = getWrappedStatement(ps); 
            if (ps1 ==  ps2)
               throw new EJBException("Statement " + ps1 + " should be different with different result set parameters: got " + ps2);
            ps.close();
         }
         finally
         {
            con.close();
         }
      }
      catch (SQLException e)
      {
         throw new RemoteException("Unexpected sql exception", e);
      }
      catch (NamingException e)
      {
         throw new RemoteException("Failed to lookup query", e);         
      }
   }

   public void testCallableStatementCacheDoubleClose(String name) throws RemoteException
   {
      try
      {
         InitialContext ctx = new InitialContext();
         String query = (String) ctx.lookup("java:comp/env/"+name);

         Connection con = ds.getConnection();
         try
         {
            CallableStatement ps = con.prepareCall(query);
            ps.close();
            try
            {
               ps.close();
            }
            catch (SQLException e)
            {
               log.debug("Got expected double close exception", e);
            }
         }
         finally
         {
            con.close();
         }
      }
      catch (SQLException e)
      {
         throw new RemoteException("Unexpected sql exception", e);
      }
      catch (NamingException e)
      {
         throw new RemoteException("Failed to lookup query", e);         
      }
   }

   public String executeStoredProc(String name)
      throws RemoteException
   {
      String value = null;
      try
      {
         InitialContext ctx = new InitialContext();
         String query = (String) ctx.lookup("java:comp/env/"+name);

         Connection con = ds.getConnection();
         try
         {
            CallableStatement ps = con.prepareCall(query);
            try
            {
               ResultSet rs = ps.executeQuery();
               while( rs.next() )
               {
                  value = rs.getString(1);
               }
               rs.close();
            }
            finally
            {
               ps.close();
            }
         }
         finally
         {
            con.close();
         }
      }
      catch (SQLException e)
      {
         throw new RemoteException("Failed to execuate CallableStatement", e);
      }
      catch (NamingException e)
      {
         throw new RemoteException("Failed to lookup query", e);         
      }
      return value;
   }

   private void ensureTableExists()
   {
      boolean exists = true;

      try
      {
         Connection con = ds.getConnection();
         try
         {
            Statement s = con.createStatement();
            try
            {
               ResultSet rs = s.executeQuery("SELECT * FROM BMPTABLE");
               rs.close();
            }
            finally
            {
               s.close();
            }
         }
         finally
         {
            con.close();
         }
      }
      catch (SQLException e)
      {
         exists = false;
      }

      if (!exists)
      {
         try
         {
            Connection con = ds.getConnection();
            try
            {
               Statement s = con.createStatement();
               try
               {
                  s.executeUpdate("CREATE TABLE BMPTABLE (pk VARCHAR(16), name VARCHAR(32))");
               }
               finally
               {
                  s.close();
               }
            }
            finally
            {
               con.close();
            }
         }
         catch (SQLException e)
         {
            throw new EJBException(e);
         }
      }
   }

   public Statement getWrappedStatement(Statement s) throws SQLException
   {
      while (s instanceof WrappedStatement)
         s = ((WrappedStatement) s).getUnderlyingStatement();
      return s;
   }

}
