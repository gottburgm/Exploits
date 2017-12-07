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
package org.jboss.test.jca.ejb;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.transaction.UserTransaction;

import org.jboss.logging.Logger;
import org.jboss.test.jca.adapter.TestConnection;
import org.jboss.test.jca.adapter.TestConnectionFactory;

/**
 * UserTxSessionBean.java
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version <tt>$Revision: 81036 $</tt>
 *
 * @ejb.bean   name="UserTxSession"
 *             jndi-name="UserTxSession"
 *             local-jndi-name="LocalUserTxSession"
 *             view-type="both"
 *             type="Stateless"
 *             transaction-type="Bean"
 * @ejb.transaction type="NotSupported"
 */
public class UserTxSessionBean 
   implements SessionBean  
{

   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;
   private SessionContext ctx;
   private Logger log = Logger.getLogger(getClass());

   public UserTxSessionBean() 
   {
      
   }
   
   /**
    * Describe <code>testUserTxJndi</code> method here.
    *
    * @return a <code>boolean</code> value
    *
    * @ejb:interface-method
    */
   public boolean testUserTxJndi()
   {
      try 
      {
         TestConnectionFactory tcf = (TestConnectionFactory)new InitialContext().lookup("java:/JBossTestCF");
         TestConnection tc = (TestConnection)tcf.getConnection();
         UserTransaction ut = (UserTransaction)new InitialContext().lookup("UserTransaction");
         ut.begin();
         boolean result = tc.isInTx();
         log.info("Jndi test, inTx: " + result);
         ut.commit();
         tc.close();
         return result;
      }
      catch (Exception e)
      {
         throw new EJBException(e.getMessage());
      }
      
   }

   /**
    * Describe <code>testUserTxSessionCtx</code> method here.
    *
    * @return a <code>boolean</code> value
    *
    * @ejb:interface-method
    */
   public boolean testUserTxSessionCtx()
   {
      try 
      {
         TestConnectionFactory tcf = (TestConnectionFactory)new InitialContext().lookup("java:/JBossTestCF");
         TestConnection tc = (TestConnection)tcf.getConnection();
         UserTransaction ut = ctx.getUserTransaction();
         ut.begin();
         boolean result = tc.isInTx();
         log.info("ctx test, inTx: " + result);
         ut.commit();
         tc.close();
         return result;
      }
      catch (Exception e)
      {
         throw new EJBException(e.getMessage());
      }
      
   }
   
   /**
    * @ejb:interface-method
    */
   public void testUnclosedError() throws Exception
   {
      TestConnectionFactory tcf = (TestConnectionFactory)new InitialContext().lookup("java:/JBossTestCF");
      tcf.getConnection(); // DONT CLOSE 
   }

   public void ejbCreate() 
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

   public void unsetSessionContext()
   {
      this.ctx = null;
   }

}
