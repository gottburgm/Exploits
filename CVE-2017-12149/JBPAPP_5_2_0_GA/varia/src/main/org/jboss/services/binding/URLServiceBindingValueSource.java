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

package org.jboss.services.binding;

import java.net.URL;

/**
 * A {@link ServiceBindingValueSource} that returns a URL or a String
 * representation of one.
 * <p>
 * Typical usage is in file transformation operations, where the content
 * of a given <code>input</code> URL or classpath resource is read,
 * transformed, written to a temp file, and the URL of the temp file returned.
 * </p>
 * 
 * @author Brian Stansberry
 * @version $Revision: 85945 $
 */
public interface URLServiceBindingValueSource extends ServiceBindingValueSource
{
   /**
    * Returns the URL to use for the binding value.
    * 
    * @param binding the binding. Cannot be <code>null</code>
    * @param input the URL to use as input data
    *  
    * @return a URL to use as the binding value. Will not return <code>null</code>.
    */
   URL getURLServiceBindingValue(ServiceBinding binding, URL input);
   
   /**
    * Returns a String representation of a URL path to use for the binding value.
    * 
    * @param binding the binding. Cannot be <code>null</code>
    * @param input either a String representation of a URL or a value that
    *              can be passed to {@link ClassLoader#getResourceAsStream(String)}
    *              
    * @return a filesystem path to use as the binding value. May return <code>null</code>.
    */
   String getResourceServiceBindingValue(ServiceBinding binding, String input);
}
