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

import javax.ejb.CreateException;

import org.jboss.test.banknew.interfaces.BankData;
import org.jboss.test.banknew.interfaces.BankPK;
import org.jboss.test.util.ejb.EntitySupport;

/**
 * The Session bean represents a bank.
 *
 * @author Andreas Schaefer
 * @version $Revision: 81036 $
 *
 * @ejb:bean name="bank/Bank"
 *           display-name="Bank Entity"
 *           type="CMP"
 *           view-type="remote"
 *           jndi-name="ejb/bank/Bank"
 *           schema="Bank"
 *
 * @ejb:interface extends="javax.ejb.EJBObject"
 *
 * @ejb:home extends="javax.ejb.EJBHome"
 *
 * @ejb:pk extends="java.lang.Object"
 *
 * @ejb:data-object extends="java.lang.Object"
 *                  generate="true"
 *
 * @ejb:finder signature="java.util.Collection findAll()"
 *             query="SELECT OBJECT(o) FROM Bank AS o"
 *
 * @ejb:transaction type="Required"
 *
 * @jboss:table-name table-name="New_Bank"
 *
 * @jboss:create-table create="true"
 *
 * @jboss:remove-table remove="true"
 */
public abstract class BankBean
   extends EntitySupport
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   
   /**
    * @ejb:persistent-field
    * @ejb:pk-field
    *
    * @jboss:column-name name="Id"
    **/
   public abstract String getId();
   
   public abstract void setId( String pId );
   
   /**
    * @ejb:persistent-field
    *
    * @jboss:column-name name="Name"
    **/
   public abstract String getName();
   
   public abstract void setName( String pName );
   
   /**
    * @ejb:persistent-field
    *
    * @jboss:column-name name="Address"
    **/
   public abstract String getAddress();
   
   public abstract void setAddress( String pAddress );
   
   /**
    * @ejb:interface-method view-type="remote"
    **/
   public abstract void setData( BankData pData );
   
   /**
    * @ejb:interface-method view-type="remote"
    **/
   public abstract BankData getData();
   
   // EntityBean implementation -------------------------------------
   
   /**
    * @ejb:create-method view-type="remote"
    **/
   public BankPK ejbCreate( String pName, String pAddress ) 
      throws CreateException
   { 
      setId( "Bank ( " + System.currentTimeMillis() + " )" );
      setName( pName );
      setAddress( pAddress );
      
      return null;
   }
   
   public void ejbPostCreate( String pName, String pAddress ) 
      throws CreateException
   { 
   }
}

/*
 *   $Id: BankBean.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $
 *   Currently locked by:$Locker$
 *   Revision:
 *   $Log$
 *   Revision 1.4  2006/03/01 16:09:58  adrian
 *   Remove xdoclet from jca tests
 *
 *   Revision 1.2.16.1  2005/10/29 05:04:35  starksm
 *   Update the LGPL header
 *
 *   Revision 1.2  2002/05/06 00:07:37  danch
 *   Added ejbql query specs, schema names
 *
 *   Revision 1.1  2002/05/04 01:08:25  schaefera
 *   Added new Stats classes (JMS related) to JSR-77 implemenation and added the
 *   bank-new test application but this does not work right now properly but
 *   it is not added to the default tests so I shouldn't bother someone.
 *
 *   Revision 1.1.2.5  2002/04/30 01:21:23  schaefera
 *   Added some fixes to the marathon test and a windows script.
 *
 *   Revision 1.1.2.4  2002/04/29 21:05:17  schaefera
 *   Added new marathon test suite using the new bank application
 *
 *   Revision 1.1.2.3  2002/04/17 05:07:24  schaefera
 *   Redesigned the banknew example therefore to a create separation between
 *   the Entity Bean (CMP) and the Session Beans (Business Logic).
 *   The test cases are redesigned but not finished yet.
 *
 */
