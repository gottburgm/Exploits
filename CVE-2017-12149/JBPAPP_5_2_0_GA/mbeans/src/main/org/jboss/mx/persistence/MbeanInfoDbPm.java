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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Vector;

import javax.management.Descriptor;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBean;
import javax.management.modelmbean.ModelMBeanInfo;

import org.jboss.mx.modelmbean.ModelMBeanConstants;
import org.jboss.mx.modelmbean.ModelMBeanInvoker;
import org.jboss.mx.modelmbean.RequiredModelMBeanInstantiator;
import org.jboss.mx.server.registry.MbeanInfoDb;

/**
 * Class responsible for storing and loading the MBean info database.
 * @author Matt Munz
 */
public class MbeanInfoDbPm
   extends MBeanInfoOdb
   implements PersistenceManager
{

   protected boolean fLoadedFromFs;
   protected Vector fNamesToRestore;
   protected Vector fInfosToRestore;
   protected MBeanServer fMbeanServer;
   protected MbeanInfoDb fMbeanInfoDb;
   protected File fMbiDbRoot;

   public MbeanInfoDbPm()
   {
      super();
   }

   /**
    * @todo finish implementing
    */
   public void load(ModelMBeanInvoker mbean, MBeanInfo info)
     throws MBeanException
   {

      logger().debug("Loading the MBI DB");
      setMbeanInfoDb((MbeanInfoDb) mbean.getResource());
      if(!loadedFromFs())
      {

         // get mbi db root from descriptor
         Descriptor d = ((ModelMBeanInfo) info).getMBeanDescriptor();
         String dir = (String) d.getFieldValue(
                         ModelMBeanConstants.PERSIST_LOCATION);
         if((dir != null) && (!dir.equals("")))
         {

            setMbiDbRoot(new File(dir));
         }
         String message = "Loading the MBI DB from the filesystem.  location: ";
         message += mbiDbRoot();
         logger().debug(message);
         File[] mbiFiles = mbiDbRoot().listFiles();
         if(mbiFiles == null)
         {

            return;
         }
         // loop over each item in the mbeaninfo store
         for(int index = 0; index < mbiFiles.length; index++)
         {

            /// for each, load, make a new mbean, and register that mbean
            File curFile = mbiFiles[index];
            ObjectName nameToRestore = null;
            try
            {

               nameToRestore = objectName(curFile);
            }
            catch(MalformedObjectNameException cause)
            {

               throw new MBeanException(cause, 
                                        "Object Name was stored incorrectly.");
            }
            MBeanInfo infoToRestore = null;
            try
            {

               infoToRestore = load(curFile);
            }
            catch(IOException cause)
            {

               throw new MBeanException(cause, 
                                        "Couldn't read the MBeanInfo file.");
            }
            catch(ClassNotFoundException cause2)
            {

               String message2 = "Couldn't find the Class specified in the object file.";
               throw new MBeanException(cause2, message2);
            }
            if(infoToRestore == null)
            {

               throw new MBeanException(new Exception(
                                           "Null loaded MBean info.  Error on load."), 
                                        "Could not load");
            }
            namesToRestore().add(nameToRestore);
            infosToRestore().add(infoToRestore);
         }
         setLoadedFromFs(true);
      }
      try
      {

         register();
      }
      catch(Exception cause)
      {

         throw new MBeanException(cause, 
                                  "Error trying to register loaded MBeans");
      }
   }

   public void store(MBeanInfo info) throws MBeanException
   {

      logger().debug("storing MBI DB State");
      Enumeration queue = mbeanInfoDb().mbiPersistenceQueue();
      for(; queue.hasMoreElements();)
      {

         ObjectName curName = (ObjectName) queue.nextElement();
         logger().debug("queue elem: " + curName);
         MBeanInfo curInfo = null;
         try
         {

            curInfo = getMBeanServer().getMBeanInfo(curName);
         }
         catch(InstanceNotFoundException cause)
         {

            throw new MBeanException(cause);
         }
         catch(JMException cause3)
         {

            throw new MBeanException(cause3);
         }
         if(curInfo == null)
         {

            throw new MBeanException(
               new Exception("Current MBean Info object is null."), 
               "Could not store null object.");
         }
         try
         {

            store(curName, curInfo);
         }
         catch(IOException cause2)
         {

            throw new MBeanException(cause2);
         }
         mbeanInfoDb().removeFromMbiQueue(curName);
         logger().info("Successfully stored mbi for " + curName);
      }
   }

   protected void store(ObjectName name, MBeanInfo info)
     throws IOException
   {

      File location = new File(mbiDbRoot(), fileName(name));
      logger().debug("Storing mbi at: " + location);
      mbiDbRoot().mkdirs();
      store(info, location);
   }

   /**
    * Iterates over the loaded MBean infos and ObjectNames, creates an MBean for each, and
    * registers it with the server.  This includes creating the resource object as well.
    */
   protected void register() throws JMException, 
                                    InvalidTargetObjectTypeException
   {

      // iterate over all loaded infos and register them.  
      logger().debug("registering...");
      Enumeration names = namesToRestore().elements();
      Enumeration infos = infosToRestore().elements();
      try
      {

         while(names.hasMoreElements() && infos.hasMoreElements())
         {

            ObjectName curName = (ObjectName) names.nextElement();
            logger().debug("curName: " + curName);
            ModelMBeanInfo curInfo = (ModelMBeanInfo) infos.nextElement();
            Descriptor mbeanDescriptor = curInfo.getMBeanDescriptor();
            String fieldName = ModelMBeanConstants.RESOURCE_CLASS;
            String className = (String) mbeanDescriptor.getFieldValue(fieldName);
            logger().debug("className: " + className);
            Object resource = getMBeanServer().instantiate(className);
            ModelMBean modelmbean = RequiredModelMBeanInstantiator.instantiate();
            modelmbean.setModelMBeanInfo(curInfo);
            modelmbean.setManagedResource(resource, "ObjectReference");
            getMBeanServer().registerMBean(modelmbean, curName);
         }
      }
      finally
      {

         namesToRestore().removeAllElements();
         infosToRestore().removeAllElements();
      }
   }

   protected MBeanServer getMBeanServer() throws JMException
   {

      if(fMbeanServer == null)
      {

         Collection col = MBeanServerFactory.findMBeanServer(null);
         if(col.isEmpty())
         {

            throw new JMException("No MBeanServer found");
         }
         fMbeanServer = (MBeanServer) col.iterator().next();
      }
      return fMbeanServer;
   }
   
   /**
    * JDK 1.3.1 substitute for jdk 1.4.1 String.replaceAll()
    * Does not accept regular expressions...
    * returns the object string where all instances of target are replaced with replacement
    * @todo replace with a more appropriate string util if available
    */
   protected String replaceAll(String object, String target, String replacement)
   {
      return replaceAll(object, target, replacement, 0);
   }
   
   /**
    * Iterative
    */
   protected String replaceAll(String object, String target, String replacement, int curIndex)
   {
     int indexOfMatch = object.indexOf(target, curIndex);
     if(indexOfMatch < curIndex)
     {
      
        return object;
     }
     String prefix = "";
     if(indexOfMatch > 0)
     {
         
        prefix = object.substring(0, indexOfMatch);
     }
     String tail = object.substring(indexOfMatch + target.length());
     String newObject = prefix + replacement + tail;
     return replaceAll(newObject, target, replacement, indexOfMatch + replacement.length());     
   }

   protected String objNameSeparator()
   {

      return ":";
   }

   protected String objNameSepRep()
   {

      return "___";
   }

   protected String fileName(ObjectName name)
   {

      String fileName = name.getCanonicalName();
      fileName = replaceAll(fileName, objNameSeparator(), objNameSepRep());
      return fileName;
   }

   protected ObjectName objectName(File fileName)
     throws MalformedObjectNameException
   {

      String objectName = fileName.getName();
      objectName = replaceAll(objectName, objNameSepRep(), objNameSeparator());
      return new ObjectName(objectName);
   }

   protected boolean loadedFromFs()
   {

      return fLoadedFromFs;
   }

   protected void setLoadedFromFs(boolean newLoadedFromFs)
   {

      fLoadedFromFs = newLoadedFromFs;
   }

   protected Vector namesToRestore()
   {

      if(fNamesToRestore == null)
      {

         fNamesToRestore = new Vector(10);
      }
      return fNamesToRestore;
   }

   protected Vector infosToRestore()
   {

      if(fInfosToRestore == null)
      {

         fInfosToRestore = new Vector(10);
      }
      return fInfosToRestore;
   }

   protected File mbiDbRoot()
   {

      if(fMbiDbRoot == null)
      {

         fMbiDbRoot = new File("../conf/mbean-info-db/");
      }
      return fMbiDbRoot;
   }

   protected void setMbiDbRoot(File newMbiDbRoot)
   {

      fMbiDbRoot = newMbiDbRoot;
   }

   protected MbeanInfoDb mbeanInfoDb()
   {

      return fMbeanInfoDb;
   }

   protected void setMbeanInfoDb(MbeanInfoDb newMbeanInfoDb)
   {

      fMbeanInfoDb = newMbeanInfoDb;
   }
}