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
import org.jboss.invocation.PayloadKey;

/**
 * Used for testing clustering; allows an explicit call to make a node fail.
 * This will mimic a dead server.
 *
 * @see org.jboss.ha.framework.test.ExplicitFailoverServerInterceptor
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

public class ExplicitFailoverClientInterceptor extends org.jboss.proxy.Interceptor
{
   
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   
   /** The serialVersionUID */
   private static final long serialVersionUID = -23836213158136538L;
   
   // Constructors --------------------------------------------------

   public ExplicitFailoverClientInterceptor ()
   {
   }
   
   // Public --------------------------------------------------------
   
   // Z implementation ----------------------------------------------
   
   // Interceptor overrides ---------------------------------------------------
   
   public Object invoke (Invocation mi) throws Throwable
   {
      Object failover = System.getProperty ("JBossCluster-DoFail");
      boolean doFail = false;
      
      if (failover != null && 
          failover instanceof java.lang.String)
      {
         String strFailover = (java.lang.String)failover;
         if (strFailover.equalsIgnoreCase ("true"))
         {
            doFail = true;
         }
         else if (strFailover.equalsIgnoreCase ("once"))
         {
            doFail = true;
            System.setProperty ("JBossCluster-DoFail", "false");
         }          
      }
      
      if (doFail)
      {
         mi.setValue ("DO_FAIL_DURING_NEXT_CALL", Boolean.TRUE, PayloadKey.AS_IS);
         System.out.println("SYSTEM : We fail during next call!!!");
      }
      else
         mi.setValue ("DO_FAIL_DURING_NEXT_CALL", Boolean.FALSE, PayloadKey.AS_IS);
         

      return getNext().invoke(mi);
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
   
}
