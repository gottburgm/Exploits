/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.test.naming.restart;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;

import org.jboss.logging.Logger;
import org.jboss.naming.NamingServiceMBean;
import org.jnp.interfaces.MarshalledValuePair;
import org.jnp.interfaces.Naming;
import org.jnp.interfaces.NamingParser;

/**
 * Binds an object into JNDI.
 * 
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 85945 $
 */
public class ObjectBinder
{
   private static Logger log = Logger.getLogger(ObjectBinder.class);
   
   public static final String NAME = "NamingRestartBinding";
   public static final String BAD_BINDING = "NamingRestartBadBinding";
   public static final String VALUE = "VALUE";
   public static final String SUBCONTEXT_NAME = "LocalSubcontext";
//   private String providerURL;  
   private NamingParser parser = new NamingParser();
   private NamingServiceMBean naming;
   
   /* (non-Javadoc)
    * @see org.jboss.test.naming.restart.ObjectBinderMBean#setNamingService(org.jboss.naming.NamingServiceMBean)
    */
   public void setNamingService(NamingServiceMBean naming)
   {
//      this.providerURL = (naming == null) 
//                              ? null 
//                              : naming.getBindAddress() + ":" + naming.getPort();
      this.naming = naming;
   }
   
   /**
    * Bind an object both in standard JNDI (to expose via HA-JNDI) and in our
    * injected NamingServer
    * 
    * @throws Exception
    */
   public void start() throws Exception
   {      
      // Standard JNDI
      Context ctx = new InitialContext();
      ctx.bind(NAME, VALUE);
      log.info("Bound " + VALUE + " to " + ctx + " under " + NAME);
      ctx.bind(BAD_BINDING, new NonDeserializable());
      log.info("Bound a NonDeserializable to " + ctx + " under " + BAD_BINDING);
      
      // For some reason creating a context for our own JNDI doesn't work
      // inside the server, so as a hack we directly deal with the NamingServer
      // to bind the object
      
//    Properties env = new Properties();
//    env.setProperty("java.naming.provider.url", providerURL);
//    log.info("Env = " + env);
//    Context ctx = new InitialContext(env);
//    ctx.bind(NAME, VALUE);
      
      Naming namingServer = naming.getNamingInstance();
      namingServer.bind(parser.parse(NAME), 
                                      new MarshalledValuePair(VALUE), 
                                      VALUE.getClass().getName());
      log.info("Bound " + VALUE + " to " + namingServer + " under " + NAME);
      Context sub = namingServer.createSubcontext(parser.parse(SUBCONTEXT_NAME));
      sub.bind(parser.parse(NAME), VALUE);
      log.info("Bound " + VALUE + " to " + sub + " under " + NAME);
      
      // NOTE: we must bind the NonDeserializable directly, or else the 
      // NamingContext will wrap it in a MarshalledValuePair, which will
      // defeat the test by triggering deserialization too late
      namingServer.bind(parser.parse(BAD_BINDING), new NonDeserializable(), 
                                     NonDeserializable.class.getName());

      log.info("Bound a NonDeserializable to " + namingServer + " under " + BAD_BINDING);
      
   }
   
   /**
    * Undoes the bindings done in start().
    * 
    * @throws Exception
    */
   public void stop() throws Exception
   {
      // Standard JNDI
      Context ctx = new InitialContext();
      ctx.unbind(NAME);
      log.info("Unbound " + NAME + " from " + ctx);
      ctx.unbind(BAD_BINDING);
      log.info("Unbound " + BAD_BINDING + " from " + ctx);
      
      // For some reason creating a context for our own JNDI doesn't work
      // inside the server, so as a hack we directly deal with the NamingServer
      // to bind the object
      
//    Properties env = new Properties();
//    env.setProperty("java.naming.provider.url", providerURL);
//    
//    Context ctx = new InitialContext(env);
//      ctx.unbind(NAME);
      
      Naming namingServer = naming.getNamingInstance();
      try
      {
         namingServer.unbind(parser.parse(SUBCONTEXT_NAME + "/" + NAME));
         log.info("Unbound " + SUBCONTEXT_NAME + "/" + NAME + " from " + namingServer);
      }
      catch (NameNotFoundException ignored)
      {
         // already unbound by test
      }
      try
      {
         namingServer.unbind(parser.parse(SUBCONTEXT_NAME));
         log.info("Unbound " + SUBCONTEXT_NAME + " from " + namingServer);
      }
      catch (NameNotFoundException ignored)
      {
         // already unbound by test
      }
      try
      {
         namingServer.unbind(parser.parse(NAME));
         log.info("Unbound " + NAME + " from " + namingServer);
      }
      catch (NameNotFoundException ignored)
      {
         // already unbound by test
      }
      try
      {
         namingServer.unbind(parser.parse(BAD_BINDING));
         log.info("Unbound " + BAD_BINDING + " from " + namingServer);
      }
      catch (NameNotFoundException ignored)
      {
         // already unbound by test
      }
   }
}
