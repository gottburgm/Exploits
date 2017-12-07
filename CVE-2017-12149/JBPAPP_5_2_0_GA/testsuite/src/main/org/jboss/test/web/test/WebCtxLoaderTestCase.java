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
package org.jboss.test.web.test;

import org.jboss.test.JBossTestCase;

/**
 *  Testcase that tests various things with the JBossWebLoader
 *  set to true in the tomcat configuration
 *  @author <mailto:Anil.Saldhana@jboss.org>Anil Saldhana
 *  @since  Jan 10, 2006
 *  @version $Revision: 81036 $
 */
public class WebCtxLoaderTestCase extends JBossTestCase
{ 
   public WebCtxLoaderTestCase(String name)
   {
      super(name); 
   } 
   
   /**
    * Test that the WebCtxLoader only takes in the jar files
    * from the WEB-INF/lib directory 
    */
   public void testWebInfLibOnlyJars() throws Exception
   {
      try
      {
         deploy("jbosstest-webctx.war");
      }catch(Exception e)
      {
         fail(e.getLocalizedMessage());
      }
      undeploy("jbosstest-webctx.war");
   }

}
