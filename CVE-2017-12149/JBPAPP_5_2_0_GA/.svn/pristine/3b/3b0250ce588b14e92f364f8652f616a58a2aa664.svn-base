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
package org.jboss.test.messagedriven.beans;

import java.util.Enumeration;
import java.util.Properties;

import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.transaction.Transaction;

import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.test.messagedriven.mbeans.TestMessageDrivenManagementMBean;

/**
 * A Test Message Driven Bean
 * 
 * @author <a href="mailto:adrian@jboss.com>Adrian Brock</a>
 * @version <tt>$Revision: 1.4</tt>
 */
public class TestMessageDriven implements MessageDrivenBean, MessageListener
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   protected static final Logger log = Logger.getLogger(TestMessageDriven.class);

   protected MessageDrivenContext ctx;
   protected TestMessageDrivenManagementMBean mbean;

   /**
    * Replaces legacy
    * org.jboss.ejb.plugins.jms.DLQHandler.JBOSS_ORIG_DESTINATION
    */
   private static final String JBOSS_DESTINATION = "JBOSS_ORIG_DESTINATION";

   public void onMessage(Message message)
   {
      synchronized (TestMessageDriven.class)
      {
         log.info("Got message: " + message);
         mbean.addMessage(message);
         if (isDLQ(message))
            return;
         logProperties();
         logTransaction();
         String rollback = getRollback();
         if (rollback.equals("DLQ"))
         {
            log.info("Rollback DLQ");
            ctx.setRollbackOnly();
         }
      }
   }

   public boolean isDLQ(Message message)
   {
      try
      {
         if (message.getStringProperty(JBOSS_DESTINATION) != null)
            return true;
      } catch (JMSException e)
      {
         log.error("Unhandled error", e);
      }
      return false;
   }

   public String getRollback()
   {
      return System.getProperty("test.messagedriven.rollback", "None");
   }

   public void logProperties()
   {
      Properties props = System.getProperties();
      for (Enumeration e = props.keys(); e.hasMoreElements();)
      {
         String key = (String) e.nextElement();
         if (key.startsWith("test.messagedriven."))
            log.info(key + "=" + props.getProperty(key));
      }
   }

   public Transaction logTransaction()
   {
      Transaction tx = mbean.getTransaction();
      log.info("tx=" + tx);
      return tx;
   }

   public void ejbCreate()
   {
      mbean = (TestMessageDrivenManagementMBean) MBeanProxyExt.create(TestMessageDrivenManagementMBean.class,
            TestMessageDrivenManagementMBean.OBJECT_NAME);
   }

   public void ejbRemove()
   {
   }

   public void setMessageDrivenContext(MessageDrivenContext ctx)
   {
      this.ctx = ctx;
   }
}
