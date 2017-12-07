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
package org.jboss.resource.adapter.jdbc.jdk5;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.Statement;

import org.jboss.resource.adapter.jdbc.BaseWrapperManagedConnection;
import org.jboss.resource.adapter.jdbc.WrappedCallableStatement;
import org.jboss.resource.adapter.jdbc.WrappedConnection;
import org.jboss.resource.adapter.jdbc.WrappedPreparedStatement;
import org.jboss.resource.adapter.jdbc.WrappedStatement;

/**
 * WrappedConnectionJDK6.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class WrappedConnectionJDK5 extends WrappedConnection
{
   /**
    * Create a new WrappedConnectionJDK5.
    * 
    * @param mc the managed connection
    */
   public WrappedConnectionJDK5(BaseWrapperManagedConnection mc)
   {
      super(mc);
   }

   protected WrappedStatement wrapStatement(Statement statement)
   {
      return new WrappedStatementJDK5(this, statement);
   }

   protected WrappedPreparedStatement wrapPreparedStatement(PreparedStatement statement)
   {
      return new WrappedPreparedStatementJDK5(this, statement);
   }

   protected WrappedCallableStatement wrapCallableStatement(CallableStatement statement)
   {
      return new WrappedCallableStatementJDK5(this, statement);
   }
}
