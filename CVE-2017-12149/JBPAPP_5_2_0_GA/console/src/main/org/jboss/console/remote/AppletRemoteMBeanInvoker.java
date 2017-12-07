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
package org.jboss.console.remote;
/**
 * <description>
 *
 * @see <related>
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 81010 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>21. avril 2003 Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li>
 * </ul>
 */

public class AppletRemoteMBeanInvoker
implements SimpleRemoteMBeanInvoker
{
   
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   java.net.URL baseUrl = null;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   public AppletRemoteMBeanInvoker (String baseUrl) throws java.net.MalformedURLException
   {
      this.baseUrl = new java.net.URL (baseUrl);
   }
   
   // Public --------------------------------------------------------
   
   // SimpleRemoteMBeanInvoker implementation ----------------------------------------------
   
   public Object invoke (javax.management.ObjectName name, String operationName, Object[] params, String[] signature) throws Exception
   {
      return Util.invoke (this.baseUrl, new RemoteMBeanInvocation (name, operationName, params, signature));
   }

   public Object getAttribute (javax.management.ObjectName name, String attrName) throws Exception
   {
      return Util.getAttribute(this.baseUrl, new RemoteMBeanAttributeInvocation(name, attrName));
   }

   // Y overrides ---------------------------------------------------
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
   
}
