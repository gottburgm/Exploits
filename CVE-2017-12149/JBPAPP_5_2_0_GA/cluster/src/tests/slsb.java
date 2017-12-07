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
import java.rmi.*;
import java.awt.*;
import java.util.*;
import javax.ejb.*;
import javax.naming.*;
import java.awt.event.*;
import java.util.*;
import java.lang.*;
import java.io.*;
import org.jboss.test.testbean.interfaces.StatelessSessionHome;
import org.jboss.test.testbean.interfaces.StatelessSession;
import org.jboss.test.testbean.interfaces.EnterpriseEntityHome;
import org.jboss.test.testbean.interfaces.EnterpriseEntity;
import javax.ejb.DuplicateKeyException;
import javax.ejb.Handle;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBHome;
import javax.ejb.HomeHandle;

public class slsb
{
    public static void main(String[] args)
    {
        try
        {
            Properties p = new Properties();
            
            p.put(Context.INITIAL_CONTEXT_FACTORY, 
                  "org.jnp.interfaces.NamingContextFactory");
            p.put(Context.PROVIDER_URL, "10.10.10.13:1100,10.10.10.14:1100");
            // p.put(Context.PROVIDER_URL, "localhost:1100");
            p.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
            InitialContext ctx = new InitialContext(p);
            
            StatelessSessionHome  statelessSessionHome =  (StatelessSessionHome) ctx.lookup("nextgen.StatelessSession");
            EnterpriseEntityHome  cmpHome =  (EnterpriseEntityHome)ctx.lookup("nextgen.EnterpriseEntity");
            StatelessSession statelessSession = statelessSessionHome.create();
            EnterpriseEntity cmp = null;
            try
            {
               cmp = cmpHome.findByPrimaryKey("bill");
            }
            catch (Exception ex)
            {
               cmp = cmpHome.create("bill");
            }
            int count = 0;
            while (true)
            {
               System.out.println(statelessSession.callBusinessMethodB());
               try
               {
                  cmp.setOtherField(count++);
               }
               catch (Exception ex)
               {
                  System.out.println("exception, trying to create it: " + ex);
                  cmp = cmpHome.create("bill");
                  cmp.setOtherField(count++);
               }
               System.out.println("Entity: " + cmp.getOtherField());
               Thread.sleep(2000);
            }
        }
        catch (NamingException nex)
        {
           if (nex.getRootCause() != null)
           {
              nex.getRootCause().printStackTrace();
           }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
