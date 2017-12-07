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

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Heiko Braun <heiko.braun@jboss.com>
 * @since Nov 7, 2006
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "orderType",
   namespace="http://org.jboss.ws/samples/retail",
   propOrder = { "orderNum", "state", "customer", "items" }
)
public class Order implements Serializable {

   public enum OrderState {TRANSIENT, PREPARED, VERIFIED, PROCESSED}

   private OrderState state;
   private long orderNum;
   private Customer customer;
   private List<OrderItem> items;

   public Order(Customer customer) {
      this.customer = customer;
   }

   public Order() {
      this.state = OrderState.TRANSIENT;
   }

   public long getOrderNum() {
      return orderNum;
   }

   public void setOrderNum(long orderNum) {
      this.orderNum = orderNum;
   }

   public Customer getCustomer() {
      return customer;
   }

   public void setCustomer(Customer customer) {
      this.customer = customer;
   }

   public List<OrderItem> getItems() {
      if(null==items)
         items = new ArrayList<OrderItem>();
      return items;
   }

   public OrderState getState() {
      return state;
   }

   public void setState(OrderState state) {
      this.state = state;
   }

   public String toString() {
      return "Order {num="+orderNum+"}";   
   }
}
