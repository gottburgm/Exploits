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
package org.jboss.security.srp;

import org.jboss.invocation.Invocation;
import org.jboss.system.ServiceMBean;

/**
 * The JMX mbean interface for the SRP service. This mbean sets up an
 * RMI implementation of the 'Secure Remote Password' cryptographic authentication
 * system developed by Tom Wu (tjw@CS.Stanford.EDU). For more info on SRP
 * see http://www-cs-students.stanford.edu/~tjw/srp/.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81038 $
 */
public interface SRPServiceMBean
   extends ServiceMBean
{
   /**
    * Get the jndi name for the SRPVerifierSource implementation binding.
    */
   String getVerifierSourceJndiName();
   
   /**
    * set the jndi name for the SRPVerifierSource implementation binding.
    */
   void setVerifierSourceJndiName(String jndiName);
   
   /**
    * Get the jndi name under which the SRPServerInterface proxy should be bound
    */
   String getJndiName();
   
   /**
    * Set the jndi name under which the SRPServerInterface proxy should be bound
    */
   void setJndiName(String jndiName);
   
   /**
    * Get the jndi name under which the SRPServerInterface proxy should be bound
    */
   String getAuthenticationCacheJndiName();
   
   /**
    * Set the jndi name under which the SRPServerInterface proxy should be bound
    */
   void setAuthenticationCacheJndiName(String jndiName);

   /**
    * Get the auth cache timeout period in seconds
    */
   int getAuthenticationCacheTimeout();
   
   /**
    * Set the auth cache timeout period in seconds
    */
   void setAuthenticationCacheTimeout(int timeoutInSecs);
   
   /**
    * Get the auth cache resolution period in seconds
    */
   int getAuthenticationCacheResolution();
   
   /**
    * Set the auth cache resolution period in seconds
    */
   void setAuthenticationCacheResolution(int resInSecs);

   /** Get if the client must supply an auxillary challenge as part of the
    * verify phase.
    */
   public boolean getRequireAuxChallenge();
   /** Set if the client must supply an auxillary challenge as part of the
    * verify phase.
    */
   public void setRequireAuxChallenge(boolean flag);

   /** A flag indicating if a successful user auth for an existing session
    should overwrite the current session.
    */
   public boolean getOverwriteSessions();
   /** Set the flag indicating if a successful user auth for an existing session
    should overwrite the current session.
    */
   public void setOverwriteSessions(boolean flag);

   /**
    * Get the RMIClientSocketFactory implementation class. If null the default
    * RMI client socket factory implementation is used.
    */
   String getClientSocketFactory();
   
   /**
    * Set the RMIClientSocketFactory implementation class. If null the default
    * RMI client socket factory implementation is used.
    */
   void setClientSocketFactory(String factoryClassName)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException;
   
   /**
    * Get the RMIServerSocketFactory implementation class. If null the default
    * RMI server socket factory implementation is used.
    */
   String getServerSocketFactory();
   
   /**
    * Set the RMIServerSocketFactory implementation class. If null the default
    * RMI server socket factory implementation is used.
    */
   void setServerSocketFactory(String factoryClassName)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException;
   
   /**
    * Get the RMI port for the SRPRemoteServerInterface
    */
   int getServerPort();
   
   /**
    * Set the RMI port for the SRPRemoteServerInterface
    */
   void setServerPort(int port);

   /** Expose the Invoker signature via JMX
   */
   public Object invoke(Invocation invocation) throws Exception;
}
