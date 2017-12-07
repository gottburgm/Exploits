/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.deployers.plugins.managed;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.deployers.spi.deployer.managed.ManagedDeploymentCreator;
import org.jboss.deployers.spi.management.KnownDeploymentTypes;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.logging.Logger;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.annotation.ManagementDeployment;
import org.jboss.managed.api.annotation.ManagementConstants;
import org.jboss.managed.plugins.ManagedDeploymentImpl;

/**
 * A ManagedDeploymentCreator that looks for {@linkplain KnownDeploymentTypes}
 * attachments and maps those to the {@linkplain ManagedDeployment#getTypes()}
 * set.
 * 
 * This also does attachment type mapping based on the registered attachment
 * type class to deployment type mapping.
 * @see {@link #addAttachmentType(Class, String)}
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 90181 $
 */
public class TypedManagedDeploymentCreator
   implements ManagedDeploymentCreator
{
   private static Logger log = Logger.getLogger(TypedManagedDeploymentCreator.class);
   /** A mapping from a deployment attachment type to the {@link ManagedDeployment#addType(String)} type */
   private Map<Class, String> attachmentToTypeMap = new ConcurrentHashMap<Class, String>();
   private Map<Class, List<TypeVersion>> attachmentToTypeVersionsMap = new ConcurrentHashMap<Class, List<TypeVersion>>();

   /**
    * 
    */
   public ManagedDeployment build(DeploymentUnit unit,
         Map<String, ManagedObject> unitMOs,
         ManagedDeployment parent)
   {
      ManagementDeployment mdAnnotation = null;
      HashMap<String, ManagedObject> validUnitMOs = new HashMap<String, ManagedObject>();
      for(String name : unitMOs.keySet())
      {
         // TODO: why should there be null ManagedObjects?
         ManagedObject mo = unitMOs.get(name);
         if(mo == null)
            continue;
         validUnitMOs.put(name, mo);
         Map<String, Annotation> annotations = mo.getAnnotations();
         if(annotations != null && mdAnnotation == null)
            mdAnnotation = (ManagementDeployment) annotations.get(ManagementDeployment.class.getName());
      }
      String simpleName = unit.getSimpleName();
      String[] types = {};
      if(mdAnnotation != null)
      {
         String mdaSimpleName = mdAnnotation.simpleName();
         if(mdaSimpleName.length() > 0 && ManagementConstants.GENERATED.equals(mdaSimpleName) == false)
            simpleName = mdAnnotation.simpleName();
         types = mdAnnotation.types();
      }
      
      ManagedDeployment md = new ManagedDeploymentImpl(unit.getName(), simpleName, parent, validUnitMOs);
      if(types.length > 0)
      {
         for(String type : types)
            addType(md, type);
      }

      // Check for KnownDeploymentTypes attachment(s)
      Set<? extends KnownDeploymentTypes> knownTypes = unit.getAllMetaData(KnownDeploymentTypes.class);
      if(knownTypes != null)
      {
         for(KnownDeploymentTypes type : knownTypes)
         {
            addType(md, type.getType());
         }
      }

      // Check for attachment to type mappings
      for(Class attachmentType : attachmentToTypeMap.keySet())
      {
         if(unit.isAttachmentPresent(attachmentType))
         {
            String type = attachmentToTypeMap.get(attachmentType);
            addType(md, type);
         }
      }

      //
      for(Class attachmentType : attachmentToTypeVersionsMap.keySet())
      {
         if(unit.isAttachmentPresent(attachmentType))
         {
            List<TypeVersion> versions = attachmentToTypeVersionsMap.get(attachmentType);
            Object attachment = unit.getAttachment(attachmentType);
            if(attachment == null)
               continue;

            for(TypeVersion tv : versions)
            {
               String version = getVersion(attachment, tv.versionGetter);
               if(version == null)
                  continue;

               Matcher m = tv.versionPattern.matcher(version);
               if(m.matches())
                  addType(md, tv.type);
            }
         }
      }
      return md;
   }

   /**
    * 
    * @param attachmentType
    * @param deploymentType
    */
   public void addAttachmentType(Class attachmentType, String deploymentType)
   {
      log.debug("addAttachmentType, "+attachmentType+":"+deploymentType);
      attachmentToTypeMap.put(attachmentType, deploymentType);
   }
   public void addVersionedAttachmentType(Class attachmentType, String deploymentType,
         String versionPattern)
   {
      addVersionedAttachmentType(attachmentType, deploymentType, versionPattern, "getVersion");
   }
   public void addVersionedAttachmentType(Class attachmentType, String deploymentType,
         String versionPattern, String versionGetterName)
   {
      Pattern p = Pattern.compile(versionPattern);
      log.debug("addVersionedAttachmentType, "+attachmentType+":"+deploymentType
            +", version: "+versionPattern);
      TypeVersion tv = new TypeVersion(deploymentType, p, versionGetterName);
      List<TypeVersion> tvlist = attachmentToTypeVersionsMap.get(attachmentType);
      if(tvlist == null)
      {
         tvlist = new ArrayList<TypeVersion>();
         attachmentToTypeVersionsMap.put(attachmentType, tvlist);         
      }
      tvlist.add(tv);
   }

   public void removeAttachmentType(Class attachmentType)
   {
      log.debug("removeAttachmentType, "+attachmentType);
      attachmentToTypeMap.remove(attachmentType);
   }
   public void removeVersionedAttachmentType(Class attachmentType, String deploymentType)
   {
      log.debug("removeVersionedAttachmentType, "+attachmentType);
      List<TypeVersion> tvlist = attachmentToTypeVersionsMap.get(attachmentType);
      if(tvlist != null)
      {
         Iterator<TypeVersion> iter = tvlist.iterator();
         while(iter.hasNext())
         {
            TypeVersion tv = iter.next();
            if(deploymentType.equals(tv.type))
               iter.remove();
         }
         attachmentToTypeVersionsMap.put(attachmentType, tvlist);         
         if(tvlist.size() == 0)
            attachmentToTypeVersionsMap.remove(attachmentType);      
      }
   }

   /**
    * Look for a String getterName method using reflection
    * @param attachment
    * @param getterName - the method name to call to get the version
    * @return the attachment version if found, null otherwise
    */
   private String getVersion(Object attachment, String getterName)
   {
      String version = null;
      try
      {
         Class[] parameterTypes = {};
         Method getVersion = attachment.getClass().getMethod(getterName, parameterTypes);
         Object[] args = {};
         version = (String) getVersion.invoke(attachment, args);
      }
      catch(Throwable t)
      {
         log.debug("Failed to get version:"+t);
      }
      return version;
   }

   private void addType(ManagedDeployment md, String type)
   {
      if(md.getTypes() == null)
         md.setTypes(new HashSet<String>());
      md.addType(type);
   }

   static class TypeVersion
   {
      private String type;
      private Pattern versionPattern;
      private String versionGetter;

      public TypeVersion(String type, Pattern versionPattern, String versionGetter)
      {
         super();
         this.type = type;
         this.versionPattern = versionPattern;
         this.versionGetter = versionGetter;
      }
   }
   public static void main(String[] args)
   {
      Pattern p = Pattern.compile("3.*");
      Matcher m = p.matcher("3.0");
      System.out.println("3.0 matches: "+m.matches());
      m = p.matcher("3.1");
      System.out.println("3.1 matches: "+m.matches());
      m = p.matcher("3.x");
      System.out.println("3.x matches: "+m.matches());
      m = p.matcher("2.1");
      System.out.println("2.1 matches: "+m.matches());

      Pattern p2x = Pattern.compile("[1-2].*");
      m = p2x.matcher("3.1");
      System.out.println("3.1 is 2x: "+m.matches());
      m = p2x.matcher("1.1");
      System.out.println("1.1 is 2x: "+m.matches());
      m = p2x.matcher("2.1");
      System.out.println("2.1 is 2x: "+m.matches());
   }
}
