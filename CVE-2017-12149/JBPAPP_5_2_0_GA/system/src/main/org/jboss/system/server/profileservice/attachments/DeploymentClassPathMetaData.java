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

import javax.xml.bind.annotation.XmlElement;

/**
 * The DeploymentClassPathMetaData is the xml representation
 * of the ClassPathEntry in ContextInfo.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 85526 $
 */
public class DeploymentClassPathMetaData 
{

   /** The path */
   private String path;
   
   /** The suffixes */
   private String suffixes;
   
   public DeploymentClassPathMetaData()
   {
      //
   }
   
   public DeploymentClassPathMetaData(String path, String suffixes)
   {
      this.path = path;
      this.suffixes = suffixes;
   }
   
   @XmlElement(name = "path")
   public String getPath()
   {
      return path;
   }
   
   public void setPath(String path)
   {
      this.path = path;
   }
   
   @XmlElement(name = "suffixes")
   public String getSuffixes()
   {
      return suffixes;
   }
   
   public void setSuffixes(String suffixes)
   {
      this.suffixes = suffixes;
   }

}
