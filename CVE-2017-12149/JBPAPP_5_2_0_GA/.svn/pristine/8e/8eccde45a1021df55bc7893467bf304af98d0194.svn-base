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
package org.jboss.test.mdbsessionpoolclear.bean;

import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.ejb.EJBException;

import javax.jms.MessageListener;
import javax.jms.Message;
import javax.jms.TextMessage;

import javax.naming.InitialContext;

import org.jboss.logging.Logger;

/**
 * MessageBeanImpl.java
 *
 *
 * Created: Sat Nov 25 18:07:50 2000
 *
 * @author Peter Antman DN <peter.antman@dn.se>
 * @version
 */

public class Mdb implements MessageDrivenBean, MessageListener
{
   private static final Logger log = Logger.getLogger(Mdb.class);
   
   private MessageDrivenContext ctx = null;
    
   public Mdb()
   {
   }
   
   public void setMessageDrivenContext(MessageDrivenContext ctx) throws EJBException
   {
      this.ctx = ctx;
   }
    
   public void ejbCreate() {}

   public void ejbRemove() {ctx=null;}

   public void onMessage(Message message)
   {
      try
      {
         InitialContext jndiContext = new InitialContext();
         TestStatusHome statusHome = (TestStatusHome)jndiContext.lookup("TestStatus");
         TestStatus status = statusHome.create();
         int count = status.increment();
         log.info("**** Mdb got message " + count + " " + ((TextMessage)message).getText());
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
} 


