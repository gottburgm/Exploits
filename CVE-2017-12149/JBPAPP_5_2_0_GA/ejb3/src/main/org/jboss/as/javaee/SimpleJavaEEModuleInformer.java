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
package org.jboss.as.javaee;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.jpa.javaee.JavaEEModuleInformer;
import org.jboss.metadata.client.jboss.JBossClientMetaData;
import org.jboss.metadata.ear.jboss.JBossAppMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossEntityBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: 112207 $
 */
public class SimpleJavaEEModuleInformer implements JavaEEModuleInformer
{
	public String getApplicationName(DeploymentUnit unit)
	{
		DeploymentUnit topLevel = unit.getTopLevel();
		if (topLevel.isAttachmentPresent(JBossAppMetaData.class))
			return topLevel.getSimpleName();
		return null;
	}

	public String getModulePath(DeploymentUnit unit)
	{
		// first check if this a JavaEE Application (i.e. if it's a .ear).
		// If yes, then return the relative path (i.e. module name) of the unit
		// relative to the .ear
		DeploymentUnit topLevel = unit.getTopLevel();
		if (topLevel.isAttachmentPresent(JBossAppMetaData.class))
		{
			return unit.getRelativePath();
		}
		// if it's not a JavaEE application (i.e. not a .ear), then
		// return the simple name of the unit.
		return unit.getSimpleName();
	}

	public ModuleType getModuleType(DeploymentUnit unit)
	{
		if (unit.isAttachmentPresent(JBossClientMetaData.class))
			return ModuleType.APP_CLIENT;
		if (unit.isAttachmentPresent(JBossMetaData.class) && isReallyAnEjbDeployment(unit))
			return ModuleType.EJB;
		if (unit.isAttachmentPresent(JBossWebMetaData.class))
			return ModuleType.WEB;
		return ModuleType.JAVA;
	}

	/*
	 * Some hacks to counter problems.
	 */
	private boolean isReallyAnEjbDeployment(DeploymentUnit unit)
	{
		JBossMetaData metaData = unit.getAttachment(JBossMetaData.class);
		// JBMETA-69
		if (metaData.getEnterpriseBeans() == null || metaData.getEnterpriseBeans().size() == 0)
			return false;
		// JBMETA-70
		// The chance of a persistence unit being defined with couple of EJB
		// entity beans is
		// pretty slim.
		for (JBossEnterpriseBeanMetaData bean : metaData.getEnterpriseBeans())
		{
			if (!(bean instanceof JBossEntityBeanMetaData))
				return true;
		}
		return false;
	}
}
