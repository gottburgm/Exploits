/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.resource.connectionmanager;

import javax.management.ObjectName;
import javax.transaction.xa.XAException;

import org.jboss.logging.Logger;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.tm.XAExceptionFormatter;

/**
 * JBossLocalXAExceptionFormatter.java
 *
 * @author <a href="mailto:igorfie at yahoo dot com">Igor Fedorenko</a>.
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 71554 $
 */

public class JBossLocalXAExceptionFormatter extends ServiceMBeanSupport
      implements XAExceptionFormatter, JBossLocalXAExceptionFormatterMBean
{
   private ObjectName transactionManagerService;

   public JBossLocalXAExceptionFormatter()
   {
   }

   public ObjectName getTransactionManagerService()
   {
      return transactionManagerService;
   }

   public void setTransactionManagerService(ObjectName transactionManagerService)
   {
      this.transactionManagerService = transactionManagerService;
   }

   protected void startService() throws Exception
   {
      getServer().invoke(transactionManagerService, "registerXAExceptionFormatter", 
            new Object[] { JBossLocalXAException.class, this }, 
            new String[] { Class.class.getName(), XAExceptionFormatter.class.getName() }
      );
   }

   protected void stopService() throws Exception
   {
      getServer().invoke(transactionManagerService, "unregisterXAExceptionFormatter", 
            new Object[] { JBossLocalXAException.class }, 
            new String[] { Class.class.getName() }
      );
   }

   public void formatXAException(XAException xae, Logger log)
   {
      try
      {
         log.warn("JBoss Local XA wrapper error: ", ((JBossLocalXAException) xae).getCause());
      }
      catch (Exception e)
      {
         log.warn("Problem trying to format XAException: ", e);
      }
   }
}
