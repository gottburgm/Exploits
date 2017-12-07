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

package org.jboss.services.binding.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration object for a ServiceBindingValueSource that uses
 * XSL Transformation.
 * 
 * @author Brian Stansberry
 * @version $Revision: 85945 $
 */
public class XSLTServiceBindingValueSourceConfig
{
   private final String xslt;
   private final HashMap<String, String> additionalAttributes = new HashMap<String, String>();
   
   public XSLTServiceBindingValueSourceConfig(String xslt)
   {
      this(xslt, null);      
   }
   
   public XSLTServiceBindingValueSourceConfig(String xslt, Map<String, String> additionalAttributes)
   {
      if (xslt == null)
         throw new IllegalArgumentException("xslt is null");
      this.xslt = xslt;     
      
      if (additionalAttributes != null)
         this.additionalAttributes.putAll(additionalAttributes);
   }

   public String getXslt()
   {
      return xslt;
   }
   
   public Map<String, String> getAdditionalAttributes()
   {
      return Collections.unmodifiableMap(additionalAttributes);
   }
   
}
