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
package org.jboss.mx.remoting;

import java.util.HashMap;
import java.util.Map;
import javax.management.NotificationFilter;
import javax.management.ObjectName;

/**
 * ClientListener
 *
 * @author <a href="mailto:jhaynie@vocalocity.net">Jeff Haynie</a>
 * @version $Revision: 81023 $
 */
public class ClientListener
{
   final ObjectName objectName;
   final Object listener;
   final NotificationFilter filter;
   final Object handback;
   final String sessionId;

   private static final Map listeners = new HashMap();

   private ClientListener(String sessionId, ObjectName objectName, Object listener, NotificationFilter filter, Object handback)
   {
      this.sessionId = sessionId;
      this.objectName = objectName;
      this.listener = listener;
      this.filter = filter;
      this.handback = handback;
   }

   public String toString()
   {
      return "ClientListener [sessionid:" + sessionId + ",objectName:" + objectName + ",listener:" + listener + ",filter:" + filter + ",handback:" + handback + "]";
   }

   public static boolean hasListeners()
   {
      return listeners.isEmpty() == false;
   }

   public static Object makeId(String sessionId, ObjectName objectName, Object listener)
   {
      return sessionId + "/" + objectName + "/" + listener;
   }

   public static synchronized ClientListener remove(Object id)
   {
      return (ClientListener) listeners.remove(id);
   }

   public static synchronized ClientListener remove(String sessionId, ObjectName objectName, Object listener)
   {
      Object id = null;
      if(listener instanceof ClientListener)
      {
         id = makeId(sessionId, objectName, ((ClientListener) listener).listener);
      }
      else
      {
         id = makeId(sessionId, objectName, listener);
      }
      return (ClientListener) listeners.remove(id);
   }

   public static synchronized Object register(String sessionId, ObjectName objectName, Object listener, NotificationFilter filter, Object handback)
   {
      ClientListener l = new ClientListener(sessionId, objectName, listener, filter, handback);
      Object id = makeId(sessionId, objectName, listener);
      listeners.put(id, l);
      return id;
   }

   public static synchronized ClientListener get(Object id)
   {
      return (ClientListener) listeners.get(id);
   }

   static synchronized String dump()
   {
      return listeners.toString();
   }

}
