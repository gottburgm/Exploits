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
package org.jboss.varia.property;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigLocator;
import org.jboss.util.Strings;
import org.jboss.util.property.Property;
import org.jboss.util.property.PropertyGroup;
import org.jboss.util.property.PropertyListener;

/**
 * A service to access system properties.
 *
 * @jmx:mbean name="jboss.varia:type=Service,name=SystemProperties"
 *            extends="org.jboss.system.ServiceMBean"
 *
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81038 $
 */
public class SystemPropertiesService
   extends ServiceMBeanSupport
   implements SystemPropertiesServiceMBean
{
   /** The server's home dir as a string (for making urls). */
   protected String serverHome;
   
   ///////////////////////////////////////////////////////////////////////////
   //                    Property/PropertyManager Access                    //
   ///////////////////////////////////////////////////////////////////////////
   
   /**
    * Set a system property.
    *
    * @jmx:managed-operation
    *
    * @param name    The name of the property to set.
    * @param value   The value of the property.
    * @return        Previous property value or null
    */
   public String set(final String name, final String value)
   {
      return Property.set(name, value);
   }

   /**
    * Get a system property.
    * 
    * @jmx:managed-operation
    *
    * @param name          Property name
    * @param defaultValue  Default property value
    * @return              Property value or default
    */
   public String get(final String name, final String defaultValue)
   {
      return Property.get(name, defaultValue);
   }

   /**
    * Get a system property.
    *
    * @jmx:managed-operation
    *
    * @param name       Property name
    * @return           Property value or null
    */
   public String get(final String name)
   {
      return Property.get(name);
   }

   /**
    * Remove a system property.
    *
    * @jmx:managed-operation
    *
    * @param name    The name of the property to remove.
    * @return        Removed property value or null
    */
   public String remove(final String name)
   {
      return Property.remove(name);
   }

   /**
    * Get an array style system property.
    *
    * @jmx:managed-operation
    * 
    * @param base          Base property name
    * @param defaultValues Default property values
    * @return              ArrayList of property values or default
    */
   public List getArray(final String base, final List defaultValues)
   {      
      String[] array = new String[defaultValues.size()];
      defaultValues.toArray(array);
      String[] values = Property.getArray(base, array);
      return Arrays.asList(values);
   }

   /**
    * Get an array style system property.
    *
    * @jmx:managed-operation
    *
    * @param name       Property name
    * @return           ArrayList of property values or empty array
    */
   public List getArray(String name)
   {
      String[] array = Property.getArray(name);
      return Arrays.asList(array);
   }

   /**
    * Check if a system property of the given name exists.
    *
    * @jmx:managed-operation
    *
    * @param name    Property name
    * @return        True if property exists
    */
   public boolean exists(String name)
   {
      return Property.exists(name);
   }

   /**
    * Get a property group for under the given system property base.
    *
    * @jmx:managed-operation
    *
    * @param basename   Base property name
    * @return           Property group
    */
   public PropertyGroup getGroup(String basename)
   {
      return Property.getGroup(basename);
   }

   /**
    * Get a property group for under the given system property base
    * at the given index.
    *
    * @jmx:managed-operation
    *
    * @param basename   Base property name
    * @param index      Array property index
    * @return           Property group
    */
   public PropertyGroup getGroup(String basename, int index)
   {
      return Property.getGroup(basename, index);
   }

   /**
    * Add a property listener.
    *
    * @jmx:managed-operation
    *
    * @param listener   Property listener to add
    */
   public void addListener(final PropertyListener listener)
   {
      Property.addListener(listener);
   }

   /**
    * Add an array of property listeners.
    *
    * @jmx:managed-operation
    *
    * @param listeners     Array of property listeners to add
    */
   public void addListeners(final PropertyListener[] listeners)
   {
      Property.addListeners(listeners);
   }

   /**
    * Remove a property listener.
    *
    * @jmx:managed-operation
    *
    * @param listener   Property listener to remove
    * @return           True if listener was removed
    */
   public boolean removeListener(final PropertyListener listener)
   {
      return Property.removeListener(listener);
   }

   
   ///////////////////////////////////////////////////////////////////////////
   //                           Property Loading                            //
   ///////////////////////////////////////////////////////////////////////////

   /**
    * Load some system properties from the given URL.
    *
    * @jmx:managed-operation
    *
    * @param url    The url to load properties from.
    */
   public void load(final URL url) throws IOException
   {
      log.trace("Loading system properties from: " + url);

      Properties props = System.getProperties();
      InputStream is = url.openConnection().getInputStream();
      props.load(is);
      is.close();

      log.info("Loaded system properties from: " + url);
   }

   /**
    * Load some system properties from the given URL.
    *
    * @jmx:managed-operation
    *
    * @param url    The url to load properties from.
    */
   public void load(final String url) throws IOException, MalformedURLException
   {
      load(Strings.toURL(url, serverHome));
   }
   

   ///////////////////////////////////////////////////////////////////////////
   //                      JMX & Configuration Helpers                      //
   ///////////////////////////////////////////////////////////////////////////

   /**
    * Construct and add a property listener.
    *
    * @jmx:managed-operation
    *
    * @param type   The type of property listener to add.
    */
   public void addListener(final String typename)
      throws ClassNotFoundException, IllegalAccessException, InstantiationException
   {
      Class type = Class.forName(typename);
      PropertyListener listener = (PropertyListener) type.newInstance();

      addListener(listener);
   }

   /**
    * Load system properties for each of the given comma separated urls.
    *
    * @jmx:managed-attribute
    *
    * @param list   A list of comma separated urls.
    */
   public void setURLList(final String list) throws MalformedURLException, IOException
   {
      StringTokenizer stok = new StringTokenizer(list, ",");

      while (stok.hasMoreTokens())
      {
         String url = stok.nextToken();
         load(url);
      }
   }

   /** Set system properties by merging the given properties object. This will
    * replace valid references to properties of the form ${x} in 'props' or a
    * System property with the value of x.
    *
    * @jmx:managed-attribute
    *
    * @param props    Properties object to merge.
    */
   public void setProperties(final Properties props) throws IOException
   {
      log.debug("Merging with system properties: " + props);
      System.getProperties().putAll(props);
   }

   /**
    * Return a Map of System.getProperties() with a toString implementation
    * that provides an html table of the key/value pairs.
    *
    * @jmx:managed-operation
    */
   public Map showAll()
   {
      return new HTMLMap(System.getProperties());
   }

   /**
    * Return a Map of the property group for under the given system property base
    * with a toString implementation that provides an html table of the key/value pairs.
    *
    * @jmx:managed-operation
    *
    * @param basename   Base property name
    * @return           Property group
    */
   public Map showGroup(final String basename)
   {
      return new HTMLMap(getGroup(basename));
   }

   /**
    * A helper to render a map as HTML on toString()
    *
    * <p>
    * The html adapter should in theory be able to render a map (nested map
    * list, array or whatever), but until then we can do it for it.
    */
   protected static class HTMLMap
      extends HashMap
   {
      public HTMLMap(final Map map)
      {
         super(map);
      }

      public String toString()
      {
         StringBuffer buff = new StringBuffer();

         buff.append("<table>");

         SortedSet keys = new TreeSet(this.keySet());
         Iterator iter = keys.iterator();
         while (iter.hasNext())
         {
            String key = (String) iter.next();
            buff.append("<tr><td align=\"left\"><b>")
               .append(key)
               .append("</b></td><td align=\"left\">")
               .append(this.get(key))
               .append("</td></tr>\n\r");
         }

         buff.append("</table>");

         return buff.toString();
      }
   }
   
   
   ///////////////////////////////////////////////////////////////////////////
   //                    ServiceMBeanSupport Overrides                      //
   ///////////////////////////////////////////////////////////////////////////

   /**
    * Setup our reference to the server's home directory.  This is done
    * here because one or more attribute setters makes use of this value.
    */
   public ObjectName preRegister(final MBeanServer server, final ObjectName name)
      throws Exception
   {
      // get server's home for relative paths, need this for making urls
      serverHome = ServerConfigLocator.locate().getServerHomeDir().getPath();

      return super.preRegister(server, name);
   }

}
