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
package org.jboss.test.jbossts.recovery;

import java.util.HashSet;
import java.util.Set;

import javax.ejb.EJBException;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.logging.Logger;
import org.jboss.test.jbossts.recovery.RecoveredXid;


/**
 * Helper class for playing with in-doubt txs.
 * 
 * @author <a href="istudens@redhat.com">Ivo Studensky</a>
 * @version $Revision: 1.1 $
 */
public abstract class CommonCrashHelper implements CrashHelperRem
{
   protected static Logger log = Logger.getLogger(CommonCrashHelper.class);


   /**
    * Wipes out in-doubt xids according to <code>xidToRecover</code> list. 
    * If xidToRecover is null, it wipes out all in-doubt xids. 
    * The in-doubt xids are taken from {@link #getNewXAResource()}. 
    * 
    * @param xidToRecover list of Xids to recover
    */
   @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
   public boolean wipeOutTxsInDoubt(Set<RecoveredXid> xidsToRecover)
   {
      log.info("wipe out in-doubt txs");

      XAResource xares = null;
      try
      {
         xares = getNewXAResource();
         
         if (xares == null)
            return false;

         Xid[] txInDoubt = null;
         try
         {               
            txInDoubt = xares.recover(XAResource.TMSTARTRSCAN);
         }
         catch (XAException e)
         {
            log.error("Cannot start recovering on xares", e);

            try
            {
               xares.recover(XAResource.TMENDRSCAN);
            }
            catch (Exception e1)
            {
            }

            return false;
         }

         if (txInDoubt == null || txInDoubt.length == 0)
            return true;

         log.info("There are " + txInDoubt.length + " xids in doubt");

         for (int k=0; k < txInDoubt.length; k++)
         {
            RecoveredXid xid = convertToRecoveredXid(txInDoubt[k]);
            if (xidsToRecover == null || xidsToRecover.contains(xid))
            {
               try
               {
                  log.info("rollbacking of Xid " + xid);
                  xares.rollback(txInDoubt[k]);
               }
               catch (Exception e)
               {
                  log.error("Error in rollback of Xid " + txInDoubt[k], e);
               }
            }
         }

         try
         {
            if (xares != null)
               xares.recover(XAResource.TMENDRSCAN);
         }
         catch (XAException e)
         {
            log.error("Cannot finish recovering on xares", e);
         }
      }
      catch (Exception e)
      {
         log.error("Cannot get a new XA resource", e);
         throw new EJBException("Cannot get a new XA resource: " + e.getMessage());
      }
      finally
      {
         closeXAResource();
      }
      return true;
   }

   /**
    * Checks in-doubt xids by XAResource taken from {@link #getNewXAResource()}. 
    * 
    * @return in-doubt xids
    */
   @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
   public Set<RecoveredXid> checkXidsInDoubt()
   {
      Set<RecoveredXid> xids = new HashSet<RecoveredXid>();

      XAResource xares = null;
      try
      {               
         xares = getNewXAResource();

         try
         {
            Xid[] xidsInDoubt = xares.recover(XAResource.TMSTARTRSCAN);

            if (xidsInDoubt != null)
            {
               log.info("There are " + xidsInDoubt.length + " xids in doubt");

               for (int k=0; k < xidsInDoubt.length; k++)
                  xids.add(convertToRecoveredXid(xidsInDoubt[k]));
            }
         }
         catch (XAException e)
         {
            log.error("Cannot start recover scan on xares", e);
            throw new EJBException("Cannot start recover scan on xares: " + e.getMessage());
         }
         finally
         {
            try
            {
               if (xares != null)
                  xares.recover(XAResource.TMENDRSCAN);
            }
            catch (XAException e)
            {
               log.error("Cannot finish recover scan on xares", e);
            }
         }
      }
      catch (Exception e)
      {
         log.error("Cannot get a new XA resource", e);
         throw new EJBException("Cannot get a new XA resource: " + e.getMessage());
      }
      finally
      {
         closeXAResource();
      }

      return xids;
   }

   private RecoveredXid convertToRecoveredXid(Xid xid)
   {
      RecoveredXid recoveredXid = new RecoveredXid();

      recoveredXid.setBranchQualifier(xid.getBranchQualifier());
      recoveredXid.setFormatId(xid.getFormatId());
      recoveredXid.setGlobalTransactionId(xid.getGlobalTransactionId());

      return recoveredXid;
   }

   
   protected abstract XAResource getNewXAResource() throws Exception;

   // Should not throw out any exception.
   protected abstract void closeXAResource();

}
