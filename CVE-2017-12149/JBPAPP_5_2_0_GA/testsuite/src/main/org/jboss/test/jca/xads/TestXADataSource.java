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
package org.jboss.test.jca.xads;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * Test XA DataSource
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 112944 $
 */
public class TestXADataSource implements XADataSource
{
	private boolean flag; 

	public void setSomeProperty(String someProperty)
	{
		if (someProperty == null)
			throw new NullPointerException("someProperty is null");
		if (someProperty.equals("${org.jboss.test.jca.xads.SomeProperty2}") == false)
			throw new IllegalArgumentException("Wrong value: " + someProperty);
	}
	public void setBackSlash(String backslash)
	{
		if (backslash == null)
			throw new NullPointerException("backslash is null");
		if (backslash.equals("\\") == false)
			throw new IllegalArgumentException("Wrong value: " + backslash);
	}

	// http://community.jboss.org/message/228733#228733
	// https://issues.jboss.org/browse/JBAS-4072
	// https://issues.jboss.org/browse/JBPAPP-5815
	public void setFlag(boolean flag)
	{
		if (!flag)
			throw new IllegalArgumentException("Wrong value: " + flag);
		this.flag = flag;
	}
	
	public boolean isFlag()
	{
		return flag;
	}
	
	public int getLoginTimeout() throws SQLException
	{
		return 0;
	}

   public Logger getParentLogger() throws SQLFeatureNotSupportedException
   {
      throw new SQLFeatureNotSupportedException("NYI: org.jboss.test.jca.xads.TestXADataSource.getParentLogger");
   }

   public PrintWriter getLogWriter() throws SQLException
	{
		return null;
	}
	public XAConnection getXAConnection() throws SQLException
	{
		  throw new SQLException("expected");
	}
	public XAConnection getXAConnection(String user, String password) throws SQLException
	{
		return null;
	}
	public void setLoginTimeout(int seconds) throws SQLException
	{
	}
	public void setLogWriter(PrintWriter out) throws SQLException
	{
	}
}
