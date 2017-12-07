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
package org.jboss.test.cmp2.cmrtree.ejb;

import java.util.Collection;
import org.jboss.logging.Logger;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.CreateException;

/**
 * @ejb:bean type="Stateless"
 * name="Facade"
 * view-type="remote"
 * @ejb.util generate="physical"
 * @ejb:transaction type="Required"
 * @ejb:transaction-type type="Container"
 *
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 81036 $</tt>
 */
public class FacadeSessionBean
   implements SessionBean
{
   private static Logger log = Logger.getLogger(FacadeSessionBean.class);

   SessionContext ctx;

   // Business methods

   /**
    * @ejb.interface-method
    * @ejb.transaction type="RequiresNew"
    */
   public void setup() throws Exception
   {
      final long startTime = System.currentTimeMillis();
      log.debug("SETUP>");

      ALocal a = AUtil.getLocalHome().create(1, "A", "1.A");
      BLocal b1 = BUtil.getLocalHome().create(1, "B1", "1.B1");
      b1.setAMinorId("A");

      BLocal b2 = BUtil.getLocalHome().create(1, "B2", "1.B2");
      b2.setParent(b1);

      log.debug("SETUP> done in " + (System.currentTimeMillis() - startTime) + " ms.");
   }

   /**
    * @ejb.interface-method
    * @ejb.transaction type="RequiresNew"
    */
   public void test(long sleep) throws Exception
   {
      final long startTime = System.currentTimeMillis();
      log.debug("RUN>");

      AUtil.getLocalHome().remove(new APK(1, "A"));

      log.debug("RUN> done in " + (System.currentTimeMillis() - startTime) + " ms.");
   }

   /**
    * @ejb.interface-method
    * @ejb.transaction type="RequiresNew"
    */
   public void tearDown() throws Exception
   {
      final long startTime = System.currentTimeMillis();
      log.debug("TEAR DOWN>");

      log.debug("TEAR DOWN> done in " + (System.currentTimeMillis() - startTime) + " ms.");
   }

   /**
    * @ejb.interface-method
    * @ejb.transaction type="RequiresNew"
    */
   public void setup2() throws Exception
   {
      final long startTime = System.currentTimeMillis();
      log.debug("SETUP2>");

      ALocal a = AUtil.getLocalHome().create(1, "A", "1.A");
      BLocal b1 = BUtil.getLocalHome().create(1, "B1", "some name");
      b1.setA(a);

      log.debug("SETUP2> done in " + (System.currentTimeMillis() - startTime) + " ms.");
   }

   /**
    * @ejb.interface-method
    * @ejb.transaction type="RequiresNew"
    */
   public void setBNameToNull() throws Exception
   {
      ALocal a = AUtil.getLocalHome().findByPrimaryKey(new APK(1, "A"));
      Collection bs = a.getB();
      if(bs.size() != 1)
      {
         throw new IllegalStateException("Expected only one B but got " + bs);
      }

      BLocal b = (BLocal)bs.iterator().next();
      b.setName(null);
   }

   /**
    * @ejb.interface-method
    * @ejb.transaction type="RequiresNew"
    */
   public String getBName() throws Exception
   {
      BLocal b = BUtil.getLocalHome().findByPrimaryKey(new BPK(1, "B1"));
      return b.getName();
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
}
