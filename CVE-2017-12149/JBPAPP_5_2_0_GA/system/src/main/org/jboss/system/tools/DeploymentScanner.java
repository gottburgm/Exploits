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

import org.jboss.aop.microcontainer.aspects.jmx.JMX;
import org.jboss.beans.metadata.api.annotations.Start;
import org.jboss.beans.metadata.api.annotations.Stop;
import org.jboss.util.StringPropertyReplacer;
import org.jboss.bootstrap.spi.ServerConfig;

/**
 * Deployment scanner impl - hooking into ProfileService.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@JMX(name = "jboss.deployment:flavor=URL,type=DeploymentScanner", exposedInterface = DeploymentScannerMBean.class, registerDirectly = true)
public class DeploymentScanner implements DeploymentScannerMBean
{
   private ServerConfig serverConfig;
   private URL serverHomeURL;

   private DeploymentRepositoryAdapter adapter;

   public DeploymentScanner(DeploymentRepositoryAdapter adapter)
   {
      if (adapter == null)
         throw new IllegalArgumentException("Null adapter");

      this.adapter = adapter;
   }

   /**
    * Create server home.
    */
   public void create()
   {
      if (serverConfig == null)
         throw new IllegalArgumentException("Null server config.");

      serverHomeURL = serverConfig.getServerHomeURL();
   }

   @Start(ignored = true)
   public void start()
   {
      adapter.resume();
   }

   @Stop(ignored = true)
   public void stop()
   {
      adapter.suspend();
   }

   public void addURL(String url) throws MalformedURLException, URISyntaxException
   {
      if (url == null)
         throw new IllegalArgumentException("Null url");

      addURL(makeURL(url));
   }

   public void addURL(URL url) throws URISyntaxException
   {
      if (url == null)
         throw new IllegalArgumentException("Null url");

      adapter.addURL(url);
   }

   public void removeURL(String url) throws MalformedURLException, URISyntaxException
   {
      if (url == null)
         throw new IllegalArgumentException("Null url");

      removeURL(makeURL(url));
   }

   public void removeURL(URL url) throws URISyntaxException
   {
      if (url == null)
         throw new IllegalArgumentException("Null url");

      adapter.removeURL(url);
   }

   public boolean hasURL(String url) throws MalformedURLException, URISyntaxException
   {
      if (url == null)
         throw new IllegalArgumentException("Null url");

      return hasURL(makeURL(url));
   }

   public boolean hasURL(URL url) throws URISyntaxException
   {
      if (url == null)
         throw new IllegalArgumentException("Null url");

      return adapter.hasURL(url);
   }

   public String[] listDeployedURLs()
   {
      return adapter.listDeployedURLs();
   }

   /**
    * A helper to make a URL from a full url, or a filespec.
    *
    * @param urlspec the url string
    * @return url based on server home
    * @throws MalformedURLException for any error
    */
   protected URL makeURL(String urlspec) throws MalformedURLException
   {
      // First replace URL with appropriate properties
      urlspec = StringPropertyReplacer.replaceProperties(urlspec);
      return new URL(serverHomeURL, urlspec);
   }

   /**
    * Set the server config.
    *
    * @param serverConfig the server config
    */
   public void setServerConfig(ServerConfig serverConfig)
   {
      this.serverConfig = serverConfig;
   }
}
