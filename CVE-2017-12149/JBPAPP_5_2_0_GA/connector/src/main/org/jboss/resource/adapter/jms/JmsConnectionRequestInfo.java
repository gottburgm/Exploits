/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.resource.adapter.jms;

import javax.resource.spi.ConnectionRequestInfo;

import javax.jms.Session;

import org.jboss.util.Strings;

/**
 * Request information used in pooling
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 71554 $
 */
public class JmsConnectionRequestInfo
   implements ConnectionRequestInfo
{
   private String userName;
   private String password;
   private String clientID;

   private boolean transacted = true;
   private int acknowledgeMode = Session.AUTO_ACKNOWLEDGE;
   private int type = JmsConnectionFactory.BOTH;

   /**
    * Creats with the MCF configured properties.
    */
   public JmsConnectionRequestInfo(JmsMCFProperties prop)
   {
      this.userName = prop.getUserName();
      this.password = prop.getPassword();
      this.clientID = prop.getClientID();
      this.type = prop.getType();
   }

   /**
    * Create with specified properties.
    */
   public JmsConnectionRequestInfo(final boolean transacted, 
				   final int acknowledgeMode,
				   final int type)
   {
      this.transacted = transacted;
      this.acknowledgeMode = acknowledgeMode;
      this.type = type;
   }
   
   /**
    * Fill in default values if missing. Only applies to user and password.
    */
   public void setDefaults(JmsMCFProperties prop)
   {
      if (userName == null)
         userName = prop.getUserName();//May be null there to
      if (password == null) 
         password = prop.getPassword();//May be null there to
      if (clientID == null) 
         clientID = prop.getClientID();//May be null there to
   }

   public String getUserName() 
   {
      return userName;
   }
    
   public void setUserName(String name) 
   {
      userName = name;
   }

   public String getPassword() 
   {
      return password;
   }

   public void setPassword(String password) 
   {
      this.password = password;
   }

   public String getClientID() 
   {
      return clientID;
   }

   public void setClientID(String clientID) 
   {
      this.clientID = clientID;
   }

   public boolean isTransacted()
   {
      return transacted;
   }
    
   public int getAcknowledgeMode()
   {
      return acknowledgeMode;
   }

   public int getType()
   {
      return type;
   }

   public boolean equals(Object obj)
   {
      if (obj == null) return false;
      if (obj instanceof JmsConnectionRequestInfo)
      {
         JmsConnectionRequestInfo you = (JmsConnectionRequestInfo) obj;
         return (this.transacted == you.isTransacted() &&
            this.acknowledgeMode == you.getAcknowledgeMode() &&
            this.type == you.getType() &&
            Strings.compare(userName, you.getUserName()) &&
            Strings.compare(password, you.getPassword()) &&
            Strings.compare(clientID, you.getClientID()));
      }
      else
         return false;
   }
 
   public int hashCode()
   {
      int hashCode = 0;
      if (transacted)
         hashCode += 1;
      if (type == JmsConnectionFactory.QUEUE)
         hashCode += 3;
      else if (type == JmsConnectionFactory.TOPIC)
         hashCode += 5;
      if (acknowledgeMode == Session.AUTO_ACKNOWLEDGE)
         hashCode += 7;
      else if (acknowledgeMode == Session.DUPS_OK_ACKNOWLEDGE)
         hashCode += 11;
      if (userName != null)
         hashCode += userName.hashCode();
      if (password != null)
         hashCode += password.hashCode();
      if (clientID != null)
         hashCode += clientID.hashCode();
      
      return hashCode;
   }
}
