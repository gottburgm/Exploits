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
package org.jboss.system.metadata;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jboss.deployment.DeploymentException;
import org.jboss.util.StringPropertyReplacer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 85945 $
 */
public class ServiceDeploymentClasspathAdapter extends XmlAdapter<Object, ServiceDeploymentClassPath>
{

   @Override
   public ServiceDeploymentClassPath unmarshal(Object e) throws Exception
   {
      Element classpathElement = (Element) e;
      if (classpathElement.hasAttribute("codebase") == false)
         throw new DeploymentException("Invalid classpath element missing codebase: " + classpathElement);

      String codebase = classpathElement.getAttribute("codebase").trim();
      codebase = StringPropertyReplacer.replaceProperties(codebase);

      String archives = null;
      if (classpathElement.hasAttribute("archives"))
      {
         archives = classpathElement.getAttribute("archives").trim();
         archives = StringPropertyReplacer.replaceProperties(archives);
         if ("".equals(archives))
            archives = null;
      }

      return new ServiceDeploymentClassPath(codebase, archives);
   }

   @Override
   public Object marshal(ServiceDeploymentClassPath path) throws Exception
   {
      if(path == null)
         return null;
      
      Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      Element classPath = document.createElement("classpath");
      classPath.setAttribute("codebase", path.getCodeBase());
      classPath.setAttribute("archives", path.getArchives());
      return classPath;
   }
}

