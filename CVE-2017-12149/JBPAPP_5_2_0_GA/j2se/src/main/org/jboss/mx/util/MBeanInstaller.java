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
package org.jboss.mx.util;

import java.beans.PropertyEditor;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.jboss.common.beans.property.finder.PropertyEditorFinder;
import org.jboss.logging.Logger;
import org.jboss.mx.loading.MBeanElement;
import org.jboss.mx.server.ObjectInputStreamWithClassLoader;
import org.jboss.mx.server.ServerConstants;
import org.jboss.util.UnreachableStatementException;

/**
 * MBean installer utility<p>
 *
 * This installer allows MLet to install or upgrade a mbean based on the version
 * specified in the MLet conf file. If the mbean version is newer than the 
 * registered in the server, the installer unregisters the old mbean and then
 * registers the new one. This management needs to store the mbean version into
 * the MBeanRegistry in the server.
 *
 * When we register mbeans, however, we can't pass the metadata to MBeanServer
 * through the standard JMX api because Both of createMBean() and registerMBean()
 * have no extra arguments to attach the metadata. Thus we call 
 * MBeanServer.invoke() directly to set/get the internal MBean metadata.
 *
 * Currently version and date are stored in the mbean registry as mbean metadata.
 * The date will be used for preparing presentaionString for this mbean info.
 * For managment purpose, we can add any extra data to the matadata if you need.
 *
 * @author  <a href="mailto:Fusayuki.Minamoto@fujixerox.co.jp">Fusayuki Minamoto</a>.
 * @author  <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>.
 *
 * @version $Revision: 113110 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>20020219 Juha Lindfors:</b>
 * <ul>
 * <li>Clarified the use of classloaders in the code, renaming loader to a more
 *     explicit ctxClassLoader
 * </li>
 * <li>Fixed some irregularities with the install/update code -- original
 *     implementatio was cause IndexOutOfBoundsExceptions which prevented
 *     some replacements in valid cases. Fixing this uncovered update logic
 *     that would replace MBeans that were not associated with versioning
 *     information at all.
 *     <p>
 *     The current semantics should be:
 *     <ol>
 *     <li>If an MBean is registered without versioning information it can
 *         never be automatically replaced by another MBean (regardless of
 *         the versioning information in the new MBean).
 *     </li>
 *     <li>An MBean that has a higher version number (as determined by the
 *         MLetVersion Comparable interface) can automatically replace an 
 *         MBean that was registered with a lower version number.
 *     </li>
 *     <li>An MBean without versioning info can never automatically replace
 *         an MBean that was registered with version.
 *     </li>
 *     </ol>
 * </li>
 * </ul>
 */
public class MBeanInstaller
{
   // Constants -----------------------------------------------------
   
   public static final String VERSIONS = "versions";
   public static final String DATE     = "date";

   
   // Static --------------------------------------------------------
   
   /**
    * Logger instance.
    */
   private static final Logger log = Logger.getLogger(MBeanInstaller.class);
   
   // Attributes ----------------------------------------------------
   
   /**
    * Reference to the MBean server the installed MBeans will get registered to.
    */
   private MBeanServer server;
   
   /**
    * Reference to the context classloader of the MLet MBean that is installing
    * the new MBeans.
    */
   private ClassLoader ctxClassLoader;
   
   /**
    * Object name of the MLet MBean installing new MBeans to the server. This
    * object name is used as the explicit classloader object name when
    * instantiating new MBeans. This is to ensure the MLet's classloader is the
    * first one to be consulted when loading classes. This is implicitly
    * guaranteed by the UnifiedLoaderRepository but is not necessarily the case
    * with other loader repository implementations.
    */
   private ObjectName  loaderName;
   
   /**
    * Object name of the MBeanServer registry MBean.
    */
   private ObjectName  registryName;

   
   // Constructors --------------------------------------------------
   
   /**
    * Create a new MBean installer instance.
    *
    * @param   server         reference to the MBean server where the new MBeans will
    *                         be registered to
    * @param   ctxClassLoader Context class loader reference which will be 
    *                         stored in the registry for the new MBeans. This
    *                         classloader will be set as the thread context
    *                         classloader when the MBean is invoked.
    * @param   loaderName     Object name of the classloader that should be
    *                         used to instantiate the newly registered MBeans.
    *                         This should normally be the object name of the
    *                         MLet MBean that is installing the new MBeans.
    */
   public MBeanInstaller(MBeanServer server, ClassLoader ctxClassLoader, ObjectName loaderName)
      throws Exception
   {
      this.server = server;
      this.ctxClassLoader = ctxClassLoader;
      this.loaderName   = loaderName;
      this.registryName = new ObjectName(ServerConstants.MBEAN_REGISTRY);
   }

   
   // Public --------------------------------------------------------
   
   /**
    * Install a mbean with mbean metadata<p>
    *
    * @param element    the data parsed from the Mlet file
    *
    * @return mbean instance
    */
   public ObjectInstance installMBean(MBeanElement element)
      throws MBeanException,
             ReflectionException,
             InstanceNotFoundException,
             MalformedObjectNameException
   {
      log.debug("Installing MBean: " + element);
      
      ObjectInstance instance = null;
      ObjectName elementName = getElementName(element);

      if (element.getVersions().isEmpty() || !server.isRegistered(elementName))
      {
         if (element.getCode() != null)
            instance = createMBean(element);
         else if (element.getObject() != null)
            instance = deserialize(element);
         else
            throw new MBeanException(new IllegalArgumentException("No code or object tag"));
      }
      else
         instance = updateMBean(element);

      return instance;
   }

   public ObjectInstance createMBean(MBeanElement element)
      throws MBeanException,
             ReflectionException,
             InstanceNotFoundException,
             MalformedObjectNameException
   {
      log.debug("Creating MBean.. ");
      
      ObjectName elementName = getElementName(element);

      // Set up the valueMap passing to the registry.
      // This valueMap contains mbean meta data and update time.
      Map valueMap = createValueMap(element);

      // Create the mbean instance
      
      // TODO:
      // check the delegateToCLR attribute in the MLetElement here to determine
      // the loading behavior in case of CNFE
      
      String[] classes = element.getConstructorTypes();
      String[] paramStrings = element.getConstructorValues();
      Object[] params = new Object[paramStrings.length];
      for (int i = 0; i < paramStrings.length; ++i)
      {
         try
         {
            Class typeClass = server.getClassLoaderRepository().loadClass(classes[i]);
            PropertyEditor editor = PropertyEditorFinder.getInstance().find(typeClass);
            if (editor == null)
               throw new IllegalArgumentException("No property editor for type=" + typeClass);

            editor.setAsText(paramStrings[i]);
            params[i] = editor.getValue();
         }
         catch (Exception e)
         {
            throw new MBeanException(e);
         }
      }
      Object instance = server.instantiate(
            element.getCode(),
            loaderName,
            params,
            classes);

      // Call MBeanRegistry.invoke("registerMBean") instead of server.registerMBean() to pass
      // the valueMap that contains management values including mbean metadata and update time.
      return registerMBean(instance, elementName, valueMap);
   }

   public ObjectInstance deserialize(MBeanElement element) throws MBeanException,
             ReflectionException,
             InstanceNotFoundException,
             MalformedObjectNameException
   {
      InputStream is = null;
      Object instance = null;
      try
      {
         is = ctxClassLoader.getResourceAsStream(element.getObject());
         if (is == null)
            throw new IllegalArgumentException("Object not found " + element.getObject());
         ObjectInputStreamWithClassLoader ois = new ObjectInputStreamWithClassLoader(is, ctxClassLoader);
         instance = ois.readObject();
      }
      catch (Exception e)
      {
         throw new MBeanException(e);
      }
      finally
      {
         if (is != null)
         {
            try
            {
               is.close();
            }
            catch (Exception ignored)
            {
            }
         }
      }
      ObjectName elementName = getElementName(element);

      // Set up the valueMap passing to the registry.
      // This valueMap contains mbean meta data and update time.
      Map valueMap = createValueMap(element);
      return registerMBean(instance, elementName, valueMap);
   }

   public ObjectInstance updateMBean(MBeanElement element)
      throws MBeanException,
             ReflectionException,
             InstanceNotFoundException,
             MalformedObjectNameException
   {
      log.debug("updating MBean... ");
      
      ObjectName elementName = getElementName(element);

      // Compare versions to decide whether to skip installation of this mbean
      MLetVersion preVersion  = new MLetVersion(getVersions(elementName));
      MLetVersion newVersion  = new MLetVersion(element.getVersions());

      log.debug("Installed version : " + preVersion);
      log.debug("Loaded version    : " + newVersion);

      // FIXME: this comparison works well only if both versions are specified
      //        because jmx spec doesn't fully specify this behavior.
      if (!preVersion.isNull() && !newVersion.isNull() && preVersion.compareTo(newVersion) < 0)
      {
         // Unregister previous mbean
         if (server.isRegistered(elementName))
         {
            unregisterMBean(elementName);
            
            log.debug("Unregistering previous version " + preVersion);
         }

         log.debug("Installing newer version " + newVersion);
         
         // Create mbean with value map
         return createMBean(element);
      }

      return server.getObjectInstance(elementName);
   }

   
   // Private -------------------------------------------------------
   
   private ObjectName getElementName(MBeanElement element)
      throws MalformedObjectNameException
   {
      return (element.getName() != null) ? new ObjectName(element.getName()) : null;
   }

   private Map createValueMap(MBeanElement element)
   {
      HashMap valueMap = new HashMap();

      // We need to set versions here because we can't get the mbean entry
      // outside the server.
      if (element.getVersions() != null && !element.getVersions().isEmpty())
         valueMap.put(VERSIONS, element.getVersions());

      // The date would be used to make a presentationString for this mbean.
      valueMap.put(DATE, new Date(System.currentTimeMillis()));

      // Context class loader for the MBean.
      valueMap.put(ServerConstants.CLASSLOADER, ctxClassLoader);

      return valueMap;
   }

   private List getVersions(ObjectName name)
         throws MBeanException, ReflectionException, InstanceNotFoundException
   {
      if (!server.isRegistered(name))
         return null;

      return (List) getValue(name, VERSIONS);
   }


   private Object getValue(ObjectName name, String key)
      throws MBeanException, ReflectionException, InstanceNotFoundException
   {
      Object value =
            server.invoke(registryName, "getValue",
                          new Object[]
                          {
                             name,
                             key
                          },
                          new String[]
                          {
                             ObjectName.class.getName(),
                             String.class.getName()
                          }
            );

      return value;
   }

   private ObjectInstance registerMBean(Object object, ObjectName name, Map valueMap)
      throws MBeanException, ReflectionException, InstanceNotFoundException
   {
      if (object == null)
      {
         throw new ReflectionException(new IllegalArgumentException(
               "Attempting to register a null object"
         ));
      }
      
      return (ObjectInstance)
            server.invoke(registryName, "registerMBean",
                          new Object[]
                          {
                             object,
                             name,
                             valueMap
                          },
                          new String[]
                          {
                             Object.class.getName(),
                             ObjectName.class.getName(),
                             Map.class.getName()
                          }
            );
   }

   private void unregisterMBean(ObjectName name)
      throws MBeanException, ReflectionException, InstanceNotFoundException
   {
      server.invoke(registryName, "unregisterMBean",
                    new Object[]
                    {
                       name,
                    },
                    new String[]
                    {
                       ObjectName.class.getName(),
                    }
      );
   }
}

/**
 * MLetVersion for encapsulating the version representation<p>
 *
 * Because this class is comparable, you can elaborate the
 * version comparison algorithm if you need better one.
 */
class MLetVersion implements Comparable
{
   protected List versions;

   public MLetVersion(List versions)
   {
      this.versions = versions;
   }

   public List getVersions()
   {
      return versions;
   }

   public boolean isNull()
   {
      return versions == null || versions.isEmpty();
   }

   public int compareTo(Object o)
   {
      MLetVersion other = (MLetVersion) o;

      if (isNull() || other.isNull())
         throw new IllegalArgumentException("MLet versions is null");

      // FIXME: this compares only first element of the versions.
      //        do we really need multiple versions?
      String thisVersion = (String) versions.get(0);
      String otherVersion = (String) other.getVersions().get(0);

      return (thisVersion.compareTo(otherVersion));
   }
   
   public String toString()
   {
      return "Version " + versions.get(0);
   }
}
