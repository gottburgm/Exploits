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
package org.jboss.system.metadata;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jboss.mx.loading.LoaderRepositoryFactory;
import org.jboss.mx.loading.LoaderRepositoryFactory.LoaderRepositoryConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A LoaderRepositoryAdapter.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 85945 $
 */
public class ServiceLoaderRepositoryAdapter extends XmlAdapter<Object, LoaderRepositoryConfig>
{
   
   @Override
   public LoaderRepositoryConfig unmarshal(Object e) throws Exception
   {
      return LoaderRepositoryFactory.parseRepositoryConfig((Element) e);
   }

   @Override
   public Element marshal(LoaderRepositoryConfig config) throws Exception
   {
      if(config == null)
         return null;
      
      // TODO: move this to a separate 'marshaler' 
      Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      // <loader-repository>
      Element loaderRepository = document.createElement("loader-repository");
      loaderRepository.setAttribute("loaderRepositoryClass", config.repositoryClassName);
      loaderRepository.setTextContent(config.repositoryName.getCanonicalName());
      // <loader-repository-config>
      Element loaderRepositoryConfig = document.createElement("loader-repository-config");
      loaderRepositoryConfig.setAttribute("configParserClass", config.configParserClassName);
      loaderRepositoryConfig.setTextContent(config.repositoryConfig);
      // Append <loader-repository-config/>
      loaderRepository.appendChild(loaderRepositoryConfig);
      //
      return loaderRepository;
   }
}
