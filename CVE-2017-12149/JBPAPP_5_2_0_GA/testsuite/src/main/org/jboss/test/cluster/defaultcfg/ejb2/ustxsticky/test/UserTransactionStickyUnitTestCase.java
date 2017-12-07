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
package org.jboss.test.cluster.defaultcfg.ejb2.ustxsticky.test;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import junit.framework.Test;

import org.jboss.logging.Logger;
import org.jboss.test.JBossClusteredTestCase;
import org.jboss.test.cluster.ejb2.ustxsticky.UserTransactionStickyHome;
import org.jboss.test.cluster.ejb2.ustxsticky.UserTransactionStickyRemote;

/**
 * UserTransactionStickyTestCase.
 * 
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 */
public class UserTransactionStickyUnitTestCase extends JBossClusteredTestCase
{   
   private static final String deployment = "ustxsticky.jar";

   private static final Logger log = Logger.getLogger(UserTransactionStickyUnitTestCase.class);   

   private Context ctx;   
   
   public UserTransactionStickyUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(UserTransactionStickyUnitTestCase.class, deployment);
   }
   
   public void testSeveralTransactionalStickyCalls() throws Exception
   {
      severalTransactionalCalls(3, 3, "ejb/UserTransactionStickyEjb");
   }

   private void severalTransactionalCalls(int numTxs, int numCallsPerTx, String jndiName) throws Exception
   {
      UserTransaction tx;

      // Connect to the server0 JNDI
      String[] urls = getNamingURLs();
      Properties env1 = new Properties();
      env1.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.NamingContextFactory");
      env1.setProperty(Context.PROVIDER_URL, urls[0]);
      ctx = new InitialContext(env1);
      
      // ctx = createDefaultPartitionContext();
      
      for (int i = 1; i <= numTxs; i++)
      {         
         tx = (UserTransaction)ctx.lookup("UserTransaction");
         tx.begin();
         try
         {
            UserTransactionStickyHome home = (UserTransactionStickyHome)ctx.lookup(jndiName);
            UserTransactionStickyRemote bean = home.create();
            for (int j = 1; j <= numCallsPerTx; j++)
            {
               String origin = "tx" + i + "-c" + j;            
               log(origin + " " + bean.amISticky());
            }
            log("");
         }
         catch (Exception e)
         {
            tx.setRollbackOnly(); // Force a rollback for this error
            throw e;
         }
         finally
         {
            if (tx.getStatus() == Status.STATUS_ACTIVE)
               tx.commit();
            else
               tx.rollback();
         }
      }      
   }

//   private Properties getDefaultProperties()
//   {
//      Properties p = new Properties();
//      p.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
//      p.put(Context.URL_PKG_PREFIXES, "jboss.naming:org.jnp.interfaces");
//      return p;
//   }   
//   
//   private Context createDefaultPartitionContext() throws Exception
//   {
//      Properties p = getDefaultProperties();
//      p.put(NamingContext.JNP_PARTITION_NAME, "DefaultPartition"); // partition name.
//      return new InitialContext(p);
//   }
   
   private static void log(Object message)
   {
      log.info(message);
   }
}
