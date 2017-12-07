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
package org.jboss.test.hibernate.timers.interfaces;

import java.io.Serializable;
import java.util.Properties;

/**
 Example timer info object.

 @author Scott.Stark@jboss.org
 @version $Revision: 81036 $
 */
public class Info implements Serializable
{
   private static final long serialVersionUID = 1L;

   private Properties props;

   public Info()
   {      
   }
   public Info(Properties props)
   {
      this.props = props;
   }

   public Properties getProps()
   {
      return props;
   }
   public void setProps(Properties props)
   {
      this.props = props;
   }

   public int hashCode()
   {
      return props.hashCode();
   }
   public boolean equals(Object o)
   {
      Info i = (Info) o;
      return props.equals(i.props);
   }


   public String toString()
   {
      final StringBuffer sb = new StringBuffer();
      sb.append("Info");
      sb.append("{props=");
      sb.append(props);
      sb.append('}');
      return sb.toString();
   }
}
