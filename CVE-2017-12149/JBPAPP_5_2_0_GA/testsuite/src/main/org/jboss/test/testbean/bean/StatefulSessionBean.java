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

public class StatefulSessionBean implements SessionBean {
  public static org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(StatefulSessionBean.class);
  private SessionContext sessionContext;
  public String name;

  public void ejbCreate() throws RemoteException, CreateException {
	
	  log.debug("StatefulSessionBean.ejbCreate() called");
	  this.name= "noname";
  }
  
  public void ejbCreate(String name) throws RemoteException, CreateException {
      log.debug("StatefulSessionBean.ejbCreate("+name+") called");
      this.name = name;
  }

  public void ejbCreate(String name, String address) throws RemoteException, CreateException {
      log.debug("StatefulSessionBean.ejbCreate("+name+"@"+address+") called");
      this.name = name;
  }

  public void ejbCreateMETHOD(String name, String address) throws RemoteException, CreateException {
      log.debug("StatefulSessionBean.ejbCreateMETHOD("+name+"@"+address+") called");
      this.name = name;
  }

  public void ejbActivate() throws RemoteException {
      log.debug("StatefulSessionBean.ejbActivate() called");
  }

  public void ejbPassivate() throws RemoteException {
     log.debug("StatefulSessionBean.ejbPassivate() called");
  }

  public void ejbRemove() throws RemoteException {
     log.debug("StatefulSessionBean.ejbRemove() called");
  }

  public String callBusinessMethodA() {
     log.debug("StatefulSessionBean.callBusinessMethodA() called");
     return "I was created with Stateful String "+name;
  }

	public String callBusinessMethodB() {
		 log.debug("StatefulSessionBean.callBusinessMethodB() called");
         // Check that my EJBObject is there
		 EJBObject ejbObject = sessionContext.getEJBObject();
		 if (ejbObject == null) {
		 	 return "ISNULL:NOT FOUND!!!!!";
		
		 }
		 else {
		 	return "OK ejbObject is "+ejbObject.toString();
			
		 }			 
  }
  
  
  public String callBusinessMethodB(String words) {
  	 log.debug("StatefulSessionBean.callBusinessMethodB(String) called");
         // Check that my EJBObject is there
		 EJBObject ejbObject = sessionContext.getEJBObject();
		 if (ejbObject == null) {
		 	 return "ISNULL:NOT FOUND!!!!!";
		
		 }
		 else {
		 	return "OK ejbObject is "+ejbObject.toString()+" words "+words;
			
		 }			 
  
  }
  
  
  public void setSessionContext(SessionContext context) throws RemoteException {
     log.debug("StatefulSessionBean.setSessionContext("+context+") called");
     sessionContext = context;
  }
} 
