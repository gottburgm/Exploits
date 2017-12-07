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

/**
 * Indicates the emitting of Events and also
 * indicates what types of events it emits.
 * <br><br>
 * <b>Attention</b>: This interface is not indented to be used by the client
 * but it is morely a desription of what the client will get when he/she
 * receives attributes from the JSR-77 server or what method can be
 * invoked.
 * <br><br>
 * All attributes (getXXX) can be received by
 * <br>
 * {@link javax.management.j2ee.Management#getAttribute Management.getAttribute()}
 * <br>
 * or in bulk by:
 * <br>
 * {@link javax.management.j2ee.Management#getAttributes Management.getAttributes()}
 * <br>
 * Methods (all except getXXX()) can be invoked by:
 * <br>
 * {@link javax.management.j2ee.Management#invoke Management.invoke()}
 * <br>
 *
 * @author <a href="mailto:marc@jboss.org">Marc Fleury</a>
 * @author Andreas Schaefer
 * @version $Revision: 81025 $
 */
public interface EventProvider
{
   /**
    * @return The actual list of Types of Events this Managed Object emits.
    *         The list is never null nor empty
    */
   String[] getEventTypes();

   /**
    * Returns the given Type of Events it emits according to its index in the list
    *
    * @param index Index of the requested Event Type
    * @return Event Type if given Index is within the boundaries otherwise null
    */
   String getEventType(int index);
}
