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
package org.jboss.mx.remoting.provider.iiop;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorProvider;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnector;

/**
 * @author <a href="mailto:tom@jboss.org">Tom Elrod</a>
 */
public class ClientProvider implements JMXConnectorProvider
{
   public static final String PROTOCOL = "iiop";

   public ClientProvider()
   {

   }

   public JMXConnector newJMXConnector(JMXServiceURL serviceURL, Map environment)
         throws IOException
   {
      if(serviceURL != null && serviceURL.getProtocol() != null && serviceURL.getProtocol().equalsIgnoreCase(PROTOCOL))
      {
         return new RMIConnector(serviceURL, environment);
      }
      else
      {
         throw new MalformedURLException("JMXServiceURL provided is invalid for this provider.  " +
                                         "Protocol must be " + PROTOCOL + ".  JMXServiceURL provided is " +
                                         serviceURL);
      }
   }
}