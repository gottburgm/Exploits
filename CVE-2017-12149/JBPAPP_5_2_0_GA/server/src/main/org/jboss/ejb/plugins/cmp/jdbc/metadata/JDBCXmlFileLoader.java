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
package org.jboss.ejb.plugins.cmp.jdbc.metadata;

import java.net.URL;
import org.jboss.deployment.DeploymentException;
import org.jboss.logging.Logger;
import org.jboss.metadata.XmlFileLoader;
import org.jboss.ejb.Container;
import org.jboss.virtual.VirtualFile;
import org.w3c.dom.Element;

/**
 * Immutable class which loads the JDBC application meta data from the jbosscmp-jdbc.xml files.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 *   @author <a href="sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @author <a href="mailto:alex@jboss.com">Alexey Loubyansky</a>
 *   @version $Revision: 81030 $
 */
public final class JDBCXmlFileLoader {
   private final Container container;
   private final Logger log;
   
   /**
    * Constructs a JDBC XML file loader, which loads the JDBC application meta data from
    * the jbosscmp-xml files.
    *
    * @param con  the container
    * @param log the log for this application
    */
   public JDBCXmlFileLoader(Container con, Logger log)
   {
      this.container = con;
      this.log = log;
   }

   /**
    * Loads the application meta data from the jbosscmp-jdbc.xml file
    *
    * @return the jdbc application meta data loaded from the jbosscmp-jdbc.xml files
    */
   public JDBCApplicationMetaData load() throws DeploymentException {
      JDBCApplicationMetaData jamd = new JDBCApplicationMetaData(
         container.getBeanMetaData().getApplicationMetaData(), container.getClassLoader()
      );
      
      // Load standardjbosscmp-jdbc.xml from the default classLoader
      // we always load defaults first
      URL stdJDBCUrl = container.getClassLoader().getResource("standardjbosscmp-jdbc.xml");
      if(stdJDBCUrl == null) {
         throw new DeploymentException("No standardjbosscmp-jdbc.xml found");
      }

      boolean debug = log.isDebugEnabled();
      if (debug)
         log.debug("Loading standardjbosscmp-jdbc.xml : " + stdJDBCUrl.toString());
      Element stdJDBCElement = XmlFileLoader.getDocument(stdJDBCUrl, true).getDocumentElement();

      // first create the metadata
      jamd = new JDBCApplicationMetaData(stdJDBCElement, jamd);

      // Load jbosscmp-jdbc.xml if provided
      URL jdbcUrl = null;
      VirtualFile dd = container.getDeploymentUnit().getMetaDataFile("jbosscmp-jdbc.xml");
      if(dd != null)
      {
         try
         {
            jdbcUrl = dd.toURL();
         }
         catch(Exception e)
         {
            throw new IllegalStateException("Failed to create URL for " + dd.getPathName(), e);
         }
      }

      if(jdbcUrl != null)
      {
         if (debug)
            log.debug(jdbcUrl.toString() + " found. Overriding defaults");
         Element jdbcElement = XmlFileLoader.getDocument(jdbcUrl, true).getDocumentElement();
         jamd = new JDBCApplicationMetaData(jdbcElement, jamd);
      }

      return jamd;
   }
}
