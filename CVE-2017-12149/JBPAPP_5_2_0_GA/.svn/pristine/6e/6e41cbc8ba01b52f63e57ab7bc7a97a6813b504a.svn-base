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
package org.jboss.test.jca.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jboss.logging.Logger;

import org.jboss.resource.adapter.jdbc.URLSelectorStrategy;

/**
 * MyURLSelector
 *
 * @author <a href="mailto:vkak@redhat.com">Vicky Kak</a>
 * 
 */

   public class MyURLSelector implements URLSelectorStrategy
   {
	  protected final Logger log = Logger.getLogger(getClass());
      private final List urls;
      private int urlIndex;
      private String url;

      public MyURLSelector(List urls)
      {
         if(urls == null || urls.size() == 0)
         {
            throw new IllegalStateException("Expected non-empty list of connection URLs but got: " + urls);
         }
         this.urls = Collections.unmodifiableList(urls);
		 log.debug("Constructed MyURLSelector");
      }

      public synchronized String getUrl()
      {
         if(url == null)
         {
            if(urlIndex == urls.size())
            {
               urlIndex = 0;
            }
            url = (String)urls.get(urlIndex++);
         }
		 log.debug("Getting url from the MyURLSelector");
         return url;
      }

      public synchronized void failedUrl(String url)
      {
         if(url.equals(this.url))
         {
            this.url = null;
         }
      }

      public List getUrlList()
      {
         return urls;
      }

	  /* URLSelectorStrategy Implementation goes here*/
	  public List getCustomSortedUrls()
	  {
		 return urls;
	  }
	  public void failedUrlObject(Object urlObject)
	  {
		 failedUrl((String)urlObject);
	  }
	  public List getAllUrlObjects()
	  {
		 return urls;
      }
	  public Object getUrlObject()
	  {
		 return getUrl();
	  }

   }
