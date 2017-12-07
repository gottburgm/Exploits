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
package javax.management.remote;

import java.io.IOException;

/**
 * @author <a href="mailto:tom@jboss.org">Tom Elrod</a>
 */
public class JMXProviderException extends IOException
{
   private static final long serialVersionUID = -3166703627550447198l;

   private Throwable cause = null;

   public JMXProviderException()
   {
      super();
   }

   public JMXProviderException(String message)
   {
      super(message);
   }

   public JMXProviderException(String message, Throwable cause)
   {
      super(message);
      this.cause = cause;
   }

   public Throwable getCause()
   {
      return cause;
   }
}
