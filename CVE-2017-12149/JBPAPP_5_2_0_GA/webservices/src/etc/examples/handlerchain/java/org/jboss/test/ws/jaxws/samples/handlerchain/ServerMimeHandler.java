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

import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;
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
public class ServerMimeHandler extends GenericSOAPHandler
{
   // Provide logging
   private static Logger log = Logger.getLogger(ServerMimeHandler.class);
   
   private boolean setCookieOnResponse;

   protected boolean handleInbound(MessageContext msgContext)
   {
      log.info("handleInbound");

      SOAPMessage soapMessage = ((SOAPMessageContext)msgContext).getMessage();
      MimeHeaders mimeHeaders = soapMessage.getMimeHeaders();
      String[] cookies = mimeHeaders.getHeader("Cookie");
      if (cookies != null && cookies.length == 1 && cookies[0].equals("client-cookie=true"))
         setCookieOnResponse = true;

      return true;
   }


   protected boolean handleOutbound(MessageContext msgContext)
   {
      log.info("handleOutbound");
      
      if (setCookieOnResponse)
      {
         SOAPMessage soapMessage = ((SOAPMessageContext)msgContext).getMessage();
         MimeHeaders mimeHeaders = soapMessage.getMimeHeaders();
         mimeHeaders.setHeader("Set-Cookie", "server-cookie=true");
         setCookieOnResponse = false;
      }

      return true;
   }
}
