/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.cmp2.commerce;

import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import javax.naming.InitialContext;
import org.jboss.test.util.ejb.EJBTestCase;

public class UserLocalTest extends EJBTestCase {

   public UserLocalTest(String name) {
      super(name);
   }

   private UserLocalHome getUserLocalHome() {
      try {
         InitialContext jndiContext = new InitialContext();

         return (UserLocalHome) jndiContext.lookup("commerce/UserLocalHome"); 
      } catch(Exception e) {
         e.printStackTrace();
         fail("Exception in getUserLocalHome: " + e.getMessage());
      }
      return null;
   }

   public void testDeclaredSql() {
      UserLocalHome userLocalHome = getUserLocalHome();

      try {
         UserLocal main = userLocalHome.create("main");
         UserLocal tody1 = userLocalHome.create("tody1");
         UserLocal tody2 = userLocalHome.create("tody2");
         UserLocal tody3 = userLocalHome.create("tody3");
         UserLocal tody4 = userLocalHome.create("tody4");

         Collection userIds = main.getUserIds();

         assertTrue(userIds.size() == 5);
         Iterator i = userIds.iterator();
         assertTrue(i.next().equals("main"));
         assertTrue(i.next().equals("tody1"));
         assertTrue(i.next().equals("tody2"));
         assertTrue(i.next().equals("tody3"));
         assertTrue(i.next().equals("tody4"));

      } catch(Exception e) {
         e.printStackTrace();
         fail("Error in testDeclaredSql");
      }
   }
   

   public void setUpEJB(Properties props) throws Exception {
      deleteAllUsers(getUserLocalHome());
   }
   
   public void tearDownEJB(Properties props) throws Exception {
   }
   
   public void deleteAllUsers(UserLocalHome userLocalHome) throws Exception {
      Iterator currentUsers = userLocalHome.findAll().iterator();
      while(currentUsers.hasNext()) {
         UserLocal user = (UserLocal)currentUsers.next();
         user.remove();
      }   
   }
}



