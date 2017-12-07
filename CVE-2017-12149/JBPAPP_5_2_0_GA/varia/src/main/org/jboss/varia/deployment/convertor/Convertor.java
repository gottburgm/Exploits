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
package org.jboss.varia.deployment.convertor;

import java.io.File;
import java.net.URL;

import org.jboss.deployment.DeploymentInfo;

/**
 * Defines the methods of a Converter
 *
 * @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @version $Revision: 81038 $
 *
 * <p><b>20020519 Andreas Schaefer:</b>
 * <ul>
 *    <li>Creation</li>
 * </ul>
 */
public interface Convertor
{
   // Public --------------------------------------------------------
   /**
    * Checks if the a deployment unit can be converted to a JBoss deployment
    * unit by this converter.
    *
    * @param url The url of the deployment unit to be converted
    *
    * @return True if this converter is able to convert
    */
   public boolean accepts(URL url);

   /**
    * Converts the necessary files to make the given deployment deployable
    * into the JBoss
    *
    * @param di Deployment info to be converted
    * @param path Path of the extracted deployment
    **/
   public void convert(DeploymentInfo di, File path)
      throws Exception;
}
