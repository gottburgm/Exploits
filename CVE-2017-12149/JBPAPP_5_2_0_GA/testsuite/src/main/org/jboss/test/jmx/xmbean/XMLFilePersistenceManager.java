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
package org.jboss.test.jmx.xmbean;

import java.beans.PropertyEditor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.Descriptor;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanInfo;

import org.jboss.mx.metadata.MBeanInfoConversion;
import org.jboss.mx.modelmbean.ModelMBeanConstants;
import org.jboss.mx.modelmbean.ModelMBeanInvoker;
import org.jboss.mx.persistence.PersistenceManager;

import org.jboss.common.beans.property.BeanUtils;
import org.jboss.common.beans.property.finder.PropertyEditorFinder;
import org.jboss.logging.Logger;
import org.jboss.util.StringPropertyReplacer;

/**
 * XML File Persistence Manager. <p>
 *
 * Persists the MBean to the file system using an XML file.
 * 
 * The file has to follow this (simple) DTD:
 * <pre>
 * <!ELEMENT attribute (#PCDATA)>
 * <!ATTLIST attribute
 *	name CDATA #REQUIRED
 *	type CDATA #REQUIRED
 *  >
 * <!ELEMENT attributes (attribute*)>
 * </pre>
 * 
 * @author Heiko.Rupp@cellent.de
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>.
 * @version $Revision: 113110 $
 */
public class XMLFilePersistenceManager extends Object
   implements PersistenceManager
{
   protected static Logger log = Logger.getLogger(XMLFilePersistenceManager.class);
   /** A flag set to true to prevent attribute updates from within load
    * triggering stores.
    */
   protected boolean isLoading;

   // Constructors --------------------------------------------------

   public XMLFilePersistenceManager()
   {
      super();
   }

   // Public --------------------------------------------------------

   /**
    * Loads the attributes from the given file
    *
    * @param  mbean to store to
    * @param  metadata with file location etc.
    * @exception  MBeanException
    */
   public void load(ModelMBeanInvoker mbean, MBeanInfo metadata)
      throws MBeanException
   {
      log.debug("load, resource:" + mbean.getResource());

      if (metadata == null)
      {
         return;
      }
      if (log.isTraceEnabled())
         log.trace("metadata: " + metadata);

      File storeFile = getStoreFile(metadata, false);
      if (storeFile == null)
      {
         return;
      }

      try
      {
         AttributeList attributes = new AttributeList();

         FileInputStream fis = new FileInputStream(storeFile);
         BufferedReader buf = new BufferedReader(new InputStreamReader(fis));

         String line, line2;
         while ((line = buf.readLine()) != null)
         {
            log.trace("Line: " + line);
            if (line.equals("<attributes>"))
               continue;
            if (line.equals("</attributes>"))
               continue;

            // trim leading spaces
            line = line.substring(line.indexOf("<"));

            if (!line.startsWith("<attribute"))
            {
               log.warn("Unknown line, skipping " + line);
               continue;
            }

            // read attribute name
            int pos = line.indexOf("name=\"") + 6; // skip name="
            line2 = line.substring(pos);
            int pos2 = line2.indexOf("\"");
            String name = line2.substring(0, pos2);
            log.debug("name: " + name);

            // read attribute type
            pos = line.indexOf("type=\"") + 6; // skip type="
            line2 = line.substring(pos);
            pos2 = line2.indexOf("\"");
            String type = line2.substring(0, pos2);
            log.debug("type: " + type);

            // read attribute value
            pos = line.indexOf(">");
            pos2 = line.lastIndexOf("<");
            String value = line.substring(pos + 1, pos2);
            log.debug("value :" + value);

            Object oVal = convert(value, type);
            Attribute att = new Attribute(name, oVal);
            attributes.add(att);

         } // while

         mbean.setAttributes(attributes);
      }
      catch (Exception e)
      {
         log.error("Error loading MBean state", e);
      }
      setIsLoading(false);
   }

   /** What we need to get here is 1) the persist location, and 2) the entire
    * contents of the mbean. #2 contains the entire contents (state) of the
    * model object, as well as the meta data that the mbean provides.
    * As such, serializing this (MBeanInfo) object (brute force) in effect
    * serializes the model as well.
    *
    * @param  metadata
    * @exception  MBeanException
    */
   public void store(MBeanInfo metadata) throws MBeanException
   {
      if (isLoading())
      {
         return;
      }

      ModelMBeanInfo mmeta =
         MBeanInfoConversion.toModelMBeanInfo(metadata, true);

      log.debug("store");
      if (log.isTraceEnabled())
         log.trace("metadata: " + metadata);
      File storeFile = getStoreFile(metadata, true);
      if (storeFile == null)
      {
         return;
      }

      try
      {
         log.debug("Storing to file: " + storeFile.getAbsolutePath());
         FileOutputStream fos = new FileOutputStream(storeFile);
         MBeanAttributeInfo[] mais = mmeta.getAttributes();
         StringBuffer buf = new StringBuffer();
         buf.append("<attributes>\n");
         for (int i = 0; i < mais.length; i++)
         {
            ModelMBeanAttributeInfo mai = (ModelMBeanAttributeInfo) mais[i];
            buf.append(" <attribute name=\"" + mai.getName() + "\" ");
            buf.append("type=\"" + mai.getType() + "\">");
            log.debug("Trying to load " + mai.getName());
            Descriptor aDesc = mai.getDescriptor();
            if (aDesc==null)
            	throw new Exception("aDesc is null");
            log.debug(aDesc.toString());
            Object att = aDesc.getFieldValue(ModelMBeanConstants.ATTRIBUTE_VALUE);
            if (att!=null)
            	buf.append(att.toString());
            else
            	log.warn("att was null");
            buf.append("</attribute>\n");
         }
         buf.append("</attributes>");
         log.trace(buf.toString());
         fos.write(buf.toString().getBytes());
         fos.close();
      }
      catch (Exception e)
      {
      		e.printStackTrace();
         throw new MBeanException(e, "Error in persisting MBean.");
      }
   }

   // Protected -----------------------------------------------------

   protected boolean isLoading()
   {
      return isLoading;
   }

   protected void setIsLoading(boolean newIsLoading)
   {
      isLoading = newIsLoading;
   }

   /**
    * Obtain the store location from the ModelMBean Descriptor.
    * If the file name does not end on ".xml", it will be converted
    * to do so.
    * @param metadata
    * @param createFile
    * @return
    * @throws MBeanException
    */
   protected File getStoreFile(MBeanInfo metadata, boolean createFile)
      throws MBeanException
   {
      Descriptor d = ((ModelMBeanInfo) metadata).getMBeanDescriptor();
      String dirPath =
         (String) d.getFieldValue(ModelMBeanConstants.PERSIST_LOCATION);
      String file = (String) d.getFieldValue(ModelMBeanConstants.PERSIST_NAME);
      if (dirPath == null)
      {
         log.debug(
            "No "
               + ModelMBeanConstants.PERSIST_LOCATION
               + " descriptor value found, using '.'");
         dirPath = ".";
      }
      if (file == null)
      {
         log.debug(
            "No "
               + ModelMBeanConstants.PERSIST_NAME
               + " descriptor value found");
         return null;
      }

      dirPath = StringPropertyReplacer.replaceProperties(dirPath);
      file = StringPropertyReplacer.replaceProperties(file);
      // tack on .xml if not there
      if (!file.endsWith(".xml"))
         file = file + ".xml";
      File dir = new File(dirPath);
      File storeFile = new File(dir, file);
      boolean exists = storeFile.exists();
      log.debug("Store file is: " + storeFile.getAbsolutePath());
      if (exists == false && createFile == true)
      {
         dir.mkdirs();
         try
         {
            storeFile.createNewFile();
         }
         catch (IOException e)
         {
            throw new MBeanException(e, "Failed to create store file");
         }
      }
      else if (exists == false)
      {
         storeFile = null;
      }
      return storeFile;
   }

   /**
    * Convert val into an Object of type type -- same as in twiddle.setCommand
    * @param val The given value
    * @param oType the wanted return type
    * @return the value in the correct representation
    * @throws Exception various :)
    */
   private Object convert(String val, String oType) throws Exception
   {
      PropertyEditor editor = PropertyEditorFinder.getInstance().find(BeanUtils.findClass(oType));
      editor.setAsText(val);
      return editor.getValue();
   }
}
