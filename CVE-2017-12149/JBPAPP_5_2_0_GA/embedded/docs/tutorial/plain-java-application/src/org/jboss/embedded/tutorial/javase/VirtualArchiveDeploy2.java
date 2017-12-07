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
package org.jboss.embedded.tutorial.javase;

import org.jboss.virtual.plugins.context.vfs.AssembledDirectory;
import org.jboss.virtual.plugins.context.vfs.AssembledContextFactory;
import org.jboss.embedded.tutorial.javase.beans.Customer;
import org.jboss.embedded.tutorial.javase.beans.CustomerDAOBean;
import org.jboss.embedded.tutorial.javase.beans.CustomerDAOLocal;
import org.jboss.embedded.tutorial.javase.beans.CustomerDAORemote;
import org.jboss.embedded.Bootstrap;

/**
 * comment
 *
 * @author <a href="bill@jboss.com">Bill Burke</a>
 * @version $Revision: 85945 $
 */
public class VirtualArchiveDeploy2 extends Main
{
   public static void main(String[] args) throws Exception
   {
      AssembledDirectory jar = AssembledContextFactory.getInstance().create("tutorial.jar");
      String[] includes = {"**/beans/*.class"};
      jar.addResources(Customer.class, includes, null);
      // Get tutorial-persistence.xml from classloader and alias it within the archive.
      jar.mkdir("META-INF").addResource("tutorial-persistence.xml", "persistence.xml");

      Bootstrap.getInstance().bootstrap();
      Bootstrap.getInstance().deploy(jar);

      run();

      Bootstrap.getInstance().shutdown();
   }
}
