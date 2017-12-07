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
package org.jboss.resource.adapter.jdbc.remote;

import org.jboss.invocation.Invocation;
import org.jboss.proxy.Interceptor;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:telrod@e2technologies.net">Tom Elrod</a>
 * @version $Revision: 71554 $
 */
public class StatementInterceptor extends Interceptor
{
   static final long serialVersionUID = -8069364664602119774L;

   public Object invoke(Invocation mi) throws Throwable
   {
      Method m = mi.getMethod();
      String methodName = m.getName();

      if (methodName.equals("setAsciiStream") ||
            methodName.equals("setBinaryStream"))
      {
         Object[] args = mi.getArguments();
         InputStream ins = (InputStream) args[1];
         args[1] = new SerializableInputStream(ins);
      }
      else if (methodName.equals("setCharacterStream"))
      {
         Object[] args = mi.getArguments();
         Reader rdr = (Reader) args[1];
         args[1] = new SerializableReader(rdr);
      }

      return getNext().invoke(mi);
   }
}
