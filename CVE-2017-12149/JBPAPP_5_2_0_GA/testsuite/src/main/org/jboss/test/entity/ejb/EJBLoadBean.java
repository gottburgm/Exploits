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
package org.jboss.test.entity.ejb;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;

import org.jboss.logging.Logger;

/**
 * A bean to test whether ejb load was called.
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 81036 $
 */
public class EJBLoadBean
   implements EntityBean
{
   private static final Logger log = Logger.getLogger(EJBLoadBean.class);

   private EntityContext entityContext;

   private String name;

   private boolean ejbLoadCalled = false;

   private boolean active = false;

   public String getName()
   {
      log.info("getName");
      assertActive();
      return name;
   }

   public boolean wasEJBLoadCalled()
   {
      log.info("wasEJBLoadCalled");
      assertActive();
      boolean result = ejbLoadCalled;
      ejbLoadCalled = false;
      return result;
   }

   public void noTransaction()
   {
      log.info("noTransaction");
      assertActive();
      ejbLoadCalled = false;
   }
	
   public String ejbCreate(String name)
      throws CreateException
   {
      assertActive();
      log.info("ejbCreate");
      this.name = name;
      return name;
   }
	
   public void ejbPostCreate(String name)
      throws CreateException
   {
      assertActive();
      log.info("ejbPostCreate");
   }

   public String ejbFindByPrimaryKey(String name)
   {
      log.info("ejbFindByPrimaryKey");
      return name;
   }
	
   public void ejbActivate()
   {
      log.info("ejbActivate");
      active = true;
   }
	
   public void ejbLoad()
   {
      log.info("ejbLoad");
      ejbLoadCalled = true;
      assertActive();
   }
	
   public void ejbPassivate()
   {
      log.info("ejbPassivate");
      assertActive();
      active = false;
   }
	
   public void ejbRemove()
      throws RemoveException
   {
      log.info("ejbRemove");
      assertActive();
      active = false;
   }
	
   public void ejbStore()
   {
      log.info("ejbStore");
      assertActive();
   }
	
   public void setEntityContext(EntityContext context)
   {
      log.info("setEntityContext");
      entityContext = context;
   }
	
   public void unsetEntityContext()
   {
      log.info("unsetEntityContext");
      entityContext = null;
   }

   private void assertActive()
   {
      if (active == false)
         throw new RuntimeException("The bean is not active");
   }
}
