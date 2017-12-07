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
package org.jboss.test.jpa.entity;

import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

/**
 * Company customer
 *
 * @author Emmanuel Bernard
 */
@Entity
public class Customer implements java.io.Serializable
{
   Long id;
   String name;
   Set<Ticket> tickets;
   Set<Flight> flights;
   Address address;

   public
   Customer()
   {
   }

   @Id
   @GeneratedValue(strategy= GenerationType.AUTO)
           public
   Long getId()
   {
      return id;
   }

   public
   String getName()
   {
      return name;
   }

   public
   void setId(Long long1)
   {
      id = long1;
   }

   public
   void setName(String string)
   {
      name = string;
   }

   @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy="customer")
   public Set<Ticket> getTickets()
   {
      return tickets;
   }

   public void setTickets(Set<Ticket> tickets)
   {
      this.tickets = tickets;
   }

   @OneToOne(cascade = {CascadeType.ALL})
           @JoinColumn(name = "ADDRESS_ID")
           public Address getAddress()
   {
      return address;
   }

   public void setAddress(Address address)
   {
      this.address = address;
   }

   @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER, mappedBy="customers")
   public Set<Flight> getFlights()
   {
      return flights;
   }

   public void setFlights(Set<Flight> flights)
   {
      this.flights = flights;
   }


   /*
   @OneToMany(cascade = CascadeType.ALL,
              targetEntity = "org.hibernate.test.metadata.Discount")
   @JoinColumn(name = "CUSTOMER_ID")
   public Collection getDiscountTickets()
   {
      return discountTickets;
   }

   public void setDiscountTickets(Collection collection)
   {
      discountTickets = collection;
   }
   */
}

