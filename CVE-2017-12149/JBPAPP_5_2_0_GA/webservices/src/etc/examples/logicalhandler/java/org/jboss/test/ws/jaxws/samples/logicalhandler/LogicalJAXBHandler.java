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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;

import org.jboss.wsf.common.handler.GenericLogicalHandler;

public class LogicalJAXBHandler extends GenericLogicalHandler
{
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
         // Get the payload as Source
         LogicalMessageContext logicalContext = (LogicalMessageContext)msgContext;
         JAXBContext jaxb = JAXBContext.newInstance(Echo.class.getPackage().getName());
         Object payload = logicalContext.getMessage().getPayload(jaxb);

         JAXBElement jaxbElement = null;
         if (payload instanceof JAXBElement)
         {
            jaxbElement = (JAXBElement)payload;
            payload = jaxbElement.getValue();
         }

         if (payload instanceof Echo)
         {
            Echo echo = (Echo)payload;
            String value = echo.getString1();
            echo.setString1(value + ":" + direction + ":LogicalJAXBHandler");
         }
         else if (payload instanceof EchoResponse)
         {
            EchoResponse echo = (EchoResponse)payload;
            String value = echo.getResult();
            echo.setResult(value + ":" + direction + ":LogicalJAXBHandler");
         }
         else
         {
            throw new WebServiceException("Invalid payload type: " + payload);
         }

         if (jaxbElement != null)
         {
            jaxbElement.setValue(payload);
            payload = jaxbElement;
         }

         // Set the updated payload
         logicalContext.getMessage().setPayload(payload, jaxb);

         return true;
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         throw new WebServiceException(ex);
      }
   }
}
