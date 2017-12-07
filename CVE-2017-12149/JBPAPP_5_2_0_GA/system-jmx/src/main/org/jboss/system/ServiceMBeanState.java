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
package org.jboss.system;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jboss.util.state.State;
import org.jboss.util.state.StateMachine;
import org.jboss.util.state.Transition;

/**
 * A final class that encapsulates the constants necessary
 * for creating StateMachines that follow the ServiceMBean
 * lifecycle model.
 * 
 * @author <a href="dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81033 $
**/
public final class ServiceMBeanState
{
   // ServiceMBean States
   public static final State STATE_UNREGISTERED = new State("Unregistered"); // Initial State
   public static final State STATE_REGISTERED   = new State("Registered");
   public static final State STATE_CREATED      = new State("Created");
   public static final State STATE_STARTING     = new State("Starting");
   public static final State STATE_STARTED      = new State("Started");
   public static final State STATE_STOPPING     = new State("Stopping");
   public static final State STATE_STOPPED      = new State("Stopped");
   public static final State STATE_DESTROYED    = new State("Destroyed");
   public static final State STATE_FAILED       = new State("Failed");
   
   // ServiceMBean State Transitions
   public static final Transition TRANS_REGISTER    = new Transition("register", STATE_REGISTERED);
   public static final Transition TRANS_CREATE      = new Transition("create", STATE_CREATED);
   public static final Transition TRANS_START_BEGIN = new Transition("startBegin", STATE_STARTING);
   public static final Transition TRANS_START_END   = new Transition("startEnd", STATE_STARTED);
   public static final Transition TRANS_STOP_BEGIN  = new Transition("stopBegin", STATE_STOPPING);
   public static final Transition TRANS_STOP_END    = new Transition("stopEnd", STATE_STOPPED);
   public static final Transition TRANS_DESTROY     = new Transition("destroy", STATE_DESTROYED);
   public static final Transition TRANS_UNREGISTER  = new Transition("unregister", STATE_UNREGISTERED);
   public static final Transition TRANS_FAIL        = new Transition("fail", STATE_FAILED);

   /** The possible set of States a ServiceMBean can be in */
   public static final Set STATES = new HashSet(
      Arrays.asList(new State[] {
         STATE_UNREGISTERED,
         STATE_REGISTERED,
         STATE_CREATED,
         STATE_STARTING,
         STATE_STARTED,
         STATE_STOPPING,
         STATE_STOPPED,
         STATE_DESTROYED,
         STATE_FAILED
      }));
   
   // Associate States with valid Transitions, or else, define explicitly
   // the ServiceMBean lifecycle that drives the StateMachine
   static
   {
      STATE_UNREGISTERED
         .addTransition(TRANS_REGISTER)
         .addTransition(TRANS_FAIL);
      
      STATE_REGISTERED
         .addTransition(TRANS_CREATE)
         .addTransition(TRANS_UNREGISTER)
         .addTransition(TRANS_FAIL);
      
      STATE_CREATED
         .addTransition(TRANS_START_BEGIN)
         .addTransition(TRANS_DESTROY)
         .addTransition(TRANS_FAIL);
      
      STATE_STARTING
         .addTransition(TRANS_START_END)
         .addTransition(TRANS_FAIL);
      
      STATE_STARTED  
         .addTransition(TRANS_STOP_BEGIN)
         .addTransition(TRANS_FAIL);
      
      STATE_STOPPING
         .addTransition(TRANS_STOP_END)
         .addTransition(TRANS_FAIL);
      
      STATE_STOPPED  
         .addTransition(TRANS_START_BEGIN)
         .addTransition(TRANS_DESTROY)
         .addTransition(TRANS_FAIL);
      
      STATE_DESTROYED
         .addTransition(TRANS_CREATE)
         .addTransition(TRANS_UNREGISTER)
         .addTransition(TRANS_FAIL);
      
      // STATE_FAILED - no transitions
   }
   
   /**
    * Create a new StateMachine that follows the ServiceMBean
    * lifecycle, initialized to the STATE_UNREGISTERED state.
    * 
    * @param description A string description for this state machine, or null
    * @return the StateMachine
    */
   public static StateMachine createStateMachine(String description)
   {
      return new StateMachine(STATES, STATE_UNREGISTERED, description);
   }
   
   /**
    * Dissallow instances of this class
    */
   private ServiceMBeanState()
   {
      // empty
   }

}
