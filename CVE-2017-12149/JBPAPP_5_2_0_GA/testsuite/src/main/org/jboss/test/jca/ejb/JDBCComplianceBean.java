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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;


/**
 * A JDBCComplianceBean.
 * 
 * @ejb.bean
 *   name="JDBCComplianceBean"
 *    view-type="remote"
 *    type="Stateless"
 * 
 * @author <a href="weston.price@jboss.com">Weston Price</a>
 * @version $Revision: 85945 $
 */
public class JDBCComplianceBean implements SessionBean
{
   /**
    * @throws javax.ejb.CreateException Description of Exception
    * @ejb.create-method
    */
   public void ejbCreate()
   {
      
   }
   public void ejbActivate() throws EJBException, RemoteException
   {
      
   }

   public void ejbPassivate() throws EJBException, RemoteException
   {
      
   }

   public void ejbRemove() throws EJBException, RemoteException
   {
      
   }

   public void setSessionContext(SessionContext ctx) throws EJBException, RemoteException
   {
      
   }
   
   /**
    * @ejb.interface-method
    * @ejb.transaction type="Supports"
    */
   public void testJdbcCloseCompliance()
   {
      InitialContext ctx = null;
      DataSource ds = null;
      Connection conn = null;
      Statement s = null;
      ResultSet rs = null;
      
      try
      {
         ctx = new InitialContext();
         ds = (DataSource)ctx.lookup("java:/ComplianceDS");
         conn = ds.getConnection("sa", "");
         s = conn.createStatement();
         s.execute("CREATE TABLE DUMMY (id int, dummy varchar(10))");
         rs = s.executeQuery("SELECT * FROM DUMMY");
         s.execute("DROP TABLE DUMMY");
         rs.close();
         s.close();
         conn.close();
      }
      catch (NamingException e)
      {
         throw new EJBException(e.getMessage());
      }
      catch (SQLException e)
      {
         throw new EJBException(e.getMessage());

      }finally
      {
         
         try
         {
            if(rs != null)
            {
               rs.close();
               
            }
            
            if(s != null)
            {
               s.close();
            }
            
            if(conn != null)
            {
               conn.close();
               
            }
         }
         catch (SQLException e)
         {
            throw new EJBException(e.getMessage());
         }
      }
      
   }

   
}
