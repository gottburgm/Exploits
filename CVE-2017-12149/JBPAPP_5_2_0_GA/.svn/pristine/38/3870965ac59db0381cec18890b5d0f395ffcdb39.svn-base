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

import java.util.Collection;
import javax.ejb.*;
import javax.sql.*;
import java.sql.*;
import javax.naming.*;


/**
 *   This is a session bean whose only purpose is to look for and test datasources. It is an example of how to use the EJBDoclet tags.
 *   
 *   @ejb:stateless-session
 *   @ejb:ejb-name test/jmx/TestDataSource
 *   @ejb:jndi-name ejb/test/jmx/TestDataSource
 *   @ejb:security-role-ref admin Administrator
 *   @ejb:permission Teller
 *   @ejb:permission Administrator
 *   @ejb:transaction Required
 *   @ejb:transaction-type Container
 *
 *   JBoss specific
 *   @jboss:container-configuration Standard Stateless SessionBean
 *
 */
public class TestDataSourceBean
   implements SessionBean
{
   // Public --------------------------------------------------------
   /**
    * The <code>testDataSource</code> method looks for the datasource at the supplied name
    * and tests if it can supply a working connection.
    *
    * @param dsName a <code>String</code> value
    * @ejb:interface-method type="remote"
    */
   public void testDataSource(String dsName)
   {
      try
      {
         InitialContext ctx = new InitialContext();
         DataSource ds = (DataSource)ctx.lookup(dsName);
         if (ds == null) {
            throw new Exception("DataSource lookup was null");
         }
         Connection c = ds.getConnection();
         if (c == null) {
             throw new Exception("Connection was null!!");
         }
         DatabaseMetaData dmd = c.getMetaData();
         ResultSet rs = dmd.getTables(null, null, "%", null);
         c.close();
      } catch (Exception e)
      {
         throw new EJBException(e);
      }
   }

   /**
    * The <code>isBound</code> method checks to see if the supplied name is bound in jndi.
    *
    * @param name a <code>String</code> value
    * @return a <code>boolean</code> value
    * @ejb:interface-method type="remote"
    */
   public boolean isBound(String name)
   {
      try
      {
         InitialContext ctx = new InitialContext();
         Object ds = ctx.lookup(name);
         if (ds == null) {
            return false;
         }
         return true;
      }
      catch (NamingException e)
      {
         return false;
      } // end of catch
      
   }
   
   /**
    * Create.
    */
   public void ejbCreate()
      throws CreateException
   { 
   }
   
   // SessionBean implementation ------------------------------------
   public void ejbActivate() {}
   public void ejbPassivate() {}
   public void setSessionContext(SessionContext ctx) {}
   
   /**
    * Remove
    *
    */
   public void ejbRemove() {}

}
