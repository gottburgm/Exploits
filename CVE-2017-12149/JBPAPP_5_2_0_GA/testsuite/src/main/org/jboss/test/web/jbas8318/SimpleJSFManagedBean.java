/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

package org.jboss.test.web.jbas8318;

import javax.annotation.Resource;
import javax.faces.FacesException;
import javax.jms.Queue;
import javax.transaction.UserTransaction;

/**
 * User: jpai
 */
public class SimpleJSFManagedBean extends JSFBaseComponent
{

   @Resource(mappedName = "queue/DLQ")
   private Queue dlq;

   @Resource(name = "simpleString")
   private String simpleString;

   @Resource
   private UserTransaction userTransaction;

   public boolean isSimpleEnvEntryInjected()
   {
      if (this.simpleString == null)
      {
         throw new FacesException("Simple env entry string was not injected in JSF managed bean");
      }
      return true;
   }

   public boolean isUserTransactionInjected()
   {
      if (this.userTransaction == null)
      {
         throw new FacesException("UserTransaction was not injected in JSF managed bean");
      }
      return true;
   }

   public boolean isQueueInjected()
   {
      if (this.dlq == null)
      {
         throw new FacesException("Queue was not injected in JSF managed bean");
      }
      return true;
   }

   public boolean isBaseClassResourcesInjected()
   {
      if (this.envEntryStringInBaseClass == null)
      {
         throw new FacesException("Simple env-entry string in base class of JSF managed bean was not injected");
      }
      if (this.utInBaseClass == null)
      {
         throw new FacesException("UserTransaction in base class of JSF managed bean was not injected");
      }
      return true;
   }
}
