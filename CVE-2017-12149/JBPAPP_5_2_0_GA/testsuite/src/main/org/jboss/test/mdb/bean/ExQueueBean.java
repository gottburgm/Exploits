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
 * MessageBeanImpl.java
 *
 *
 * Created: Sat Nov 25 18:07:50 2000
 *
 * @author 
 * @version
 */

public class ExQueueBean implements MessageDrivenBean, MessageListener{

   org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(getClass());
   
    private MessageDrivenContext ctx = null;
    public ExQueueBean() {
	
    }
    public void setMessageDrivenContext(MessageDrivenContext ctx)
	throws EJBException {
	this.ctx = ctx;
    }
    
    public void ejbCreate() {}

    public void ejbRemove() {ctx=null;}

    public void onMessage(Message message) {
       log.debug("ExQueueBean rolling back transaction" + message.toString());
       ctx.setRollbackOnly();
    }
} // MessageBeanImpl



