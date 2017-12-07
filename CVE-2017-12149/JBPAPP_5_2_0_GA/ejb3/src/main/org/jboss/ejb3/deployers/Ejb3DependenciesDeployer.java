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
package org.jboss.ejb3.deployers;

import javax.transaction.TransactionManager;

import org.jboss.dependency.plugins.AbstractDependencyItem;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractRealDeployerWithInput;
import org.jboss.deployers.spi.deployer.helpers.DeploymentVisitor;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.plugins.dependency.DependenciesMetaData;
import org.jboss.deployers.vfs.plugins.dependency.DependencyItemMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: 85945 $
 */
public class Ejb3DependenciesDeployer extends AbstractRealDeployerWithInput<JBossMetaData>
{
   public Ejb3DependenciesDeployer()
   {
      setStage(DeploymentStages.PRE_REAL);
      setOutput(DependenciesMetaData.class);
      DeploymentVisitor<JBossMetaData> visitor = new DeploymentVisitor<JBossMetaData>()
      {
         public void deploy(DeploymentUnit unit, JBossMetaData deployment) throws DeploymentException
         {
            log.info("Encountered deployment " + unit);
            
            if(!deployment.isEJB3x())
               return;
            
            // TODO: get the dependencies from beans.xml
            /*
            DependenciesMetaData dependencies = new DependenciesMetaData();
            List<DependencyItemMetaData> items = new ArrayList<DependencyItemMetaData>();
            items.add(createDependencyItemMetaData(TransactionManager.class.getName()));
            dependencies.setItems(items);
            log.debug("Encountered EJB3 deployment " + unit + ", adding dependencies " + items);
            // We add to toplevel, because DeploymentControllerContext will deploy all kids when it goes to install
            unit.getTopLevel().addAttachment(DependenciesMetaData.class, dependencies);
            */
            //ControllerContext context = unit.getAttachment(ControllerContext.class);
            unit.addIDependOn(new AbstractDependencyItem(unit.getName(), TransactionManager.class, new ControllerState("Real"), ControllerState.INSTALLED));
         }

         private DependencyItemMetaData createDependencyItemMetaData(String name)
         {
            DependencyItemMetaData item = new DependencyItemMetaData();
            item.setValue(name);
            // This is ugly, MC checks on String value
            //item.setWhenRequired(new ControllerState(DeploymentStages.REAL.toString()));
            item.setWhenRequired(new ControllerState("Real"));
            item.setDependentState(ControllerState.INSTALLED);
            return item;
         }

         public Class<JBossMetaData> getVisitorType()
         {
            return JBossMetaData.class;
         }

         public void undeploy(DeploymentUnit unit, JBossMetaData deployment)
         {
         }
      };
      setDeploymentVisitor(visitor);
   }
}
