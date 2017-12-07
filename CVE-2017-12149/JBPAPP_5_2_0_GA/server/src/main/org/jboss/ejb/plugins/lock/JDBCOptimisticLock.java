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
package org.jboss.ejb.plugins.lock;


/**
 * This class is an optmistic lock implementation.
 * It locks tableFields and their values during transaction.
 * Locked tableFields and their values are added to the WHERE clause of the
 * UPDATE SQL statement when entity is stored.
 * The following strategies are supported:
 * - fixed group of tableFields
 *   Fixed group of tableFields is used for locking . The tableFields and their values are
 *   locked at the beginning of a transaction. The group name must match
 *   one of the entity's load-group-name.
 * - modified strategy
 *   The tableFields that were modified during transaction are used as lock.
 *   All entity's field values are locked at the beginning of the transaction.
 *   The tableFields are locked only after its actual change.
 * - read strategy
 *   The tableFields that were read/modified during transaction.
 *   All entity's field values are locked at the beginning of the transaction.
 *   The tableFields are locked only after they were accessed.
 * - version-column strategy
 *   This adds additional version field of type java.lang.Long. Each update
 *   of the entity will increase the version value by 1.
 * - timestamp-column strategy
 *   Adds additional timestamp column of type java.util.Date. Each update
 *   of the entity will set the field to the current time.
 * - key-generator column strategy
 *   Adds additional column. The type is defined by user. The key generator
 *   is used to set the next value.
 *
 * Note: all optimistic locking related code should be rewritten when in the
 * new CMP design.
 *
 * Note2: done.
 *
 * @author <a href="mailto:aloubyansky@hotmail.com">Alex Loubyansky</a>
 * @version $Revision: 81030 $
 */
public final class JDBCOptimisticLock
   extends NoLock
{
}
