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
package org.jboss.test.server.profileservice.component.persistence.support;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Map;

import org.jboss.managed.api.ComponentType;
import org.jboss.managed.api.ManagedCommon;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.RunState;
import org.jboss.managed.plugins.DelegateManagedCommonImpl;

/**
 * A test managed component, to have access to the ManagedObject over
 * the getParent() method.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class TestMgtComponentImpl extends DelegateManagedCommonImpl
   implements ManagedComponent, Serializable
{

   private ManagedObject mo;
   
   public TestMgtComponentImpl(ManagedObject delegate)
   {
      super(delegate);
      this.mo = delegate;
   }
   
   @Override
   public ManagedCommon getParent()
   {
      return mo;
   }

   public Map<String, Annotation> getAnnotations()
   {
      // FIXME getAnnotations
      return null;
   }

   public ManagedDeployment getDeployment()
   {
      // FIXME getDeployment
      return null;
   }

   public RunState getRunState()
   {
      // FIXME getRunState
      return null;
   }

   public ComponentType getType()
   {
      // FIXME getType
      return null;
   }

   public boolean update()
   {
      // FIXME update
      return false;
   }


}

