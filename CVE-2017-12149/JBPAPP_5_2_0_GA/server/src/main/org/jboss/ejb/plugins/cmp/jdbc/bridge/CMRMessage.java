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

import java.io.Serializable;
import java.io.ObjectStreamException;

/**
 * Type safe enumeration of method object is passed through the invocation
 * interceptor chain and caught by the JDBCRelationInterceptor.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version $Revision: 81030 $
 */
public final class CMRMessage implements Serializable
{
   private static int nextOrdinal = 0;
   private static final CMRMessage[] VALUES = new CMRMessage[5];

   public static final CMRMessage GET_RELATED_ID = new CMRMessage("GET_RELATED_ID");
   public static final CMRMessage ADD_RELATION = new CMRMessage("ADD_RELATION");
   public static final CMRMessage REMOVE_RELATION = new CMRMessage("REMOVE_RELATION");
   public static final CMRMessage SCHEDULE_FOR_CASCADE_DELETE = new CMRMessage("CASCADE_DELETE");
   public static final CMRMessage SCHEDULE_FOR_BATCH_CASCADE_DELETE = new CMRMessage("BATCH_CASCADE_DELETE");


   private final transient String name;
   private final int ordinal;

   private CMRMessage(String name)
   {
      this.name = name;
      this.ordinal = nextOrdinal++;
      VALUES[ordinal] = this;
   }

   public String toString()
   {
      return name;
   }

   Object readResolve() throws ObjectStreamException
   {
      return VALUES[ordinal];
   }

}


