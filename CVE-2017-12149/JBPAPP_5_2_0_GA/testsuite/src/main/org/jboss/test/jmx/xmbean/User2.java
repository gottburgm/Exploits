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
package org.jboss.test.jmx.xmbean;

import java.util.Date;
import org.jboss.logging.Logger;

/** An alternate non-xdoclet JBoss model mbean
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class User2
{
   private Logger log = Logger.getLogger(User2.class);

   private String attr1;
   private int attr2;

   public User2()
   {
   }
   public User2(String attr1)
   {
      this.attr1 = attr1;
   }

   public Integer getHashCode()
   {
      return new Integer(hashCode());
   }

   public String getAttr1()
   {
      return attr1;
   }
   public void setAttr1(String attr1)
   {
      this.attr1 = attr1;
   }

   public int getAttr2()
   {
      return attr2;
   }
   public void setAttr2(int attr2)
   {
      this.attr2 = attr2;
   }

   public void start() throws Exception
   {
      log.info("Started");
   }
   public void stop() throws Exception
   {
      log.info("Stopped");
   }

   public void noop()
   {
   }
   public String echoDate(String prefix)
   {
      return prefix + ": " + new Date();
   }

}
