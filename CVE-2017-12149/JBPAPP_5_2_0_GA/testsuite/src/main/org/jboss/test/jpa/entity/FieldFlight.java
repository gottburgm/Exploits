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
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.persistence.Version;

/**
 * Flight
 *
 * @author Emmanuel Bernard
 */
@Entity
public class FieldFlight implements java.io.Serializable
{
   @Id
   Long id;

   @Column(updatable = false, name = "flight_name", nullable = false, length = 50)
   String name;

   @Basic(fetch = FetchType.LAZY)
   long duration;

   @Transient
   long durationInSec;

   @Version
   @Column(name = "OPTLOCK")
   Integer version;

   @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
   @JoinColumn(name = "COMP_ID")
   FieldCompany company;

   @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
   Set<FieldCustomer> customers;

   public Long getId()
   {
      return id;
   }

   public void setId(Long long1)
   {
      id = long1;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String string)
   {
      name = string;
   }

   public long getDuration()
   {
      return duration;
   }

   public void setDuration(long l)
   {
      duration = l;
   }

   public long getDurationInSec()
   {
      return durationInSec;
   }

   public void setDurationInSec(long l)
   {
      durationInSec = l;
   }

   public Integer getVersion()
   {
      return version;
   }

   public void setVersion(Integer i)
   {
      version = i;
   }

   public FieldCompany getCompany()
   {
      return company;
   }

   public void setCompany(FieldCompany company)
   {
      this.company = company;
   }

   public Set<FieldCustomer> getCustomers()
   {
      return customers;
   }

   public void setCustomers(Set<FieldCustomer> customers)
   {
      this.customers = customers;
   }
}
