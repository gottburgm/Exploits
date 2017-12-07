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
package org.jboss.mx.modelmbean;

/**
 * Constants used with Model MBean implementations.
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author  <a href="mailto:thomas.diesler@jboss.com">Thomas Diesler</a>.
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>.
 * @author Matt Munz
 * @version $Revision: 81019 $
 */
public interface ModelMBeanConstants
{
   // Model MBean notification type string -------------------------
   String GENERIC_MODELMBEAN_NOTIFICATION = "jmx.modelmbean.generic";

   // Mandatory descriptor fields -----------------------------------

   String NAME                     = "name";
   String DESCRIPTOR_TYPE          = "descriptorType";

   // Optional shared descriptor fields -----------------------------------

   String CLASS                    = "class";
   String CURRENCY_TIME_LIMIT      = "currencyTimeLimit";
   String DISPLAY_NAME             = "displayName";
   String LAST_UPDATED_TIME_STAMP  = "lastUpdatedTimeStamp";
   String LOG                      = "log";
   String LOG_FILE                 = "logFile";
   String PERSIST_POLICY           = "persistPolicy";
   String PERSIST_PERIOD           = "persistPeriod";
   String PRESENTATION_STRING      = "presentationString";
   String VISIBILITY               = "visibility";

   // MBean descriptor fields -----------------------------------

   String PERSIST_LOCATION         = "persistLocation";
   String PERSIST_NAME             = "persistName";
   String EXPORT                   = "export";

   // Attribute descriptor fields -----------------------------------

   String CACHED_VALUE             = "value";               // cached value, may be disabled
   String ATTRIBUTE_VALUE          = "attributeValue";      // last attribute value set
   String DEFAULT                  = "default";             // default, if no accessors defined
   String GET_METHOD               = "getMethod";
   String SET_METHOD               = "setMethod";
   String PROTOCOL_MAP             = "protocolMap";

   // constructor descriptor fields -----------------------------------

   String ROLE                     = "role";

   // Operation descriptor fields -----------------------------------

   String TARGET_OBJECT            = "targetObject";
   String TARGET_TYPE              = "targetType";

   // Notification descriptor fields -----------------------------------

   String SEVERITY                 = "severity";
   String MESSAGE_ID               = "messageId";

   // Persistence policies ------------------------------------------
   String PP_NEVER                 = "Never";
   String PP_ON_TIMER              = "OnTimer";
   String PP_ON_UPDATE             = "OnUpdate";
   String PP_NO_MORE_OFTEN_THAN    = "NoMoreOftenThan";
   String PP_ALWAYS                = "Always";

   String[] PERSIST_POLICIES = new String[]
   {
      ModelMBeanConstants.PP_NEVER,
      ModelMBeanConstants.PP_ON_TIMER,
      ModelMBeanConstants.PP_ON_UPDATE,
      ModelMBeanConstants.PP_NO_MORE_OFTEN_THAN,
      ModelMBeanConstants.PP_ALWAYS
   };

   // Severities ------------------------------------------
   String SEVERITY_UNKNOWN         = "0";
   String SEVERITY_NON_RECOVERABLE = "1";
   String SEVERITY_CRITICAL        = "2";
   String SEVERITY_MAJOR           = "3";
   String SEVERITY_MINOR           = "4";
   String SEVERITY_WARNING         = "5";
   String SEVERITY_NORMAL          = "6";

   // Descriptor types ----------------------------------------------

   String MBEAN_DESCRIPTOR         = "mbean";
   String ATTRIBUTE_DESCRIPTOR     = "attribute";
   String CONSTRUCTOR_DESCRIPTOR   = "constructor";
   String OPERATION_DESCRIPTOR     = "operation";
   String NOTIFICATION_DESCRIPTOR  = "notification";

   // Role types ----------------------------------------------------

   String ROLE_CONSTRUCTOR         = "constructor";
   String ROLE_GETTER              = "getter";
   String ROLE_SETTER              = "setter";
   String ROLE_OPERATION           = "operation";

   // Visibility values ---------------------------------------------
   String HIGH_VISIBILITY          = "1";
   String NORMAL_VISIBILITY        = "2";
   String LOW_VISIBILITY           = "3";
   String MINIMAL_VISIBILITY       = "4";

   // Cache policies ------------------------------------------------
   String CACHE_NEVER              = "-1";
   long CACHE_NEVER_LIMIT          = -1;
   String CACHE_ALWAYS             = "0";
   long CACHE_ALWAYS_LIMIT         = 0;

   // Operation impact ----------------------------------------------
   String ACTION                   = "ACTION";
   String ACTION_INFO              = "ACTION_INFO";
   String INFO                     = "INFO";
   
   // END of standard descriptor fields *****************************

    // Default Model MBean resource type, <tt>"ObjectReference"</tt>.
   String OBJECT_REF               = "ObjectReference";

   /**
    * A convenience constant to use with 
    * {@link javax.management.modelmbean.ModelMBeanInfo#getDescriptors getDescriptors()}
    * to return the descriptors of all management interface elements 
    * (a <tt>null</tt> string).
    */
   String ALL_DESCRIPTORS          = null;

   // Optional descriptor fields ------------------------------------
   
   String LAST_RETURNED_TIME_STAMP = "lastReturnedTimestamp";
   
   /** used to mark the update (set) of an ATTRIBUTE_VALUE */
   String LAST_UPDATED_TIME_STAMP2 = "lastUpdatedTimeStamp2";
   
   /** */
   String INTERCEPTORS = "interceptors";
   /**
    * Indicates whether MBean Info should be stored.
    */
   String PERSIST_INFO  = "persistmbeaninfo";
   /**
    * Indicates the FQN of the resource class
    */
   String RESOURCE_CLASS  = "resourceClass";   
   /**
    * constant used by the 1.0 xmbean parser 
    * this defines the name of the descriptor used to designate the persistence manager 
    * that is to be used for a given XMBean
    */
   String PERSISTENCE_MANAGER = "persistence-manager";

   /**
    * Used to store the JMX ObjectName of the MBean, so it can be referenced
    * by subsystem that may need it
    */
   String OBJECT_NAME = "objectname";
   
   /**
    * Extended descriptor used in conjuction with DelegatingPersistenceManager
    * to specify an external MBean service that will be used as a factory
    * for creating AttributePersistenceManager objects.
    * 
    * If the descriptor is not specified, the ObjectName below will be used
    * as the default.
    */
   String DELEGATING_PM_SERVICE_DESCRIPTOR    = "attribute-persistence-service";
   String DELEGATING_PM_SERVICE_DEFAULT_VALUE = "jboss:service=AttributePersistenceService";

   /**
    * Extended descriptor used in conjuction with DelegatingPersistenceManager
    * to specify the operation name that will be called on the external MBean service
    * for creating AttributePersistenceManager objects.
    * 
    * If the descriptor is not specified, the default will apply
    */
   String DELEGATING_PM_OPERATION_DESCRIPTOR     = "attribute-persistence-operation";
   String DELEGATING_PM_OPERATION_DEFAULT_VALUE  = "apmCreate";   

   /** The MBeanServer injection id type */
   final String MBEAN_SERVER_INJECTION_TYPE = "MBeanServerType";
   /** The MBeanInfo injection id type */
   final String MBEAN_INFO_INJECTION_TYPE = "MBeanInfoType";
   /** The ObjectName injection id type */
   final String OBJECT_NAME_INJECTION_TYPE = "ObjectNameType";

   // Constants for metadata objects --------------------------------
   boolean IS_READABLE             = true;
   boolean IS_WRITABLE             = true;
   boolean IS_IS                   = true; 
}

