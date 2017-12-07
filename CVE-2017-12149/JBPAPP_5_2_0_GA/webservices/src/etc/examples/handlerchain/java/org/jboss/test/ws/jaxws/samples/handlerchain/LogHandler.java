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
package org.jboss.test.ws.jaxws.samples.handlerchain;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.logging.Logger;
import org.jboss.wsf.common.handler.GenericSOAPHandler;

/**
 * A server side handler
 *
 * @author Thomas.Diesler@jboss.org
 * @since 08-Oct-2005
 */
public class LogHandler extends GenericSOAPHandler
{
   // Provide logging
   private static Logger log = Logger.getLogger(LogHandler.class);

   protected boolean handleInbound(MessageContext msgContext)
   {
      log.info("handleInbound");

      try
      {
         SOAPMessage soapMessage = ((SOAPMessageContext)msgContext).getMessage();
         SOAPHeader soapHeader = getFailsafeSOAPHeader(soapMessage);
         SOAPBody soapBody = soapMessage.getSOAPBody();

         SOAPFactory soapFactory = SOAPFactory.newInstance();
         Name headerName = soapFactory.createName("LogHandlerInbound", "ns1", "http://somens");
         SOAPHeaderElement she = soapHeader.addHeaderElement(headerName);
         she.setValue("true");

         SOAPBodyElement soapBodyElement = (SOAPBodyElement)soapBody.getChildElements().next();
         SOAPElement soapElement = (SOAPElement)soapBodyElement.getChildElements().next();
         String value = soapElement.getValue();
         soapElement.setValue(value + "|LogIn");
      }
      catch (SOAPException e)
      {
         throw  new WebServiceException(e);
      }

      return true;
   }

   protected boolean handleOutbound(MessageContext msgContext)
   {
      log.info("handleOutbound");

      try
      {
         SOAPMessage soapMessage = ((SOAPMessageContext)msgContext).getMessage();
         SOAPHeader soapHeader = getFailsafeSOAPHeader(soapMessage);
         SOAPBody soapBody = soapMessage.getSOAPBody();
         
         SOAPFactory soapFactory = SOAPFactory.newInstance();
         Name headerName = soapFactory.createName("LogHandlerOutbound", "ns1", "http://somens");
         SOAPHeaderElement she = soapHeader.addHeaderElement(headerName);
         she.setValue("true");

         SOAPBodyElement soapBodyElement = (SOAPBodyElement)soapBody.getChildElements().next();
         SOAPElement soapElement = (SOAPElement)soapBodyElement.getChildElements().next();
         String value = soapElement.getValue();
         soapElement.setValue(value + "|LogOut");
      }
      catch (SOAPException e)
      {
         throw  new WebServiceException(e);
      }

      return true;
   }

   private SOAPHeader getFailsafeSOAPHeader(SOAPMessage soapMessage) throws SOAPException
   {
      SOAPHeader soapHeader = soapMessage.getSOAPHeader();
      if (soapHeader == null)
      {
         soapHeader = soapMessage.getSOAPPart().getEnvelope().addHeader();
      }
      return soapHeader;
   }
}
