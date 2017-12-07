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
package org.jboss.test.jmx.test;

import org.jboss.system.ServiceMBeanState;
import org.jboss.test.JBossTestCase;
import org.jboss.util.state.IllegalTransitionException;
import org.jboss.util.state.State;
import org.jboss.util.state.StateMachine;

/**
 * ServiceMBean StateMachine tests
 * 
 * @author <a href="dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81036 $
**/
public class ServiceMBeanStateMachineUnitTestCase extends JBossTestCase
{
   /** The ServiceMBean StateMachine */
   private StateMachine sm = ServiceMBeanState.createStateMachine("A test state machine");
   
   public ServiceMBeanStateMachineUnitTestCase(String name)
   {
      super(name);
   }

   public void setUp()
   {
      sm.reset();
   }
   
   public void testRegisterUnregister() throws Exception
   {
      getLog().debug("+++ testRegisterUnregister");
      
      String[] script = {
            "register",
            "unregister"
      };
      tryActionScript(script, false);
   }
   
   public void testCreateDestroy() throws Exception
   {
      getLog().debug("+++ testCreateDestroy");
      
      String[] script = {
            "register",
            "create",
            "destroy"
      };
      tryActionScript(script, false);
   }
   
   public void testCreateDestroyCreate() throws Exception
   {
      getLog().debug("+++ testCreateDestroyCreate");
      
      String[] script = {
            "register",
            "create",
            "destroy",
            "create"
      };
      tryActionScript(script, false);
   }
   
   public void testStartStopStart() throws Exception
   {
      getLog().debug("+++ testStartStopStart");
      
      String[] script = {
            "register",
            "create",
            "startBegin",
            "startEnd",
            "stopBegin",
            "stopEnd",
            "startBegin",
            "startEnd"
      };
      tryActionScript(script, false);
   }
   
   public void testFullLifecycle() throws Exception
   {
      getLog().debug("+++ testFullLifecycle");
      
      String[] script = {
            "register",
            "create",
            "startBegin",
            "startEnd",
            "stopBegin",
            "stopEnd",
            "destroy",
            "unregister"
      };
      tryActionScript(script, false);
   }
   
   public void testFailedCreate() throws Exception
   {
      getLog().debug("+++ testFailedCreate");
      
      String[] script = {
            "register",
            "create",
            "fail"
      };
      tryActionScript(script, false);
   }
   
   public void testFailedStart() throws Exception
   {
      getLog().debug("+++ testFailedStart");
      
      String[] script = {
            "register",
            "create",
            "startBegin",
            "fail"
      };
      tryActionScript(script, false);
   }
   
   public void testFailedStop() throws Exception
   {
      getLog().debug("+++ testFailedStop");
      
      String[] script = {
            "register",
            "create",
            "startBegin",
            "startEnd",
            "stopBegin",
            "fail"
      };
      tryActionScript(script, false);
   }
   
   public void testFailedDestroy() throws Exception
   {
      getLog().debug("+++ testFailedDestroy");
      
      String[] script = {
            "register",
            "create",
            "startBegin",
            "startEnd",
            "stopBegin",
            "stopEnd",
            "destroy",
            "fail"
      };
      tryActionScript(script, false);
   }
   
   public void testDontCreateBeforeRegister() throws Exception
   {
      getLog().debug("+++ testDontCreateBeforeRegister");
      
      String[] script = {
            "create"
      };
      tryActionScript(script, true);
   }
   
   public void testDontStartBeforeCreate() throws Exception
   {
      getLog().debug("+++ testDontStartBeforeCreate");
      
      String[] script = {
            "register",
            "startBegin"
      };
      tryActionScript(script, true);
   }
   
   public void testDontDestroyBeforeStop() throws Exception
   {
      getLog().debug("+++ testDontDestroyBeforeStop");
      
      String[] script = {
            "register",
            "create",
            "startBegin",
            "startEnd",
            "destroy"
      };
      tryActionScript(script, true);
   }
   
   private void tryActionScript(String[] script, boolean exceptionExpected) throws Exception
   {
      for (int i = 0; i < script.length; i++)
      {
         getLog().debug("Current State: " + sm.getCurrentState().getName() + ", Action: " + script[i]);
         try
         {
            State stateAfter = sm.nextState(script[i]);
         }
         catch (IllegalTransitionException e)
         {
            if (exceptionExpected)
            {
               getLog().debug("Caught Excepted IllegalTransitionException: " + e.getMessage());
            }
            else
            {
               getLog().debug("Caught Unexcepted IllegalTransitionException: " + e.getMessage());
               
               throw e;
            }
         }
      }
      getLog().debug("Final State: " + sm.getCurrentState().getName());
   }
}
