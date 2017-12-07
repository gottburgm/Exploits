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

import org.jboss.xb.binding.ObjectModelFactory;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;

/**
 * A simple subdeployer that deploys a managed object after parsing the 
 * deployment's xml file using an ObjectModelFactory.
 *
 * @author  <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81033 $
 */
public abstract class ObjectModelFactorySimpleSubDeployerSupport extends SimpleSubDeployerSupport
{
   /**
    * Get the object model factory 
    * 
    * @return the object model factory
    */
   public abstract ObjectModelFactory getObjectModelFactory();

   protected void parseMetaData(DeploymentInfo di, URL url) throws DeploymentException
   {
      try
      {
         Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
         ObjectModelFactory factory = getObjectModelFactory();
         Object root = null;
         di.metaData = unmarshaller.unmarshal(url.toString(), factory, root);
      }
      catch (Throwable t)
      {
         DeploymentException.rethrowAsDeploymentException("Error parsing meta data " + url, t);
      }
   }
}
