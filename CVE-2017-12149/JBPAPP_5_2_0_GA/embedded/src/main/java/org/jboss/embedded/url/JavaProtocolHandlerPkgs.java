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
package org.jboss.embedded.url;

/**
 * Adds handler string to current System.getProperty("java.protocol.handler.pkgs")
 *
 * @author <a href="bill@jboss.com">Bill Burke</a>
 * @version $Revision: 85945 $
 */
public class JavaProtocolHandlerPkgs
{
   private String pkgs;

   public String getPkgs()
   {
      return pkgs;
   }

   public void setPkgs(String pkgs)
   {
      this.pkgs = pkgs;
   }

   public void start()
   {
      setupHandlerPkgs(pkgs);
   }

   public static void setupHandlerPkgs(String pkgs)
   {
      setupHandlerPkgs(pkgs.split("|"));
   }

   public static void setupHandlerPkgs(String[] additionalPkgs)
   {
      String pkgs = System.getProperty("java.protocol.handler.pkgs");
      if (pkgs == null || pkgs.trim().length() == 0)
      {
         pkgs = "";
         for (int i = 0; i < additionalPkgs.length; i++)
         {
            pkgs += additionalPkgs[i];
            if (i + 1 < additionalPkgs.length) pkgs += "|";
         }
         System.setProperty("java.protocol.handler.pkgs", pkgs);
      }
      else
      {
         for (String pkg : additionalPkgs)
         {
            pkgs += "|" + pkg;
         }
         System.setProperty("java.protocol.handler.pkgs", pkgs);
      }
   }
}
