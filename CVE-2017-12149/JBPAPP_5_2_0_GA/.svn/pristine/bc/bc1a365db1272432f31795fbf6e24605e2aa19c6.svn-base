/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.test.sessionsync.test;


import junit.framework.Test;

import org.jboss.test.JBossTestCase;
import org.jboss.test.sessionsync.actions.GetAfterBeginEntryAction;
import org.jboss.test.sessionsync.actions.GetAfterCompletionEntryAction;
import org.jboss.test.sessionsync.actions.GetBeforeCompletionEntryAction;
import org.jboss.test.sessionsync.interfaces.ActionExecutor;
import org.jboss.test.sessionsync.interfaces.ActionExecutorHome;
import org.jboss.test.sessionsync.interfaces.StatefulSession;
import org.jboss.test.sessionsync.interfaces.StatefulSessionHome;

/**
 * A CompEnvInSessionSyncCallbacksUnitTestCase.
 * 
 * @author <a href="alex@jboss.com">Alexey Loubyansky</a>
 * @version $Revision: 1.1 $
 */
public class CompEnvInSessionSyncCallbacksUnitTestCase extends JBossTestCase
{
   public static Test suite() throws Exception
   {
      return JBossTestCase.getDeploySetup(CompEnvInSessionSyncCallbacksUnitTestCase.class, "compenv-sessionsync.jar");
   }
   
   public CompEnvInSessionSyncCallbacksUnitTestCase(String name)
   {
      super(name);
   }

   /**
    * This test is based on command pattern and calling methods on the target stateful session bean
    * from a delegating stateless session bean instead of direct method invocations on the target stateful session bean.
    * This is done on purpose to make sure the container sets up comp/env properly in the transaction synchronization implementation
    * before invoking SessionSynchronization callbacks.
    * 
    * @throws Exception
    */
   public void testBeforeCompletion() throws Exception
   {
      ActionExecutorHome executorHome = (ActionExecutorHome) getInitialContext().lookup("ActionExecutorBean");
      ActionExecutor executor = executorHome.create();

      StatefulSessionHome sessionHome = (StatefulSessionHome) getInitialContext().lookup("StatefulSessionBean");
      final StatefulSession session = sessionHome.create();
      try
      {

         assertEquals("after-begin", executor.execute(new GetAfterBeginEntryAction(session.getHandle())));
         assertEquals("before-completion", executor.execute(new GetBeforeCompletionEntryAction(session.getHandle())));
         assertEquals("after-completion", executor.execute(new GetAfterCompletionEntryAction(session.getHandle())));
      }
      finally
      {
         if(session != null)
            session.remove();
      }
   }
}
