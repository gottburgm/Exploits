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
package org.jboss.monitor.services;

import org.jboss.system.ListenerServiceMBean;

/**
 * MBean interface of a configurable JMX notification listener
 * that processes notification 
 * 
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81038 $
 */
public interface ScriptingListenerMBean extends ListenerServiceMBean
{
   // Attributes ----------------------------------------------------
   
   /** The script to execute when a notification is received */
   void setScript(String script);
   String getScript();
   
   /** The language the script is written into */
   void setScriptLanguage(String language);
   String getScriptLanguage();
   
   /** Flag to enable/disable dynamic subscriptions */
   void setDynamicSubscriptions(boolean dynamicSubscriptions);
   boolean getDynamicSubscriptions();
   
   /** The number of notifications received */
   long getNotificationsReceived();
   
   /** The number of notification processed */
   long getNotificationsProcessed();
   
   /** The time spent processing notifications in msecs */
   long getTotalProcessingTime();
   
   /** The average notification processing time in msecs */
   long getAverageProcessingTime();
  
}
