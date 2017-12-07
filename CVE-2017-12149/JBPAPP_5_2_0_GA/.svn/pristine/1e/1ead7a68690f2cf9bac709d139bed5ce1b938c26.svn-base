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
package org.jboss.system.tools;

import java.net.URL;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

/**
 * Deployment api for tools.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface DeploymentScannerMBean
{
   /**
    * Enable scanning.
    */
   void start();

   /**
    * Disable scanning.
    */
   void stop();

   /**
    * Add url to scanner.
    *
    * @param url the url
    * @throws MalformedURLException for any error
    * @throws URISyntaxException for any error
    */
   void addURL(String url) throws MalformedURLException, URISyntaxException;

   /**
    * Add url to scanner.
    *
    * @param url the url
    * @throws URISyntaxException for any error
    */
   void addURL(URL url) throws URISyntaxException;

   /**
    * Add url to scanner.
    *
    * @param url the url
    * @throws MalformedURLException for any error
    * @throws URISyntaxException for any error
    */
   void removeURL(String url) throws MalformedURLException, URISyntaxException;

   /**
    * Add url to scanner.
    *
    * @param url the url
    * @throws URISyntaxException for any error
    */
   void removeURL(URL url) throws URISyntaxException;

   /**
    * Does this repository contain a url.
    *
    * @param url the url
    * @return true if the url is contained
    * @throws MalformedURLException for any error
    * @throws URISyntaxException for any error
    */
   boolean hasURL(String url) throws MalformedURLException, URISyntaxException;

   /**
    * Does this repository contain a url.
    *
    * @param url the url
    * @return true if the url is contained
    * @throws URISyntaxException for any error
    */
   boolean hasURL(URL url) throws URISyntaxException;

   /**
    * List deployed urls as strings.
    *
    * @return the list of deployed urls
    */
   String[] listDeployedURLs();
}