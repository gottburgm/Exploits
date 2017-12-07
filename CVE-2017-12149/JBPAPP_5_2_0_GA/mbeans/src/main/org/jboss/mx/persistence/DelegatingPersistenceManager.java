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
package org.jboss.mx.persistence;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.Descriptor;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanInfo;

import org.jboss.logging.Logger;
import org.jboss.mx.modelmbean.ModelMBeanConstants;
import org.jboss.mx.modelmbean.ModelMBeanInvoker;

/**
 * DelegatingPersistenceManager.
 * 
 * An XMBean Persistence Manager that delegates to an external
 * MBean-controlled implementation the actual persistence of
 * MBean attributes.  
 *
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81026 $
 */
public class DelegatingPersistenceManager
   implements PersistenceManager
{
   // Private Data --------------------------------------------------
   
   private static Logger log = Logger.getLogger(DelegatingPersistenceManager.class);
   
   /** where calls are delegated  */
   private AttributePersistenceManager persistor;
   
   /** the associated name to use at load/store */
   private String persistName;
   
   /** load operation triggers save, so we want to prevent this */
   private boolean isLoading;

   // Constructors --------------------------------------------------

   public DelegatingPersistenceManager()
   {
      // emtpy
   }
   
   // PersistenceManager overrides ----------------------------------
   
   /**
    * Called initialy when the XMBean is constructed in order
    * to load and set the attributes of the MBean,
    * if their persistent image exists.
    */
   public void load(ModelMBeanInvoker invoker, MBeanInfo metadata)
      throws MBeanException
   {
      if (this.persistor == null) {
         // lazy initialization on first load - couldn't do 
         // otherwise with this PersistenceManager interface
         init(invoker, metadata);
      }

      if (log.isDebugEnabled())
         log.debug("load() called for: '" + this.persistName + "'");
      
      AttributeList attrs = null;
      
      // load from the persistor
      try {
          attrs = this.persistor.load(this.persistName);
      }
      catch (Exception e) {
         // problem while loading
         log.warn("Caught exception while loading", e);
         throw new MBeanException(e);
      }

      if (attrs != null) {
         // a persistent attribute list image was found so restore it

         try {
            // need to mark we are loading because setting the attributes
            // will triger a store() that should be ignored
            setIsLoading(true);
           
            if (log.isDebugEnabled())
               log.debug("loading attributes: " + attrs);
            invoker.setAttributes(attrs);
         }
         finally {
            setIsLoading(false);
         }
      }
      else {
         if (log.isDebugEnabled())
            log.debug("No attributes to load");
      }
   }
      
   /**
    * store() is triggered by the PersistenceInterceptor based
    * on the persistence policy.
    * 
    * In the simple case, it will be called for every attribute set.
    * 
    * store() will save *all* attributes that:
	* (a) are writable (so we can re-load them later on)
	* (b) their value exists in the ATTRIBUTE_VALUE descriptor
	* (c) are not marked as PM_NEVER
    */
   public void store(MBeanInfo metadata)
      throws MBeanException
   {
      if (this.persistor == null) {
         // shouln't happen
         throw new MBeanException(new Exception("store() called before instance initialized"));
      }
      
      // while loading store() is triggered
      if (isLoading()) {   
         return; // ignore call
      }
      else {
         if (log.isDebugEnabled())
            log.debug("store() called for: '" + this.persistName + "'");
         
         // placehold for attributes to be persisted
         AttributeList attributes = new AttributeList();

         // iterate over all attributes in metadata
         MBeanAttributeInfo[] attrs = metadata.getAttributes();
         
         if (log.isDebugEnabled() && attrs.length > 0)
            log.debug("store() --- ModelMBeanAttributeInfo[] ---");
         
         for (int i = 0; i < attrs.length; i++)
         {
            /// for each (a) writable attribute (b) in the model cache,
            // create a new Attribute object and add it to the collection.
            ModelMBeanAttributeInfo attributeInfo = (ModelMBeanAttributeInfo)attrs[i];
            
            if (log.isDebugEnabled())
               log.debug("  attr (#" + i + ") - " + attributeInfo);
            
            if (attributeInfo.isWritable()) {
	            Descriptor attrDesc = attributeInfo.getDescriptor();
	
	            Object name    = attrDesc.getFieldValue(ModelMBeanConstants.NAME);
	            Object value   = attrDesc.getFieldValue(ModelMBeanConstants.ATTRIBUTE_VALUE);
                Object updated = attrDesc.getFieldValue(ModelMBeanConstants.LAST_UPDATED_TIME_STAMP2);                
	            Object pPolicy = attrDesc.getFieldValue(ModelMBeanConstants.PERSIST_POLICY);
	            
	            boolean noPersistPolicy =
	               pPolicy != null && 
	               ((String)pPolicy).equalsIgnoreCase(ModelMBeanConstants.PP_NEVER) ? true : false;
	            
	            // to persist the attribute:
	            //
	            // (a) must be writable (so we can re-load it later on)
	            // (b) its value must be set in the ATTRIBUTE_VALUE descriptor
	            // (c) must not be marked as PM_NEVER
	            if (updated != null && noPersistPolicy == false) {
	               attributes.add(new Attribute(name.toString(), value));
	            }
            }
         }
         try {
            if (!attributes.isEmpty()) {
               
               if (log.isDebugEnabled())
                  log.debug("calling persistor.store(" + this.persistName + ") attrs=" + attributes);
               
               persistor.store(this.persistName, attributes);
            }
            else {
               if (log.isDebugEnabled())
                  log.debug("nothing to persist");
            }
         }
         catch (Exception e) {
            log.warn("cought exception during store()", e);
         }
      }
   }

   // Protected -----------------------------------------------------

   /**
    * Lazy initialization
    * 
    * Gets the external persistor to use and decides on the
    * persistName to use for this MBean load()/store() calls.
    */
   protected void init(ModelMBeanInvoker invoker, MBeanInfo metadata)
         throws MBeanException
   {
      Descriptor desc = ((ModelMBeanInfo)metadata).getMBeanDescriptor();
      
      if (log.isDebugEnabled()) {
         log.debug("init() --- ModelMBeanInfo Descriptor --- ");
         log.debug(desc);
      }
      
      // Decide what to use as a persistent name (id) for this MBean
      
      // If the user has explicitly specified a "persistName", use it
      String name = (String)desc.getFieldValue(ModelMBeanConstants.PERSIST_NAME);
      
      if (name != null) {
         this.persistName = name;
      }
      else {
         // Try to find ObjectName stored (or lets say hidden) there by ModelMBeanInvoker
         ObjectName objectName = (ObjectName)desc.getFieldValue(ModelMBeanConstants.OBJECT_NAME);
         
         if (objectName != null) {
            this.persistName = objectName.toString();
         }
         else {
            throw new MBeanException(new Exception("must specify a value for: " + ModelMBeanConstants.PERSIST_NAME));
         }
      }
      
      if (log.isDebugEnabled())
         log.debug("chosen persistent id: '" + this.persistName + "'");

      // get the name of the MBean factory service that creates
      // the AttributePersistenceManager implementation
      String service = (String)desc.getFieldValue(ModelMBeanConstants.DELEGATING_PM_SERVICE_DESCRIPTOR);
      if (service == null)
      {
         // use default
         service = ModelMBeanConstants.DELEGATING_PM_SERVICE_DEFAULT_VALUE;
      }

      // get the name of the operation to call on the MBean service
      String operation = (String)desc.getFieldValue(ModelMBeanConstants.DELEGATING_PM_OPERATION_DESCRIPTOR);
      if (operation == null)
      {
         // use default
         operation = ModelMBeanConstants.DELEGATING_PM_OPERATION_DEFAULT_VALUE;
      }

      // Create the AttributePersistenceManager
      try {
         ObjectName objName = new ObjectName(service);
         MBeanServer server = invoker.getServer();
         
         this.persistor = (AttributePersistenceManager)server.invoke(objName, 
                                                                     operation,
                                                                     new Object[] {},
                                                                     new String[] {});
         if (this.persistor == null) {
            throw new MBeanException(new NullPointerException("null AttributePersistenceManager from: " + service));
         }
      }
      catch (MalformedObjectNameException e) {
         throw new MBeanException(e, "not a valid ObjectName: " + service);
      }
      catch (InstanceNotFoundException e) {
         throw new MBeanException(e, "service not registered: " + service);
      }
      catch (ReflectionException e) {
         throw new MBeanException(e);
      }
      
      if (log.isDebugEnabled())
         log.debug("using AttributePersistenceManager: " + this.persistor.getClass().getName());
   }
   
   /**
    * Check if we are loading state
    */
   protected boolean isLoading()
   {
      return isLoading;
   }

   /**
    * Set the loading status
    * 
    * @param newIsLoading
    */
   protected void setIsLoading(boolean newIsLoading)
   {
      isLoading = newIsLoading;
   }

}
