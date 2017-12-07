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
package org.jboss.management.j2ee.factory;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployment.DeploymentInfo;

/**
 * Common management factory utilities 
 *
 * @author Dimitris.Andreadis@jboss.org
 * @version $Revision: 85945 $
 */
public final class FactoryUtils
{
   /**
    * For a given DeploymentInfo instance return the shortname of the
    * parent .ear, if one exists, or null.
    * 
    * @param di the module 
    * @return .ear parent shortname, or null
    */
   public static String findEarParent(DeploymentInfo di)
   {
      if (di.parent != null && di.parent.shortName.endsWith(".ear"))
      {
         return di.parent.shortName;
      }
      else
      {
         return null;
      }
   }

   /**
    * For a given DeploymentUnit instance return the shortname of the
    * parent .ear, if one exists, or null.
    *
    * @param di the module
    * @return .ear parent shortname, or null
    */
   public static String findEarParent(DeploymentUnit di)
   {
      DeploymentUnit parent = di.getParent();
      if (parent != null && parent.getSimpleName().endsWith(".ear"))
      {
         return parent.getSimpleName();
      }
      else
      {
         return null;
      }
   }
}
