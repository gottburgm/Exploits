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
package org.jboss.test.cluster.ejb3.ustxsticky;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.transaction.Transaction;
import javax.transaction.UserTransaction;

import org.jboss.aop.DispatcherConnectException;
import org.jboss.ejb3.annotation.Clustered;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ha.framework.interfaces.GenericClusteringException;
import org.jboss.logging.Logger;
import org.jboss.remoting.CannotConnectException;
import org.jboss.tm.TransactionPropagationContextImporter;
import org.jboss.tm.TransactionPropagationContextFactory;
import org.jboss.tm.TransactionPropagationContextUtil;

/**
 * UserTransactionStickyBean.
 * 
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 */
@Stateless(mappedName = "ejb3/UserTransactionSticky")
@Remote(UserTransactionSticky.class)
@Clustered(loadBalancePolicy="org.jboss.ha.client.loadbalance.RoundRobin")
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class UserTransactionStickyBean implements UserTransactionSticky
{
   private static Logger log = Logger.getLogger(UserTransactionStickyBean.class);   

   /** The serialVersionUID */
   private static final long serialVersionUID = 3926319963458764127L;
   
   @Override
   public void test(int status) throws GenericClusteringException
   {
      if (status > 0)
      {
         throw new GenericClusteringException(status);
      }
   }

   @AroundInvoke
   public Object intercept(InvocationContext ctx) throws Exception
   {
      javax.transaction.Transaction tx = org.jboss.ejb3.tx.TxUtil.getTransactionManager().getTransaction();
      System.out.println("Transaction is " + ((tx != null) ? "not " : "") + "null");
         
      Object tpc = TransactionPropagationContextUtil.getTPCFactoryClientSide().getTransactionPropagationContext();
      System.out.println("TPC = " + tpc);

      log.debug("Tpc " + tpc + " is associated with tx " + tx);
      
      /* If a tpc is retrieved on the server side but matches no transaction 
       * there, then it means that invocation was not sticky. An invocation 
       * containing a tpc must, under transaction sticky rules, hit the server
       * where the tpc was generated from and where the transaction is running.
       */
      if (tx == null && tpc != null)
      {
         throw new IllegalStateException("Tpc " + tpc + " does not match a transaction on this node, invocation not sticky!");
      }
      
      return ctx.proceed();
   }
}
