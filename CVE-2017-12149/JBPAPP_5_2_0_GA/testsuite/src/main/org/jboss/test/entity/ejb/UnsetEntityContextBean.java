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
 * A bean to test whether unsetEntityContext is called.
 *
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81036 $
 */
public class UnsetEntityContextBean implements EntityBean
{
   private static final Logger log = Logger.getLogger(UnsetEntityContextBean.class);

   private static int setEntityContextCounter = 0;   
   private static int unsetEntityContextCounter = 0;
   
   private EntityContext entityContext;

   private String name;

   public void setEntityContext(EntityContext context)
   {
      log.info("setEntityContext - " + name);
      ++setEntityContextCounter;
      entityContext = context;
   }
   
   public void unsetEntityContext()
   {
      log.info("unsetEntityContext - " + name);
      ++unsetEntityContextCounter;
      entityContext = null;
   }
   
   public String getName()
   {
      log.info("getName");
      return name;
   }

   public String ejbCreate(String name)
      throws CreateException
   {
      log.info("ejbCreate - " + name);
      this.name = name;
      return name;
   }
	
   public void ejbPostCreate(String name)
      throws CreateException
   {
      log.info("ejbPostCreate - " + name);
   }

   public String ejbFindByPrimaryKey(String name)
   {
      log.info("ejbFindByPrimaryKey - " + name);
      return name;
   }
	
   public void ejbActivate()
   {
      log.info("ejbActivate - " + name);
   }
	
   public void ejbLoad()
   {
      log.info("ejbLoad - " + name);
   }
	
   public void ejbPassivate()
   {
      log.info("ejbPassivate - " + name);
   }
	
   public void ejbRemove()
      throws RemoveException
   {
      log.info("ejbRemove - " + name);
   }
	
   public void ejbStore()
   {
      log.info("ejbStore - " + name);
   }

   public int ejbHomeGetSetEntityContextCallCounter()
   {
      return setEntityContextCounter;
   }

   public int ejbHomeGetUnsetEntityContextCallCounter()
   {
      return unsetEntityContextCounter;
   }
   
   public void ejbHomeClearSetEntityContextCallCounter()
   {
      log.info("ejbHomeClearSetEntityContextCallCounter - " + name);
      setEntityContextCounter = 0;
   }
   
   public void ejbHomeClearUnsetEntityContextCallCounter()
   {
      log.info("ejbHomeClearUnsetEntityContextCallCounter - " + name);      
      unsetEntityContextCounter = 0;
   }
   
}
