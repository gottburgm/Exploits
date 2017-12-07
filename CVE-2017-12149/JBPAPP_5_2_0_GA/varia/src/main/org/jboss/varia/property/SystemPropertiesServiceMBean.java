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

/**
 * MBean interface.
 */
public interface SystemPropertiesServiceMBean extends org.jboss.system.ServiceMBean {

   //default object name
   public static final javax.management.ObjectName OBJECT_NAME = org.jboss.mx.util.ObjectNameFactory.create("jboss.varia:type=Service,name=SystemProperties");

   /**
    * Set a system property.
    * @param name The name of the property to set.
    * @param value The value of the property.
    * @return Previous property value or null    */
  java.lang.String set(java.lang.String name,java.lang.String value) ;

   /**
    * Get a system property.
    * @param name Property name
    * @param defaultValue Default property value
    * @return Property value or default    */
  java.lang.String get(java.lang.String name,java.lang.String defaultValue) ;

   /**
    * Get a system property.
    * @param name Property name
    * @return Property value or null    */
  java.lang.String get(java.lang.String name) ;

   /**
    * Remove a system property.
    * @param name The name of the property to remove.
    * @return Removed property value or null    */
  java.lang.String remove(java.lang.String name) ;

   /**
    * Get an array style system property.
    * @param base Base property name
    * @param defaultValues Default property values
    * @return ArrayList of property values or default    */
  java.util.List getArray(java.lang.String base,java.util.List defaultValues) ;

   /**
    * Get an array style system property.
    * @param name Property name
    * @return ArrayList of property values or empty array    */
  java.util.List getArray(java.lang.String name) ;

   /**
    * Check if a system property of the given name exists.
    * @param name Property name
    * @return True if property exists    */
  boolean exists(java.lang.String name) ;

   /**
    * Get a property group for under the given system property base.
    * @param basename Base property name
    * @return Property group    */
  org.jboss.util.property.PropertyGroup getGroup(java.lang.String basename) ;

   /**
    * Get a property group for under the given system property base at the given index.
    * @param basename Base property name
    * @param index Array property index
    * @return Property group    */
  org.jboss.util.property.PropertyGroup getGroup(java.lang.String basename,int index) ;

   /**
    * Add a property listener.
    * @param listener Property listener to add    */
  void addListener(org.jboss.util.property.PropertyListener listener) ;

   /**
    * Add an array of property listeners.
    * @param listeners Array of property listeners to add    */
  void addListeners(org.jboss.util.property.PropertyListener[] listeners) ;

   /**
    * Remove a property listener.
    * @param listener Property listener to remove
    * @return True if listener was removed    */
  boolean removeListener(org.jboss.util.property.PropertyListener listener) ;

   /**
    * Load some system properties from the given URL.
    * @param url The url to load properties from.    */
  void load(java.net.URL url) throws java.io.IOException;

   /**
    * Load some system properties from the given URL.
    * @param url The url to load properties from.    */
  void load(java.lang.String url) throws java.io.IOException, java.net.MalformedURLException;

   /**
    * Construct and add a property listener.
    * @param type The type of property listener to add.    */
  void addListener(java.lang.String typename) throws java.lang.ClassNotFoundException, java.lang.IllegalAccessException, java.lang.InstantiationException;

   /**
    * Load system properties for each of the given comma separated urls.
    * @param list A list of comma separated urls.    */
  void setURLList(java.lang.String list) throws java.net.MalformedURLException, java.io.IOException;

   /**
    * Set system properties by merging the given properties object. This will replace valid references to properties of the form ${x} in 'props' or a System property with the value of x.
    * @param props Properties object to merge.    */
  void setProperties(java.util.Properties props) throws java.io.IOException;

   /**
    * Return a Map of System.getProperties() with a toString implementation that provides an html table of the key/value pairs.
    */
  java.util.Map showAll() ;

   /**
    * Return a Map of the property group for under the given system property base with a toString implementation that provides an html table of the key/value pairs.
    * @param basename Base property name
    * @return Property group    */
  java.util.Map showGroup(java.lang.String basename) ;

}
