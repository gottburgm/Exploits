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
package org.jboss.test.ws.jaxws.samples.xop.doclit;

import java.io.IOException;
import java.io.InputStream;

import javax.activation.DataHandler;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.MTOM;

import org.jboss.logging.Logger;

@WebService(name = "MTOMEndpoint", serviceName = "MTOMService", endpointInterface = "org.jboss.test.ws.jaxws.samples.xop.doclit.MTOMEndpoint")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
@MTOM
public class MTOMEndpointBean implements MTOMEndpoint
{
   private Logger log = Logger.getLogger(MTOMEndpointBean.class);

   public DHResponse echoDataHandler(DHRequest request)
   {

      DataHandler dataHandler = request.getDataHandler();

      try
      {
         log.info("Recv " + dataHandler.getContentType());
         Object dataContent = dataHandler.getContent();
         log.info("Content is " + dataContent);
         if ( dataContent instanceof InputStream )
         {
            ((InputStream)dataContent).close();
         }
      }
      catch (IOException e)
      {
         throw new WebServiceException(e);
      }

      DataHandler responseData = new DataHandler("Server data", "text/plain");
      return new DHResponse(responseData);
   }

   public ImageResponse echoImage(ImageRequest request)
   {
      return new ImageResponse(request.getData());
   }

   public SourceResponse echoSource(SourceRequest request)
   {
      return new SourceResponse(request.getData());
   }
}
