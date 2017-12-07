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
package org.jboss.test.jca.test;

import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import junit.framework.Test;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.jboss.test.JBossTestCase;

/** Tests of remote access to a jdbc datasource.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class RemoteDSUnitTestCase extends JBossTestCase
{
   public RemoteDSUnitTestCase (String name)
   {
      super(name);
   }

   
   public void testStatement() throws Exception
   {
      log.info("+++ testStatement");
      InitialContext ctx = super.getInitialContext();
      DataSource ds = (DataSource) ctx.lookup("RemoteDS");
      Connection conn = ds.getConnection("sa", "");
      DatabaseMetaData dmd = conn.getMetaData();
      log.info(dmd);
      Statement stmt = conn.createStatement();
      // Create a table
      stmt.executeUpdate("CREATE TABLE COFFEES " +
         "(NAME VARCHAR(32), SUP_ID INTEGER, PRICE FLOAT, SALES INTEGER, TOTAL INTEGER)");
      // Add some data
      stmt.executeUpdate("INSERT INTO COFFEES VALUES ('Colombian', 100, 7.99, 0, 0)");
      stmt.executeUpdate("INSERT INTO COFFEES VALUES ('FrenchRoast', 101, 8.99, 0, 0)");
      stmt.executeUpdate("INSERT INTO COFFEES VALUES ('JavaBean', 102, 6.99, 0, 0)");
      // Query the data
      ResultSet rs = stmt.executeQuery("SELECT * FROM COFFEES where NAME = 'Colombian'");
      ResultSetMetaData rsmd = rs.getMetaData();
      assertTrue("ResultSetMetaData.getColumnCount == 5",
         rsmd.getColumnCount() == 5);
      int nameIndex = rs.findColumn("NAME");
      String cname = rsmd.getColumnName(nameIndex);
      assertTrue("NAME column maps", cname.equalsIgnoreCase("NAME"));
      
      assertTrue("ResultSet.next == true",
         rs.next() == true );
      String name = rs.getString("NAME");
      assertTrue("name == Colombian", name.equals("Colombian"));
      int id = rs.getInt("SUP_ID");
      assertTrue("id == 100", id == 100);
      float price = rs.getFloat("PRICE");
      int iprice = Math.round(100 * price);
      log.info("iprice = "+iprice);
      assertTrue("price == 7.99", 799 == iprice);
      int sales = rs.getInt("SALES");
      assertTrue("sales == 0", sales == 0);
      int total = rs.getInt("TOTAL");
      assertTrue("total == 0", total == 0);
      rs.close();

      // Drop the table
      stmt.executeUpdate("DROP TABLE COFFEES");
      stmt.close();
      conn.close();
   }

   public static Test suite() throws Exception
   {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      URL resURL = loader.getResource("jca/remote-jdbc/remote-ds.xml");
      return getDeploySetup(RemoteDSUnitTestCase.class, resURL.toString());
   }
}
