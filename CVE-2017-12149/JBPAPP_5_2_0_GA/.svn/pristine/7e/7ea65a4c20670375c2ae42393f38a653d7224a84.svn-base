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
package org.jboss.ejb;

import java.util.Iterator;

import javax.management.ObjectName;

import org.jboss.deployment.SubDeployerExtMBean;
import org.jboss.mx.util.ObjectNameFactory;

/**
 * EJBDeployer MBean interface.
 */
public interface EJBDeployerMBean extends SubDeployerExtMBean
{
   /** The default ObjectName */
   ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.ejb:service=EJBDeployer");

   // Attributes ----------------------------------------------------
   
   /**
    * Whether ear deployments should be call by value
    */
   void setCallByValue(boolean callByValue);   
   boolean isCallByValue();

   /**
    * Enables/disables the application bean verification upon deployment.
    */
   void setVerifyDeployments(boolean verify);
   boolean getVerifyDeployments();
   
   /**
    * Enables/disables the verbose mode on the verifier.
    */
   void setVerifierVerbose(boolean verbose);
   boolean getVerifierVerbose();   

   /**
    * Enables/disables the strict mode on the verifier.
    */
   void setStrictVerifier(boolean strictVerifier);
   boolean getStrictVerifier();
   
   /**
    * Enables/disables the metrics interceptor for containers.
    */
   void setMetricsEnabled(boolean enable);
   boolean isMetricsEnabled();
   
   /**
    * Enables/disables the flag indicating that ejb-jar.dtd, jboss.dtd
    * and jboss-web.dtd conforming documents should be validated against the DTD.
    */
   void setValidateDTDs(boolean validate);
   boolean getValidateDTDs();
   
   /**
    * The dynamic class loading simple web server name.
    */
   void setWebServiceName(ObjectName webServiceName);
   ObjectName getWebServiceName();
   
   /**
    * The TransactionManagerServiceName.
    */
   void setTransactionManagerServiceName(ObjectName transactionManagerServiceName);
   ObjectName getTransactionManagerServiceName();

   // Operations ----------------------------------------------------
   
   /**
    * Returns the deployed applications.
    * 
    * @return an iterator over DeploymentInfo instances
    */
   Iterator listDeployedApplications();
   
}
