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
package org.jboss.aspects.versioned;

import org.jboss.aop.Advised;
import org.jboss.aop.proxy.ClassProxy;
import org.jboss.aop.proxy.ClassProxyFactory;
import org.jboss.aop.util.PayloadKey;

/**
 *  This interceptor handles chooses an object to invoke
 *  on based on the transaction
 *
 *  @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 *  @version $Revision: 80997 $
 */
public class Versioned
{
   public static final String VERSIONED = "VERSIONED";
   public static final String VERSIONED_OBJECT = "VERSIONED_OBJECT";
   public static final String READONLY = "read-only";

   public static Object makeVersioned(Object target)
      throws Exception
   {
      if (target instanceof ClassProxy) throw new IllegalStateException("A ClassProxy is not allowed to be Versioned.  You must have a concrete object");
      ClassProxy proxy = ClassProxyFactory.newInstance(target.getClass());
      VersionedObject versioned = new VersionedObject(target);
      proxy._getInstanceAdvisor().getMetaData().addMetaData(VERSIONED, VERSIONED_OBJECT, versioned, PayloadKey.TRANSIENT);
      proxy._getInstanceAdvisor().appendInterceptor(VersionedObjectInterceptorFactory.getInstance());
      proxy._getInstanceAdvisor().appendInterceptor(new VersionedObjectForwardingInterceptor());
      return proxy;
   }

   static DistributedVersionManager localManager = null;
   public static synchronized DistributedVersionManager getLocalVersionManager() throws Exception
   {
      if (localManager == null)
      {
         LocalSynchronizationManager synchManager = new LocalSynchronizationManager(null);
         localManager = new DistributedVersionManager(1000, synchManager);
         synchManager.versionManager = localManager;
         
      }
      return localManager;
   }

   public static void makePerFieldVersioned(Object target) throws Exception
   {
      if (!(target instanceof Advised)) throw new IllegalArgumentException("Cannot do per field versioning with a non-Advised object");
      Advised advised = (Advised)target;
      getLocalVersionManager().makeVersioned(advised);
   }
   
}
