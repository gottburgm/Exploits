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
package org.jboss.jms.jndi;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * A provider adapter that uses properties.
 *
 * @author Peter Antman DN <peter.antman@dn.se>
 * @author  <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version <pre>$Revision: 81030 $</pre>
 */
public class JNDIProviderAdapter extends AbstractJMSProviderAdapter
{
   private static final long serialVersionUID = 8723565158472171136L;

   public Context getInitialContext() throws NamingException
   {
      if (properties == null)
         return new InitialContext();
      else
         return new InitialContext(properties);
   }
}
