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
package org.jboss.varia.deployment;

/**
 * MBean interface.
 * @see org.jboss.varia.deployment.convertor.Convertor
 */
public interface FoeDeployerMBean extends org.jboss.deployment.SubDeployerMBean {

   //default object name
   public static final javax.management.ObjectName OBJECT_NAME = org.jboss.mx.util.ObjectNameFactory.create("jboss.system:service=ServiceDeployer");

   /**
    * Returns true if the there is a converter available to convert the deployment unit.
    */
  boolean accepts(org.jboss.deployment.DeploymentInfo di) ;

   /**
    * At the init phase the deployment unit and its subdeployment units are unpacked.
    */
  void init(org.jboss.deployment.DeploymentInfo di) throws org.jboss.deployment.DeploymentException;

   /**
    * At the create phase, the conversion and packing is done.
    */
  void create(org.jboss.deployment.DeploymentInfo di) throws org.jboss.deployment.DeploymentException;

   /**
    * This method stops this deployment because it is not of any use anymore (conversion is done)
    */
  void start(org.jboss.deployment.DeploymentInfo di) throws org.jboss.deployment.DeploymentException;

  void stop(org.jboss.deployment.DeploymentInfo di) ;

  void destroy(org.jboss.deployment.DeploymentInfo di) ;

   /**
    * Add a new conveter to the list. If the same converter is added, this new one won't be added, meaning everything stays the same. This method is normally called by a Converter to be called by this deployer to convert.
    * @param converter New Converter to be added
    */
  void addConvertor(org.jboss.varia.deployment.convertor.Convertor converter) ;

   /**
    * Removes a conveter from the list of converters. If the converter does not exist nothing happens. This method is normally called by a Converter to be removed from the list if not serving anymore.
    * @param converter Conveter to be removed from the list
    */
  void removeConvertor(org.jboss.varia.deployment.convertor.Convertor converter) ;

}
