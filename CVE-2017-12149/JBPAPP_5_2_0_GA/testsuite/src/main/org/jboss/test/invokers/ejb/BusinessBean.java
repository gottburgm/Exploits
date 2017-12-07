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
package org.jboss.test.invokers.ejb;

import java.util.Date;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.jboss.logging.Logger;

/** A simple session bean for testing access via direct rpc and asynch jms.

@author Scott.Stark@jboss.org
@version $Revision: 81036 $
*/
public class BusinessBean implements SessionBean
{
   static Logger log = Logger.getLogger(BusinessBean.class);
   private SessionContext sessionContext;

   public void ejbCreate()
   {
      log.debug("ejbCreate() called");
   }

   public void ejbActivate()
   {
      log.debug("ejbActivate() called");
   }

   public void ejbPassivate()
   {
      log.debug("ejbPassivate() called");
   }

   public void ejbRemove()
   {
      log.debug("ejbRemove() called");
   }

   public void setSessionContext(SessionContext context)
   {
      sessionContext = context;
   }

   public boolean doSomething()
   {
      log.info("doSomething");
      return true;
   }

   public String doSomethingSlowly(Object arg1, String arg2)
   {
      log.info("doSomethingSlowly, arg1="+arg1);
      try
      {
         Thread.sleep(10 * 1000);
      }
      catch(Exception ex)
      {
         throw new EJBException(ex);
      }
      String result = arg2 + " - " + new Date();
      log.info("Result: "+result);
      return result;
   }

}
