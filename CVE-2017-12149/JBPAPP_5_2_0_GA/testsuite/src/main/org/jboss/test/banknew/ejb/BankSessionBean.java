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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.test.banknew.interfaces.Bank;
import org.jboss.test.banknew.interfaces.BankData;
import org.jboss.test.banknew.interfaces.BankHome;
import org.jboss.test.banknew.interfaces.BankPK;
import org.jboss.test.banknew.interfaces.CustomerData;
import org.jboss.test.banknew.interfaces.CustomerSession;
import org.jboss.test.banknew.interfaces.CustomerSessionHome;
import org.jboss.test.util.ejb.SessionSupport;

/**
 * The Session bean represents a bank's business interface.
 *
 * @author Andreas Schaefer
 * @version $Revision: 81036 $
 *
 * @ejb:bean name="bank/BankSession"
 *           display-name="Bank Session"
 *           type="Stateless"
 *           view-type="remote"
 *           jndi-name="ejb/bank/BankSession"
 *
 * @ejb:interface extends="javax.ejb.EJBObject"
 *
 * @ejb:home extends="javax.ejb.EJBHome"
 *
 * @ejb:pk extends="java.lang.Object"
 *
 * @ejb:transaction type="Required"
 *
 * @ejb:ejb-ref ejb-name="bank/Bank"
 *
 * @ejb:ejb-ref ejb-name="bank/CustomerSession"
 */
public class BankSessionBean
   extends SessionSupport
{
   
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   
   /** The serialVersionUID */
   private static final long serialVersionUID = -4732008507323917820L;

   /**
    * @ejb:interface-method view-type="remote"
    **/
   public BankData createBank( String pName, String pAddress )
      throws CreateException, RemoteException
   {
      Bank lBank = getBankHome().create( pName, pAddress );
      return lBank.getData();
   }
   
   /**
    * @ejb:interface-method view-type="remote"
    **/
   public void removeBank( String pBankId )
      throws RemoveException, RemoteException
   {
      try {
         getBankHome().findByPrimaryKey(
            new BankPK( pBankId )
         ).remove();
      }
      catch( FinderException fe ) {
      }
   }
   
   /**
    * @ejb:interface-method view-type="remote"
    **/
   public Collection getBanks()
      throws FinderException, RemoteException
   {
      Collection lBanks = getBankHome().findAll();
      Collection lList = new ArrayList( lBanks.size() );
      Iterator i = lBanks.iterator();
      while( i.hasNext() ) {
         lList.add( ( (Bank) i.next() ).getData() );
      }
      return lList;
   }
   
   /**
    * @ejb:interface-method view-type="remote"
    **/
   public CustomerData getCustomer( String pCustomerId )
      throws FinderException, RemoteException
   {
      return getCustomerSession().getCustomer( pCustomerId );
   }
   
   /**
    * @ejb:interface-method view-type="remote"
    **/
   public Collection getCustomers( String pBankId )
      throws FinderException, RemoteException
   {
      return getCustomerSession().getCustomers( pBankId );
   }
   
   /**
    * @ejb:interface-method view-type="remote"
    **/
   public CustomerData createCustomer( String pBankId, String pName, float pInitialDeposit )
      throws CreateException, RemoteException
   {
      return getCustomerSession().createCustomer(
         pBankId,
         pName,
         pInitialDeposit
      );
   }
   
   /**
    * @ejb:interface-method view-type="remote"
    **/
   public void removeCustomer( String pCustomerId )
      throws RemoveException, RemoteException
   {
      getCustomerSession().removeCustomer(
         pCustomerId
      );
   }
   
   private BankHome getBankHome() {
      try {
         return (BankHome) new InitialContext().lookup( BankHome.COMP_NAME );
      }
      catch( NamingException ne ) {
         throw new EJBException( ne );
      }
   }
   
   private CustomerSession getCustomerSession()
      throws RemoteException
   {
      try {
         return ( (CustomerSessionHome) new InitialContext().lookup( CustomerSessionHome.COMP_NAME ) ).create();
      }
      catch( NamingException ne ) {
         throw new EJBException( ne );
      }
      catch( CreateException ce ) {
         throw new EJBException( ce );
      }
   }
   
   // SessionBean implementation ------------------------------------
   public void setSessionContext(SessionContext context) 
   {
      super.setSessionContext(context);
   }
}

/*
 *   $Id: BankSessionBean.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $
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
 *   Revision 1.2  2001/01/07 23:14:34  peter
 *   Trying to get JAAS to work within test suite.
 *
 *   Revision 1.1.1.1  2000/06/21 15:52:37  oberg
 *   Initial import of jBoss test. This module contains CTS tests, some simple examples, and small bean suites.
 *
 *
 *  
 */
