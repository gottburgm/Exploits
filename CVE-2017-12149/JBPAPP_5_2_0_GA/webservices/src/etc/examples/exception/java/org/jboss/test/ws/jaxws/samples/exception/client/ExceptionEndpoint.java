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
package org.jboss.test.ws.jaxws.samples.exception.client;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;


@WebService(name = "ExceptionEndpoint", targetNamespace = "http://server.exception.samples.jaxws.ws.test.jboss.org/")
public interface ExceptionEndpoint {


    /**
     * 
     */
    @WebMethod
    @RequestWrapper(localName = "throwRuntimeException", targetNamespace = "http://server.exception.samples.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.samples.exception.client.ThrowRuntimeException")
    @ResponseWrapper(localName = "throwRuntimeExceptionResponse", targetNamespace = "http://server.exception.samples.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.samples.exception.client.ThrowRuntimeExceptionResponse")
    public void throwRuntimeException();

    /**
     * 
     */
    @WebMethod
    @RequestWrapper(localName = "throwSoapFaultException", targetNamespace = "http://server.exception.samples.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.samples.exception.client.ThrowSoapFaultException")
    @ResponseWrapper(localName = "throwSoapFaultExceptionResponse", targetNamespace = "http://server.exception.samples.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.samples.exception.client.ThrowSoapFaultExceptionResponse")
    public void throwSoapFaultException();

    /**
     * 
     * @throws UserException_Exception
     */
    @WebMethod
    @RequestWrapper(localName = "throwApplicationException", targetNamespace = "http://server.exception.samples.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.samples.exception.client.ThrowApplicationException")
    @ResponseWrapper(localName = "throwApplicationExceptionResponse", targetNamespace = "http://server.exception.samples.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.samples.exception.client.ThrowApplicationExceptionResponse")
    public void throwApplicationException()
        throws UserException_Exception
    ;

}
