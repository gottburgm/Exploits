/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.security.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

/**
 * The MBean interface for <code>JaasSecurityDomainServerSocketFactory</code>.
 * 
 * @author <a href="mmoyses@redhat.com">Marcus Moyses</a>
 * @version $Revision: 1 $
 */
public interface JaasSecurityDomainServerSocketFactoryMBean
{

   public void start() throws Exception;
   
   public void stop() throws Exception;
   
   public void create() throws Exception;
   
   public void destroy() throws Exception;
   
   public String getSecurityDomainName();
   
   public void setSecurityDomainName(String securityDomainName);
   
   public String[] getCipherSuites();
   
   public void setCipherSuites(String[] cipherSuites);
   
   public boolean isWantsClientAuth();
   
   public void setWantsClientAuth(boolean wantsClientAuth);
   
   public boolean isNeedsClientAuth();
   
   public void setNeedsClientAuth(boolean needsClientAuth);
   
   public ServerSocket createServerSocket() throws IOException;

   public ServerSocket createServerSocket(int i) throws IOException;

   public ServerSocket createServerSocket(int i, int i1) throws IOException;

   public ServerSocket createServerSocket(int i, int i1, InetAddress inetAddress) throws IOException;
}
