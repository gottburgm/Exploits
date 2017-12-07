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
package org.jboss.embedded.tutorial.javase;

import org.jboss.embedded.tutorial.javase.beans.CustomerDAORemote;
import org.jboss.embedded.tutorial.javase.beans.Customer;
import org.jboss.embedded.tutorial.javase.beans.CustomerDAOLocal;

import java.util.Hashtable;
import javax.naming.InitialContext;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 85945 $
 */
public class Main
{
   public static void run() throws Exception
   {
      InitialContext ctx = new InitialContext();
      CustomerDAOLocal local = (CustomerDAOLocal)ctx.lookup("CustomerDAOBean/local");
      CustomerDAORemote remote = (CustomerDAORemote)ctx.lookup("CustomerDAOBean/remote");

      System.out.println("----------------------------------------------------------");
      int id = local.createCustomer("Gavin");
      Customer cust = local.findCustomer(id);
      System.out.println("    Successfully created and found Gavin from @Local interface");

      id = remote.createCustomer("Emmanuel");
      cust = remote.findCustomer(id);
      System.out.println("    Successfully created and found Emmanuel from @Remote interface");
      System.out.println("----------------------------------------------------------");
   }
}
