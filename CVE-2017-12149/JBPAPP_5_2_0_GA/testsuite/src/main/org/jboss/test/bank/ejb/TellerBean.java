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
package org.jboss.test.bank.ejb;

import java.util.*;

import javax.naming.InitialContext;

import org.jboss.test.util.ejb.SessionSupport;
import org.jboss.test.bank.interfaces.*;


/**
 *      
 *   @see <related>
 *   @author $Author: dimitris@jboss.org $
 *   @version $Revision: 81036 $
 */
public class TellerBean
   extends SessionSupport
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   static int invocations;
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------
   public void transfer(Account from, Account to, float amount)
      throws BankException
   {
      try
      {
         log.debug("Invocation #"+invocations++);
         from.withdraw(amount);
         to.deposit(amount);
      } catch (Exception e)
      {
         throw new BankException("Could not transfer "+amount+" from "+from+" to "+to, e);
      }
   }

   public Account createAccount(Customer customer, float balance)
      throws BankException
   {
      try
      {
         BankHome bankHome = (BankHome)new InitialContext().lookup(BankHome.COMP_NAME);
         Bank bank = bankHome.create();

         AccountHome home = (AccountHome)new InitialContext().lookup(AccountHome.COMP_NAME);
         AccountData data = new AccountData();
         data.setId(bank.createAccountId(customer));
         data.setBalance(balance);
         data.setOwner(customer);
         Account acct = home.create(data);
       customer.addAccount(acct);

         return acct;
      } catch (Exception e)
      {
         log.debug("failed", e);
         throw new BankException("Could not create account", e);
      }
   }

   public Account getAccount(Customer customer, float balance)
      throws BankException
   {
      try
      {
         // Check for existing account
         Collection accounts = customer.getAccounts();
         if (accounts.size() > 0)
         {
            Iterator i = accounts.iterator();
            Account acct = (Account)i.next();
            // Set balance
            acct.withdraw(acct.getBalance()-balance);

            return acct;
         } else
         {
            // Create account
            return createAccount(customer, balance);
         }
      } catch (Exception e)
      {
         log.debug("failed", e);
         throw new BankException("Could not get account for "+customer, e);
      }
   }

   public Customer getCustomer(String name)
      throws BankException
   {
      try
      {
         // Check for existing customer
         CustomerHome home = (CustomerHome)new InitialContext().lookup(CustomerHome.COMP_NAME);
         Collection customers = home.findAll();

         Iterator i = customers.iterator();
         while(i.hasNext())
         {
            Customer cust = (Customer)i.next();
            if (cust.getName().equals(name))
               return cust;

         }

         // Create customer
         BankHome bankHome = (BankHome)new InitialContext().lookup(BankHome.COMP_NAME);
         Bank bank = bankHome.create();

         Customer cust = home.create(bank.createCustomerId(), name);
         log.debug("Customer created");
         return cust;
      } catch (Exception e)
      {
         log.debug("failed", e);
         throw new BankException("Could not get customer for "+name, e);
      }
   }

   public void transferTest(Account from, Account to, float amount, int iter)
      throws java.rmi.RemoteException, BankException
   {
      for (int i = 0; i < iter; i++)
      {
         from.withdraw(amount);
         to.deposit(amount);
      }
   }
}
