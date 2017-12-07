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
package org.jboss.ejb.plugins;

import org.jboss.ejb.EnterpriseContext;
import org.jboss.util.Executable;

/** Abstract class for passivation jobs.
Subclasses should implement {@link #execute} synchronizing it in some way because
the execute method is normally called in the passivation thread,
while the cancel method is normally called from another thread.
To avoid that subclasses override methods of this class without
make them synchronized (except execute of course), they're declared final.

@author <a href="mailto:simone.bordet@compaq.com">Simone Bordet</a>
@author Scott.Stark@jboss.org
@version $Revision: 81030 $
*/
public abstract class AbstractPassivationJob implements Executable
{
   protected EnterpriseContext ctx;
   protected Object key;
   protected boolean isCancelled;
   protected boolean isExecuted;

   AbstractPassivationJob(EnterpriseContext ctx, Object key)
   {
      this.ctx = ctx;
      this.key = key;
   }

   /**
    * (Bill Burke) We can't rely on the EnterpriseContext to provide PassivationJob
    * with a valid key because it may get freed to the InstancePool, then
    * reused before the PassivationJob executes.
    */
   final Object getKey()
   {
      return key;
   }
   /**
    * Returns the EnterpriseContext associated with this passivation job,
    * so the bean that will be passivated.
    * No need to synchronize access to this method, since the returned
    * reference is immutable
    */
   final EnterpriseContext getEnterpriseContext()
   {
      return ctx;
   }
   /**
    * Mark this job for cancellation.
    * @see #isCancelled
    */
   final synchronized void cancel()
   {
      isCancelled = true;
   }
   /**
    * Returns whether this job has been marked for cancellation
    * @see #cancel
    */
   final synchronized boolean isCancelled()
   {
      return isCancelled;
   }
   /**
    * Mark this job as executed
    * @see #isExecuted
    */
   final synchronized void executed()
   {
      isExecuted = true;
   }
   /**
    * Returns whether this job has been executed
    * @see #executed
    */
   final synchronized boolean isExecuted()
   {
      return isExecuted;
   }

}
