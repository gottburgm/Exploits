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

import java.util.ArrayList;
import java.util.List;

import org.jboss.deployment.DeploymentException;
import org.jboss.logging.Logger;
import org.jboss.mx.loading.LoaderRepositoryFactory;
import org.jboss.mx.loading.LoaderRepositoryFactory.LoaderRepositoryConfig;
import org.jboss.util.StringPropertyReplacer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * ServiceDeploymentParser
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class ServiceDeploymentParser
{
   /** The log */
   private static final Logger log = Logger.getLogger(ServiceDeploymentParser.class);
   
   /** The document */
   private Document document;
   
   /**
    * Create a new service deployment parser
    * 
    * @param document the xml config
    */
   public ServiceDeploymentParser(Document document)
   {
      if (document == null)
         throw new IllegalArgumentException("Null document");
      
      this.document = document;
   }

   /**
    * Parse the xml 
    * 
    * @return the service deployment
    * @throws DeploymentException for any error
    */
   public ServiceDeployment parse() throws DeploymentException
   {
      ServiceDeployment parsed = new ServiceDeployment();

      List<ServiceDeploymentClassPath> classPaths = parseXMLClasspath(document);
      parsed.setClassPaths(classPaths);

      LoaderRepositoryConfig repository = parseLoaderRepositoryConfig(document);
      if (repository != null)
         parsed.setLoaderRepositoryConfig(repository);

      // We can't parse the services yet, because it requires the classloader
      parsed.setConfig(document.getDocumentElement());
      return parsed;
   }

   /**
    * Parse the xml classpath
    * 
    * @param document the document
    * @return the list of classpaths
    * @throws DeploymentException for any error
    */
   private List<ServiceDeploymentClassPath> parseXMLClasspath(Document document) throws DeploymentException
   {
      ArrayList<ServiceDeploymentClassPath> classPaths = null;
      
      NodeList children = document.getDocumentElement().getChildNodes();
      for (int i = 0; i < children.getLength(); ++i)
      {
         if (children.item(i).getNodeType() == Node.ELEMENT_NODE)
         {
            Element classpathElement = (Element)children.item(i);
            if (classpathElement.getTagName().equals("classpath"))
            {
               log.debug("Found classpath element: " + classpathElement);
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
               
               if (classPaths == null)
                  classPaths = new ArrayList<ServiceDeploymentClassPath>();

               ServiceDeploymentClassPath classPath = new ServiceDeploymentClassPath(codebase, archives);
               classPaths.add(classPath);
            }
         }
      }
      return classPaths;
   }

   /**
    * Parse the loader repository config
    * 
    * @param document the document
    * @return the config
    * @throws DeploymentException for any error
    */
   private LoaderRepositoryConfig parseLoaderRepositoryConfig(Document document) throws DeploymentException
   {
      // Check for a custom loader-repository for scoping
      NodeList loaders = document.getElementsByTagName("loader-repository");
      if( loaders.getLength() > 0 )
      {
    	 if(loaders.getLength() > 1)
    	 	throw new DeploymentException("SAR Deployment cannot have more than one loader-repository entry.");    	  
         Element loader = (Element) loaders.item(0);
         try
         {
            return LoaderRepositoryFactory.parseRepositoryConfig(loader);
         }
         catch (Exception e)
         {
            throw DeploymentException.rethrowAsDeploymentException("Unable to parse loader repository config", e);
         }
      }
      return null;
   }
}
