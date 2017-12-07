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
package org.jboss.management.j2ee;

/**
 * Indicates that this Managed Object supports all the
 * operations and attributes to support state management.
 * An Managed Object implementing this interface is
 * termed as State Manageable Object (SMO).
 * An SMO generates events when its state changes.
 * <br><br>
 * <b>Attention</b>: This interface is not indented to be used by the client
 * but it is morely a desription of what the client will get when he/she
 * receives attributes from the JSR-77 server or what method can be
 * invoked.
 * <br><br>
 * All attributes (getXXX) can be received by
 * <br>
 * {@link javax.management.j2ee.Management#getAttribute Management.getAttribute()}
 * <br>
 * or in bulk by:
 * <br>
 * {@link javax.management.j2ee.Management#getAttributes Management.getAttributes()}
 * <br>
 * Methods (all except getXXX()) can be invoked by:
 * <br>
 * {@link javax.management.j2ee.Management#invoke Management.invoke()}
 * <br>
 *
 * @author <a href="mailto:marc@jboss.org">Marc Fleury</a>
 * @author Andreas Schaefer
 * @version $Revision: 81025 $
 */
public interface StateManageable extends EventProvider
{
   // Constants -----------------------------------------------------
   
   public static final int STARTING = 0;
   public static final int RUNNING = 1;
   public static final int STOPPING = 2;
   public static final int STOPPED = 3;
   public static final int FAILED = 4;
   public static final int CREATED = 5;
   public static final int DESTROYED = 6;
   public static final int REGISTERED = 7;
   public static final int UNREGISTERED = 8;
   
   // Public --------------------------------------------------------
   
   /**
    * @return The Time (in milliseconds since 1/1/1970 00:00:00) that this
    *         managed object was started
    */
   public long getStartTime();

   /**
    * @return Current State of the SMO which could be either {@link #STARTING
    *         starting}, {@link #RUNNING running}, {@link #STOPPING stopping},
    *         {@link #STOPPED stopped} or {@link FAILED failed}
    */
   public int getState();

   /**
    * @return Current State string from amont {@link #STARTING
    *         STARTING}, {@link #RUNNING RUNNING}, {@link #STOPPING STOPPING},
    *         {@link #STOPPED STOPPED} or {@link FAILED FAILED}
    */
   public String getStateString();

   /**
    * Starts this SMO which can only be invoked when the SMO is in the State
    * {@link #STOPPED stopped}. The SMO will go into the State {@link @STARTING
    * started} and after it completes successfully the SMO will go to the State
    * {@link #RUNNING running}.
    * The children of the SMO will not be started by this method call.
    * <p/>
    * <b>Attention</b>: According to the specification this is named <i>start()</i>
    * but in order to avoid name conflicts this is renamed to
    * <i>mejbStart()</i>. The MEJB interface will make the conversion
    * from <i>start</i> to <i>mejbStart</i> to make it transparent
    * to the client.
    */
   public void mejbStart();

   /**
    * Starts this SMO like {@link @start start()}. After the SMO is started all
    * its children in the State of {@link @STOPPED stopped} theirs startRecursive()
    * are started too.
    * <p/>
    * <b>Attention</b>: According to the specification this is named <i>startRecursive()</i>
    * but in order to avoid name conflicts this is renamed to
    * <i>mejbStartRecursive()</i>. The MEJB interface will make the conversion
    * from <i>startRecursive</i> to <i>mejbStartRecursive</i> to make it transparent
    * to the client.
    */
   public void mejbStartRecursive();

   /**
    * Stops this SMO which can only be into the {@link #RUNNING running} or
    * {@link #STARTING starting}. The State will change to {@link #STOPPING
    * stoping} and after it completes successfully it will go into the
    * State {@link #STOPPED stopped}. Afterwards all its children stop()
    * method is called.
    * <p/>
    * <b>Attention</b>: According to the specification this is named <i>stop()</i>
    * but in order to avoid name conflicts this is renamed to
    * <i>mejbStop()</i>. The MEJB interface will make the conversion
    * from <i>stop</i> to <i>mejbStop</i> to make it transparent
    * to the client.
    */
   public void mejbStop();

}
