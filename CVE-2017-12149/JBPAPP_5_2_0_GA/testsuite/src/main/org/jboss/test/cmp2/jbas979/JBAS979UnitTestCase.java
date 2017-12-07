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
package org.jboss.test.cmp2.jbas979;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionRolledbackException;
import javax.ejb.EJBException;
import org.jboss.test.JBossTestCase;
import org.jboss.test.util.ejb.EJBTestCase;
import junit.framework.Test;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 81036 $</tt>
 */
public class JBAS979UnitTestCase
   extends EJBTestCase
{
   private static final String STORE_NOT_FLUSHED_FALSE = "AStoreNotFlushedFalse";
   private static final String STORE_NOT_FLUSHED_TRUE = "AStoreNotFlushedTrue";
   private static final Integer PK = new Integer(1);

   public static boolean PASSIVATED_IN_AFTER_COMPLETION;
   public static Exception ERROR_IN_EJB_PASSIVATE;

   public static Test suite() throws Exception
   {
      return JBossTestCase.getDeploySetup(JBAS979UnitTestCase.class, "cmp2-jbas979.jar");
   }

   public JBAS979UnitTestCase(String methodName)
   {
      super(methodName);
   }

   protected void setUp() throws Exception
   {
   }

   protected void tearDown() throws Exception
   {
   }

   // tests

   public void testPassivateAfterCommit_storeNotFlushedTrue() throws Exception
   {
      passivateAfterCommit(STORE_NOT_FLUSHED_TRUE);
   }

   public void testPassivateAfterCommit_storeNotFlushedFalse() throws Exception
   {
      passivateAfterCommit(STORE_NOT_FLUSHED_FALSE);
   }

   public void testUpdateAfterFlush_storeNotFlushedTrue() throws Exception
   {
      String jndiName = STORE_NOT_FLUSHED_TRUE;
      Facade facade = getFacadeHome().create();
      facade.create(jndiName, PK, "name1");
      try
      {
         assertEquals("name2", facade.getNameFlushCacheSetName(jndiName, PK, "name2"));
      }
      finally
      {
         facade.remove(jndiName, PK);
      }
   }

   public void testUpdateAfterFlush_storeNotFlushedFalse() throws Exception
   {
      String jndiName = STORE_NOT_FLUSHED_FALSE;
      Facade facade = getFacadeHome().create();
      facade.create(jndiName, PK, "name1");
      try
      {
         facade.getNameFlushCacheSetName(jndiName, PK, "name2");
         fail("Flushed modified instances are not stored.");
      }
      catch(TransactionRolledbackException expected)
      {
      }
      finally
      {
         facade.remove(jndiName, PK);
      }
   }

   public void testAgeOutDoesntSchedulePassivationAfterCommit_storeNotFlushedTrue() throws Exception
   {
      ageOutDoesntSchedulePassivationAfterCommit(STORE_NOT_FLUSHED_TRUE);
   }

   public void testAgeOutDoesntSchedulePassivationAfterCommit_storeNotFlushedFalse() throws Exception
   {
      ageOutDoesntSchedulePassivationAfterCommit(STORE_NOT_FLUSHED_FALSE);
   }

   // Private

   private void ageOutDoesntSchedulePassivationAfterCommit(String jndiName)
      throws Exception
   {
      Facade facade = getFacadeHome().create();
      facade.create(jndiName, PK, "name1");
      try
      {
         facade.longTx(jndiName, PK, 5000);
         if(ERROR_IN_EJB_PASSIVATE != null)
         {
            throw new EJBException("Error in ejbPassivate", ERROR_IN_EJB_PASSIVATE);
         }

         if(PASSIVATED_IN_AFTER_COMPLETION)
         {
            fail("Natural aging out doesn't schedule passivation when transaction ends.");
         }
      }
      finally
      {
         facade.remove(jndiName, PK);
      }
   }

   private void passivateAfterCommit(String jndiName)
      throws Exception
   {
      Facade facade = getFacadeHome().create();
      facade.create(jndiName, PK, "name1");
      try
      {
         String name1 = facade.getName(jndiName, PK);
         facade.updateDB(jndiName, PK, "name2");
         // commit option A
         assertEquals(name1, facade.getNameFlushCacheGetName(jndiName, PK));
         assertEquals("name2", facade.getName(jndiName, PK));
      }
      finally
      {
         facade.remove(jndiName, PK);
      }
   }

   private FacadeHome getFacadeHome()
      throws NamingException
   {
      return (FacadeHome)lookup("Facade");
   }

   private Object lookup(String name) throws NamingException
   {
      InitialContext ic = null;
      try
      {
         ic = new InitialContext();
         return ic.lookup(name);
      }
      finally
      {
         if(ic != null)
         {
            ic.close();
         }
      }
   }
}
