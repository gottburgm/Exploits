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
package org.jboss.web;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.metadata.web.jboss.JBossWebMetaData;

/** A WebApplication represents the information for a war deployment.

 @see AbstractWebContainer

 @author Scott.Stark@jboss.org
 @author adrian@jboss.org
 @version $Revision: 83775 $
 */
public class WebApplication
{
   /** */
   private DeploymentUnit unit;
   /** Class loader of this application */
   protected ClassLoader classLoader = null;
   /** name of this application */
   protected String name = "";
   protected String canonicalName;
   /** URL where this application was deployed from */
   protected URL url;
   /** The web app metadata from the web.xml and jboss-web.xml descriptors */
   protected JBossWebMetaData metaData;
   /** Arbitary data object for storing application specific data */
   protected Object data;
   /** The jmx domain of the web container */
   protected String domain;
   /**
    * The parent class loader first model flag
    */
   protected boolean java2ClassLoadingCompliance = false;
   /**
    * A flag indicating if war archives should be unpacked
    */
   protected boolean unpackWars = true;
   /**
    * If true, ejb-links that don't resolve don't cause an error (fallback to
    * jndi-name)
    */
   protected boolean lenientEjbLink = false;
   /**
    * The default security-domain name to use
    */
   protected String defaultSecurityDomain;
   protected HashMap vhostToHostNames = new HashMap();
   /** Deployer config bean */
   protected Object deployerConfig;

   /** Create an empty WebApplication instance
    */
   public WebApplication()
   {
   }

   /** Create a WebApplication instance with with given web-app metadata.
    @param metaData the web-app metadata containing the web.xml and
    jboss-web.xml descriptor metadata.
    */
   public WebApplication(JBossWebMetaData metaData)
   {
      this.metaData = metaData;
   }

   /** Create a WebApplication instance with with given name,
    url and class loader.
    @param name name of this application
    @param url url where this application was deployed from
    @param classLoader Class loader of this application
    */
   public WebApplication(String name, URL url, ClassLoader classLoader)
   {
      this.name = name;
      this.url = url;
      this.classLoader = classLoader;
   }

   public DeploymentUnit getDeploymentUnit()
   {
      return unit;
   }
   
   public void setDeploymentUnit(DeploymentUnit unit)
   {
      this.unit = unit;
   }

   /** Get the class loader of this WebApplication.
    * @return The ClassLoader instance of the web application
    */
   public ClassLoader getClassLoader()
   {
      return classLoader;
   }

   /** Set the class loader of this WebApplication.
    * @param classLoader The ClassLoader instance for the web application
    */
   public void setClassLoader(ClassLoader classLoader)
   {
      this.classLoader = classLoader;
   }

   /** Get the name of this WebApplication.
    * @return String name of the web application
    */
   public String getName()
   {
      String n = name;
      if (n == null)
         n = url.getFile();
      return n;
   }

   /** Set the name of this WebApplication.
    * @param name of the web application
    */
   public void setName(String name)
   {
      this.name = name;
   }

   public String getCanonicalName()
   {
      return canonicalName;
   }
   public void setCanonicalName(String canonicalName)
   {
      this.canonicalName = canonicalName;
   }

   /** Get the URL from which this WebApplication was deployed
    * @return URL where this application was deployed from
    */
   public URL getURL()
   {
      return url;
   }

   /** Set the URL from which this WebApplication was deployed
    * @param url URL where this application was deployed from
    */
   public void setURL(URL url)
   {
      if (url == null)
         throw new IllegalArgumentException("Null URL");
      this.url = url;
   }

   /** Getter for property metaData.
    * @return Value of property metaData.
    */
   public JBossWebMetaData getMetaData()
   {
      return metaData;
   }

   /** Setter for property metaData.
    * @param metaData New value of property metaData.
    */
   public void setMetaData(JBossWebMetaData metaData)
   {
      this.metaData = metaData;
   }

   public Object getAppData()
   {
      return data;
   }

   public void setAppData(Object data)
   {
      this.data = data;
   }


   public String getDomain()
   {
      return domain;
   }
   public void setDomain(String domain)
   {
      this.domain = domain;
   }
   /**
    * Get the flag indicating if the normal Java2 parent first class loading
    * model should be used over the servlet 2.3 web container first model.
    * @return true for parent first, false for the servlet 2.3 model
    * @jmx.managed-attribute
    */
   public boolean getJava2ClassLoadingCompliance()
   {
      return java2ClassLoadingCompliance;
   }

   /**
    * Set the flag indicating if the normal Java2 parent first class loading
    * model should be used over the servlet 2.3 web container first model.
    * @param flag true for parent first, false for the servlet 2.3 model
    * @jmx.managed-attribute
    */
   public void setJava2ClassLoadingCompliance(boolean flag)
   {
      java2ClassLoadingCompliance = flag;
   }

   /**
    * Get the flag indicating if war archives should be unpacked. This may need
    * to be set to false as long extraction paths under deploy can show up as
    * deployment failures on some platforms.
    * @return true is war archives should be unpacked
    * @jmx.managed-attribute
    */
   public boolean getUnpackWars()
   {
      return unpackWars;
   }

   /**
    * Get the flag indicating if war archives should be unpacked. This may need
    * to be set to false as long extraction paths under deploy can show up as
    * deployment failures on some platforms.
    * @param flag , true is war archives should be unpacked
    * @jmx.managed-attribute
    */
   public void setUnpackWars(boolean flag)
   {
      this.unpackWars = flag;
   }

   /**
    * Get the flag indicating if ejb-link errors should be ignored in favour of
    * trying the jndi-name in jboss-web.xml
    * @return a <code>boolean</code> value
    * @jmx.managed-attribute
    */
   public boolean getLenientEjbLink()
   {
      return lenientEjbLink;
   }

   /**
    * Set the flag indicating if ejb-link errors should be ignored in favour of
    * trying the jndi-name in jboss-web.xml
    * @jmx.managed-attribute
    */
   public void setLenientEjbLink(boolean flag)
   {
      lenientEjbLink = flag;
   }

   /**
    * Get the default security domain implementation to use if a war does not
    * declare a security-domain.
    * @return jndi name of the security domain binding to use.
    * @jmx.managed-attribute
    */
   public String getDefaultSecurityDomain()
   {
      return defaultSecurityDomain;
   }

   /**
    * Set the default security domain implementation to use if a war does not
    * declare a security-domain.
    * @param defaultSecurityDomain - jndi name of the security domain binding to
    * use.
    * @jmx.managed-attribute
    */
   public void setDefaultSecurityDomain(String defaultSecurityDomain)
   {
      this.defaultSecurityDomain = defaultSecurityDomain;
   }

   public Map getVhostToHostNames()
   {
      return this.vhostToHostNames;
   }
   public void setVhostToHostNames(Map map)
   {
      this.vhostToHostNames.clear();
      this.vhostToHostNames.putAll(map);
   }

   public Object getDeployerConfig()
   {
      return deployerConfig;
   }
   public void setDeployerConfig(Object config)
   {
      this.deployerConfig = config;
   }

   public String toString()
   {
      StringBuffer buffer = new StringBuffer("{WebApplication: ");
      buffer.append(getName());
      buffer.append(", URL: ");
      buffer.append(url);
      buffer.append(", classLoader: ");
      buffer.append(classLoader);
      buffer.append(':');
      buffer.append(classLoader.hashCode());
      buffer.append('}');
      return buffer.toString();
   }

}
