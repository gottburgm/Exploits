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
package org.jboss.test.ws.jaxws.samples.logicalhandler;

import java.util.Iterator;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.logging.Logger;
import org.jboss.wsf.common.handler.GenericSOAPHandler;

/**
 * A jaxws protocol handler
 *
 * @author Thomas.Diesler@jboss.org
 * @since 24-Nov-2005
 */
public class PortHandler extends GenericSOAPHandler
{
   // provide logging
   private static final Logger log = Logger.getLogger(PortHandler.class);
   
   @Override
   public boolean handleOutbound(MessageContext msgContext)
   {
      return appendHandlerName(msgContext, "Outbound");
   }

   @Override
   public boolean handleInbound(MessageContext msgContext)
   {
      return appendHandlerName(msgContext, "Inbound");
   }

   private boolean appendHandlerName(MessageContext msgContext, String direction)
   {
      try
      {
         SOAPMessage soapMessage = ((SOAPMessageContext)msgContext).getMessage();
         SOAPElement soapElement = getFirstChildElement(soapMessage.getSOAPBody());
         soapElement = getFirstChildElement(soapElement);
         
         String oldValue = soapElement.getValue();
         String newValue = oldValue + ":" + direction + ":PortHandler";
         soapElement.setValue(newValue);
         
         log.debug("oldValue: " + oldValue);
         log.debug("newValue: " + newValue);
         
         return true;
      }
      catch (SOAPException ex)
      {
         throw new WebServiceException(ex);
      }
   }
   
   private SOAPElement getFirstChildElement(SOAPElement parentNode)
   {
      Iterator i = parentNode.getChildElements();
      while (i.hasNext())
      {
         Object currentNode = i.next();
         if (currentNode instanceof SOAPElement)
         {
            return (SOAPElement) currentNode;
         }
      }
      
      return null;
   }
}
