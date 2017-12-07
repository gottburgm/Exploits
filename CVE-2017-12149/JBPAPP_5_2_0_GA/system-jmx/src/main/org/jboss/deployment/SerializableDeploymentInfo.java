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
package org.jboss.deployment;

// $Id: SerializableDeploymentInfo.java 81033 2008-11-14 13:05:58Z dimitris@jboss.org $

import javax.management.ObjectName;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * DeploymentInfo for remote access by the DeploymentManager.
 * It provides a serializable subset of the information available in DeploymentInfo.
 *
 * @author thomas.diesler@jboss.org
 * @version $Revision: 81033 $
 */
public class SerializableDeploymentInfo implements Serializable
{
   /** @since 4.0.2 */
   private static final long serialVersionUID = -3847995513551913798L;
   
   // The initial construction timestamp
   public Date date;
   // The URL identifing this SDI
   public URL url;
   // An optional URL to a local copy of the deployment
   public URL localUrl;
   // The URL used to watch for changes when the deployment is unpacked
   public URL watch;
   // The suffix of the deployment url
   public String shortName;
   // The last system time the deployment inited by the MainDeployer
   public long lastDeployed;
   // Use for "should we redeploy failed"
   public long lastModified;
   // A free form status for the "state" can be Deployed/failed etc etc
   public String status;
   // The current state of the deployment
   public DeploymentState state;
   // The subdeployer that handles the deployment
   public ObjectName deployer;
   // The classpath declared by this xml descriptor, needs <classpath> entry
   public Collection classpath = new ArrayList();
   // The mbeans deployed
   public List mbeans;
   // Anyone can have subdeployments
   public List subDeployments;
   // And the subDeployments have a parent
   public SerializableDeploymentInfo parent;
   // the web root context in case of war file
   public String webContext;
   // An optional URL to the URL of the document loaded
   public URL documentUrl;
   // Is this a stand-alone service descriptor
   public boolean isXML;
   public boolean isScript;
   // Does the deployment url point to a directory
   public boolean isDirectory;
   // Can contain the MBean that is created through the deployment
   public ObjectName deployedObject;

   // Constructors *****************************************************************************************************

   /**
    * Construct this object from a DeploymentInfo
    */
   public SerializableDeploymentInfo(DeploymentInfo info)
   {
      this.date = info.date;
      this.url = info.url;
      this.localUrl = info.localUrl;
      this.watch = info.watch;
      this.shortName = info.shortName;
      this.lastDeployed = info.lastDeployed;
      this.lastModified = info.lastModified;
      this.status = info.status;
      this.state = info.state;
      this.deployer = info.deployer.getServiceName();
      this.classpath = info.classpath;
      this.mbeans = info.mbeans;
      this.webContext = info.webContext;
      this.documentUrl = info.documentUrl;
      this.isXML = info.isXML;
      this.isScript = info.isScript;
      this.isDirectory = info.isDirectory;
      this.deployedObject = info.deployedObject;

      // we do these in a second iteration
      this.parent = null;
      this.subDeployments = new ArrayList();
   }

   /**
    * Returns a string representation of the object.
    */
   public String toString()
   {
      StringBuffer s = new StringBuffer(super.toString());
      s.append(" { url=" + url + " }\n");
      s.append("  deployer: " + deployer + "\n");
      s.append("  status: " + status + "\n");
      s.append("  state: " + state + "\n");
      s.append("  watch: " + watch + "\n");
      s.append("  lastDeployed: " + lastDeployed + "\n");
      s.append("  lastModified: " + lastModified + "\n");
      s.append("  mbeans: " + mbeans + "\n");
      s.append(" }\n");
      return s.toString();
   }
}
