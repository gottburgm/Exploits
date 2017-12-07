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
package org.jboss.test.jms;

import javax.management.MBeanServerConnection;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.test.JBossTestServices;
import org.jboss.util.NestedRuntimeException;

/**
 * JBossASJMSTestAdmin.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81036 $
 */
public class JBossASJMSTestAdmin extends AbstractAdmin
{
   private JBossTestServices delegate;
   
   public JBossASJMSTestAdmin(Class clazz) throws Exception
   {
      delegate = new JBossTestServices(clazz.getName());
      delegate.init();
   }
   
   public InitialContext createInitialContext() throws NamingException
   {
      try
      {
         return delegate.getInitialContext();
      }
      catch (NamingException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new NestedRuntimeException(e);
      }
   }

   protected MBeanServerConnection getServer() throws Exception
   {
      return delegate.getServer();
   }
}
