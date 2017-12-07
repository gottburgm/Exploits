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
package org.jboss.mq.server.jmx;

import java.util.List;

import javax.jms.InvalidSelectorException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.w3c.dom.Element;

public interface TopicMBean
{

   public abstract void create() throws Exception;

   public abstract void destroy();

   public abstract int getDownCacheSize();

   public abstract int getFullSize();

   public abstract String getJNDIName();

   public List listNonDurableMessages(String subscriptionId) throws Exception;

   public abstract String getName();

   public abstract int getPageSize();

   public abstract MBeanServer getServer();

   public abstract ObjectName getServerPeer();

   public abstract boolean isCreatedProgrammatically();

   public abstract List listMessagesDurableSub(String arg0, String arg1,
         String arg2) throws Exception;

   public abstract List listMessagesNonDurableSub(long arg0, String arg1)
         throws Exception;

   public abstract String listSubscriptionsAsText() throws Exception;

   public abstract String listSubscriptionsAsText(boolean arg0)
         throws Exception;

   public abstract void removeAllMessages() throws Exception;

   public abstract void setDownCacheSize(int arg0);

   public abstract void setFullSize(int arg0);

   public abstract void setJNDIName(String arg0) throws Exception;

   public abstract void setPageSize(int arg0);

   public abstract void setSecurityConfig(Element arg0) throws Exception;

   public abstract void setSecurityConf(Element arg0) throws Exception;

   public abstract void setSecurityManager(ObjectName arg0);

   public abstract void setServerPeer(ObjectName arg0);

   public abstract void setDestinationManager(ObjectName arg0) throws Exception;

   /**
    * Returns the expiry destination.
    */
   public ObjectName getExpiryDestination();

   /**
    * Sets the expiry destination.
    */
   public void setExpiryDestination(ObjectName destination);

   public abstract void start() throws Exception;

   public abstract void stop();

   public abstract int subscriptionCount() throws Exception;

   public abstract int subscriptionCount(boolean arg0) throws Exception;

}