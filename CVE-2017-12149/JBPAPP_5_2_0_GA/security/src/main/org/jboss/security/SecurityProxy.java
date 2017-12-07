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
package org.jboss.security;

import java.lang.reflect.Method;
import javax.ejb.EJBContext;

/** An interface describing the requirements for a SecurityInterceptor proxy.
A SecurityProxy allows for the externalization of custom security checks 
on a per-method basis for both the EJB home and remote interface methods.
Custom security checks are those that cannot be described using the
standard EJB deployment time declarative role based security.

@author Scott.Stark@jboss.org
@version $Revision: 85945 $
*/
public interface SecurityProxy
{
   /** Inform a proxy of the context in which it is operating.
    * @param beanHome The EJB remote home interface class
    * @param beanRemote The EJB remote interface class
    * @param securityMgr The security manager from the security domain
    * @throws InstantiationException
    */
   public void init(Class beanHome, Class beanRemote, Object securityMgr)
      throws InstantiationException;
   /** Inform a proxy of the context in which it is operating.
    * @param beanHome The EJB remote home interface class
    * @param beanRemote The EJB remote interface class
    * @param beanLocalHome The EJB local home interface class, may be null
    * @param beanLocal The EJB local interface class, may be null
    * @param securityMgr The security manager from the security domain
    * @throws InstantiationException
    */
   public void init(Class beanHome, Class beanRemote,
      Class beanLocalHome, Class beanLocal, Object securityMgr)
      throws InstantiationException;
    /** Called prior to any method invocation to set the current EJB context.
    */
    public void setEJBContext(EJBContext ctx);
    /** Called to allow the security proxy to perform any custom security
        checks required for the EJB remote or local home interface method.
    @param m , the EJB home or local home interface method
    @param args , the invocation args
    */
    public void invokeHome(Method m, Object[] args) throws Exception;
    /** Called to allow the security proxy to perform any custom security
        checks required for the EJB remote or local interface method.
    @param m , the EJB remote or local interface method
    @param args , the invocation args
    @param bean, the EJB implementation class instance
    */
    public void invoke(Method m, Object[] args, Object bean) throws Exception;
}
