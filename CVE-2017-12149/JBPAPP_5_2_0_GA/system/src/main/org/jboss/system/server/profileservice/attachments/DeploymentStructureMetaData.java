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
package org.jboss.system.server.profileservice.attachments;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

/**
 * Basic xml representation helper class of the StructureMetaData.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 85526 $
 */
public class DeploymentStructureMetaData
{

   /** The meta data path */
   private List<String> metaDataPaths;
   
   /** The class paths */
   private List<DeploymentClassPathMetaData> classPaths;
   
   /** The comparator class name */
   private String comparatorClass;
   
   /** The modifcation type */
   private String modificationType;
   
   /** The relative order */
   private int relatativeOrder;
 
   
   @XmlElement(name = "meta-data-path")
   public List<String> getMetaDataPaths()
   {
      return metaDataPaths;
   }
   
   public void setMetaDataPaths(List<String> metaDataPaths)
   {
      this.metaDataPaths = metaDataPaths;
   }
   
   @XmlElement(name = "class-path")
   public List<DeploymentClassPathMetaData> getClassPaths()
   {
      return classPaths;
   }
   
   public void setClassPaths(List<DeploymentClassPathMetaData> classPaths)
   {
      this.classPaths = classPaths;
   }

   @XmlElement(name = "comparator-class")
   public String getComparatorClass()
   {
      return comparatorClass;
   }
   
   public void setComparatorClass(String comparatorClass)
   {
      this.comparatorClass = comparatorClass;
   }
   
   @XmlElement(name = "relative-order")
   public int getRelatativeOrder()
   {
      return relatativeOrder;
   }
   
   public void setRelatativeOrder(int relatativeOrder)
   {
      this.relatativeOrder = relatativeOrder;
   }
   
   @XmlElement(name = "modifcation-type")
   public String getModificationType()
   {
      return modificationType;
   }
   
   public void setModificationType(String modificationType)
   {
      this.modificationType = modificationType;
   }

}
