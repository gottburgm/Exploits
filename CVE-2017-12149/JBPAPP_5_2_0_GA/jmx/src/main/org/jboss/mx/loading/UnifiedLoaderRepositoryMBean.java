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
package org.jboss.mx.loading;

import java.net.URL;

import org.jboss.util.loading.Translator;


/**
 * UnifiedLoaderRepositoryMBean.java
 *
 *
 * Created: Sun Apr 14 13:04:04 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 */

public interface UnifiedLoaderRepositoryMBean 
{
   public RepositoryClassLoader newClassLoader(final URL url, boolean addToRepository)
      throws Exception;
   public RepositoryClassLoader newClassLoader(final URL url, final URL origURL, boolean addToRepository)
      throws Exception;

   public void removeClassLoader(ClassLoader cl);

   public LoaderRepository registerClassLoader(RepositoryClassLoader ucl);

   public RepositoryClassLoader getWrappingClassLoader(ClassLoader cl);
   
   public LoaderRepository getInstance();

   public URL[] getURLs();

   // Aspect stuff
   public Translator getTranslator();
   public void setTranslator(Translator t);

}// UnifiedLoaderRepositoryMBean
