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
package org.jboss.deployment;

/**
 * Thrown by a deployer if an application component could not be
 * deployed.
 *
 * @see DeployerMBean
 * 
 * @deprecated use org.jboss.deployers.spi.DeploymentException
 * @author <a href="mailto:toby.allsopp@peace.com">Toby Allsopp</a>
 * @version $Revision: 81033 $
 */
public class DeploymentException extends org.jboss.deployers.spi.DeploymentException
{
   /** @since 4.0.2 */
   private static final long serialVersionUID = 1416258464473965574L;   
   
   /**
    * Rethrow a throwable as a deployment exception if it isn't already.
    *
    * @param message the message
    * @param t the throwable
    * @throws DeploymentException always
    */
   public static DeploymentException rethrowAsDeploymentException(String message, Throwable t)
      throws DeploymentException
   {
      if (t instanceof DeploymentException)
         throw (DeploymentException) t;
      else
         throw new DeploymentException(message, t);
   }

   /**
    * Construct a <tt>DeploymentException</tt> with the specified detail 
    * message.
    *
    * @param msg  Detail message.
    */
   public DeploymentException(String msg)
   {
      super(msg);
   }

   /**
    * Construct a <tt>DeploymentException</tt> with the specified detail 
    * message and nested <tt>Throwable</tt>.
    *
    * @param msg     Detail message.
    * @param nested  Nested <tt>Throwable</tt>.
    */
   public DeploymentException(String msg, Throwable nested)
   {
      super(msg, nested);
   }

   /**
    * Construct a <tt>DeploymentException</tt> with the specified
    * nested <tt>Throwable</tt>.
    *
    * @param nested  Nested <tt>Throwable</tt>.
    */
   public DeploymentException(Throwable nested)
   {
      super(nested);
   }

   /**
    * Construct a <tt>DeploymentException</tt> with no detail.
    */
   public DeploymentException()
   {
      super();
   }
}
