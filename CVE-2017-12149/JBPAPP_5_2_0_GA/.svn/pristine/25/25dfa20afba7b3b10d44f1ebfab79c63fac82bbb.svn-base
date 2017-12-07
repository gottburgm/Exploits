/*
 * JBoss, Home of Professional Open Source Copyright 2006, JBoss Inc., and
 * individual contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of individual
 * contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package org.jboss.test.ws.jaxws.samples.xop.doclit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/**
 * A SOAPHandler that checks for Include elements in the
 * outbound SOAPMessage in order to see if MTOM is enabled.
 * 
 * @author alessio.soldano@jboss.com
 * @since 15-Jan-2009
 */
public class MTOMCheckClientHandler implements SOAPHandler<SOAPMessageContext>
{

   public boolean handleMessage(SOAPMessageContext smc)
   {
      try
      {
         return check(smc);
      }
      catch (Exception e)
      {
         throw new WebServiceException(e);
      }
   }

   public boolean handleFault(SOAPMessageContext smc)
   {
      //NOOP
      return true;
   }

   private static boolean check(SOAPMessageContext smc) throws SOAPException, IOException
   {
      Boolean outboundProperty = (Boolean)smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

      if (outboundProperty.booleanValue())
      {
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
         SOAPMessage message = smc.getMessage();
         message.writeTo(outputStream);
         String messageString = outputStream.toString();
         if (!messageString.contains("Include"))
            throw new IllegalStateException("XOP request inlined");
      }
      return true;
   }

   public void close(MessageContext messageContext)
   {
      //NOOP
   }

   public Set<QName> getHeaders()
   {
      return new HashSet<QName>(); //empty set
   }

}
