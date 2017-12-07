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
package org.jboss.test.cmp2.jbas1361;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import org.jboss.test.JBossTestCase;
import org.jboss.test.util.ejb.EJBTestCase;
import junit.framework.Test;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 81036 $</tt>
 */
public class JBAS1361UnitTestCase
   extends EJBTestCase
{
   public static Test suite() throws Exception
   {
      return JBossTestCase.getDeploySetup(JBAS1361UnitTestCase.class, "cmp2-jbas1361.jar");
   }

   public JBAS1361UnitTestCase(String methodName)
   {
      super(methodName);
   }

   protected void setUp() throws Exception
   {
      // NOTE: setUp, tearDown and tests appear to run in THE SAME transaction!
      TransactionManager tm = getTM();
      Transaction oldTx = tm.getTransaction();
      if(oldTx != null)
      {
         tm.suspend();
      }
      tm.begin();

      try
      {
         //System.out.println("setUp: " + tm.getTransaction());
         ALocal a = getALocalHome().create(new Integer(1), "a");
         BLocalHome bh = getBLocalHome();
         for(int i = 1; i <= 3; ++i)
         {
            bh.create(new Integer(i), "b").setA(a);
         }

         tm.commit();
      }
      catch(Throwable t)
      {
         tm.rollback();
      }

      if(oldTx != null)
      {
         tm.resume(oldTx);
      }
   }

   protected void tearDown() throws Exception
   {
      //System.out.println("tearDown: " + (getTM()).getTransaction());
      getALocalHome().remove(new Integer(1));
      BLocalHome bh = getBLocalHome();
      for(int i = 1; i <= 3; ++i)
      {
         bh.remove(new BPK(new Integer(i), "b"));
      }
   }

   public void testJBAS1361() throws Throwable
   {
      TransactionManager tm = getTM();
      Transaction oldTx = tm.getTransaction();
      if(oldTx != null)
      {
         tm.suspend();
      }
      tm.begin();

      try
      {
         //System.out.println("test: " + (getTM()).getTransaction());

         ALocal a = getALocalHome().findByPrimaryKey(new Integer(1));

         a.getB().clear();

         BLocalHome bh = getBLocalHome();

         BPK bpk = new BPK();
         bpk.name = "b";
         for(int i = 1; i <= 3; ++i)
         {
            bpk.id = new Integer(i);
            BLocal b = bh.findByPrimaryKey(bpk);
            assertTrue(a.getB().add(b));
         }

         assertEquals(3, a.getB().size());

         tm.commit();
      }
      catch(Throwable t)
      {
         tm.rollback();
         throw t;
      }
      finally
      {
         if(oldTx != null)
         {
            tm.resume(oldTx);
         }
      }
   }

   private TransactionManager getTM()
      throws NamingException
   {
      return (TransactionManager)lookup("java:/TransactionManager");
   }

   private ALocalHome getALocalHome()
      throws NamingException
   {
      return (ALocalHome)lookup(ALocalHome.JNDI_NAME);
   }

   private BLocalHome getBLocalHome()
      throws NamingException
   {
      return (BLocalHome)lookup(BLocalHome.JNDI_NAME);
   }

   private Object lookup(String name) throws NamingException
   {
      InitialContext ic = new InitialContext();
      return ic.lookup(name);
   }
}
