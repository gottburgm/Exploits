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
package org.jboss.test.mdb.bean;

import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.ejb.EJBException;

import javax.jms.MessageListener;
import javax.jms.Message;
/**
 * The TXTimeOutBean simulates when the onMessage() takes
 * a long time to process the message.  When this happens,
 * the TM might time-out the transaction.  This bean 
 * can be used to see if the TX times outs occur.
 *
 * @author Hiram Chirino
 */

public class TXTimeOutBean implements MessageDrivenBean, MessageListener{

   org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(getClass());
   
	long PROCESSING_DELAY = 10; // simulate 10 seconds of processing
	
    public void setMessageDrivenContext(MessageDrivenContext ctx) {}
    public void ejbCreate() {}
    public void ejbRemove() {}

    public void onMessage(Message message) {
    	try {
			log.debug("Simulating "+PROCESSING_DELAY+" second(s) of message processing ");
			Thread.sleep(PROCESSING_DELAY*1000);
			log.debug("Message processing simulation done.");
    	} catch (Throwable ignore) {}
    }
} 


