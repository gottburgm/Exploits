package org.jboss.test.iiop.jbpapp6469;

import java.rmi.RemoteException;

import javax.ejb.EJBObject;

public interface HelloRemote extends EJBObject
{
   String whoAmI() throws RemoteException;
   
   String hello(String name) throws RemoteException;
}
