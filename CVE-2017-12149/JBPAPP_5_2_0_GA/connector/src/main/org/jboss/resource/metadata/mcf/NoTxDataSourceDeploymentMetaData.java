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
package org.jboss.resource.metadata.mcf;

import javax.xml.bind.annotation.XmlType;

import org.jboss.managed.api.annotation.ManagementComponent;
import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementProperty;


/**
 * A NoTxDataSourceDeploymentMetaData.
 * 
 * @author <a href="weston.price@jboss.org">Weston Price</a>
 * @version $Revision: 85945 $
 */
@XmlType(name="no-tx-datasource")
@ManagementObject(componentType=@ManagementComponent(type="DataSource",subtype="NoTx"))
public class NoTxDataSourceDeploymentMetaData extends NonXADataSourceDeploymentMetaData
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -5354506847562754007L;
   
   @Override
   @ManagementProperty(ignored=true)
   public ManagedConnectionFactoryTransactionSupportMetaData getTransactionSupportMetaData()
   {
      return ManagedConnectionFactoryTransactionSupportMetaData.NONE;
      
   }
   

}
