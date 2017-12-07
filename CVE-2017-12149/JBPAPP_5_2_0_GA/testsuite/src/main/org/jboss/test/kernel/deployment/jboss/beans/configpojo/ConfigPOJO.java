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
package org.jboss.test.kernel.deployment.jboss.beans.configpojo;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * A simple pojo to encapsulate configuration settings
 * that we'll bind in jndi.
 *
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81036 $
 */
public class ConfigPOJO implements Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 666L;

   // Private -------------------------------------------------------
   
   private String userid;
   private String passwd;
   private List roles;
   
   // Constructor ---------------------------------------------------
   
   public ConfigPOJO(String userid, String passwd, List roles)
   {
      this.userid = userid;
      this.passwd = passwd;
      this.roles = roles;
   }
   
   // Accessors -----------------------------------------------------
   
   public String getUserId()
   {
      return userid;
   }
   
   public String getPassword()
   {
      return passwd;
   }
  
   public List getRoles()
   {
      return Collections.unmodifiableList(roles);
   }
   
   // Overrides -----------------------------------------------------
   
   public String toString()
   {
      StringBuffer sbuf = new StringBuffer();
      sbuf
      .append(getClass().getName())
      .append("[ userId=").append(userid)
      .append(", passwd=").append(passwd)
      .append(", roles=").append(roles)
      .append(" ]");
      
      return sbuf.toString();
   }
}
