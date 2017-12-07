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
package org.jboss.test.cluster.defaultcfg.clusteredentity.test;

import java.util.List;
import java.util.Properties;

import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.test.cluster.clusteredentity.BulkOperationsTest;
import org.jboss.test.cluster.clusteredentity.Contact;
import org.jboss.test.JBossClusteredTestCase;

/**
 * Sample client for the jboss container.
 *
 * @author Brian Stansberry
 * @version $Id: EntityUnitTestCase.java 60697 2007-02-20 05:08:31Z bstansberry@jboss.com $
 */
public class BulkOperationsUnitTestCase
extends JBossClusteredTestCase
{
   public BulkOperationsUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testBulkOperations() throws Exception
   {
      System.out.println("*** testBulkOperations()");
      String node0 = System.getProperty("jbosstest.cluster.node0");
        
      Properties prop0 = new Properties();
      prop0.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
      prop0.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
      prop0.put("java.naming.provider.url", "jnp://" + node0 + ":1099");
      
      System.out.println("===== Node0 properties: ");
      System.out.println(prop0);
      
      System.out.println("Lookup node 0");
      InitialContext ctx0 = new InitialContext(prop0);
      
      BulkOperationsTest tester = (BulkOperationsTest) ctx0.lookup("BulkOperationsTestBean/remote");
      
      try
      {
         tester.createContacts();
         
         List<Integer> rhContacts = tester.getContactsByCustomer("Red Hat");
         assertNotNull("Red Hat contacts exist", rhContacts);
         assertEquals("Created expected number of Red Hat contacts", 10, rhContacts.size());
         
         assertEquals("Deleted all Red Hat contacts", 10, tester.deleteContacts());
         
         List<Integer> jbContacts = tester.getContactsByCustomer("JBoss");
         assertNotNull("JBoss contacts exist", jbContacts);
         assertEquals("JBoss contacts remain", 10, jbContacts.size());
         
         for (Integer id : rhContacts)
         {
            assertNull("Red Hat contact " + id + " cannot be retrieved",
                       tester.getContact(id));
         }
         rhContacts = tester.getContactsByCustomer("Red Hat");
         if (rhContacts != null)
         {
            assertEquals("No Red Hat contacts remain", 0, rhContacts.size());
         }
         
         tester.updateContacts("Kabir", "Updated");
         for (Integer id : jbContacts)
         {
            Contact contact = tester.getContact(id);
            assertNotNull("JBoss contact " + id + " exists", contact);
            String expected = ("Kabir".equals(contact.getName())) ? "Updated" : "2222";
            assertEquals("JBoss contact " + id + " has correct TLF",
                         expected, contact.getTlf());
         }
         
         List<Integer> updated = tester.getContactsByTLF("Updated");
         assertNotNull("Got updated contacts", updated);
         assertEquals("Updated contacts", 5, updated.size());
      }
      finally
      {
         // cleanup the db so we can run this test multiple times w/o restarting the cluster
         tester.remove();
      }
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(BulkOperationsUnitTestCase.class, "clusteredentity-test.jar");
   }
}
