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
package org.jboss.ejb.plugins.cmp.jdbc.keygen;

import javax.ejb.CreateException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCEntityCommandMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCInsertPKCreateCommand;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCStoreManager;
import org.jboss.ejb.plugins.keygenerator.KeyGenerator;
import org.jboss.ejb.plugins.keygenerator.KeyGeneratorFactory;

/**
 * JDBCKeyGeneratorCreateCommand executes an INSERT INTO query.
 * This command will ask the corresponding key generator for a
 * value for the primary key before inserting the row.
 *
 * @author <a href="mailto:loubyansky@hotmail.com">Alex Loubyansky</a>
 *
 * @version $Revision: 81030 $
 */
public class JDBCKeyGeneratorCreateCommand extends JDBCInsertPKCreateCommand
{
   protected KeyGenerator keyGenerator;
   protected JDBCCMPFieldBridge pkField;

   public void init(JDBCStoreManager manager) throws DeploymentException
   {
      super.init(manager);
      pkField = getGeneratedPKField();
   }

   protected void initEntityCommand(JDBCEntityCommandMetaData entityCommand) throws DeploymentException
   {
      super.initEntityCommand(entityCommand);

      String factoryName = entityCommand.getAttribute("key-generator-factory");
      if(factoryName == null)
      {
         throw new DeploymentException("key-generator-factory attribute must be set for entity " + entity.getEntityName());
      }

      try
      {
         KeyGeneratorFactory keyGeneratorFactory = (KeyGeneratorFactory) new InitialContext().lookup(factoryName);
         keyGenerator = keyGeneratorFactory.getKeyGenerator();
      }
      catch(NamingException e)
      {
         throw new DeploymentException("Error: can't find key generator factory: " + factoryName, e);
      }
      catch(Exception e)
      {
         throw new DeploymentException("Error: can't create key generator instance; key generator factory: " + factoryName, e);
      }
   }

   protected void generateFields(EntityEnterpriseContext ctx) throws CreateException
   {
      super.generateFields(ctx);

      Object pk = keyGenerator.generateKey();
      log.debug("Generated new pk: " + pk);
      pkField.setInstanceValue(ctx, pk);
   }
}
