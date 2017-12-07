/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.jbossts.jms;

import java.util.Set;

import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.jms.XASession;
import javax.naming.InitialContext;
import javax.transaction.xa.XAResource;

import org.jboss.test.jbossts.recovery.CommonCrashHelper;
import org.jboss.test.jbossts.recovery.RecoveredXid;


/**
 * Helper class for playing with in-doubt txs on JMS.
 * 
 * @author <a href="istudens@redhat.com">Ivo Studensky</a>
 * @version $Revision: 1.1 $
 */
@Stateless
public class JMSCrashHelper extends CommonCrashHelper implements JMSCrashHelperRem
{
   public static final String REMOTE_JNDI_NAME = JMSCrashHelper.class.getSimpleName() + "/remote";

   private XAConnection xaConnection = null;
   private String connectionFactoryJNDIName = "java:/XAConnectionFactory";

   @Override
   public boolean wipeOutTxsInDoubt(String connectionFactoryJNDIName, Set<RecoveredXid> xidsToRecover)
   {
      this.connectionFactoryJNDIName = connectionFactoryJNDIName;
      return super.wipeOutTxsInDoubt(xidsToRecover);
   }

   @Override
   public Set<RecoveredXid> checkXidsInDoubt(String connectionFactoryJNDIName)
   {
      this.connectionFactoryJNDIName = connectionFactoryJNDIName;
      return super.checkXidsInDoubt();
   }
   

   @Override
   protected XAResource getNewXAResource() throws Exception
   {
      try
      {
         if (xaConnection == null)
         {
            InitialContext ic = new InitialContext();
            XAConnectionFactory xacf = (XAConnectionFactory) ic.lookup(connectionFactoryJNDIName);

            xaConnection = xacf.createXAConnection();
         }

         XASession session = xaConnection.createXASession();

         return session.getXAResource();
      }
      catch (Exception e)
      {
         log.warn("Cannot create new XA resource", e);
         throw e;
      }
   }

   // Should not throw out any exception.
   @Override
   protected void closeXAResource()
   {
      if (xaConnection != null)
      {
         try
         {
            xaConnection.close();
         }
         catch (Exception e)
         {
            log.warn("Cannot close jms xa connection", e);
         }
         xaConnection = null;
      }      
   }

}
