/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.ejb3.deployers;

import org.jboss.deployers.vfs.spi.deployer.SchemaResolverDeployer;
import org.jboss.metadata.jpa.spec.PersistenceMetaData;

/**
 * Find and parse persistence.xml.
 * 
 * In a jar:
 * META-INF/persistence.xml
 * 
 * In a war (JPA 6.2):
 * WEB-INF/classes/META-INF/persistence.xml
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 * @version $Revision: 74481 $
 */
public class PersistenceUnitParsingDeployer extends SchemaResolverDeployer<PersistenceMetaData>
{
   public PersistenceUnitParsingDeployer()
   {
      super(PersistenceMetaData.class);
      setName("persistence.xml");
      setRegisterWithJBossXB(true);
   }
}
