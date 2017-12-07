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
package org.jboss.resource.connectionmanager;

import javax.management.ObjectName;
import javax.resource.spi.ManagedConnectionFactory;

import org.jboss.mx.util.ObjectNameFactory; 
import org.jboss.security.SubjectFactory; 
import org.jboss.system.ServiceMBean;

/**
 * MBean interface.
 * 
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @author Anil.Saldhana@redhat.com
 * @version $Revision: 80828 $
 */
public interface BaseConnectionManager2MBean extends ServiceMBean
{
   public static final ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.jca:service=BaseConnectionManager");

   /**
    * The JndiName attribute holds the jndi name the ConnectionFactory will be bound under in jndi. Note that an entry of the form DefaultDS2 will be bound to java:/DefaultDS2.
    * 
    * @return the JndiName value.
    */
   String getJndiName();

   /**
    * Set the JndiName value.
    * 
    * @param jndiName The JndiName value.
    */
   void setJndiName(String jndiName);

   /**
    * The ManagedConnectionPool holds the ObjectName of the mbean representing the pool for this connection manager. Normally it will be an embedded mbean in a depends tag rather than an ObjectName reference to the mbean.
    * 
    * @return the ManagedConnectionPool value.
    */
   ObjectName getManagedConnectionPool();

   /**
    * Set the ManagedConnectionPool value.
    * 
    * @param newManagedConnectionPool The new ManagedConnectionPool value.
    */
   void setManagedConnectionPool(ObjectName newManagedConnectionPool);

   /**
    * The CachecConnectionManager holds the ObjectName of the CachedConnectionManager mbean used by this ConnectionManager. Normally this will be a depends tag with the ObjectName of the unique CachedConnectionManager for the server.
    * 
    * @param ccmName an <code>ObjectName</code> value
    */
   void setCachedConnectionManager(ObjectName ccmName);

   /**
    * Describe <code>getCachedConnectionManager</code> method here.
    * 
    * @return an <code>ObjectName</code> value
    */
   ObjectName getCachedConnectionManager();

   /**
    * The SecurityDomainJndiName holds the jndi name of the security domain configured for the ManagedConnectionFactory this ConnectionManager manages. It is normally of the form java:/jaas/firebirdRealm, where firebirdRealm is the name found in auth.conf or equivalent file.
    * 
    * @param securityDomainJndiName an <code>String</code> value
    */
   void setSecurityDomainJndiName(String securityDomainJndiName);

   /**
    * Get the SecurityDomainJndiName value.
    * 
    * @return the SecurityDomainJndiName value.
    */
   String getSecurityDomainJndiName();

   /**
    * Get the JaasSecurityManagerService value.
    * 
    * @return the JaasSecurityManagerService value.
    * @deprecated
    */
   ObjectName getJaasSecurityManagerService();

   /**
    * Set the JaasSecurityManagerService value.
    * 
    * @param jaasSecurityManagerService The new JaasSecurityManagerService value.
    * @deprecated
    */
   void setJaasSecurityManagerService(ObjectName jaasSecurityManagerService); 
   
   /**
    * Inject SubjectFactory to create Subjects
    * @param subjectFactory
    */
   void setSubjectFactory(SubjectFactory subjectFactory);
   
   /**
    * ManagedConnectionFactory is an internal attribute that holds the ManagedConnectionFactory instance managed by this ConnectionManager.
    * 
    * @return value of managedConnectionFactory
    */
   ManagedConnectionFactory getManagedConnectionFactory();

   /**
    * Describe <code>getInstance</code> method here.
    * 
    * @return a <code>BaseConnectionManager2</code> value
    */
   BaseConnectionManager2 getInstance();

   /**
    * Set the number of allocation retries
    * @param number
    */
   void setAllocationRetry(int number);

   /**
    * Get the number of allocation retries
    * @return The number of retries
    */
   int getAllocationRetry();

   /**
    * Set the wait time between each allocation retry
    * @param millis
    */
   void setAllocationRetryWaitMillis(long millis);

   /**
    * Get the wait time between each allocation retry
    * @return The millis
    */
   long getAllocationRetryWaitMillis();
}