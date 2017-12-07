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
package org.jboss.test.cluster.multicfg.web.test;

import junit.framework.Test;

import org.jboss.test.cluster.web.JBossClusteredWebTestCase;

/**
 * Tests that a clustered session still functions properly on the second
 * node after the webapp is undeployed from the first node.
 * <p/>
 * This version tests an AttributeBasedClusteredSession.
 * 
 * @author Brian Stansberry
 * @version $Id: UndeployAttrTestCase.java 81084 2008-11-14 17:30:43Z dimitris@jboss.org $
 */
public class UndeployAttrTestCase extends UndeployTestCase
{

   public UndeployAttrTestCase(String name)
   {
      super(name);
   }
   
   protected String getContextPath()
   {
      return "/http-scoped-attr/";
   }
   
   protected String getWarName()
   {
      return "http-scoped-attr.war";
   }

   public static Test suite() throws Exception
   {
      return JBossClusteredWebTestCase.getDeploySetup(UndeployAttrTestCase.class,
                                                      "http-scoped-attr.war");
   }

}
