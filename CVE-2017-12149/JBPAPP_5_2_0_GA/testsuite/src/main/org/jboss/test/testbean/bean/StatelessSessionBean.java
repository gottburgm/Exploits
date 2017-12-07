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
package org.jboss.test.testbean.bean;

import java.rmi.*;
import javax.ejb.*;
import javax.naming.InitialContext;
import javax.naming.Context;
import org.jboss.test.testbean.interfaces.BusinessMethodException;

public class StatelessSessionBean implements SessionBean {
   org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(getClass());
  private SessionContext sessionContext;

  public void ejbCreate() throws RemoteException, CreateException {
  log.debug("StatelessSessionBean.ejbCreate() called");
  }

  public void ejbActivate() throws RemoteException {
    log.debug("StatelessSessionBean.ejbActivate() called");
  }

  public void ejbPassivate() throws RemoteException {
      log.debug("StatelessSessionBean.ejbPassivate() called");
  }

  public void ejbRemove() throws RemoteException {
     log.debug("StatelessSessionBean.ejbRemove() called");
  }

  public void setSessionContext(SessionContext context) throws RemoteException {
    sessionContext = context;
    //Exception e = new Exception("in set Session context");
    //log.debug("failed", e);
  }

  public void callBusinessMethodA() {
      //Do nothing just make sure the call works
        
      try {
         Object tx = ((javax.transaction.TransactionManager) new InitialContext().lookup("java:/TransactionManager")).getTransaction();
         if (tx == null) 
           log.debug("I don't see a transaction");
         else
           log.debug("I see a transaction "+tx.hashCode());
      }
      catch (Exception e) {log.debug("failed", e);}
      log.debug("StatelessSessionBean.callBusinessMethodA() called");
   }

  public String callBusinessMethodB() {
       log.debug("StatelessSessionBean.callBusinessMethodB() called");
       try {

           Context context = new InitialContext();
           String name = (String) context.lookup("java:comp/env/myNameProp");
           return "from the environment properties my name is "+name;

       }catch (Exception e) { return "Error in the lookup of properties  "+e.getMessage();}
  }

  public String callBusinessMethodB(String words) {
         // test if overloaded methods are properly called
         log.debug("StatelessSessionBean.callBusinessMethodB(String) called");
         // Check that my EJBObject is there
        EJBObject ejbObject = sessionContext.getEJBObject();
        if (ejbObject == null) {
          return "ISNULL:NOT FOUND!!!!!";
       
        }
        else {
         return "OK ejbObject is "+ejbObject.toString()+" words "+words;
         
        }			 
  
  }

  public String callBusinessMethodC() {
       log.debug("StatelessSessionBean.callBusinessMethodC() called");
       try {

           Context context = new InitialContext();
           String name = (String) context.lookup("java:comp/env/subContext/myNameProp");
           return "from the environment properties (subContext) my name is "+name;

       }catch (Exception e) { return "Error in the lookup of properties  "+e.getMessage();}
  }

   public void callBusinessMethodD() throws BusinessMethodException {
       log.debug("StatelessSessionBean.callBusinessMethodD() called");
       throw new BusinessMethodException();
  }
  
  public String callBusinessMethodE() {
        log.debug("StatelessSessionBean.callBusinessMethodE() called");
         // Check that my EJBObject is there
        EJBObject ejbObject = sessionContext.getEJBObject();
        if (ejbObject == null) {
          return "ISNULL:NOT FOUND!!!!!";
        }
        else {
         return ejbObject.toString();
        }			 
  }
  
   public void testClassLoading() throws BusinessMethodException {
       log.debug("StatelessSessionBean.testClassLoading() called");
       
        try{
            Class.forName("org.somepackage.SomeClass");
        } catch( Exception e){
            log.debug("failed", e);
            throw new BusinessMethodException();
        }
  }
  
}
