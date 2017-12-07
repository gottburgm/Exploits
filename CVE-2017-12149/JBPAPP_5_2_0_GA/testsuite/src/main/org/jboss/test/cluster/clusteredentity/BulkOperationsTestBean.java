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

package org.jboss.test.cluster.clusteredentity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.PersistenceContext;

import org.jboss.ejb3.annotation.RemoteBinding;

/**
 * @author Brian Stansberry
 * @version $Revision$
 */
@Stateless
@Remote(BulkOperationsTest.class)
@RemoteBinding(jndiBinding="BulkOperationsTestBean/remote")
public class BulkOperationsTestBean implements BulkOperationsTest
{
   @PersistenceContext
   private EntityManager manager;
   
   
   public void createContacts()
   {
      for (int i = 0; i < 10; i++)
         createCustomer(i);
   }

   public int deleteContacts()
   {
      String deleteHQL = "delete Contact where customer in ";
      deleteHQL += " (select customer FROM Customer as customer ";
      deleteHQL += " where customer.name = :cName)";

      int rowsAffected = manager.createQuery(deleteHQL)
                                .setFlushMode(FlushModeType.AUTO)
                                .setParameter("cName", "Red Hat")
                                .executeUpdate();
      return rowsAffected;
   }
   
   public List<Integer> getContactsByCustomer(String customerName)
   {
      String selectHQL = "select contact.id from Contact contact";
      selectHQL += " where contact.customer.name = :cName";
   
      List results = manager.createQuery(selectHQL)
                            .setFlushMode(FlushModeType.AUTO)
                            .setParameter("cName", customerName)
                            .getResultList();
      
      return results;      
   }
   
   public List<Integer> getContactsByTLF(String tlf)
   {
      String selectHQL = "select contact.id from Contact contact";
      selectHQL += " where contact.tlf = :cTLF";
   
      List results = manager.createQuery(selectHQL)
                            .setFlushMode(FlushModeType.AUTO)
                            .setParameter("cTLF", tlf)
                            .getResultList();
      
      return results;      
   }

   public int updateContacts(String name, String newTLF)
   {
      String updateHQL = "update Contact set tlf = :cNewTLF where name = :cName";

      int rowsAffected = manager.createQuery(updateHQL)
                                .setFlushMode(FlushModeType.AUTO)
                                .setParameter("cNewTLF", newTLF)
                                .setParameter("cName", name)
                                .executeUpdate();
      return rowsAffected;
   }
   
   public Contact getContact(Integer id)
   {
      return manager.find(Contact.class, id);
   }
   
   @Remove
   public void remove()
   {
      
   }
   
   private Customer createCustomer(int id)
   {
      System.out.println("CREATE CUSTOMER " + id);
      try
      {
         Customer customer = new Customer();
         customer.setId(id);
         customer.setName((id % 2 == 0) ? "JBoss" : "Red Hat");
         Set<Contact> contacts = new HashSet<Contact>();
         
         Contact kabir = new Contact();
         kabir.setId(1000 + id);
         kabir.setCustomer(customer);
         kabir.setName("Kabir");
         kabir.setTlf("1111");
         contacts.add(kabir);
         
         Contact bill = new Contact();
         bill.setId(2000 +id);
         bill.setCustomer(customer);
         bill.setName("Bill");
         bill.setTlf("2222");
         contacts.add(bill);

         customer.setContacts(contacts);

         manager.persist(customer);
         return customer;
      }
      catch (RuntimeException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         System.out.println("CREATE CUSTOMER " +  id + " -  END");         
      }
   }

}
