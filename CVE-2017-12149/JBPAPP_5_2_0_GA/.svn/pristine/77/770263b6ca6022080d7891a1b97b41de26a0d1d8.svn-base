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
package org.jboss.test.ws.jaxws.samples.context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.security.Principal;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;

import org.jboss.wsf.common.DOMUtils;
import org.jboss.wsf.common.DOMWriter;
import org.jboss.wsf.spi.annotation.AuthMethod;
import org.jboss.wsf.spi.annotation.TransportGuarantee;
import org.jboss.wsf.spi.annotation.WebContext;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

@Stateless
@SOAPBinding(style = Style.RPC)
@WebService
(
   name = "Endpoint",
   serviceName="EndpointService",
   targetNamespace = "http://org.jboss.ws/jaxws/context"
)
@WebContext
(
   contextRoot = "/jaxws-samples-context",
   urlPattern = "/*",
   authMethod = AuthMethod.BASIC,
   transportGuarantee = TransportGuarantee.NONE,
   secureWSDLAccess = false
)

// [JBWS-1339] @Security domain vs. <security-domain> 
//@SecurityDomain("JBossWS")

@RolesAllowed("friend")
public class EndpointEJB
{
   @Resource
   WebServiceContext wsCtx;

   @WebMethod
   public String testGetMessageContext()
   {
      MessageContext msgContext = (MessageContext)wsCtx.getMessageContext();
      return msgContext == null ? "fail" : "pass";
   }

   @WebMethod
   public String testMessageContextProperties()
   {
      MessageContext msgContext = (MessageContext)wsCtx.getMessageContext();
      if (msgContext == null)
         return "fail";

      // Check standard jaxws properties
      Object wsdl = msgContext.get(MessageContext.WSDL_DESCRIPTION);
      QName service = (QName)msgContext.get(MessageContext.WSDL_SERVICE);
      QName portType = (QName)msgContext.get(MessageContext.WSDL_INTERFACE);
      QName port = (QName)msgContext.get(MessageContext.WSDL_PORT);
      QName operation = (QName)msgContext.get(MessageContext.WSDL_OPERATION);
      
      if (!service.equals(new QName("http://org.jboss.ws/jaxws/context", "EndpointService")))
         throw new WebServiceException("Invalid qname: " + service);
      if (!portType.equals(new QName("http://org.jboss.ws/jaxws/context", "Endpoint")))
         throw new WebServiceException("Invalid qname: " + portType);
      if (!port.equals(new QName("http://org.jboss.ws/jaxws/context", "EndpointPort")))
         throw new WebServiceException("Invalid qname: " + port);
      if (!operation.equals(new QName("http://org.jboss.ws/jaxws/context", "testMessageContextProperties")))
         throw new WebServiceException("Invalid qname: " + operation);
      
      try
      {
         Element root = null;
         if (wsdl instanceof InputSource)
         {
            root = DOMUtils.parse((InputSource)wsdl);
         }
         else if (wsdl instanceof URI)
         {
            root = DOMUtils.parse(((URI)wsdl).toURL().openStream());
         }
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         new DOMWriter(out).setPrettyprint(true).print(root);
         if (!out.toString().contains("http://schemas.xmlsoap.org/wsdl/"))
         {
            throw new WebServiceException("Not a wsdl");
         }
      }
      catch (IOException ex)
      {
         throw new WebServiceException("Cannot parse MessageContext.WSDL_DESCRIPTION", ex);
      }
      
      return "pass";
   }

   @WebMethod
   public String testGetUserPrincipal()
   {
      Principal principal = wsCtx.getUserPrincipal();
      return principal.getName();
   }

   @WebMethod
   public boolean testIsUserInRole(String role)
   {
      return wsCtx.isUserInRole(role);
   }
}
