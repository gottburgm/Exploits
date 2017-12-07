/**
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.example.jms.common.bean;

import java.rmi.RemoteException;

import javax.ejb.EJBObject;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 * @version <tt>$Revision: 85945 $</tt>

 * $Id: Management.java 85945 2009-03-16 19:45:12Z dimitris@jboss.org $
 */

public interface Management extends EJBObject
{
   /**
    * It kills the VM running the node instance. Needed by the failover tests.
    */
   public void killAS() throws Exception, RemoteException;
}
