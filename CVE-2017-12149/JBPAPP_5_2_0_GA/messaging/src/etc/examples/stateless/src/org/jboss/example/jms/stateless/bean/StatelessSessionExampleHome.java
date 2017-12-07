/**
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.example.jms.stateless.bean;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;
/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 * @version <tt>$Revision: 85945 $</tt>

 * $Id: StatelessSessionExampleHome.java 85945 2009-03-16 19:45:12Z dimitris@jboss.org $
 */

public interface StatelessSessionExampleHome extends EJBHome
{
   public StatelessSessionExample create() throws RemoteException, CreateException;
}


