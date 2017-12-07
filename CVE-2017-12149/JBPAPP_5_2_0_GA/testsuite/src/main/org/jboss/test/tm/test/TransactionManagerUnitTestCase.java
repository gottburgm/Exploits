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
package org.jboss.test.tm.test;

import javax.management.ObjectName;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.tm.resource.Operation;
import org.jboss.test.tm.resource.Resource;

/**
 * Tests for the transaction manager
 * @author Adrian@jboss.org
 * @version $Revision: 81036 $
 */
public class TransactionManagerUnitTestCase
   extends JBossTestCase
{
   static String[] SIG = new String[] { String.class.getName(), new Operation[0].getClass().getName() };

   ObjectName tmMBean;

   public TransactionManagerUnitTestCase(String name)
   {
      super(name);

      try
      {
         tmMBean = new ObjectName("jboss.test:test=TransactionManagerUnitTestCase");
      }
      catch (Exception e)
      {
         throw new RuntimeException(e.toString());
      }
   }

   public void runTest(Operation[] ops) throws Exception
   {
      getServer().invoke(tmMBean, "testOperations", new Object[] { getName(), ops }, SIG);
   }

   public static Test suite() throws Exception
   {
      return new JBossTestSetup(getDeploySetup(TransactionManagerUnitTestCase.class, "tmtest.sar"));
   }

   public void testNoResourcesCommit() throws Exception
   {
      runTest(new Operation[]
      {
         new Operation(Operation.BEGIN, 1),
         new Operation(Operation.STATUS, 1, Status.STATUS_ACTIVE),
         new Operation(Operation.COMMIT, 1),
      });
   }

   public void testNoResourcesRollback() throws Exception
   {
      runTest(new Operation[]
      {
         new Operation(Operation.BEGIN, 1),
         new Operation(Operation.STATUS, 1, Status.STATUS_ACTIVE),
         new Operation(Operation.ROLLBACK, 1),
      });
   }

   public void testNoResourcesSuspendResume() throws Exception
   {
      runTest(new Operation[]
      {
         new Operation(Operation.BEGIN, 1),
         new Operation(Operation.STATUS, 1, Status.STATUS_ACTIVE),
         new Operation(Operation.SUSPEND, 1),
         new Operation(Operation.STATUS, 1, Status.STATUS_ACTIVE),
         new Operation(Operation.RESUME, 1),
         new Operation(Operation.STATUS, 1, Status.STATUS_ACTIVE),
         new Operation(Operation.COMMIT, 1),
      });
   }

   public void testOneResourceCommit() throws Exception
   {
      runTest(new Operation[]
      {
         new Operation(Operation.BEGIN, 1),
         new Operation(Operation.STATUS, 1, Status.STATUS_ACTIVE),
         new Operation(Operation.CREATE, 1),
         new Operation(Operation.ENLIST, 1),
         new Operation(Operation.STATE, 1, Resource.ACTIVE),
         new Operation(Operation.COMMIT, 1),
         new Operation(Operation.STATE, 1, Resource.COMMITTED),
      });
   }

   public void testOneResourceRollback() throws Exception
   {
      runTest(new Operation[]
      {
         new Operation(Operation.BEGIN, 1),
         new Operation(Operation.STATUS, 1, Status.STATUS_ACTIVE),
         new Operation(Operation.CREATE, 1),
         new Operation(Operation.ENLIST, 1),
         new Operation(Operation.STATE, 1, Resource.ACTIVE),
         new Operation(Operation.ROLLBACK, 1),
         new Operation(Operation.STATE, 1, Resource.ROLLEDBACK),
      });
   }

   public void testOneResourceSetRollback() throws Exception
   {
      runTest(new Operation[]
      {
         new Operation(Operation.BEGIN, 1),
         new Operation(Operation.STATUS, 1, Status.STATUS_ACTIVE),
         new Operation(Operation.CREATE, 1),
         new Operation(Operation.ENLIST, 1),
         new Operation(Operation.STATE, 1, Resource.ACTIVE),
         new Operation(Operation.SETROLLBACK, 1),
         new Operation(Operation.STATUS, 1, Status.STATUS_MARKED_ROLLBACK),
         new Operation(Operation.COMMIT, 1, 0, new RollbackException()),
         new Operation(Operation.STATE, 1, Resource.ROLLEDBACK),
      });
   }

   public void testLocalResourceCommit() throws Exception
   {
      runTest(new Operation[]
      {
         new Operation(Operation.BEGIN, 1),
         new Operation(Operation.STATUS, 1, Status.STATUS_ACTIVE),
         new Operation(Operation.CREATE_LOCAL, 1),
         new Operation(Operation.ENLIST, 1),
         new Operation(Operation.STATE, 1, Resource.ACTIVE),
         new Operation(Operation.COMMIT, 1),
         new Operation(Operation.STATE, 1, Resource.COMMITTED),
      });
   }

   public void testLocalResourceRollback() throws Exception
   {
      runTest(new Operation[]
      {
         new Operation(Operation.BEGIN, 1),
         new Operation(Operation.STATUS, 1, Status.STATUS_ACTIVE),
         new Operation(Operation.CREATE_LOCAL, 1),
         new Operation(Operation.ENLIST, 1),
         new Operation(Operation.STATE, 1, Resource.ACTIVE),
         new Operation(Operation.ROLLBACK, 1),
         new Operation(Operation.STATE, 1, Resource.ROLLEDBACK),
      });
   }

   public void testLocalResourceSetRollback() throws Exception
   {
      runTest(new Operation[]
      {
         new Operation(Operation.BEGIN, 1),
         new Operation(Operation.STATUS, 1, Status.STATUS_ACTIVE),
         new Operation(Operation.CREATE_LOCAL, 1),
         new Operation(Operation.ENLIST, 1),
         new Operation(Operation.STATE, 1, Resource.ACTIVE),
         new Operation(Operation.SETROLLBACK, 1),
         new Operation(Operation.STATUS, 1, Status.STATUS_MARKED_ROLLBACK),
         new Operation(Operation.COMMIT, 1, 0, new RollbackException()),
         new Operation(Operation.STATE, 1, Resource.ROLLEDBACK),
      });
   }

   public void testLocalResourceCommitFail() throws Exception
   {
      runTest(new Operation[]
      {
         new Operation(Operation.BEGIN, 1),
         new Operation(Operation.STATUS, 1, Status.STATUS_ACTIVE),
         new Operation(Operation.CREATE_LOCAL, 1),
         new Operation(Operation.ENLIST, 1),
         new Operation(Operation.STATE, 1, Resource.ACTIVE),
         new Operation(Operation.FAIL_LOCAL, 1),
         new Operation(Operation.COMMIT, 1, 0, new RollbackException()),
         new Operation(Operation.STATE, 1, Resource.ROLLEDBACK),
      });
   }

   public void testTwoResourceSameRMCommit() throws Exception
   {
      runTest(new Operation[]
      {
         new Operation(Operation.BEGIN, 1),
         new Operation(Operation.STATUS, 1, Status.STATUS_ACTIVE),
         new Operation(Operation.CREATE, 1),
         new Operation(Operation.ENLIST, 1),
         new Operation(Operation.STATE, 1, Resource.ACTIVE),
         new Operation(Operation.CREATE, 2),
         new Operation(Operation.ENLIST, 2),
         new Operation(Operation.STATE, 2, Resource.ACTIVE),
         new Operation(Operation.COMMIT, 1),
         new Operation(Operation.STATE, 1, Resource.COMMITTED),
         new Operation(Operation.STATE, 2, Resource.COMMITTED),
      });
   }

   public void testTwoResourceSameRMRollback() throws Exception
   {
      runTest(new Operation[]
      {
         new Operation(Operation.BEGIN, 1),
         new Operation(Operation.STATUS, 1, Status.STATUS_ACTIVE),
         new Operation(Operation.CREATE, 1),
         new Operation(Operation.ENLIST, 1),
         new Operation(Operation.STATE, 1, Resource.ACTIVE),
         new Operation(Operation.CREATE, 2),
         new Operation(Operation.ENLIST, 2),
         new Operation(Operation.STATE, 2, Resource.ACTIVE),
         new Operation(Operation.ROLLBACK, 1),
         new Operation(Operation.STATE, 1, Resource.ROLLEDBACK),
         new Operation(Operation.STATE, 2, Resource.ROLLEDBACK),
      });
   }

   public void testTwoResourceSameRMSetRollback() throws Exception
   {
      runTest(new Operation[]
      {
         new Operation(Operation.BEGIN, 1),
         new Operation(Operation.STATUS, 1, Status.STATUS_ACTIVE),
         new Operation(Operation.CREATE, 1),
         new Operation(Operation.ENLIST, 1),
         new Operation(Operation.STATE, 1, Resource.ACTIVE),
         new Operation(Operation.CREATE, 2),
         new Operation(Operation.ENLIST, 2),
         new Operation(Operation.STATE, 2, Resource.ACTIVE),
         new Operation(Operation.SETROLLBACK, 1),
         new Operation(Operation.STATUS, 1, Status.STATUS_MARKED_ROLLBACK),
         new Operation(Operation.COMMIT, 1, 0, new RollbackException()),
         new Operation(Operation.STATE, 1, Resource.ROLLEDBACK),
         new Operation(Operation.STATE, 2, Resource.ROLLEDBACK),
      });
   }

   public void testTwoResourceDifferentRMCommitOneReadOnly() throws Exception
   {
      runTest(new Operation[]
      {
         new Operation(Operation.BEGIN, 1),
         new Operation(Operation.STATUS, 1, Status.STATUS_ACTIVE),
         new Operation(Operation.CREATE, 1),
         new Operation(Operation.ENLIST, 1),
         new Operation(Operation.STATE, 1, Resource.ACTIVE),
         new Operation(Operation.SETSTATUS, 1, XAResource.XA_RDONLY),
         new Operation(Operation.CREATE, 2),
         new Operation(Operation.ENLIST, 2),
         new Operation(Operation.DIFFRM, 2),
         new Operation(Operation.STATE, 2, Resource.ACTIVE),
         new Operation(Operation.COMMIT, 1),
         new Operation(Operation.STATE, 1, Resource.PREPARED),
         new Operation(Operation.STATE, 2, Resource.COMMITTED),
      });
   }

   public void testTwoResourceDifferentRMRollbackOneReadOnly() throws Exception
   {
      runTest(new Operation[]
      {
         new Operation(Operation.BEGIN, 1),
         new Operation(Operation.STATUS, 1, Status.STATUS_ACTIVE),
         new Operation(Operation.CREATE, 1),
         new Operation(Operation.ENLIST, 1),
         new Operation(Operation.STATE, 1, Resource.ACTIVE),
         new Operation(Operation.SETSTATUS, 1, XAResource.XA_RDONLY),
         new Operation(Operation.CREATE, 2),
         new Operation(Operation.ENLIST, 2),
         new Operation(Operation.DIFFRM, 2),
         new Operation(Operation.STATE, 2, Resource.ACTIVE),
         new Operation(Operation.ROLLBACK, 1),
         new Operation(Operation.STATE, 1, Resource.ROLLEDBACK),
         new Operation(Operation.STATE, 2, Resource.ROLLEDBACK),
      });
   }

   public void testTwoResourceDifferentRMSetRollbackOneReadOnly() throws Exception
   {
      runTest(new Operation[]
      {
         new Operation(Operation.BEGIN, 1),
         new Operation(Operation.STATUS, 1, Status.STATUS_ACTIVE),
         new Operation(Operation.CREATE, 1),
         new Operation(Operation.ENLIST, 1),
         new Operation(Operation.STATE, 1, Resource.ACTIVE),
         new Operation(Operation.SETSTATUS, 1, XAResource.XA_RDONLY),
         new Operation(Operation.CREATE, 2),
         new Operation(Operation.ENLIST, 2),
         new Operation(Operation.DIFFRM, 2),
         new Operation(Operation.STATE, 2, Resource.ACTIVE),
         new Operation(Operation.SETROLLBACK, 1),
         new Operation(Operation.STATUS, 1, Status.STATUS_MARKED_ROLLBACK),
         new Operation(Operation.COMMIT, 1, 0, new RollbackException()),
         new Operation(Operation.STATE, 1, Resource.ROLLEDBACK),
         new Operation(Operation.STATE, 2, Resource.ROLLEDBACK),
      });
   }

   public void testTwoResourceOneLocalCommitOneReadOnly() throws Exception
   {
      runTest(new Operation[]
      {
         new Operation(Operation.BEGIN, 1),
         new Operation(Operation.STATUS, 1, Status.STATUS_ACTIVE),
         new Operation(Operation.CREATE_LOCAL, 1),
         new Operation(Operation.ENLIST, 1),
         new Operation(Operation.STATE, 1, Resource.ACTIVE),
         new Operation(Operation.CREATE, 2),
         new Operation(Operation.ENLIST, 2),
         new Operation(Operation.STATE, 2, Resource.ACTIVE),
         new Operation(Operation.SETSTATUS, 2, XAResource.XA_RDONLY),
         new Operation(Operation.COMMIT, 1),
         new Operation(Operation.STATE, 1, Resource.COMMITTED),
         new Operation(Operation.STATE, 2, Resource.PREPARED),
      });
   }

   public void testTwoResourceOneLocalRollbackOneReadOnly() throws Exception
   {
      runTest(new Operation[]
      {
         new Operation(Operation.BEGIN, 1),
         new Operation(Operation.STATUS, 1, Status.STATUS_ACTIVE),
         new Operation(Operation.CREATE_LOCAL, 1),
         new Operation(Operation.ENLIST, 1),
         new Operation(Operation.STATE, 1, Resource.ACTIVE),
         new Operation(Operation.CREATE, 2),
         new Operation(Operation.ENLIST, 2),
         new Operation(Operation.STATE, 2, Resource.ACTIVE),
         new Operation(Operation.SETSTATUS, 2, XAResource.XA_RDONLY),
         new Operation(Operation.ROLLBACK, 1),
         new Operation(Operation.STATE, 1, Resource.ROLLEDBACK),
         new Operation(Operation.STATE, 2, Resource.ROLLEDBACK),
      });
   }

   public void testTwoResourceOneLocalSetRollbackOneReadOnly() throws Exception
   {
      runTest(new Operation[]
      {
         new Operation(Operation.BEGIN, 1),
         new Operation(Operation.STATUS, 1, Status.STATUS_ACTIVE),
         new Operation(Operation.CREATE_LOCAL, 1),
         new Operation(Operation.ENLIST, 1),
         new Operation(Operation.STATE, 1, Resource.ACTIVE),
         new Operation(Operation.CREATE, 2),
         new Operation(Operation.ENLIST, 2),
         new Operation(Operation.STATE, 2, Resource.ACTIVE),
         new Operation(Operation.SETSTATUS, 2, XAResource.XA_RDONLY),
         new Operation(Operation.SETROLLBACK, 1),
         new Operation(Operation.STATUS, 1, Status.STATUS_MARKED_ROLLBACK),
         new Operation(Operation.COMMIT, 1, 0, new RollbackException()),
         new Operation(Operation.STATE, 1, Resource.ROLLEDBACK),
         new Operation(Operation.STATE, 2, Resource.ROLLEDBACK),
      });
   }

   public void testTwoResourceOneLocalCommitFailOneReadOnly() throws Exception
   {
      runTest(new Operation[]
      {
         new Operation(Operation.BEGIN, 1),
         new Operation(Operation.STATUS, 1, Status.STATUS_ACTIVE),
         new Operation(Operation.CREATE_LOCAL, 1),
         new Operation(Operation.ENLIST, 1),
         new Operation(Operation.STATE, 1, Resource.ACTIVE),
         new Operation(Operation.CREATE, 2),
         new Operation(Operation.ENLIST, 2),
         new Operation(Operation.STATE, 2, Resource.ACTIVE),
         new Operation(Operation.SETSTATUS, 2, XAResource.XA_RDONLY),
         new Operation(Operation.FAIL_LOCAL, 1),
         new Operation(Operation.COMMIT, 1, 0, new RollbackException()),
         new Operation(Operation.STATE, 1, Resource.ROLLEDBACK),
         new Operation(Operation.STATE, 2, Resource.PREPARED),
      });
   }

   public void testTwoResourceDifferentRMCommit() throws Exception
   {
      runTest(new Operation[]
      {
         new Operation(Operation.BEGIN, 1),
         new Operation(Operation.STATUS, 1, Status.STATUS_ACTIVE),
         new Operation(Operation.CREATE, 1),
         new Operation(Operation.ENLIST, 1),
         new Operation(Operation.STATE, 1, Resource.ACTIVE),
         new Operation(Operation.CREATE, 2),
         new Operation(Operation.ENLIST, 2),
         new Operation(Operation.DIFFRM, 2),
         new Operation(Operation.STATE, 2, Resource.ACTIVE),
         new Operation(Operation.COMMIT, 1),
         new Operation(Operation.STATE, 1, Resource.COMMITTED),
         new Operation(Operation.STATE, 2, Resource.COMMITTED),
      });
   }

   public void testTwoResourceDifferentRMRollback() throws Exception
   {
      runTest(new Operation[]
      {
         new Operation(Operation.BEGIN, 1),
         new Operation(Operation.STATUS, 1, Status.STATUS_ACTIVE),
         new Operation(Operation.CREATE, 1),
         new Operation(Operation.ENLIST, 1),
         new Operation(Operation.STATE, 1, Resource.ACTIVE),
         new Operation(Operation.CREATE, 2),
         new Operation(Operation.ENLIST, 2),
         new Operation(Operation.DIFFRM, 2),
         new Operation(Operation.STATE, 2, Resource.ACTIVE),
         new Operation(Operation.ROLLBACK, 1),
         new Operation(Operation.STATE, 1, Resource.ROLLEDBACK),
         new Operation(Operation.STATE, 2, Resource.ROLLEDBACK),
      });
   }

   public void testTwoResourceDifferentRMSetRollback() throws Exception
   {
      runTest(new Operation[]
      {
         new Operation(Operation.BEGIN, 1),
         new Operation(Operation.STATUS, 1, Status.STATUS_ACTIVE),
         new Operation(Operation.CREATE, 1),
         new Operation(Operation.ENLIST, 1),
         new Operation(Operation.STATE, 1, Resource.ACTIVE),
         new Operation(Operation.CREATE, 2),
         new Operation(Operation.ENLIST, 2),
         new Operation(Operation.DIFFRM, 2),
         new Operation(Operation.STATE, 2, Resource.ACTIVE),
         new Operation(Operation.SETROLLBACK, 1),
         new Operation(Operation.STATUS, 1, Status.STATUS_MARKED_ROLLBACK),
         new Operation(Operation.COMMIT, 1, 0, new RollbackException()),
         new Operation(Operation.STATE, 1, Resource.ROLLEDBACK),
         new Operation(Operation.STATE, 2, Resource.ROLLEDBACK),
      });
   }

   public void testTwoResourceOneLocalCommit() throws Exception
   {
      runTest(new Operation[]
      {
         new Operation(Operation.BEGIN, 1),
         new Operation(Operation.STATUS, 1, Status.STATUS_ACTIVE),
         new Operation(Operation.CREATE_LOCAL, 1),
         new Operation(Operation.ENLIST, 1),
         new Operation(Operation.STATE, 1, Resource.ACTIVE),
         new Operation(Operation.CREATE, 2),
         new Operation(Operation.ENLIST, 2),
         new Operation(Operation.STATE, 2, Resource.ACTIVE),
         new Operation(Operation.COMMIT, 1),
         new Operation(Operation.STATE, 1, Resource.COMMITTED),
         new Operation(Operation.STATE, 2, Resource.COMMITTED),
      });
   }

   public void testTwoResourceOneLocalRollback() throws Exception
   {
      runTest(new Operation[]
      {
         new Operation(Operation.BEGIN, 1),
         new Operation(Operation.STATUS, 1, Status.STATUS_ACTIVE),
         new Operation(Operation.CREATE_LOCAL, 1),
         new Operation(Operation.ENLIST, 1),
         new Operation(Operation.STATE, 1, Resource.ACTIVE),
         new Operation(Operation.CREATE, 2),
         new Operation(Operation.ENLIST, 2),
         new Operation(Operation.STATE, 2, Resource.ACTIVE),
         new Operation(Operation.ROLLBACK, 1),
         new Operation(Operation.STATE, 1, Resource.ROLLEDBACK),
         new Operation(Operation.STATE, 2, Resource.ROLLEDBACK),
      });
   }

   public void testTwoResourceOneLocalSetRollback() throws Exception
   {
      runTest(new Operation[]
      {
         new Operation(Operation.BEGIN, 1),
         new Operation(Operation.STATUS, 1, Status.STATUS_ACTIVE),
         new Operation(Operation.CREATE_LOCAL, 1),
         new Operation(Operation.ENLIST, 1),
         new Operation(Operation.STATE, 1, Resource.ACTIVE),
         new Operation(Operation.CREATE, 2),
         new Operation(Operation.ENLIST, 2),
         new Operation(Operation.STATE, 2, Resource.ACTIVE),
         new Operation(Operation.SETROLLBACK, 1),
         new Operation(Operation.STATUS, 1, Status.STATUS_MARKED_ROLLBACK),
         new Operation(Operation.COMMIT, 1, 0, new RollbackException()),
         new Operation(Operation.STATE, 1, Resource.ROLLEDBACK),
         new Operation(Operation.STATE, 2, Resource.ROLLEDBACK),
      });
   }

   public void testTwoResourceOneLocalCommitFail() throws Exception
   {
      runTest(new Operation[]
      {
         new Operation(Operation.BEGIN, 1),
         new Operation(Operation.STATUS, 1, Status.STATUS_ACTIVE),
         new Operation(Operation.CREATE_LOCAL, 1),
         new Operation(Operation.ENLIST, 1),
         new Operation(Operation.STATE, 1, Resource.ACTIVE),
         new Operation(Operation.CREATE, 2),
         new Operation(Operation.ENLIST, 2),
         new Operation(Operation.STATE, 2, Resource.ACTIVE),
         new Operation(Operation.FAIL_LOCAL, 1),
         new Operation(Operation.COMMIT, 1, 0, new RollbackException()),
         new Operation(Operation.STATE, 1, Resource.ROLLEDBACK),
         new Operation(Operation.STATE, 2, Resource.ROLLEDBACK),
      });
   }

   public void testOneResourceCommitHeurRB() throws Exception
   {
      runTest(new Operation[]
      {
         new Operation(Operation.BEGIN, 1),
         new Operation(Operation.STATUS, 1, Status.STATUS_ACTIVE),
         new Operation(Operation.CREATE, 1),
         new Operation(Operation.ENLIST, 1),
         new Operation(Operation.STATE, 1, Resource.ACTIVE),
         new Operation(Operation.SETSTATUS, 1, XAException.XA_HEURRB),
         new Operation(Operation.COMMIT, 1, 0,new RollbackException()),
         new Operation(Operation.STATE, 1, Resource.FORGOT),
      });
   }
}
