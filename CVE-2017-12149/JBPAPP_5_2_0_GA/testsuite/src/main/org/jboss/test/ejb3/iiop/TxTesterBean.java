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
package org.jboss.test.ejb3.iiop;

import javax.ejb.EJBException;
import javax.ejb.RemoteHome;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.ejb3.annotation.JndiInject;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: 97163 $
 */
@Stateless
//@Remote(TxTester.class) // EJB 3.0 4.6.6, no longer allowed
@RemoteHome(TxTesterHome.class)
//@RemoteBinding(factory=RemoteBindingDefaults.PROXY_FACTORY_IMPLEMENTATION_IOR) // JBMETA-117
@TransactionManagement(TransactionManagementType.CONTAINER)
public class TxTesterBean
{
   private static final Logger log = Logger.getLogger(TxTesterBean.class);
   
   @JndiInject(jndiName="java:/TransactionManager") TransactionManager tm;
   
   @TransactionAttribute(TransactionAttributeType.MANDATORY)
   public void txMandatoryMethod()
   {
      try
      {
         log.info("currentThread = " + Thread.currentThread());
         log.info("currentTransaction = " + tm.getTransaction());
         log.info("tm = " + tm);
         
         Transaction tx = tm.getTransaction();
         if(tx == null)
            throw new EJBException("no tx");
         
         if(tx.getStatus() != Status.STATUS_ACTIVE)
            throw new EJBException("tx not active");
      }
      catch(SystemException e)
      {
         throw new EJBException(e);
      }
   }
}
