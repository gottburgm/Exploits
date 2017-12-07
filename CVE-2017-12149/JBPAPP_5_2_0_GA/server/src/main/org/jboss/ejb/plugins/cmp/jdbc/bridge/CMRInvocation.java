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
package org.jboss.ejb.plugins.cmp.jdbc.bridge;

import org.jboss.ejb.plugins.lock.Entrancy;
import org.jboss.invocation.LocalEJBInvocation;

/**
 * Optimized invocation object for local CMR invocations
 *
 * @author  <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 81030 $
 */
public final class CMRInvocation extends LocalEJBInvocation
{
   private Entrancy entrancy;
   private CMRMessage cmrMessage;

   public CMRInvocation()
   {
   }

   public Entrancy getEntrancy()
   {
      return entrancy;
   }

   public void setEntrancy(Entrancy entrancy)
   {
      this.entrancy = entrancy;
   }

   public CMRMessage getCmrMessage()
   {
      return cmrMessage;
   }

   public void setCmrMessage(CMRMessage cmrMessage)
   {
      this.cmrMessage = cmrMessage;
   }
}
