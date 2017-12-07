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
package org.jboss.test.ws.jaxws.samples.serviceref;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;


/**
 * JBossWS Generated Source
 * 
 * Generation Date: Mon Mar 12 15:09:39 CET 2007
 * 
 * This generated source code represents a derivative work of the input to
 * the generator that produced it. Consult the input for the copyright and
 * terms of use that apply to this source code.
 * 
 * JAX-WS Version: 2.0
 * 
 */
@WebServiceClient(name = "EndpointService", targetNamespace = "http://serviceref.samples.jaxws.ws.test.jboss.org/", wsdlLocation = "http://tddell:8080/jaxws-samples-serviceref?wsdl")
public class EndpointService
    extends Service
{

    private final static URL TESTENDPOINTSERVICE_WSDL_LOCATION;

    static {
        URL url = null;
        try {
            url = new URL("http://tddell:8080/jaxws-samples-serviceref?wsdl");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        TESTENDPOINTSERVICE_WSDL_LOCATION = url;
    }

    public EndpointService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public EndpointService() {
        super(TESTENDPOINTSERVICE_WSDL_LOCATION, new QName("http://serviceref.samples.jaxws.ws.test.jboss.org/", "EndpointService"));
    }

    /**
     * 
     * @return
     *     returns Endpoint
     */
    @WebEndpoint(name = "EndpointPort")
    public Endpoint getEndpointPort() {
        return (Endpoint)super.getPort(new QName("http://serviceref.samples.jaxws.ws.test.jboss.org/", "EndpointPort"), Endpoint.class);
    }

}
