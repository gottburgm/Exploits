/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.deployment;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.metadata.annotation.creator.client.ApplicationClient5MetaDataCreator;
import org.jboss.metadata.annotation.creator.ejb.jboss.JBoss50Creator;
import org.jboss.metadata.annotation.creator.web.Web25MetaDataCreator;
import org.jboss.metadata.annotation.finder.AnnotationFinder;
import org.jboss.metadata.annotation.finder.DefaultAnnotationFinder;
import org.jboss.metadata.client.spec.ApplicationClientMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.spec.EjbJar3xMetaData;
import org.jboss.metadata.ejb.spec.EjbJarMetaData;
import org.jboss.metadata.web.spec.Web25MetaData;
import org.jboss.metadata.web.spec.WebMetaData;
import org.jboss.virtual.VFSUtils;
import org.jboss.virtual.VirtualFile;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestAttributeListener;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionListener;
import javax.servlet.jsp.tagext.SimpleTag;
import javax.servlet.jsp.tagext.Tag;

/**
 * A POST_CLASSLOADER deployer which generates metadata from
 * annotations
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 112204 $
 */
public class AnnotationMetaDataDeployer extends AbstractDeployer
{
   public static final String EJB_ANNOTATED_ATTACHMENT_NAME = "annotated."+EjbJarMetaData.class.getName();
   public static final String CLIENT_ANNOTATED_ATTACHMENT_NAME = "annotated."+ApplicationClientMetaData.class.getName();
   public static final String WEB_ANNOTATED_ATTACHMENT_NAME = "annotated."+WebMetaData.class.getName();

   private boolean metaDataCompleteIsDefault = false;

   /**
    * Strict servlet spec compliance implies that only a limited set of classes in a web application will be
    * checked for annotations. The type of classes eligible for such annotation scanning is listed in the Servlet specification.
    * Along with those classes, JSF managed bean classes will be included too.
    * <p/>
    * If strict servlet spec compliance is disabled (i.e. the flag is false), then all classes in a web application will
    * be checked for annotations and can lead to side effects like picking up resource injection annotations on non-Java EE
    * components.
    * 
    * @see JBAS-8318 JIRA for details
    */
   private boolean strictServletSpecCompliance = false;

   public AnnotationMetaDataDeployer()
   {
      setStage(DeploymentStages.POST_CLASSLOADER);
      addInput(EjbJarMetaData.class);
      addInput(WebMetaData.class);
      addInput(ApplicationClientMetaData.class);
      // we need the JSFDeployment information to know of any JSF managed bean classes within a deployment
      addInput(JSFDeployment.class);
      addOutput(EJB_ANNOTATED_ATTACHMENT_NAME);
      addOutput(CLIENT_ANNOTATED_ATTACHMENT_NAME);
      addOutput(WEB_ANNOTATED_ATTACHMENT_NAME);
   }

   public boolean isMetaDataCompleteIsDefault()
   {
      return metaDataCompleteIsDefault;
   }
   public void setMetaDataCompleteIsDefault(boolean metaDataCompleteIsDefault)
   {
      this.metaDataCompleteIsDefault = metaDataCompleteIsDefault;
   }

   public void deploy(DeploymentUnit unit) throws DeploymentException
   {
      if (unit instanceof VFSDeploymentUnit == false)
         return;
      
      VFSDeploymentUnit vfsDeploymentUnit = (VFSDeploymentUnit) unit;
      deploy(vfsDeploymentUnit);
   }

   public void undeploy(DeploymentUnit unit)
   {
      if (unit instanceof VFSDeploymentUnit == false)
         return;
      
      VFSDeploymentUnit vfsDeploymentUnit = (VFSDeploymentUnit) unit;
      undeploy(vfsDeploymentUnit);
   }

   /**
    * Process the 
    * 
    * @param unit the unit
    * @throws DeploymentException for any error
    */
   protected void deploy(VFSDeploymentUnit unit)
      throws DeploymentException
   {
      /* Ignore any spec metadata complete deployments. This expects that a
       deployment unit only represents one of the client, ejb or web
       deployments and its metadata completeness applies to the unit in terms
       of whether annotations should be scanned for.
       */
      boolean isComplete = this.isMetaDataCompleteIsDefault();
      EjbJarMetaData ejbJarMetaData = unit.getAttachment(EjbJarMetaData.class);
      if(ejbJarMetaData != null && ejbJarMetaData instanceof EjbJar3xMetaData)
      {
         isComplete |= ((EjbJar3xMetaData) ejbJarMetaData).isMetadataComplete();
      }
      else if(ejbJarMetaData != null)
      {
         // Any ejb-jar.xml 2.1 or earlier deployment is metadata complete
         isComplete = true;         
      }
      WebMetaData webMetaData = unit.getAttachment(WebMetaData.class);
      if(webMetaData != null && webMetaData instanceof Web25MetaData)
      {
         isComplete |= ((Web25MetaData)webMetaData).isMetadataComplete();
      }
      else if(webMetaData != null)
      {
         // Any web.xml 2.4 or earlier deployment is metadata complete
         isComplete = true;
      }
      ApplicationClientMetaData clientMetaData = unit.getAttachment(ApplicationClientMetaData.class);
      if(clientMetaData != null)
         isComplete |= clientMetaData.isMetadataComplete();

      if(isComplete)
      {
         log.debug("Deployment is metadata-complete, skipping annotation processing"
               + ", ejbJarMetaData="+ejbJarMetaData
               + ", jbossWebMetaData="+webMetaData
               + ", jbossClientMetaData="+clientMetaData
               + ", metaDataCompleteIsDefault="+metaDataCompleteIsDefault
               );
         return;
      }

      VirtualFile root = unit.getRoot();
      boolean isLeaf = true;
      try
      {
         isLeaf = root.isLeaf();
      }
      catch(IOException ignore)
      {
      }
      if(isLeaf == true)
         return;

      List<VirtualFile> classpath = unit.getClassPath();
      if(classpath == null || classpath.isEmpty())
         return;

      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("Deploying annotations for unit: " + unit + ", classpath: " + classpath);

      try
      {
         processMetaData(unit, webMetaData, clientMetaData, classpath);
      }
      catch (Exception e)
      {
         throw DeploymentException.rethrowAsDeploymentException("Cannot process metadata", e);
      }
   }

   /**
    * Process metadata.
    *
    * @param unit the deployment unit
    * @param webMetaData the web metadata
    * @param clientMetaData the client metadata
    * @param classpath the classpath
    * @throws DeploymentException for any error
    */
   protected void processMetaData(VFSDeploymentUnit unit, WebMetaData webMetaData, ApplicationClientMetaData clientMetaData, List<VirtualFile> classpath) throws Exception
   {
      String mainClassName = getMainClassName(unit);
      Collection<Class<?>> classes = getClasses(unit, mainClassName, classpath);
      if (classes.size() > 0)
      {
         AnnotationFinder<AnnotatedElement> finder = new DefaultAnnotationFinder<AnnotatedElement>();
         if (webMetaData != null)
            processJBossWebMetaData(unit, finder, classes);
         else if (clientMetaData != null || mainClassName != null)
            processJBossClientMetaData(unit, finder, classes);
         else
            processJBossMetaData(unit, finder, classes);
      }
   }

   /**
    * Get the classes we want to scan.
    *
    * @param unit the deployment unit
    * @param mainClassName the main class name
    * @param classpath the classpath
    * @return possible classes containing metadata annotations
    * @throws IOException for any error
    */
   protected Collection<Class<?>> getClasses(VFSDeploymentUnit unit, String mainClassName, List<VirtualFile> classpath) throws IOException
   {
      Map<VirtualFile, Class<?>> classpathClasses = new HashMap<VirtualFile, Class<?>>();
      for(VirtualFile path : classpath)
      {
         AnnotatedClassFilter classVisitor = new AnnotatedClassFilter(unit, unit.getClassLoader(), path, mainClassName);
         path.visit(classVisitor);
         Map<VirtualFile, Class<?>> classes = classVisitor.getAnnotatedClasses();
         if(classes != null && classes.size() > 0)
         {
            if(log.isTraceEnabled())
               log.trace("Annotated classes: " + classes);
            classpathClasses.putAll(classes);
         }
      }
      return classpathClasses.values();
   }

   /**
    * Undeploy a vfs deployment
    * 
    * @param unit the unit
    */
   protected void undeploy(VFSDeploymentUnit unit)
   {
      // Nothing
   }

   /**
    * Process annotations.
    *
    * @param unit the deployment unit
    * @param finder the annotation finder
    * @param classes the candidate classes
    */
   protected void processJBossMetaData(VFSDeploymentUnit unit,
         AnnotationFinder<AnnotatedElement> finder, Collection<Class<?>> classes)
   {
      // Create the metadata model from the annotations
      JBoss50Creator creator = new JBoss50Creator(finder);
      JBossMetaData annotationMetaData = creator.create(classes);
      if(annotationMetaData != null)
         unit.addAttachment(EJB_ANNOTATED_ATTACHMENT_NAME, annotationMetaData, JBossMetaData.class);
   }

   /**
    * Process annotations.
    *
    * @param unit the deployment unit
    * @param finder the annotation finder
    * @param classes the candidate classes
    */
   protected void processJBossWebMetaData(VFSDeploymentUnit unit,
         AnnotationFinder<AnnotatedElement> finder, Collection<Class<?>> classes)
   {
      Web25MetaDataCreator creator = new Web25MetaDataCreator(finder);
      // initialize resource injection eligible classes to all classes
      Collection<Class<?>> resourceInjectionEligibleClasses = classes;
      if (this.strictServletSpecCompliance)
      {
         // The spec states that resource injection is eligible only on a certain set of classes, so filter out
         // the rest of the classes
         resourceInjectionEligibleClasses = this.getResourceInjectionEligibleWebAppClasses(unit, classes);
      }
      if (resourceInjectionEligibleClasses.isEmpty())
      {
         log.debug("No Java EE injection eligible classes found in web application: " + unit);
         return;
      }
      WebMetaData annotationMetaData = creator.create(resourceInjectionEligibleClasses);
      if(annotationMetaData != null)
         unit.addAttachment(WEB_ANNOTATED_ATTACHMENT_NAME, annotationMetaData, WebMetaData.class);
   }

   /**
    * Process annotations.
    *
    * @param unit the deployment unit
    * @param finder the annotation finder
    * @param classes the candidate classes
    */
   protected void processJBossClientMetaData(VFSDeploymentUnit unit,
         AnnotationFinder<AnnotatedElement> finder, Collection<Class<?>> classes)
   {
      ApplicationClient5MetaDataCreator creator = new ApplicationClient5MetaDataCreator(finder);
      ApplicationClientMetaData annotationMetaData = creator.create(classes);
      if(annotationMetaData != null)
         unit.addAttachment(CLIENT_ANNOTATED_ATTACHMENT_NAME, annotationMetaData, ApplicationClientMetaData.class);      
   }

   /**
    * Get main class from manifest.
    *
    * @param unit the deployment unit
    * @return main class name
    * @throws IOException for any error
    */
   protected String getMainClassName(VFSDeploymentUnit unit)
      throws IOException
   {
      VirtualFile file = unit.getMetaDataFile("MANIFEST.MF");
      if (log.isTraceEnabled())
         log.trace("parsing " + file);

      if(file == null)
      {
         return null;
      }

      try
      {
         Manifest mf = VFSUtils.readManifest(file);
         Attributes attrs = mf.getMainAttributes();
         return attrs.getValue(Attributes.Name.MAIN_CLASS);
      }
      finally
      {
         file.close();
      }
   }

   /**
    * Returns true if the deployer is set to follow strict servlet compliance
    * during annotation scanning. Else returns false.
    *
    * @see #strictServletSpecCompliance
    * @return
    */
   public boolean isStrictServletSpecCompliance()
   {
      return this.strictServletSpecCompliance;
   }

   /**
    * Sets the strict servlet compliance flag on this deployer. The flag will be used during annotation scanning of
    * classes and limits the number of classes eligible for annotation scanning, if this flag is set to true.
    * 
    * @param strictCompliance
    * @see #strictServletSpecCompliance
    */
   public void setStrictServletSpecCompliance(boolean strictCompliance)
   {
      this.strictServletSpecCompliance = strictCompliance;
   }

   /**
    * Returns a subset of classes which are eligible for annotation scanning in a web application. The type of classes
    * eligible for annotation scanning in a web application is listed in the Servlet spec. Additionally, this method also
    * includes JSF managed beans as eligible for annotation scanning.
    * 
    * @param unit The deployment unit being processed
    * @param classes Collection of all classes that will be scanned for annotations. This method filters out those
    *                classes based on the limited types of classes which are eligible for annotation scanning.
    * @return
    */
   protected Collection<Class<?>> getResourceInjectionEligibleWebAppClasses(DeploymentUnit unit, Collection<Class<?>> classes)
   {
      Set<Class<?>> eligibleClasses = new HashSet<Class<?>>();
      if (classes == null || classes.isEmpty()) {
         return eligibleClasses;
      }
      // get the JSFDeployment metadata
      final JSFDeployment jsfDeployment = unit.getAttachment(JSFDeployment.class);
      final Collection<Class<?>> jsfManagedBeanClasses = this.loadJSFManagedBeanClasses(jsfDeployment, unit);
      for (Class<?> klass : classes)
      {
         if (klass == null)
         {
            continue;
         }
         // javax.servlet.Servlet type classes
         if (Servlet.class.isAssignableFrom(klass))
         {
            eligibleClasses.add(klass);
         }
         else if (Filter.class.isAssignableFrom(klass)) // javax.servlet.Filter classes
         {
            eligibleClasses.add(klass);
         }
         else if (ServletContextListener.class.isAssignableFrom(klass)) // javax.servlet.ServletContextListener classes
         {
            eligibleClasses.add(klass);
         }
         else if (ServletContextAttributeListener.class.isAssignableFrom(klass)) // javax.servlet.ServletContextAttributeListener classes
         {
            eligibleClasses.add(klass);
         }
         else if (ServletRequestListener.class.isAssignableFrom(klass)) // javax.servlet.ServletRequestListener classes
         {
            eligibleClasses.add(klass);
         }
         else if (ServletRequestAttributeListener.class.isAssignableFrom(klass)) // javax.servlet.ServletRequestAttributeListener classes
         {
            eligibleClasses.add(klass);
         }
         else if (HttpSessionListener.class.isAssignableFrom(klass)) // javax.servlet.http.HttpSessionListener classes
         {
            eligibleClasses.add(klass);
         }
         else if (HttpSessionAttributeListener.class.isAssignableFrom(klass)) // javax.servlet.http.HttpSessionAttributeListener classes
         {
            eligibleClasses.add(klass);
         }
         else if (Tag.class.isAssignableFrom(klass)) // javax.servlet.jsp.tagext.Tag classes
         {
            eligibleClasses.add(klass);
         }
         else if (SimpleTag.class.isAssignableFrom(klass)) // javax.servlet.jsp.tagext.SimpleTag classes
         {
            eligibleClasses.add(klass);
         }
         else
         {
             // JSF managed bean
            for (final Class<?> jsfManagedBeanClass : jsfManagedBeanClasses) {
               if (klass.isAssignableFrom(jsfManagedBeanClass)) {
                  eligibleClasses.add(klass);
                  break;
               }
            }
         }
      }

      return eligibleClasses;
   }

   private Collection<Class<?>> loadJSFManagedBeanClasses(final JSFDeployment jsfDeployment, final DeploymentUnit unit) {
      if (jsfDeployment == null) {
         return Collections.emptySet();
      }
      final Collection<String> managedBeanClassNames = jsfDeployment.getManagedBeans();
      if (managedBeanClassNames == null || managedBeanClassNames.isEmpty()) {
         return Collections.emptySet();
      }
      final ClassLoader cl = unit.getClassLoader();
      final Collection<Class<?>> managedBeanClasses = new HashSet<Class<?>>(managedBeanClassNames.size());
      for (final String managedBeanClassName : managedBeanClassNames) {
         try
         {
            final Class<?> managedBeanClass = Class.forName(managedBeanClassName, false, cl);
            managedBeanClasses.add(managedBeanClass);
         }
         catch (ClassNotFoundException cnfe)
         {
            // ignore the CNFE. The JSF spec allows non-existent classes to be referred in the faces-config.xml
            // and until that JSF managed bean is accessed in the application, it doesn't fail that application.
            // So we just ignore that CNFE and effectively the managed bean
            log.debug("Ignoring JSF managed bean class " + managedBeanClassName + " since the class cannot be found " +
                    "in classloader " + cl + " of unit " + unit);
         }
      }
      return managedBeanClasses;
   }

}

