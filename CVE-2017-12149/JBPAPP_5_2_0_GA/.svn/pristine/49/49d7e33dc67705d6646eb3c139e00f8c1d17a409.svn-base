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
package org.jboss.test.classloader.leak.ejb3;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.ejb3.annotation.Pool;
import org.jboss.ejb3.annotation.RemoteBinding;

/**
 * SLSB that uses ThreadLocalPool.
 * 
 * @author Brian Stansberry
 */
@Stateless(name="ThreadLocalPoolEjb3StatelessSession")
@Pool(value="ThreadlocalPool")
@RemoteBinding(jndiBinding="ThreadLocalPoolEjb3StatelessSession/remote")
@TransactionAttribute(value=TransactionAttributeType.REQUIRED)
public class ThreadLocalPoolEjb3StatelessSessionBean implements Ejb3StatelessSession
{
   private static final long serialVersionUID = 1L;

   public void log(String category)
   {
      Log log = LogFactory.getLog(category);
      log.info("Logging for " + getClass().getName());    
      org.jboss.test.classloader.leak.clstore.ClassLoaderStore.getInstance().storeClassLoader("EJB3_TLP_SLSB", ThreadLocalPoolEjb3StatelessSessionBean.class.getClassLoader());
      org.jboss.test.classloader.leak.clstore.ClassLoaderStore.getInstance().storeClassLoader("EJB3_TLP_SLSB_TCCL", Thread.currentThread().getContextClassLoader());
   }

}
