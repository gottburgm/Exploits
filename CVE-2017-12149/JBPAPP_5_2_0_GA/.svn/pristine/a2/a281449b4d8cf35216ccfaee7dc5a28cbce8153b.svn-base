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
package org.jboss.security.deployers;

import org.jboss.deployers.vfs.spi.deployer.SchemaResolverDeployer;
import org.jboss.security.acl.config.ACLConfiguration;
import org.jboss.security.acl.config.ACLConfigurationFactory;
import org.jboss.virtual.VirtualFile;

/**
 * Parsing Deployer for ACL jboss-acl-policy.xml that 
 * can be present in EJB/Web Deployments
 * @author Anil.Saldhana@redhat.com
 * @since Mar 18, 2009
 */
public class AclConfigParsingDeployer extends SchemaResolverDeployer<ACLConfiguration>
{ 
   public AclConfigParsingDeployer()
   {
      super(ACLConfiguration.class);
      setName("jboss-acl-policy.xml");
   }

   @Override
   protected ACLConfiguration parse(VirtualFile file) throws Exception
   {
      return ACLConfigurationFactory.getConfiguration(file.openStream());
   } 
}