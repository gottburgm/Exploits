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
package org.jboss.ejb.plugins.cmp.jdbc.bridge;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.bridge.FieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCType;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCEntityPersistenceStore;


/**
 * @author <a href="mailto:loubyansky@ua.fm">Alex Loubyansky and others</a>
 */
public interface JDBCFieldBridge extends FieldBridge
{
   /** Gets the JDBC type of this field. */
   public JDBCType getJDBCType();

   /**
    * Is this field a member of the primary key.
    * @return true if this field is a member of the primary key
    */
   public boolean isPrimaryKeyMember();

   /**
    * Is this field read only.
    * @return true if this field is read only
    */
   public boolean isReadOnly();

   /**
    * Has current data read timed out?
    */
   public boolean isReadTimedOut(EntityEnterpriseContext ctx);

   /**
    * Has the data been loaded?
    */
   public boolean isLoaded(EntityEnterpriseContext ctx);

   /**
    * Set CMPFieldValue to Java default value (i.e., 0 or null).
    */
   public void initInstance(EntityEnterpriseContext ctx);

   /**
    * Resets any persistence data maintained in the context.
    */
   public void resetPersistenceContext(EntityEnterpriseContext ctx);

   /**
    * Sets the prepared statement parameters with the data from the
    * instance associated with the context.
    */
   public int setInstanceParameters(PreparedStatement ps, int parameterIndex, EntityEnterpriseContext ctx);

   /**
    * Gets the internal value of this field without user level checks.
    * @param ctx the context for which this field's value should be fetched
    * @return the value of this field
    */
   public Object getInstanceValue(EntityEnterpriseContext ctx);

   /**
    * Sets the internal value of this field without user level checks.
    * @param ctx the context for which this field's value should be set
    * @param value the new value of this field
    */
   public void setInstanceValue(EntityEnterpriseContext ctx, Object value);

   /**
    * Loads the data from result set into the instance associated with
    * the specified context.
    */
   public int loadInstanceResults(ResultSet rs, int parameterIndex, EntityEnterpriseContext ctx);

   /**
    * Loads the value of this cmp field from result set into argument referance.
    */
   public int loadArgumentResults(ResultSet rs, int parameterIndex, Object[] argumentRef);

   /**
    * Has the value of this field changes since the last time clean was called.
    */
   public boolean isDirty(EntityEnterpriseContext ctx);

   /**
    * Mark this field as clean.
    */
   public void setClean(EntityEnterpriseContext ctx);

   boolean isCMPField();

   JDBCEntityPersistenceStore getManager();

   Object getPrimaryKeyValue(Object arg);
}
