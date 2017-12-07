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
package org.jboss.test.jbossts.crash;

import java.sql.SQLException;
import java.util.Set;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.transaction.xa.XAResource;

import org.jboss.test.jbossts.recovery.CommonCrashHelper;
import org.jboss.test.jbossts.recovery.RecoveredXid;


/**
 * Helper class for playing with txs in doubt.
 * 
 * @author <a href="istudens@redhat.com">Ivo Studensky</a>
 * @version $Revision: 1.1 $
 */
@Stateless
public class CrashHelper extends CommonCrashHelper implements JPACrashHelperRem
{
   public static final String REMOTE_JNDI_NAME = CrashHelper.class.getSimpleName() + "/remote";
   
   /**
    * Default JNDI name of recovery datasource.  
    */
   public static final String DS_JNDI_NAME          = "CrashRecoveryDS";
   public static final String DS_USER_NAME          = "crashrec";
   public static final String DS_PASSWORD           = "crashrec";

   private String datasourceName = DS_JNDI_NAME;

   
   @Override
   @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
   public Set<RecoveredXid> checkXidsInDoubt(String datasourceName)
   {
      setDatasourceName(datasourceName);
      return super.checkXidsInDoubt();
   }

   @Override
   @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
   public boolean wipeOutTxsInDoubt(String datasourceName, Set<RecoveredXid> xidsToRecover)
   {
      setDatasourceName(datasourceName);
      return super.wipeOutTxsInDoubt(xidsToRecover);
   }

   private void setDatasourceName(String datasourceName)
   {
      if (datasourceName != null && datasourceName.length() > 0)
         this.datasourceName = datasourceName;
   }
   
   /**
    * Gets XAResource for a datasource with name ({@link #DS_JNDI_NAME}).
    */
   @Override
   protected XAResource getNewXAResource() throws Exception
   {
      XAResource xares = null;
      try
      {
         AppServerJDBCXARecovery appServerRecovery = new AppServerJDBCXARecovery();

         if (appServerRecovery.initialise("jndiname=" + datasourceName))
         {
            if (appServerRecovery.hasMoreResources())
            {
               xares = appServerRecovery.getXAResource();
            }
         }
      }
      catch (SQLException e)
      {
         log.error("Cannot get any XAResource by AppServerJDBCXARecovery", e);
         throw e;
      }
      
      return xares;
   }

   @Override
   protected void closeXAResource()
   {
      // Nothing to do here. Is it alright?
   }

}
