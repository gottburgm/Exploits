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
package org.jboss.test.perf.test;
import org.jboss.test.perf.interfaces.*;

import java.util.*;

public class Command {

  public static void main(String[] args) throws Exception {
    
    if(args.length != 4) {
      System.out.println("Usage: vbj Command {create,remove} <jndi-name> <low-count> <high-count>");
      return;
    }

    String command = args[0];
    String jndiName = args[1];
    int low  = Integer.parseInt(args[2]);
    int high = Integer.parseInt(args[3]);

    javax.naming.Context context = new javax.naming.InitialContext(); 

    Object ref = context.lookup("Session");
    SessionHome sessionHome = (SessionHome) ref;
    /** CHANGES: WebLogic does not support PortableRemoteObject
     **
      //(SessionHome) javax.rmi.PortableRemoteObject.narrow(ref, SessionHome.class);
     **/

    Session session = sessionHome.create(jndiName);

    if(command.equalsIgnoreCase("create")) {
      session.create(low, high);
    }
    else if(command.equalsIgnoreCase("remove")) {
      session.remove(low, high);
    }
    else {
      System.err.println("Invalid command: " + command);
    }

    session.remove();

  }

}
