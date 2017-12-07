/*
* JBoss, Home of Professional Open Source
* Copyright 2005, Red Hat Middleware LLC., and individual contributors as indicated
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
package org.jboss.ejb3.deployers;

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.ejb3.interceptor.InterceptorInfoRepository;
import org.jboss.ejb3.vfs.impl.vfs2.VirtualFileFilterAdapter;
import org.jboss.ejb3.vfs.impl.vfs2.VirtualFileWrapper;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VisitorAttributes;
import org.jboss.virtual.plugins.context.jar.JarUtils;
import org.jboss.virtual.plugins.vfs.helpers.FilterVirtualFileVisitor;
import org.jboss.virtual.plugins.vfs.helpers.SuffixesExcludeFilter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 104907 $
 */
public class JBoss5DeploymentUnit implements org.jboss.ejb3.DeploymentUnit
{
   private VFSDeploymentUnit unit;
   private ClassLoader classLoader;
   private Map defaultPersistenceProperties;

   public JBoss5DeploymentUnit(VFSDeploymentUnit unit)
   {
      this(unit, unit.getClassLoader());
   }
   
   public JBoss5DeploymentUnit(VFSDeploymentUnit unit, ClassLoader classLoader)
   {
      assert unit != null : "unit is null";
      assert classLoader != null : "classLoader is null";
      
      this.unit = unit;
      this.classLoader = classLoader;
   }

   public Object addAttachment(String name, Object attachment)
   {
      return unit.addAttachment(name, attachment);
   }
   public Object getAttachment(String name)
   {
      return unit.getAttachment(name);
   }
   public Object removeAttachment(String name)
   {
      return unit.removeAttachment(name);
   }

   public org.jboss.ejb3.vfs.spi.VirtualFile getRootFile()
   {
      return new VirtualFileWrapper(unit.getFile(""));
   }
   
   public String getRelativePath()
   {
      return unit.getRelativePath();
   }

   public URL getRelativeURL(String jar)
   {
      try
      {
         return new URL(jar);
      }
      catch (MalformedURLException e)
      {
         try
         {
            if (getUrl() == null)
               throw new RuntimeException("relative <jar-file> not allowed when standalone deployment unit is used");
            return new URL(getUrl(), jar);
         }
         catch (Exception e1)
         {
            throw new RuntimeException("could not find relative path: " + jar, e1);
         }
      }
   }

   URL extractDescriptorUrl(String resource)
   {
      try
      {
         VirtualFile vf = unit.getMetaDataFile(resource);
         if (vf == null) return null;
         return vf.toURL();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public URL getPersistenceXml()
   {
      return extractDescriptorUrl("persistence.xml");
   }

   public URL getEjbJarXml()
   {
      return extractDescriptorUrl("ejb-jar.xml");
   }

   public URL getJbossXml()
   {
      return extractDescriptorUrl("jboss.xml");
   }

   public org.jboss.ejb3.vfs.spi.VirtualFile getMetaDataFile(String name)
   {
      return new VirtualFileWrapper(unit.getMetaDataFile(name));
   }
   
   public List<Class> getClasses()
   {
      return null;
   }

   public ClassLoader getClassLoader()
   {
      return classLoader;
   }

   public ClassLoader getResourceLoader()
   {
      return getClassLoader();
   }

   public String getShortName()
   {
      return unit.getFile("").getName();
   }

   public URL getUrl()
   {
      try
      {
         return unit.getFile("").toURL();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public String getDefaultEntityManagerName()
   {
      String url = getUrl().toString();
      String name = url.substring(url.lastIndexOf('/') + 1, url.lastIndexOf('.'));
      return name;
   }

   public Map getDefaultPersistenceProperties()
   {
      return defaultPersistenceProperties;
   }

   public void setDefaultPersistenceProperties(Map defaultPersistenceProperties)
   {
      this.defaultPersistenceProperties = defaultPersistenceProperties;
   }

   public Hashtable getJndiProperties()
   {
      return null;
   }

   @Deprecated
   public InterceptorInfoRepository getInterceptorInfoRepository()
   {
      throw new IllegalStateException("EJBTHREE-1852: InterceptorInfoRepository must not be used anymore");
   }

   public List<org.jboss.ejb3.vfs.spi.VirtualFile> getResources(org.jboss.ejb3.vfs.spi.VirtualFileFilter filter)
   {
      List<VirtualFile> classPath = unit.getClassPath();
      if(classPath == null || classPath.isEmpty())
         return Collections.emptyList();

      VisitorAttributes va = new VisitorAttributes();
      va.setLeavesOnly(true);
      SuffixesExcludeFilter noJars = new SuffixesExcludeFilter(JarUtils.getSuffixes());
      va.setRecurseFilter(noJars);
      FilterVirtualFileVisitor visitor = new FilterVirtualFileVisitor(new VirtualFileFilterAdapter(filter), va);

      for(VirtualFile root : classPath)
      {
         try
         {
            if( root.isLeaf() == false )
               root.visit(visitor);
         }
         catch (IOException e)
         {
            throw new RuntimeException(e);
         }
      }
      
      final List<VirtualFile> matches = visitor.getMatched();
      final List<org.jboss.ejb3.vfs.spi.VirtualFile> wrappedMatches = new ArrayList<org.jboss.ejb3.vfs.spi.VirtualFile>(matches.size());
      for(VirtualFile match : matches)
      {
         wrappedMatches.add(new VirtualFileWrapper(match));
      }
      return wrappedMatches;
   }
}
