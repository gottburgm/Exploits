/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.cluster.invokerha;

import java.rmi.ServerException;

import org.jboss.ha.framework.interfaces.GenericClusteringException;
import org.jboss.invocation.ServiceUnavailableException;
import org.jboss.logging.Logger;

import junit.framework.TestCase;

/**
 * InvokerHaFailureType.
 * 
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 */
public enum InvokerHaFailureType 
{
   BEFORE_SERVER,
   AFTER_SERVER_NOT_COMPLETED_BUT_SUCCESS_AFTER,
   AFTER_SERVER_NOT_COMPLETED_BOTH_SERVERS,
   AFTER_SERVER_COMPLETED;      

   private static final Logger log = Logger.getLogger(InvokerHaFailureType.class);
   
   public boolean isRecoverable(boolean injectFailureIn1stCall)
   {
      switch(this) 
      {
         case BEFORE_SERVER:
            /* a failure before even getting to the invocation call itself is 
             * never recoverable */
            return false;
            
         case AFTER_SERVER_NOT_COMPLETED_BUT_SUCCESS_AFTER:
            /* it is not recoverable because UserTransaction.begin() already 
             * reached a server.*/
            return false;
            
         case AFTER_SERVER_NOT_COMPLETED_BOTH_SERVERS:
            /* if failure happens after reaching the server but didn't complete when 
             * trying to call either server, neither in the 1st or Nth call is 
             * recoverable.*/
            return false;
            
         case AFTER_SERVER_COMPLETED:
            /* if failure happens after reaching the server and completed, neither 
             * 1st or Nth call are recoverable. */
            return false;
            
         default:
            return false;
      }
   }
   
   public void injectFailureIfExistsBeforeServer() throws IllegalStateException
   {
      switch(this) 
      {
         case BEFORE_SERVER:
            log.debug("failing because of " + this);
            throw new IllegalStateException("see how you handle this!!");
            
         default:
            break;
      }
   }
   
   public void injectFailureIfExistsAfterServer(Integer failoverCounter) throws GenericClusteringException
   {
      switch(this)
      {
         case AFTER_SERVER_NOT_COMPLETED_BUT_SUCCESS_AFTER:
            if (failoverCounter.equals(new Integer(0)))
            {
               log.debug("failing because of " + this);
               throw new GenericClusteringException(GenericClusteringException.COMPLETED_NO, this.toString());               
            }
            break;

         case AFTER_SERVER_NOT_COMPLETED_BOTH_SERVERS:
            log.debug("failing because of " + this);
            throw new GenericClusteringException(GenericClusteringException.COMPLETED_NO, this.toString());
            
         case AFTER_SERVER_COMPLETED:
            log.debug("failing because of " + this);
            throw new GenericClusteringException(GenericClusteringException.COMPLETED_YES, this.toString());
            
         default:
            break;
      }
   }

   public void assertFailoverCounter(boolean injectFailureIn1stCall, Object failoverCounter)
   {
      switch(this)
      {
         case BEFORE_SERVER:
            TestCase.assertNull(failoverCounter);
            break;
            
         case AFTER_SERVER_NOT_COMPLETED_BUT_SUCCESS_AFTER:
            TestCase.assertEquals(Integer.valueOf(1), failoverCounter);
            break;
            
         case AFTER_SERVER_NOT_COMPLETED_BOTH_SERVERS:
            /* Only one attempted failover because it won't be available to 
             * failover due to UserTransaction.begin() having already hit the 
             * server and hence, txFailoverAuthorization maps contains that tpc. */ 
            TestCase.assertEquals(1, failoverCounter);
            break;
            
         case AFTER_SERVER_COMPLETED:
            /* failover counters are always 0 because there's no chance of 
             * calculating failover at all */
            TestCase.assertEquals(Integer.valueOf(0), failoverCounter);
            break;
            
         default:
            break;
      }
   }
   
   public void assertException(Throwable t)
   {
      switch(this) 
      {
         case BEFORE_SERVER:
            TestCase.assertTrue(t instanceof IllegalStateException);
            break;
            
         case AFTER_SERVER_NOT_COMPLETED_BUT_SUCCESS_AFTER:
         case AFTER_SERVER_NOT_COMPLETED_BOTH_SERVERS:
            TestCase.assertTrue(t instanceof ServiceUnavailableException);
            TestCase.assertTrue(t.getCause() instanceof GenericClusteringException);
            break;
            
         case AFTER_SERVER_COMPLETED:
            TestCase.assertTrue(t instanceof ServerException);
            TestCase.assertTrue(t.getCause() instanceof GenericClusteringException);
            break;
      }
   }
}
