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

package org.jboss.test.services.binding.test;

import org.jboss.services.binding.ServiceBinding;
import org.jboss.services.binding.ServiceBindingValueSource;

/**
 * Mock implementation of ServiceBindingValueSource. Returms an injected return
 * value; keeps a reference to passed in params so the test driver can analyze them.
 * 
 * @author Brian Stansberry
 * @version $Revision: 85945 $
 */
public class MockServiceBindingValueSource implements ServiceBindingValueSource
{
   private final Object returnValue;
   private Object[] params;
   
   public MockServiceBindingValueSource()
   {
      this.returnValue = null;
   }
   
   public MockServiceBindingValueSource(Object returnValue)
   {
      this.returnValue = returnValue;
   }

   public Object getServiceBindingValue(ServiceBinding binding, Object... params)
   {
      this.params = params;
      return returnValue;
   }

   public Object[] getParams()
   {
      return params;
   }

}
