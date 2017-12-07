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
package org.jboss.test.cmp2.cacheinvalidation.ejb;


import org.jboss.logging.Logger;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 81036 $</tt>
 */
public class FacadeSessionBean
   implements SessionBean
{
   private static final Logger log = Logger.getLogger(FacadeSessionBean.class);
   SessionContext ctx;

   private transient CLocalHome ch;
   private transient ALocalHome ah;

   // Business methods

   /**
    * @ejb.interface-method
    * @ejb.transaction type="RequiresNew"
    */
   public void setup() throws Exception
   {
      CLocal c = getCLocalHome("CRWLocal").create(new Long(1));
      c.setFirstName("Avoka");

      ALocal a = getALocalHome("ARWLocal").create(new Long(2), "Ataka");
      a.setC(c);
   }

   /**
    * @ejb.interface-method
    * @ejb.transaction type="RequiresNew"
    */
   public void tearDown() throws Exception
   {
      try
      {
         CLocal c = getCLocalHome("CRWLocal").findByPrimaryKey(new Long(1));
         c.remove();
      }
      catch(FinderException e)
      {
      }

      try
      {
         ALocal a = getALocalHome("ARWLocal").findByPrimaryKey(new Long(2));
         a.remove();
      }
      catch(FinderException e)
      {
      }
   }

   /**
    * @ejb.interface-method
    * @ejb.transaction type="RequiresNew"
    */
   public String readFirstName(String jndiName, Long id) throws Exception
   {
      final CLocalHome ch = (CLocalHome) getHome(jndiName);
      CLocal c = ch.findByPrimaryKey(id);

      final String firstName = c.getFirstName();
      log.debug(jndiName + ".name=" + firstName);
      return firstName;
   }

   /**
    * @ejb.interface-method
    * @ejb.transaction type="RequiresNew"
    */
   public void writeFirstName(String jndiName, Long id, String name) throws Exception
   {
      final CLocalHome ch = (CLocalHome) getHome(jndiName);
      CLocal c = ch.findByPrimaryKey(id);

      c.setFirstName(name);
      log.debug(jndiName + ".name=" + c.getFirstName());
   }

   /**
    * @ejb.interface-method
    * @ejb.transaction type="RequiresNew"
    */
   public String readRelatedAFirstName(String jndiName, Long id) throws Exception
   {
      final CLocalHome ch = (CLocalHome) getHome(jndiName);
      CLocal c = ch.findByPrimaryKey(id);

      final String firstName = c.getA() == null ? null : c.getA().getName();
      log.debug(jndiName + ".a.name=" + firstName);
      return firstName;
   }

   /**
    * @ejb.interface-method
    * @ejb.transaction type="RequiresNew"
    */
   public void removeA(String jndiName, Long id) throws Exception
   {
      final ALocalHome ah = (ALocalHome) getHome(jndiName);
      ALocal a = ah.findByPrimaryKey(id);
      a.remove();
   }

// SessionBean implementation

   /**
    * @throws CreateException Description of Exception
    * @ejb.create-method
    */
   public void ejbCreate() throws CreateException
   {
   }

   public void ejbActivate()
   {
   }

   public void ejbPassivate()
   {
   }

   public void ejbRemove()
   {
   }

   public void setSessionContext(SessionContext ctx)
   {
      this.ctx = ctx;
   }

   private CLocalHome getCLocalHome(String jndiName)
   {
      if(ch == null)
      {
         ch = (CLocalHome) getHome(jndiName);
      }
      return ch;
   }

   private ALocalHome getALocalHome(String jndiName)
   {
      if(ah == null)
      {
         ah = (ALocalHome) getHome(jndiName);
      }
      return ah;
   }

   private Object getHome(String jndiName)
   {
      try
      {
         InitialContext ctx = new InitialContext();
         return ctx.lookup(jndiName);
      }
      catch(NamingException e)
      {
         throw new IllegalStateException("Failed to look up home " + jndiName + ": " + e.getMessage());
      }
   }
}
