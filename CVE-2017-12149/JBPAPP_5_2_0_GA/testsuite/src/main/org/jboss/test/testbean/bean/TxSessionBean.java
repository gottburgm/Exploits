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
import org.jboss.test.testbean.interfaces.TxSession;


public class TxSessionBean implements SessionBean {
   org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(getClass());
  private SessionContext sessionContext;

  public void ejbCreate() throws RemoteException, CreateException {
  log.debug("TxSessionBean.ejbCreate() called");
  }

  public void ejbActivate() throws RemoteException {
    log.debug("TxSessionBean.ejbActivate() called");
  }

  public void ejbPassivate() throws RemoteException {
      log.debug("TxSessionBean.ejbPassivate() called");
  }

  public void ejbRemove() throws RemoteException {
     log.debug("TxSessionBean.ejbRemove() called");
  }

  public void setSessionContext(SessionContext context) throws RemoteException {
    sessionContext = context;
    //Exception e = new Exception("in set Session context");
    //log.debug("failed", e);
  }

  /*
  * This method is defined with "Required"
  */
  public String txRequired() {
 
      log.debug("TxSessionBean.txRequired() called");  
      
      try {
         Object tx =getDaTransaction();
         if (tx == null) 
           throw new Error("Required sees no transaction");
         else
           return ("required sees a transaction "+tx.hashCode());
      }
      catch (Exception e) {log.debug("failed", e); return e.getMessage();}
   }
  
  
  /*
  * This method is defined with "Requires_new"
  */
  public String txRequiresNew() {
 
      log.debug("TxSessionBean.txRequiresNew() called");  
      
      try {
         Object tx =getDaTransaction();
         if (tx == null) 
           throw new Error("RequiresNew sees no transaction");
         else
           return ("requiresNew sees a transaction "+tx.hashCode());
      }
      catch (Exception e) {log.debug("failed", e);return e.getMessage();}
   }
  
   /*
  * testSupports is defined with Supports
  */
   
  public String txSupports() {
      
      log.debug("TxSessionBean.txSupports() called");  
      
      try {
          
        Object tx =getDaTransaction();
        
        if (tx == null) 
           return "supports sees no transaction";
        else
           return "supports sees a transaction "+tx.hashCode();
    }
    catch (Exception e) {log.debug("failed", e);return e.getMessage();}
 }  
 
 
  /*
  * This method is defined with "Mandatory"
  */
  public String txMandatory() {
 
      log.debug("TxSessionBean.txMandatory() called");  
      
      try {
         Object tx =getDaTransaction();
         if (tx == null) 
           throw new Error("Mandatory sees no transaction");
         else
           return ("mandatory sees a transaction "+tx.hashCode());
      }
      catch (Exception e) {log.debug("failed", e);return e.getMessage();}
   }
  
   /*
  * This method is defined with "Never"
  */
  public String txNever() {
 
      log.debug("TxSessionBean.txNever() called");  
      
      try {
         Object tx =getDaTransaction();
         if (tx == null) 
           return "never sees no transaction";
         else
             throw new Error("txNever sees a transaction");
      }
      catch (Exception e) {log.debug("failed", e);return e.getMessage();}
   }
    /*
  * This method is defined with "TxNotSupported"
  */
  
  public String txNotSupported() {
 
      log.debug("TxSessionBean.txNotSupported() called");  
      
      try {
         Object tx =getDaTransaction();
         if (tx == null) 
           return "notSupported sees no transaction";
         else
             throw new Error("txNotSupported sees a transaction");
      }
      catch (Exception e) {log.debug("failed", e);return e.getMessage();}
   }
 
  /*
  * This method is defined with "Required" and it passes it to a Supports Tx
  */
  public String requiredToSupports() throws RemoteException {
      
      log.debug("TxSessionBean.requiredToSupports() called");
      
      String message;        
      Object tx =getDaTransaction();
         if (tx == null) 
           throw new Error("Required doesn't see a transaction");
         else
           message = "Required sees a transaction "+tx.hashCode()+ " Supports should see the same ";
     
      message = message + ((TxSession) sessionContext.getEJBObject()).txSupports();
  
       // And after invocation we should have the same transaction
       tx =getDaTransaction();
         if (tx == null) 
           throw new Error("Required doesn't see a transaction COMING BACK");
         else
           return message + " on coming back Required sees a transaction "+tx.hashCode() ;
 
  }   
  
    /*
  * This method is defined with "Required" and it passes it to a NotSupported Tx
  */
  public String requiredToNotSupported() throws RemoteException {
      
      log.debug("TxSessionBean.requiredToNotSupported() called");
      
      String message;        
      Object tx =getDaTransaction();
         if (tx == null) 
           throw new Error("Required doesn't see a transaction");
         else
           message = "Required sees a transaction "+tx.hashCode()+ " NotSupported should see the same ";
    
        
      message = message + ((TxSession) sessionContext.getEJBObject()).txNotSupported();
      
      // And after invocation we should have the same transaction
       tx =getDaTransaction();
         if (tx == null) 
           throw new Error("Required doesn't see a transaction COMING BACK");
         else
           return message + " on coming back Required sees a transaction "+tx.hashCode() ;
  }
  
  public String requiredToRequiresNew() throws RemoteException{
      
      log.debug("TxSessionBean.requiredToRequiresNew() called");
      
      String message;        
      Object tx =getDaTransaction();
         if (tx == null) 
           throw new Error("Required doesn't see a transaction");
         else
           message = "Required sees a transaction "+tx.hashCode()+ " Requires new should see a new transaction ";
    
    message =  message + ((TxSession) sessionContext.getEJBObject()).txRequiresNew();
      
     // And after invocation we should have the same transaction
       tx =getDaTransaction();
         if (tx == null) 
           throw new Error("Required doesn't see a transaction COMING BACK");
         else
           return message + " on coming back Required sees a transaction "+tx.hashCode() ;
  }
      

  
  private Object getDaTransaction() {
  
     try { 
        
        return ((javax.transaction.TransactionManager) new InitialContext().lookup("java:/TransactionManager")).getTransaction();
    }
    catch (Exception e) { return null;}
  }
             
}
