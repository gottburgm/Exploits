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
package org.jboss.test.messagedriven.mbeans;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import javax.jms.Message;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.deployment.EjbParsingDeployerMBean;
import org.jboss.system.MissingAttributeException;
import org.jboss.system.ServiceMBeanSupport;

/**
 * Management of the test message driven bean 
 *
 * @author <a href="mailto:adrian@jboss.com>Adrian Brock</a>
 * @version <tt>$Revision: 1.4</tt>
 */
public class TestMessageDrivenManagement extends ServiceMBeanSupport implements TestMessageDrivenManagementMBean
{
   private static final Properties defaultProps = new Properties();
   
   private TransactionManager tm;
   
   private EjbParsingDeployerMBean ejbParsingDeployer;
   
   private Boolean previousValidationValue;
   
   static
   {
      defaultProps.put("destination", "NotSpecified");
      defaultProps.put("destinationType", "NotSpecified");
      defaultProps.put("transactionType", "Container");
      defaultProps.put("transactionAttribute", "Required");
      defaultProps.put("rollback", "None");
      defaultProps.put("DLQMaxResent", "5");
      defaultProps.put("DeliveryActive", "true");
      defaultProps.put("durability", "NonDurable");
      defaultProps.put("subscriptionName", "");
      defaultProps.put("user", "guest");
      defaultProps.put("password", "guest");
      defaultProps.put("createDestination", "false");
   }
   
   protected ArrayList<Object[]> messages = new ArrayList<Object[]>();
   
   public TestMessageDrivenManagement() throws Exception
   {
      tm = (TransactionManager) new InitialContext().lookup("java:/TransactionManager");
   }
   
   public void setEjbParsingDeployer(EjbParsingDeployerMBean ejbParsingDeployer)
   {
	   this.ejbParsingDeployer = ejbParsingDeployer;
   }
   
   public EjbParsingDeployerMBean getEjbParsingDeployer()
   {
	   return ejbParsingDeployer;
   }
   
   
   public void initProperties(Properties props)
   {
      setProperties(defaultProps);
      setProperties(props);
   }
   
   public void addMessage(Message message)
   {
      synchronized (messages)
      {
         try
         {
            Properties props = new Properties();
            
            Enumeration<?> names = message.getPropertyNames();
            while (names.hasMoreElements())
            {
               String key = names.nextElement().toString();
               props.put(key, message.getObjectProperty(key));
               
            }
            messages.add(new Object[]{((TextMessage)message).getText(), message.getJMSDestination(), props});
         }
         catch (Throwable e)
         {
            log.warn("Error on retreiving message's text ", e);
         }
      }
   }

   public ArrayList<Object[]> getMessages()
   {
      synchronized (messages)
      {
         ArrayList<Object[]> result = new ArrayList<Object[]>(messages);
         
         messages.clear();
         return result;
      }
   }
   
   public Transaction getTransaction()
   {
      Transaction tx = null;
      try
      {
         tx = tm.getTransaction();
      }
      catch (Throwable ignored)
      {
      }
      return tx;
   }
   
   protected void setProperties(Properties props)
   {
      for (Enumeration e = props.keys(); e.hasMoreElements();)
      {
         String key = (String) e.nextElement();
         System.setProperty("test.messagedriven." + key, props.getProperty(key));
      }
   }
   
   /**
    * JBossMessage will resend the send message, and because of that the message needs to be cloned before being stored
    * @param message
    * @return
    */
   private Message cloneMessage(Message message) 
   {
		try
		 {
			 ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			 ObjectOutputStream cloneOut = new ObjectOutputStream(byteOut);
			 cloneOut.writeObject(message);
			 cloneOut.close();
			 ObjectInputStream inputArray = new ObjectInputStream (new ByteArrayInputStream(byteOut.toByteArray()));
			 message = (Message)inputArray.readObject();
		 }
		 catch (Exception e)
		 {
			 log.error(e.toString(), e);
		 }
		return message;
	}
   
   public void startService() throws Exception
   {
      if(this.ejbParsingDeployer == null)
        throw new MissingAttributeException("EjbParsingDeployer");
      
      this.previousValidationValue = this.ejbParsingDeployer.isUseValidation();
      this.ejbParsingDeployer.setUseValidation(Boolean.FALSE);
   }
	   
   
   public void stopService()
   {
      this.ejbParsingDeployer.setUseValidation(this.previousValidationValue);
   }

}
