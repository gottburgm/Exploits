/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ha.singleton.examples;

import java.io.Serializable;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.logging.Logger;
import org.jboss.naming.Util;

/**
 * Ha Singleton Pojo example.
 * 
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 */
public class HASingletonPojoExample implements Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 6823691561869778917L;

   private static Logger log = Logger.getLogger(HASingletonPojoExample.class);
   
   private final String jndiName;
   
   private boolean isMasterNode;

   public HASingletonPojoExample(String jndiName)
   {
      this.jndiName = jndiName;
   }
   
   public void startSingleton() throws NamingException
   {
      isMasterNode = true;

      InitialContext ctx = new InitialContext();
      Util.rebind(ctx, jndiName, this);
      
      log.info("Notified to start as singleton and bound to jndi under " + jndiName);
   }

   public boolean isMasterNode()
   {
      return isMasterNode;
   }

   public void stopSingleton() throws NamingException
   {
      isMasterNode = false;
      
      InitialContext ctx = new InitialContext();
      Util.unbind(ctx, jndiName);
      
      log.info("Notified to stop as singleton and unbound from jndi under " + jndiName);
   }      

}
