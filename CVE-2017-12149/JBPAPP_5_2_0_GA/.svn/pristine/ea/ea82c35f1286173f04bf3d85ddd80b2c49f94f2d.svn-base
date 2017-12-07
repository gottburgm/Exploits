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
package org.jboss.test.ws.jaxws.samples.advanced.retail.profile;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

import org.jboss.test.ws.jaxws.samples.advanced.retail.Customer;

/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.jboss.test.ws.jaxws.samples.advanced.retail.profile package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _GetCustomerDiscount_QNAME = new QName("http://org.jboss.ws/samples/retail/profile", "getCustomerDiscount");
    private final static QName _GetCustomerDiscountResponse_QNAME = new QName("http://org.jboss.ws/samples/retail/profile", "getCustomerDiscountResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.jboss.test.ws.jaxws.samples.advanced.retail.profile
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Customer }
     * 
     */
    public Customer createCustomer() {
        return new Customer();
    }

    /**
     * Create an instance of {@link DiscountResponse }
     * 
     */
    public DiscountResponse createDiscountResponse() {
        return new DiscountResponse();
    }

    /**
     * Create an instance of {@link DiscountRequest }
     * 
     */
    public DiscountRequest createDiscountRequest() {
        return new DiscountRequest();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DiscountRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://org.jboss.ws/samples/retail/profile", name = "getCustomerDiscount")
    public JAXBElement<DiscountRequest> createGetCustomerDiscount(DiscountRequest value) {
        return new JAXBElement<DiscountRequest>(_GetCustomerDiscount_QNAME, DiscountRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DiscountResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://org.jboss.ws/samples/retail/profile", name = "getCustomerDiscountResponse")
    public JAXBElement<DiscountResponse> createGetCustomerDiscountResponse(DiscountResponse value) {
        return new JAXBElement<DiscountResponse>(_GetCustomerDiscountResponse_QNAME, DiscountResponse.class, null, value);
    }

}
