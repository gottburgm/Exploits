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
package org.jboss.test.entityexc.interfaces;

import java.rmi.RemoteException;
import javax.ejb.EJBObject;


/**
 *  Remote interface of entity exception test bean.
 *
 *  @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *  @version $Revision: 81036 $
 */
public interface EntityExc extends EJBObject
{
  /**
   *  Exception code for a {@link MyAppException].
   *  This may be used in the flags sent to various methods
   *  here, and in the home interface.
   */
  public final int EXC_MYAPPEXCEPTION = 1;

  /**
   *  Exception code for a <code>CreateException</code>.
   *  This may be used in the flags sent to various methods
   *  here, and in the home interface.
   */
  public final int EXC_CREATEEXCEPTION = 2;

  /**
   *  Exception code for a <code>EJBException</code>.
   *  This may be used in the flags sent to various methods
   *  here, and in the home interface.
   */
  public final int EXC_EJBEXCEPTION = 3;

  /**
   *  Flags exception code mask.
   */
  public final int F_EXC_MASK = 0xff;

  /**
   *  Flag telling that an exception should be thrown
   *  <em>after</em> the exception, and not before.
   */
  public final int F_THROW_BEFORE = 0x100;

  /**
   *  Flag telling that failure should not happen until the postCreate
   *  method. Ignored for non-create invocations.
   */
  public final int F_FAIL_POSTCREATE = 0x200;

  /**
   *  Flag telling that <code>context.setRollbackOnly</code> must be
   *  called before returning.
   */
  public final int F_SETROLLBACKONLY = 0x400;

  /**
   *  Text of Exception message thrown.
   *  We check this to make sure we get <i>our</i> Exception.
   */
  public final String EXCEPTION_TEXT = "Hello, cruel world.";
 

  /**
   *  Return the id of this instance.
   */
  public int getId()
    throws RemoteException;

  /**
   *  Return the value of this instance.
   *  This method really works.
   */
  public int getVal()
    throws RemoteException;

  /**
   *  Increment the value of this instance by one.
   */
  public void incrementVal()
    throws RemoteException;


  /**
   *  Increment the value of this instance by one, and fail according to
   *  the failure argument afterwards.
   *
   *  This method has a Required transaction attribute. 
   */
  public void incrementVal(int flags)
    throws MyAppException, RemoteException;

}
