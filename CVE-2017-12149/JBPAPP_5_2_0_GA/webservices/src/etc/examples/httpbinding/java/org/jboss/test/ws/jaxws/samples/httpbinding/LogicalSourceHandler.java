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
package org.jboss.test.ws.jaxws.samples.httpbinding;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;

import org.jboss.logging.Logger;
import org.jboss.wsf.common.handler.GenericLogicalHandler;
import org.jboss.wsf.common.DOMUtils;
import org.w3c.dom.Element;

/**
 * A jaxws logical handler
 *
 * @author Thomas.Diesler@jboss.org
 * @since 24-Nov-2005
 */
public class LogicalSourceHandler extends GenericLogicalHandler
{
   // provide logging
   private static final Logger log = Logger.getLogger(LogicalSourceHandler.class);

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

   public boolean appendHandlerName(MessageContext msgContext, String direction)
   {
      try
      {
         // Get the payload as Source
         LogicalMessageContext logicalContext = (LogicalMessageContext)msgContext;
         Source source = logicalContext.getMessage().getPayload();
         TransformerFactory tf = TransformerFactory.newInstance();
         ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
         tf.newTransformer().transform(source, new StreamResult(baos));

         // Parse the payload and extract the value
         Element root = DOMUtils.parse(new ByteArrayInputStream(baos.toByteArray()));

         String oldValue = DOMUtils.getTextContent(root);
         String newValue = oldValue + ":" + direction + "LogicalHandler";
         root.setTextContent(newValue);

         log.debug("oldValue: " + oldValue);
         log.debug("newValue: " + newValue);

         // Set the updated payload
         source = new DOMSource(root);
         logicalContext.getMessage().setPayload(source);

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
