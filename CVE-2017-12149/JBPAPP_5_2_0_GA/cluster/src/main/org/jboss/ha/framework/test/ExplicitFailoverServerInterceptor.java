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
package org.jboss.ha.framework.test;

import org.jboss.invocation.Invocation;
import org.jboss.ha.framework.interfaces.GenericClusteringException;

/**
 * Used for testing clustering; allows an explicit call to make a node fail.
 * This will mimic a dead server.
 *
 * @see org.jboss.ha.framework.test.ExplicitFailoverClientInterceptor
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 81001 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>8 avril 2002 Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li>
 * </ul>
 */

public class ExplicitFailoverServerInterceptor extends org.jboss.ejb.plugins.AbstractInterceptor
{
   
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   protected org.jboss.ejb.Container container;

   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   public ExplicitFailoverServerInterceptor ()
   {
   }
   
   // Public --------------------------------------------------------
   
   public void setContainer(org.jboss.ejb.Container container)
   {
      this.container = container;
   }
	
   public org.jboss.ejb.Container getContainer()
   {
      return container;
   }
	
   // Z implementation ----------------------------------------------
   
   // AbstractInterceptor overrides ---------------------------------------------------
   
   public Object invokeHome(Invocation mi)
      throws Exception
   {
      checkFailoverNeed (mi);
      
     return super.invokeHome (mi);
   }
   
   public Object invoke(Invocation mi)
      throws Exception
   {
      checkFailoverNeed (mi);
      
      return super.invoke (mi);
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   protected void checkFailoverNeed (Invocation mi) 
      throws GenericClusteringException 
   {
      Object data = mi.getValue ("DO_FAIL_DURING_NEXT_CALL");
      
      if (data != null &&
          data instanceof java.lang.Boolean &&
          data.equals (java.lang.Boolean.TRUE))
      {
         // we now determine if we have already failed
         //
         Object alreadyDone = mi.getValue ("FAILOVER_COUNTER");
         
         if (alreadyDone != null &&
             alreadyDone instanceof java.lang.Integer &&
             ((java.lang.Integer)alreadyDone).intValue () == 0)
         {
            // we do fail
            //
            this.log.debug ("WE FAILOVER IN SERVER INTERCEPTOR (explicit failover asked by client interceptor)!");
            throw new GenericClusteringException 
               (GenericClusteringException.COMPLETED_NO, "Test failover from server interceptor", false);
         }
      }
   }
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
   
}
