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
package org.jboss.test.cmp2.batchcascadedelete.test;

import org.jboss.test.JBossTestCase;
import org.jboss.test.cmp2.batchcascadedelete.ejb.Child;
import org.jboss.test.cmp2.batchcascadedelete.ejb.ChildHome;
import org.jboss.test.cmp2.batchcascadedelete.ejb.ChildUtil;
import org.jboss.test.cmp2.batchcascadedelete.ejb.GrandchildHome;
import org.jboss.test.cmp2.batchcascadedelete.ejb.GrandchildUtil;
import org.jboss.test.cmp2.batchcascadedelete.ejb.Parent;
import org.jboss.test.cmp2.batchcascadedelete.ejb.ParentHome;
import org.jboss.test.cmp2.batchcascadedelete.ejb.ParentUtil;
import junit.framework.Test;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 81036 $</tt>
 */
public class BatchCascadeDeleteUnitTestCase
   extends JBossTestCase
{
   /**
    * Constructor for the JBossTestCase object
    *
    * @param name Test case name
    */
   public BatchCascadeDeleteUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      return JBossTestCase.getDeploySetup(BatchCascadeDeleteUnitTestCase.class, "cmp2-batchcascadedelete.jar");
   }

   public void testCase4540() throws Exception
   {
      ParentHome parentHome = ParentUtil.getHome();
      Parent parent = parentHome.create("parent");

      ChildHome childHome = ChildUtil.getHome();
      Child child = childHome.create( parent, "child");

      GrandchildHome grandchildHome = GrandchildUtil.getHome();

      // If you comment out the next line, then the call to child.remove() works fine.
      grandchildHome.create(child.getId(), "grandchild");

      // this is the test for non-null foreign key
      child.remove();
   }
}
