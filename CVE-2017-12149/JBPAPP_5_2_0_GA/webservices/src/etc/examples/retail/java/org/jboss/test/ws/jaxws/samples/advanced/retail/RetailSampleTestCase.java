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
package org.jboss.test.ws.jaxws.samples.advanced.retail;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * @author Heiko Braun <heiko.braun@jboss.com>
 * @since 08-Nov-2006
 */
public class RetailSampleTestCase extends JBossWSTest {

   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-samples-retail/OrderMgmtBean";

   private OrderMgmt orderMgmtWS;

   public static Test suite()
   {
      return new JBossWSTestSetup(RetailSampleTestCase.class, "jaxws-samples-retail.jar");
   }

   protected void setUp() throws Exception
   {
      QName serviceName = new QName("http://retail.advanced.samples.jaxws.ws.test.jboss.org/", "OrderMgmtService");
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS+"?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      orderMgmtWS = (OrderMgmt)service.getPort(OrderMgmt.class);
   }

   public void testWebService() throws Exception
   {
      Customer customer = new Customer();
      customer.setFirstName("Chuck");
      customer.setLastName("Norris");
      customer.setCreditCardDetails("1000-4567-3456-XXXX");

      Order order = new Order(customer);
      order.setOrderNum(12345);
      order.getItems().add( new OrderItem("Introduction to Web Services", 39.99) );
      
      OrderStatus result = orderMgmtWS.prepareOrder(order);
      assertNotNull("Result was null", result);
      assertEquals("Prepared", result.getStatus());
   }
}
