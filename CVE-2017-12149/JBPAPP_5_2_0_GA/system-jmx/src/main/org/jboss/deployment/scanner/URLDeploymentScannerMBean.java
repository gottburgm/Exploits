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
package org.jboss.deployment.scanner;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.jboss.net.protocol.URLLister.URLFilter;

/**
 * URLDeploymentScanner MBean interface.
 * 
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81033 $
 */
public interface URLDeploymentScannerMBean extends DeploymentScannerMBean
{
   // Attributes ----------------------------------------------------
   
   void setRecursiveSearch(boolean recurse);
   boolean getRecursiveSearch();

   void setURLList(List list);
   List getURLList();
   
   void setURLComparator(String classname)
      throws ClassNotFoundException, IllegalAccessException, InstantiationException;
   String getURLComparator();

   void setFilter(String classname)
      throws ClassNotFoundException, IllegalAccessException, InstantiationException;
   String getFilter();

   void setFilterInstance(URLFilter filter);
   URLFilter getFilterInstance();

   void setURLs(String listspec) throws MalformedURLException;
   
   // Operations ----------------------------------------------------

   void addURL(URL url);

   void removeURL(URL url);

   boolean hasURL(URL url);

   void addURL(String urlspec) throws MalformedURLException;

   void removeURL(String urlspec) throws MalformedURLException;

   boolean hasURL(String urlspec) throws MalformedURLException;
   
   /**      
    * Temporarily ignore changes (addition, updates, removal) to a particular
    * deployment, identified by its deployment URL. The deployment URL is different
    * from the 'base' URLs that are scanned by the scanner (e.g. the full path to
    * deploy/jmx-console.war vs. deploy/). This can be used to avoid an attempt
    * by the scanner to deploy/redeploy/undeploy a URL that is being modified.
    * 
    * To re-enable scanning of changes for a URL, use resumeDeployment(URL, boolean).
    */      
   void suspendDeployment(URL url);

   /**
    * Re-enables scanning of a particular deployment URL, previously suspended
    * using suspendDeployment(URL). If the markUpToDate flag is true then the
    * deployment module will be considered up-to-date during the next scan.
    * If the flag is false, at the next scan the scanner will check the
    * modification date to decide if the module needs deploy/redeploy/undeploy.
    */
   void resumeDeployment(URL url, boolean markUpToDate);

   /**
    * Lists all urls deployed by the scanner, each URL on a new line.
    */
   public String listDeployedURLs();

}
