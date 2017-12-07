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
package org.jboss.web.tomcat.metadata;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 85945 $
 */
public class SessionCookieMetaData implements Serializable
{
   private String domain = null;
   private String path = null;
   private String comment = null;
   private boolean httpOnly = false;
   private boolean secure = false;

   public String getDomain()
   {
      return domain;
   }
   @XmlAttribute(name = "domain")
   public void setDomain(String domain)
   {
      this.domain = domain;
   }

   public String getPath()
   {
      return path;
   }
   @XmlAttribute(name = "path")
   public void setPath(String path)
   {
      this.path = path;
   }

   public String getComment()
   {
      return comment;
   }
   @XmlAttribute(name = "comment")
   public void setComment(String comment)
   {
      this.comment = comment;
   }

   public boolean getHttpOnly()
   {
      return httpOnly;
   }
   @XmlAttribute(name = "httpOnly")
   public void setHttpOnly(boolean httpOnly)
   {
      this.httpOnly = httpOnly;
   }

   public boolean getSecure()
   {
      return secure;
   }
   @XmlAttribute(name = "secure")
   public void setSecure(boolean secure)
   {
      this.secure = secure;
   }
   
}
