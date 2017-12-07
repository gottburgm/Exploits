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

public class jnditester
{
    public static void main(String[] args)
    {
        try
        {
            Properties p = new Properties();
            
            p.put(Context.INITIAL_CONTEXT_FACTORY, 
                  "org.jnp.interfaces.NamingContextFactory");
            p.put(Context.PROVIDER_URL, "10.10.10.13:1100,10.10.10.14:1100");
            //p.put(Context.PROVIDER_URL, "localhost:1100");
            p.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
            InitialContext ctx = new InitialContext(p);
            String hello = "hello world";

            try
            {
                String found = (String)ctx.lookup("testit");
            }
            catch (NameNotFoundException nfe)
            {
                System.out.println("creating testit!!!");
                ctx.bind("testit", hello);
            }

            while (true)
            {
                String found = null;
                try
                {
                    found = (String)ctx.lookup("testit");
                    System.out.println("found: " + found);
                }
                catch (NameNotFoundException nfe)
                {
                    System.err.println("could not find testit");
                }
                Thread.sleep(2000);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
