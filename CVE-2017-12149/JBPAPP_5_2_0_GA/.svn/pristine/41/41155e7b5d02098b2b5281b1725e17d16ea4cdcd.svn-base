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

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Heiko Braun <heiko.braun@jboss.com>
 * @since Nov 8, 2006
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "orderStatus", namespace = "http://org.jboss.ws/samples/retail")
public class OrderStatus implements Serializable {
   private String status;
   private long orderNum;
   private double discount;

   public OrderStatus() {
   }

   public OrderStatus(String status, long orderNum, double discount) {
      this.status = status;
      this.orderNum = orderNum;
      this.discount = discount;
   }

   public String getStatus() {
      return status;
   }

   public void setStatus(String status) {
      this.status = status;
   }

   public long getOrderNum() {
      return orderNum;
   }

   public void setOrderNum(long orderNum) {
      this.orderNum = orderNum;
   }

   public double getDiscount() {
      return discount;
   }

   public void setDiscount(double discount) {
      this.discount = discount;
   }
}
