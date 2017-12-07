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

import javax.activation.DataHandler;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.ws.BindingType;
import javax.xml.ws.WebServiceException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Heiko.Braun@jboss.com
 */
@WebService(name = "WrappedEndpoint", serviceName = "WrappedService")
@BindingType(value = "http://schemas.xmlsoap.org/wsdl/soap/http?mtom=true")
public class WrappedEndpointImpl implements WrappedEndpoint
{

   @WebMethod
   @XmlMimeType("text/plain")
   public DataHandler parameterAnnotation(@XmlMimeType("text/plain") DataHandler data)
   {
      try
      {
         System.out.println("Recv " + data.getContentType());
         Object dataContent = data.getContent();
         System.out.println("Got " + dataContent);
         if ( dataContent instanceof InputStream )
         {
            ((InputStream)dataContent).close();
         }
         return new DataHandler("Server data", "text/plain");
      }
      catch (IOException e)
      {
         throw new WebServiceException(e);
      }
   }
}
