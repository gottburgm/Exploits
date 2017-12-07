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
package org.jboss.deployment.security;


//$Id: JaccPolicyMBean.java 85945 2009-03-16 19:45:12Z dimitris@jboss.org $

/**
 *  Service contract for the JaccPolicy
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Dec 11, 2006 
 *  @version $Revision: 85945 $
 */
public interface JaccPolicyMBean
{
   public void create();
   public void destroy(); 
   public void start();
   public void stop(); 

   /**
    * Inject a child PolicyConfiguration facade
    * @param pcfm
    */
   public void setPolicyConfigurationFacadeMBean(PolicyConfigurationFacadeMBean pcfm);
}