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
package org.jboss.mx.notification;

import javax.management.NotificationFilter;
import javax.management.ObjectName;
import javax.management.Notification;

/**
 * NotificationFilterProxy wraps a real filter by the users to pass down
 * to the mbean server so that as notification emitters apply the filter, the
 * appropriate source can be set in the notification object before passing to the
 * real filter.
 *
 * @author <a href="mailto:jhaynie@vocalocity.net">Jeff Haynie</a>
 * @version $Revision: 81019 $
 */
public class NotificationFilterProxy implements NotificationFilter
{
    private static final long serialVersionUID = 1L;
    private ObjectName source;
    private NotificationFilter delegate;

    public NotificationFilterProxy(ObjectName source, NotificationFilter delegate)
    {
        this.source = source;
        this.delegate = delegate;
    }

    /**
     * This method is called before a notification is sent to see whether
     * the listener wants the notification.
     *
     * @param notification the notification to be sent.
     * @return true if the listener wants the notification, false otherwise
     */
    public boolean isNotificationEnabled(Notification notification)
    {
        // replace with the real source of the event
        notification.setSource(source);
        return this.delegate.isNotificationEnabled(notification);
    }

    /**
     * return the real ObjectName source
     *
     * @return
     */
    public ObjectName getSource ()
    {
        return source;
    }
    /**
     * return the real NotificationFilter
     *
     * @return
     */
    public NotificationFilter getFilter ()
    {
        return delegate;
    }
}
