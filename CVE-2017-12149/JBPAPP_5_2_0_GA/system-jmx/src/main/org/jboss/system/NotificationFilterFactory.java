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
package org.jboss.system;

import javax.management.NotificationFilter;

import org.w3c.dom.Element;

/**
 * Abstracts NotificationFilter creation based on an arbritrary
 * xml element fragment. Used by ListenerServiceMBeanSupport
 * to enable NotificationFilterFactory plugins.
 * 
 * @see ListenerServiceMBeanSupport
 *
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81033 $
**/
public interface NotificationFilterFactory
{
   /**
    * Create a notification filter implementation and initialize
    * it, using the passed xml element fragment.
    * 
    * @param filterConfig the xml fragment to use for configuring the filter
    * @return an initialized NotificationFilter
    * @throws Exception in case the xml fragment cannot be parsed properly
    */
   public NotificationFilter createNotificationFilter(Element filterConfig)
      throws Exception;
}
