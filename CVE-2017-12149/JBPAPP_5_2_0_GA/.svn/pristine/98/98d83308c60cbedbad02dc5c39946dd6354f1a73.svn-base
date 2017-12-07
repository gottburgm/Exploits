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
package org.jboss.test.ws.jaxws.samples.swaref;

import org.jboss.wsf.spi.annotation.WebContext;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.soap.SOAPBinding;
import javax.ejb.Stateless;
import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlAttachmentRef;
import javax.xml.ws.WebServiceException;

import java.io.IOException;
import java.io.InputStream;

@Stateless
@WebService(name="BareEndpoint", serviceName="BareEndpointService")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT, parameterStyle = SOAPBinding.ParameterStyle.BARE)
@WebContext(contextRoot = "jaxws-swaref")
public class BareEndpointImpl implements BareEndpoint
{
   @WebMethod
   public DocumentPayload beanAnnotation(DocumentPayload payload)
   {
      try
      {
         Object dataContent = payload.getData().getContent();
         System.out.println("Got '" + dataContent +"'");
         if (dataContent instanceof InputStream)
         {
            ((InputStream)dataContent).close();
         }
         return new DocumentPayload( new DataHandler("Server data", "text/plain"));
      }
      catch (IOException e)
      {
         throw new WebServiceException(e);
      }
   }

   @WebMethod
   @XmlAttachmentRef
   public DocumentPayloadWithoutRef parameterAnnotation(@XmlAttachmentRef DocumentPayloadWithoutRef payload)
   {
      try
      {
         if(null == payload) throw new WebServiceException("Payload was null");
         Object dataContent = payload.getData().getContent();
         System.out.println("Got " + dataContent);
         if (dataContent instanceof InputStream)
         {
            ((InputStream)dataContent).close();
         }
         return new DocumentPayloadWithoutRef(new DataHandler("Server data", "text/plain"));
      }
      catch (IOException e)
      {
         throw new WebServiceException(e);
      }
   }
}
