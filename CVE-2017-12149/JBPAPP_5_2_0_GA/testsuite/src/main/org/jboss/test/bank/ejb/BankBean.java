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

import java.rmi.*;
import javax.naming.*;
import javax.ejb.*;

import org.jboss.test.util.ejb.SessionSupport;
import org.jboss.test.bank.interfaces.*;

/**
 *      
 *   @see <related>
 *   @author $Author: dimitris@jboss.org $
 *   @version $Revision: 81036 $
 */
public class BankBean
   extends SessionSupport
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   static final String ID = "java:comp/env/id";
   String id;
   
   // Static --------------------------------------------------------
   static long nextAccountId = System.currentTimeMillis();
   static long nextCustomerId = System.currentTimeMillis();

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public String getId()
   {
      return id;
   }
   
   public String createAccountId(Customer customer)
      throws RemoteException
   {
      return getId()+"."+customer.getName()+"."+(nextAccountId++);
   }
   
   public String createCustomerId()
   {
      return getId()+"."+(nextCustomerId++);
   }
   
   // SessionBean implementation ------------------------------------
   public void setSessionContext(SessionContext context) 
   {
      super.setSessionContext(context);
      
      try
      {
         id = (String)new InitialContext().lookup(ID);
      } catch (Exception e)
      {
         log.debug(e);
         throw new EJBException(e);
      }
   }
}

/*
 *   $Id: BankBean.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $
 *   Currently locked by:$Locker$
 *   Revision:
 *   $Log$
 *   Revision 1.6  2005/10/29 23:41:15  starksm
 *   Update the jboss LGPL headers
 *
 *   Revision 1.5  2003/08/27 04:32:49  patriot1burke
 *   4.0 rollback to 3.2
 *
 *   Revision 1.3  2002/02/15 06:15:50  user57
 *    o replaced most System.out usage with Log4j.  should really introduce
 *      some base classes to make this mess more maintainable...
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
