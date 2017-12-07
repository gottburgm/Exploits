/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.profileservice.management.client.upload;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jboss.aop.joinpoint.InvocationResponse;
import org.jboss.deployers.spi.management.deploy.DeploymentID;
import org.jboss.deployers.spi.management.deploy.DeploymentTarget;
import org.jboss.logging.Logger;
import org.jboss.remoting.Client;
import org.jboss.remoting.InvokerLocator;

/**
 * An implementation of DeploymentTarget that uses remoting for streaming
 * content in distribute, and basic rpc for the other methods.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 91161 $
 */
public class StreamingDeploymentTarget
   implements DeploymentTarget, Serializable
{
   private static final long serialVersionUID = 1;
   private static final Logger log = Logger.getLogger(StreamingDeploymentTarget.class);

   /** The deployment target locator */
   private InvokerLocator locator;
   private String name;
   private String subsystem;

   /**
    * Create a target given a remoting locator 
    * @param locator -
    * @param name - 
    * @param subsystem - 
    */
   public StreamingDeploymentTarget(InvokerLocator locator, String name, String subsystem)
   {
      log.debug("new StreamingTarget: " + locator);
      this.name = name;
      this.subsystem = subsystem;
      this.locator = locator;
   }
   
   public String getDescription()
   {
      return name + "(" + locator + ")";
   }

   public String getName()
   {
      return name;
   }

   public InvokerLocator getInvokerLocator()
   {
      return locator;
   }

   /**
    * Distribute a deployment to the profile targets.
    *
    * @param deployment - the encapsulation of the deployment to distribute
    * @throws Exception for any error
    */
   public void distribute(DeploymentID deployment) throws Exception
   {
      Client client = getClient();
      try
      {
         log.debug("Begin distribute: " + deployment);
         String[] rnames;
         if (deployment.isCopyContent())
         {
            URL contentURL = deployment.getContentURL();
            SerializableDeploymentID sdtID = new SerializableDeploymentID(deployment);
            InputStream contentIS = contentURL.openStream();
            sdtID.setContentIS(contentIS);
            String repositoryName = transferDeployment(client, sdtID);
            rnames = new String[]{repositoryName};
         }
         else
         {
            rnames = (String[])invoke(client, "distribute", createArgs(deployment));
         }
         // Update the repository names on the deployment
         deployment.setRepositoryNames(rnames);
         log.debug("End distribute, repositoryNames: "+ Arrays.asList(rnames));
      }
      finally
      {
         client.disconnect();
      }
   }

   public String[] getRepositoryNames(DeploymentID dtID) throws Exception
   {
      Client client = getClient();
      try
      {
         log.debug("Begin getRepositoryNames: " + Arrays.asList(dtID.getNames()));
         String[] rnames = (String[]) invoke(client, "getRepositoryNames", createArgs(dtID));
         log.debug("End getRepositoryNames: " + Arrays.asList(rnames));
         return rnames;
      }
      finally
      {
         client.disconnect();
      }
   }

   public void redeploy(DeploymentID dtID) throws Exception
   {
      Client client = getClient();
      try
      {
         log.debug("Begin redeploy: " + dtID);
         invoke(client, "redeploy", createArgs(dtID));
         log.debug("End redeploy: "+dtID);
      }
      finally
      {
         client.disconnect();
      }
   }

   public void prepare(DeploymentID dtID) throws Exception
   {
      Client client = getClient();
      try
      {
         log.debug("Begin prepare: " + dtID);
         invoke(client, "prepare", createArgs(dtID));
         log.debug("End prepare: "+dtID);
      }
      finally
      {
         client.disconnect();
      }
   }

   /**
    * Create the client args.
    *
    * @param dtID the deployment id
    * @return args map
    */
   protected Map<?, ?> createArgs(DeploymentID dtID)
   {
      return new HashMap<Object, Object>(Collections.singletonMap("DeploymentTargetID", dtID));
   }

   /**
    * Start a given deployment(s)
    */
   public void start(DeploymentID dtID) throws Exception
   {
      Client client = getClient();
      try
      {
         log.debug("Start: " + dtID);
         invoke(client, "start", createArgs(dtID));
         log.debug("End start: "+dtID);
      }
      finally
      {
         client.disconnect();
      }
   }

   /**
    * Stop a given module
    */
   public void stop(DeploymentID dtID) throws Exception
   {
      Client client = getClient();
      try
      {
         log.debug("Begin stop: " + dtID);
         invoke(client, "stop", createArgs(dtID));
         log.debug("End stop");
      }
      finally
      {
         client.disconnect();
      }
   }
   
   /**
    * Remove a given module.
    * 
    * @param dtID the deployment id
    * @throws Exception
    */
   public void remove(DeploymentID dtID) throws Exception
   {
      Client client = getClient();
      try
      {
         log.debug("Begin remove: " + dtID);
         invoke(client, "remove", createArgs(dtID));
         log.debug("End remove");
      }
      finally
      {
         client.disconnect();
      }      
   }

   public String toString()
   {
      StringBuilder tmp = new StringBuilder();
      tmp.append("name=");
      tmp.append(name);
      tmp.append(", locator=");
      tmp.append(locator);
      tmp.append(", subsystem=");
      tmp.append(subsystem);
      return tmp.toString();
   }

   /**
    * Get the remoting client connection
    * @return
    * @throws Exception
    */
   private Client getClient() throws Exception
   {
      log.debug("Calling remoting server with locator of: " + locator);

      Client remotingClient = new Client(locator, subsystem);
      remotingClient.connect();
      return remotingClient;
   }

   /**
    * Stream a deployment to the server
    * 
    * @param client - the remoting client
    * @param sdtID - the deployment content encapsulation
    * @return the profile service repository unique name
    * @throws Exception
    */
   private String transferDeployment(Client client, SerializableDeploymentID sdtID) throws Exception
   {
      InputStream is = sdtID.getContentIS();
      try
      {
         // This return value depends on the proxy type
         InvocationResponse response = (InvocationResponse) client.invoke(is, sdtID);
         return (String) response.getResponse();
      }
      catch(Error e)
      {
         throw new RuntimeException(e);
      }
      catch(Throwable e)
      {
         throw new RuntimeException(e);         
      }
      finally
      {
         try
         {
            is.close();
         }
         catch (IOException ignored)
         {
         }
      }
   }

   private Object invoke(Client client, String name, Map<?, ?> args) throws Exception
   {
      try
      {
         return client.invoke(name, args);
      }
      catch(Error e)
      {
         throw new RuntimeException(e);
      }
      catch(Throwable e)
      {
         throw new RuntimeException(e);
      }      
   }
}
