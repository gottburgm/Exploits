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
package org.jboss.test.banknew.ejb;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.test.banknew.interfaces.AccountData;
import org.jboss.test.banknew.interfaces.AccountSession;
import org.jboss.test.banknew.interfaces.AccountSessionHome;
import org.jboss.test.banknew.interfaces.BankException;
import org.jboss.test.banknew.interfaces.CustomerData;
import org.jboss.test.banknew.interfaces.CustomerSession;
import org.jboss.test.banknew.interfaces.CustomerSessionHome;
import org.jboss.test.util.ejb.SessionSupport;

/**
 * The Session bean represents a bank.
 *
 * @author Andreas Schaefer
 * @version $Revision: 81036 $
 *
 * @ejb:bean name="bank/TellerSession"
 *           display-name="Teller Session"
 *           type="Stateless"
 *           view-type="remote"
 *           jndi-name="ejb/bank/TellerSession"
 *
 * @ejb:interface extends="javax.ejb.EJBObject"
 *
 * @ejb:home extends="javax.ejb.EJBHome"
 *
 * @ejb:transaction type="Required"
 *
 * @ejb:ejb-ref ejb-name="bank/AccountSession"
 *
 * @ejb:ejb-ref ejb-name="bank/BankSession"
 *
 * @ejb:ejb-ref ejb-name="bank/CustomerSession"
 */
public class TellerSessionBean
   extends SessionSupport
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -1977212418836289874L;

   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   
   /**
    * @ejb:interface-method view-type="remote"
    **/
   public void deposit( String pToAccountId, float pAmount )
      throws BankException
   {
      try {
         getAccountSession().deposit( pToAccountId, pAmount );
      }
      catch( Exception e ) {
         sessionCtx.setRollbackOnly();
         throw new BankException( "Could not deposit " + pAmount +
            " to " + pToAccountId, e );
      }
   }
   
   /**
    * @ejb:interface-method view-type="remote"
    **/
   public void transfer( String pFromAccountId, String pToAccountId, float pAmount )
      throws BankException
   {
      try {
         getAccountSession().transfer( pFromAccountId, pToAccountId, pAmount );
      }
      catch( Exception e ) {
         sessionCtx.setRollbackOnly();
         throw new BankException( "Could not transfer " + pAmount +
            " from " + pFromAccountId + " to " + pToAccountId, e );
      }
   }
   
   /**
    * @ejb:interface-method view-type="remote"
    **/
   public void withdraw( String pFromAccountId, float pAmount )
      throws BankException
   {
      try {
         getAccountSession().withdraw( pFromAccountId, pAmount );
      }
      catch( Exception e ) {
         sessionCtx.setRollbackOnly();
         throw new BankException( "Could not withdraw " + pAmount +
            " from " + pFromAccountId, e );
      }
   }
   
   /**
    * @ejb:interface-method view-type="remote"
    **/
   public AccountData createAccount( String pCustomerId, int pType, float pInitialDeposit )
      throws BankException
   {
      try {
         return getAccountSession().createAccount( pCustomerId, pType, pInitialDeposit );
      }
      catch( Exception e ) {
         sessionCtx.setRollbackOnly();
         e.printStackTrace();
         throw new BankException( "Could not create account", e );
      }
   }
   
   /**
    * @ejb:interface-method view-type="remote"
    **/
   public void removeAccount( String pAccountId )
      throws BankException
   {
      try {
         getAccountSession().removeAccount( pAccountId );
      }
      catch( Exception e ) {
         sessionCtx.setRollbackOnly();
         e.printStackTrace();
         throw new BankException( "Could not remove account", e );
      }
   }
   
   /**
    * @ejb:interface-method view-type="remote"
    **/
   public void removeAccount( String pCustomerId, int pType )
      throws BankException
   {
      try {
         getAccountSession().removeAccount( pCustomerId, pType );
      }
      catch( Exception e ) {
         sessionCtx.setRollbackOnly();
         e.printStackTrace();
         throw new BankException( "Could not remove account", e );
      }
   }
   
   /**
    * @ejb:interface-method view-type="remote"
    **/
   public Collection getAccounts( String pCustomerId )
      throws BankException
   {
      try {
         return getAccountSession().getAccounts( pCustomerId );
      }
      catch( Exception e ) {
         sessionCtx.setRollbackOnly();
         e.printStackTrace();
         throw new BankException( "Could not remove account", e );
      }
   }
   
   /**
    * @ejb:interface-method view-type="remote"
    **/
   public AccountData getAccount( String pCustomerId, int pType )
      throws BankException
   {
      try {
         return getAccountSession().getAccount( pCustomerId, pType );
      }
      catch( Exception e ) {
         sessionCtx.setRollbackOnly();
         e.printStackTrace();
         throw new BankException( "Could not remove account", e );
      }
   }
   
   /**
    * @ejb:interface-method view-type="remote"
    **/
   public AccountData getAccount( String pAccountId )
      throws BankException
   {
      try {
         return getAccountSession().getAccount( pAccountId );
      }
      catch( Exception e ) {
         sessionCtx.setRollbackOnly();
         e.printStackTrace();
         throw new BankException( "Could not remove account", e );
      }
   }
   
   /**
    * @ejb:interface-method view-type="remote"
    **/
   public CustomerData createCustomer( String pBankId, String pName, float pInitialDeposit )
      throws BankException
   {
      try {
         return getCustomerSession().createCustomer( pBankId, pName, pInitialDeposit );
      }
      catch( Exception e ) {
         sessionCtx.setRollbackOnly();
         e.printStackTrace();
         throw new BankException( "Could not create account", e );
      }
   }
   
   /**
    * @ejb:interface-method view-type="remote"
    **/
   public void removeCustomer( String pCustomerId )
      throws BankException
   {
      try {
         getCustomerSession().removeCustomer( pCustomerId );
      }
      catch( Exception e ) {
         sessionCtx.setRollbackOnly();
         e.printStackTrace();
         throw new BankException( "Could not remove account", e );
      }
   }
   
   /**
    * @ejb:interface-method view-type="remote"
    **/
   public CustomerData getCustomer( String pCustomerId )
      throws BankException
   {
      try {
         CustomerSession lSession = getCustomerSession();
         return lSession.getCustomer( pCustomerId );
      }
      catch( Exception e ) {
         e.printStackTrace();
         throw new BankException( "Could not get customer for " + pCustomerId, e );
      }
   }
   
   /**
    * @ejb:interface-method view-type="remote"
    **/
   public Collection getCustomers( String pBankId )
      throws BankException
   {
      try {
         return getCustomerSession().getCustomers( pBankId );
      }
      catch( Exception e ) {
         e.printStackTrace();
         throw new BankException( "Could not get customers for bank " + pBankId, e );
      }
   }
   
   private AccountSession getAccountSession()
      throws RemoteException
   {
      try {
         AccountSessionHome lHome = (AccountSessionHome) new InitialContext().lookup( AccountSessionHome.COMP_NAME );
         return lHome.create();
      }
      catch( NamingException ne ) {
         throw new EJBException( ne );
      }
      catch( CreateException ce ) {
         throw new EJBException( ce );
      }
   }
   
   private CustomerSession getCustomerSession()
      throws RemoteException
   {
      try {
         CustomerSessionHome lHome = (CustomerSessionHome) new InitialContext().lookup( CustomerSessionHome.COMP_NAME );
         return lHome.create();
      }
      catch( NamingException ne ) {
         throw new EJBException( ne );
      }
      catch( CreateException ce ) {
         throw new EJBException( ce );
      }
   }
   
}
/*
 *   $Id: TellerSessionBean.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $
 *   Currently locked by:$Locker$
 *   Revision:
 *   $Log$
 *   Revision 1.3  2006/03/01 16:09:58  adrian
 *   Remove xdoclet from jca tests
 *
 *   Revision 1.1.16.1  2005/10/29 05:04:35  starksm
 *   Update the LGPL header
 *
 *   Revision 1.1  2002/05/04 01:08:25  schaefera
 *   Added new Stats classes (JMS related) to JSR-77 implemenation and added the
 *   bank-new test application but this does not work right now properly but
 *   it is not added to the default tests so I shouldn't bother someone.
 *
 *   Revision 1.1.2.2  2002/04/29 21:05:17  schaefera
 *   Added new marathon test suite using the new bank application
 *
 *   Revision 1.1.2.1  2002/04/17 05:07:24  schaefera
 *   Redesigned the banknew example therefore to a create separation between
 *   the Entity Bean (CMP) and the Session Beans (Business Logic).
 *   The test cases are redesigned but not finished yet.
 *
 *   Revision 1.1.2.2  2002/04/15 04:28:15  schaefera
 *   Minor fixes regarding to the JNDI names of the beans.
 *
 *   Revision 1.1.2.1  2002/04/15 02:32:24  schaefera
 *   Add a new test version of the bank because the old did no use transactions
 *   and the new uses XDoclet 1.1.2 to generate the DDs and other Java classes.
 *   Also a marathon test is added. Please specify the jbosstest.duration for
 *   how long and the test.timeout (which must be longer than the duration) to
 *   run the test with run_tests.xml, tag marathon-test-and-report.
 *
 *   Revision 1.3  2001/01/07 23:14:34  peter
 *   Trying to get JAAS to work within test suite.
 *
 *   Revision 1.2  2000/09/30 01:00:55  fleury
 *   Updated bank tests to work with new jBoss version
 *
 *   Revision 1.1.1.1  2000/06/21 15:52:37  oberg
 *   Initial import of jBoss test. This module contains CTS tests, some simple examples, and small bean suites.
 */
