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
package test.dbc.office;

/**
 *
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision: 80997 $
 */
@org.jboss.aspects.dbc.Dbc
@org.jboss.aspects.dbc.Invariant ({"$tgt.name != null"})
public class Developer
{
   String name;
   Computer computer;

   @org.jboss.aspects.dbc.PreCond ({"$0 != null"})
   @org.jboss.aspects.dbc.PostCond ({"$0 == $tgt.name"})
   public Developer(String name)
   {
      this.name = name;
   }

   @org.jboss.aspects.dbc.PostCond ({"$rtn != null implies ($rtn.getDeveloper() == null) ||($rtn.getDeveloper() == $tgt)"})
   public Computer getComputer()
   {
      return computer;
   }

   @org.jboss.aspects.dbc.PostCond ({"$0 == $tgt.getComputer()"})
   public void setComputer(Computer computer)
   {
      this.computer = computer;
   }

}
