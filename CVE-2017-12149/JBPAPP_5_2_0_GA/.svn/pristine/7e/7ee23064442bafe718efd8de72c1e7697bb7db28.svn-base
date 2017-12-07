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
package org.jboss.test.ejb3.common;

import javax.jms.QueueConnectionFactory;
import javax.naming.NamingException;

import org.jboss.test.JBossTestCase;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public abstract class EJB3TestCase extends JBossTestCase
{
   protected EJB3TestCase(String name)
   {
      super(name);
   }

   protected QueueConnectionFactory getQueueConnectionFactory() throws Exception
   {
      try
      {
         return (QueueConnectionFactory) getInitialContext().lookup("ConnectionFactory");
      }
      catch (NamingException e)
      {
         return (QueueConnectionFactory) getInitialContext().lookup("java:/ConnectionFactory");
      }
   }

   protected <T> T lookup(String name, Class<T> expectedType) throws Exception
   {
      return expectedType.cast(getInitialContext().lookup(name));
   }
   
   /**
    * Make sure the deployment is successful.
    * @throws Exception
    */
   public final void testServerFound() throws Exception
   {
      // we don't want this done in suite, because then the individual
      // failure count for this test would go down. (1 failure instead of many)
      serverFound();
   }
}
