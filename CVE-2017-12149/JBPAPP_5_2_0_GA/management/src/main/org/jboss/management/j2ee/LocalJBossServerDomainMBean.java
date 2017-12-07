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
package org.jboss.management.j2ee;

import javax.management.ObjectName;

/**
 * MBean interface.
 * 
 * @version $Revision: 81025 $
 */
public interface LocalJBossServerDomainMBean
{
   ObjectName getMainDeployer();
   void setMainDeployer(ObjectName name);

   ObjectName getSARDeployer();
   void setSARDeployer(ObjectName name);

   ObjectName getEARDeployer();
   void setEARDeployer(ObjectName name);

   ObjectName getEJBDeployer();
   void setEJBDeployer(ObjectName name);

   ObjectName getRARDeployer();
   void setRARDeployer(ObjectName name);

   ObjectName getCMDeployer();
   void setCMDeployer(ObjectName name);

   ObjectName getWARDeployer();
   void setWARDeployer(ObjectName name);

   ObjectName getCARDeployer();
   void setCARDeployer(ObjectName name);

   ObjectName getJMSService();
   void setJMSService(ObjectName name);

   ObjectName getJNDIService();
   void setJNDIService(ObjectName name);

   ObjectName getJTAService();
   void setJTAService(ObjectName name);

   ObjectName getMailService();
   void setMailService(ObjectName name);

   ObjectName getUserTransactionService();
   void setUserTransactionService(ObjectName name);

   ObjectName getRMI_IIOPService();
   void setRMI_IIOPService(ObjectName name);

   ObjectName getJndiBindingService();
   void setJndiBindingService(ObjectName name);

   Class getManagementObjFactoryMapClass();
   void setManagementObjFactoryMapClass(Class cls);

   public void create() throws Exception;
   public void destroy() throws Exception;
}
