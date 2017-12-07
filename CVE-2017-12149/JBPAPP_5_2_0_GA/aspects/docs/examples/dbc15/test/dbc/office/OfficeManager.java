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

import java.util.ArrayList;

/**
 *
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision: 80997 $
 */
@org.jboss.aspects.dbc.Dbc
@org.jboss.aspects.dbc.Invariant ({"$tgt.computers != null", "$tgt.developers != null", "forall test.dbc.office.Computer c in $tgt.computers | c != null", "forall d in $tgt.developers | d != null"})
public class OfficeManager
{
   ArrayList computers = new ArrayList();
   ArrayList developers = new ArrayList();

   /**
    * PostCond: The computer should be unassigned after adding
    */
   @org.jboss.aspects.dbc.PostCond ({"exists test.dbc.office.Computer c in $tgt.computers | c.getDeveloper() == null && c == $rtn"})
   public Computer createComputer(String name)
   {
      Computer computer = new Computer(name);
      computers.add(computer);
      return computer;
   }

   /**
    * PostCond: The developer should not have a computer after adding
    */
   @org.jboss.aspects.dbc.PostCond ({"exists test.dbc.office.Developer d in $tgt.developers | d.getComputer() == null && d == $rtn"})
   public Developer createDeveloper(String name)
   {
      Developer developer = new Developer(name);
      developers.add(developer);
      return developer;
   }

   /**
    * PreCond: The computer and developer must both be not-null, and not previously assigned
    * PostCond: Make sure that all developers have a computer, and that that computer is
    * associated with the developer in question
    */
   @org.jboss.aspects.dbc.PreCond ({"$0 != null", "$1 != null", "exists test.dbc.office.Computer c in $tgt.computers | c.getDeveloper() == null && c == $0", "exists test.dbc.office.Developer d in $tgt.developers | d.getComputer() == null && d == $1"})
   @org.jboss.aspects.dbc.PostCond ({"forall d in $tgt.developers | exists c in $tgt.computers | (c == d.getComputer() && d == c.getDeveloper())"})
   public void assignComputer(Computer computer, Developer developer)
   {
      computer.setDeveloper(developer);
      developer.setComputer(computer);
   }

}
