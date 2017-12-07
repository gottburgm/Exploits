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
package org.jboss.test.ws.jaxws.ejb3Integration;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;

/**
 * A generic jaxws handler
 *
 * @author Thomas.Diesler@jboss.org
 * @since 13-Aug-2006
 */
public abstract class GenericHandler implements Handler
{
   private String handlerName;

   public String getHandlerName()
   {
      return handlerName;
   }

   public void setHandlerName(String handlerName)
   {
      this.handlerName = handlerName;
   }
   
   public boolean handleMessage(MessageContext msgContext)
   {
      Boolean outbound = (Boolean)msgContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
      if (outbound == null)
         throw new IllegalStateException("Cannot obtain required property: " + MessageContext.MESSAGE_OUTBOUND_PROPERTY);

      return outbound ? handleOutbound(msgContext) : handleInbound(msgContext);
   }

   protected boolean handleOutbound(MessageContext msgContext)
   {
      return true;
   }

   protected boolean handleInbound(MessageContext msgContext)
   {
      return true;
   }

   public boolean handleFault(MessageContext messagecontext)
   {
      return true;
   }

   public void close(MessageContext messageContext)
   {
   }

   public String toString()
   {
      return (handlerName != null ? handlerName : super.toString());
   }
}

