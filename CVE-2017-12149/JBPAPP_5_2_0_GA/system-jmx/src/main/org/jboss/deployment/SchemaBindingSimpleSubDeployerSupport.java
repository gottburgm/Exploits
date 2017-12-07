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
package org.jboss.deployment;

import java.net.URL;

import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBinding;

/**
 * A simple subdeployer that deploys a managed object after parsing the 
 * deployment's xml file using an SchemaBinding.
 *
 * @author  <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81033 $
 */
public abstract class SchemaBindingSimpleSubDeployerSupport extends SimpleSubDeployerSupport
{
   /** The unmarshaller factory */
   private UnmarshallerFactory factory = UnmarshallerFactory.newInstance();
   
   /**
    * Get the schema binding 
    * 
    * @return the schema binding
    */
   public abstract SchemaBinding getSchemaBinding();

   protected void parseMetaData(DeploymentInfo di, URL url) throws DeploymentException
   {
      try
      {
         Unmarshaller unmarshaller = factory.newUnmarshaller();
         di.metaData = unmarshaller.unmarshal(url.toString(), getSchemaBinding());
         if (di.metaData == null)
            throw new RuntimeException("The xml " + url + " is not well formed!");
      }
      catch (Throwable t)
      {
         DeploymentException.rethrowAsDeploymentException("Error parsing meta data " + url, t);
      }
   }
}
