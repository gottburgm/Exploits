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
package org.jboss.ejb;

// $Id: AllowedOperationsFlags.java 81030 2008-11-14 12:59:42Z dimitris@jboss.org $

/**
 * Constants used by the AllowedOperationsAssociation
 *
 * According to the EJB2.1 spec not all context methods can be accessed at all times
 * For example ctx.getPrimaryKey() should throw an IllegalStateException when called from within ejbCreate()
 *
 * @author Thomas.Diesler@jboss.org
 * @version $Revision: 81030 $
 */
public interface AllowedOperationsFlags
{
   // Constants -----------------------------------------------------

   /**
    * These constants are used to validate method access
    */
   public static final int NOT_ALLOWED = 0;
   public static final int IN_INTERCEPTOR_METHOD = (int) Math.pow(2, 0);
   public static final int IN_EJB_ACTIVATE = (int) Math.pow(2, 1);
   public static final int IN_EJB_PASSIVATE = (int) Math.pow(2, 2);
   public static final int IN_EJB_REMOVE = (int) Math.pow(2, 3);
   public static final int IN_EJB_CREATE = (int) Math.pow(2, 4);
   public static final int IN_EJB_POST_CREATE = (int) Math.pow(2, 5);
   public static final int IN_EJB_FIND = (int) Math.pow(2, 6);
   public static final int IN_EJB_HOME = (int) Math.pow(2, 7);
   public static final int IN_EJB_TIMEOUT = (int) Math.pow(2, 8);
   public static final int IN_EJB_LOAD = (int) Math.pow(2, 9);
   public static final int IN_EJB_STORE = (int) Math.pow(2, 10);
   public static final int IN_SET_ENTITY_CONTEXT = (int) Math.pow(2, 11);
   public static final int IN_UNSET_ENTITY_CONTEXT = (int) Math.pow(2, 12);
   public static final int IN_SET_SESSION_CONTEXT = (int) Math.pow(2, 13);
   public static final int IN_SET_MESSAGE_DRIVEN_CONTEXT = (int) Math.pow(2, 14);
   public static final int IN_AFTER_BEGIN = (int) Math.pow(2, 15);
   public static final int IN_BEFORE_COMPLETION = (int) Math.pow(2, 16);
   public static final int IN_AFTER_COMPLETION = (int) Math.pow(2, 17);
   public static final int IN_BUSINESS_METHOD = (int) Math.pow(2, 18);
   public static final int IN_SERVICE_ENDPOINT_METHOD = (int) Math.pow(2, 19);
}
