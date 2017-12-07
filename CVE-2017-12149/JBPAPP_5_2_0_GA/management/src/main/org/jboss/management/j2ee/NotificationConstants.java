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
package org.jboss.management.j2ee;

// $Id: NotificationConstants.java 81025 2008-11-14 12:49:40Z dimitris@jboss.org $

/**
 * Constants used by Notification.type
 * 
 * @author thomas.diesler@jboss.org
 */
public final class NotificationConstants
{
   public static final String OBJECT_CREATED = "j2ee.object.created";
   public static final String OBJECT_DELETED = "j2ee.object.deleted";
   public static final String OBJECT_REGISTERED = "j2ee.object.registered";
   public static final String OBJECT_UNREGISTERED = "j2ee.object.unregistered";

   public static final String STATE_STARTING = "j2ee.state.starting";
   public static final String STATE_RUNNING = "j2ee.state.running";
   public static final String STATE_STOPPING = "j2ee.state.stopping";
   public static final String STATE_STOPPED = "j2ee.state.stopped";
   public static final String STATE_FAILED = "j2ee.state.failed";

   public static final String ATTRIBUTE_CHANGED = "j2ee.attribute.changed";
}
