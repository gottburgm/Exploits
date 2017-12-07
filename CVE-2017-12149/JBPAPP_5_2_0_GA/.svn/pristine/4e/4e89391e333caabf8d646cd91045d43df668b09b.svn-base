/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2008,
 * @author JBoss Inc.
 */
package org.jboss.test.jbossts.ASCrashRecovery01;

import org.jboss.test.jbossts.recovery.CrashHelperRem;
import org.jboss.test.jbossts.taskdefs.JUnitClientTest;
import org.jboss.test.jbossts.crash.CrashHelper;
import org.apache.tools.ant.BuildException;

import java.util.Map;

/**
 * Helper test just for wiping out in-doubt txs from DB.
 * 
 * @author <a href="istudens@redhat.com">Ivo Studensky</a>
 * @version $Revision: 1.1 $
 */
public class WipeOutTxsInDoubt extends JUnitClientTest
{
   private boolean isDebug = false;

   private String serverName = "default";
   
   public void testAction()
   {
      for (Map.Entry<String, String> me : params.entrySet())
      {
         String key = me.getKey().trim();
         String val = me.getValue().trim();

         if ("name".equals(key))
            setName(val);
         else if ("serverName".equals(key))
            serverName = val;
         else if ("debug".equals(key))
            isDebug = val.equalsIgnoreCase("true");
      }

      print("Executing test " + getName() + ":\n");

      try
      {
         print("wiping out txs in doubt from database");
         wipeOutTxsInDoubt();
      }
      catch (Exception e)
      {
         if (isDebug)
            e.printStackTrace();

         throw new BuildException(e);
      }
   }
   
   
   public boolean wipeOutTxsInDoubt()
   {
      try
      {
         CrashHelperRem helper = (CrashHelperRem) config.getNamingContext(serverName).lookup(CrashHelper.REMOTE_JNDI_NAME);
         helper.wipeOutTxsInDoubt(null);
      }  
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return false;
   }

}
