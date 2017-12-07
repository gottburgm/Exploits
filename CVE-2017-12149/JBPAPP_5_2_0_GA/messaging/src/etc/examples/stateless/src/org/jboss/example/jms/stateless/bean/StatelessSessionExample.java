/**
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.example.jms.stateless.bean;

import java.rmi.RemoteException;

import javax.ejb.EJBObject;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 * @version <tt>$Revision: 85945 $</tt>

 * $Id: StatelessSessionExample.java 85945 2009-03-16 19:45:12Z dimitris@jboss.org $
 */

public interface StatelessSessionExample extends EJBObject
{
   public void drain(String queueName) throws RemoteException, Exception;
   
   public void send(String txt, String queueName) throws RemoteException, Exception;
   
   public int browse(String queueName) throws RemoteException, Exception;
   
   public String receive(String queueName) throws RemoteException, Exception;
}
