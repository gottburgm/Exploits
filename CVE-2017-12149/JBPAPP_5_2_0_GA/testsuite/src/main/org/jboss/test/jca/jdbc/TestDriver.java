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
package org.jboss.test.jca.jdbc;

import java.lang.reflect.Proxy;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.sql.Connection;
import java.sql.DriverPropertyInfo;
import java.util.logging.Logger;

/**
 * TestDriver.java
 *
 *
 * Created: Fri Feb 14 12:15:42 2003
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 */

public class TestDriver implements Driver
{

   private boolean fail = false;

   private int closedCount = 0;


   public TestDriver() {

   }

   public void setFail(boolean fail)
   {
      this.fail = fail;
   }

   public boolean getFail()
   {
      return fail;
   }

   public int getClosedCount()
   {
      return closedCount;
   }

   public void connectionClosed()
   {
      closedCount++;
   }

   // Implementation of java.sql.Driver

   public boolean acceptsURL(String string) throws SQLException {
      return string != null && string.startsWith("jdbc:jboss-test-adapter");
   }

   public Connection connect(String url, Properties info) throws SQLException
   {
      return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { Connection.class }, new TestConnection(this));
   }

   public int getMajorVersion()
   {
      return 1;
   }

   public int getMinorVersion()
   {
      return 0;
   }

   public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
   {
      return null;
   }

   public boolean jdbcCompliant()
   {
      return false;
   }

   public Logger getParentLogger() throws SQLFeatureNotSupportedException
   {
      throw new SQLFeatureNotSupportedException("NYI: org.jboss.test.jca.jdbc.TestDriver.getParentLogger");
   }

}// TestDriver
