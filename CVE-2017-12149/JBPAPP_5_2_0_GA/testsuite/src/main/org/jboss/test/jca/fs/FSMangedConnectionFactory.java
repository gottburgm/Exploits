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
package org.jboss.test.jca.fs;

import java.io.Serializable;
import java.io.File;
import java.io.PrintWriter;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.HashSet;
import java.util.Iterator;
import java.security.acl.Group;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.ResourceException;
import javax.security.auth.Subject;

import org.jboss.logging.Logger;
import org.jboss.security.SimplePrincipal;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class FSMangedConnectionFactory
   implements ManagedConnectionFactory, Serializable
{
   private static long serialVersionUID = 100000;
   private static Logger log = Logger.getLogger(FSMangedConnectionFactory.class);

   private String userName;
   private String password;
   private Set roles;
   private transient File rootDir;

   /** Creates new FSMangedConnectionFactory */
   public FSMangedConnectionFactory()
   {
   }

   public Object createConnectionFactory() throws ResourceException
   {
      log.debug("createConnectionFactory");
      throw new UnsupportedOperationException("Cannot be used in unmanaed env");
   }
   public Object createConnectionFactory(ConnectionManager cm) throws ResourceException
   {
      log.debug("createConnectionFactory, cm="+cm, new Exception("CalledBy:"));
      FSRequestInfo fsInfo = new FSRequestInfo(rootDir);
      return new DirContextFactoryImpl(cm, this, fsInfo);
   }
   public ManagedConnection createManagedConnection(Subject subject,
      ConnectionRequestInfo info)
      throws ResourceException
   {
      log.debug("createManagedConnection, subject="+subject+", info="+info,
         new Exception("CalledBy:"));
      FSRequestInfo fsInfo = (FSRequestInfo) info;
      if( roles != null && roles.size() > 0 )
      {
         validateRoles(subject);
      }
      return new FSManagedConnection(subject, fsInfo);
   }

   public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject,
      ConnectionRequestInfo info)
      throws ResourceException
   {
      log.debug("matchManagedConnections, connectionSet="+connectionSet+", subject="+subject+", info="+info);
      return (ManagedConnection) connectionSet.iterator().next();
   }
   public PrintWriter getLogWriter() throws ResourceException
   {
      return null;
   }
   public void setLogWriter(PrintWriter out) throws ResourceException
   {
   }
   public boolean equals(Object other)
   {
      return super.equals(other);
   }
   public int hashCode()
   {
      return super.hashCode();
   }

   public String getUserName()
   {
      return userName;
   }
   public void setUserName(String userName)
   {
      this.userName = userName;
   }

   public String getPassword()
   {
      return password;
   }
   public void setPassword(String password)
   {
      this.password = password;
   }

   public String getRoles()
   {
      return roles.toString();
   }
   public void setRoles(String roles)
   {
      this.roles = new HashSet();
      StringTokenizer st = new StringTokenizer(roles, ",");
      while( st.hasMoreTokens() )
      {
         String role = st.nextToken();
         this.roles.add(role);
      }
   }

   public void setFileSystemRootDir(String rootDirPath)
   {
      rootDir = new File(rootDirPath);
      if( rootDir.exists() == false )
         rootDir.mkdirs();
      log.debug("setFileSystemRootDir, rootDir="+rootDir.getAbsolutePath(),
         new Exception("CalledBy:"));
   }

   private void validateRoles(Subject theSubject)
      throws ResourceException
   {
      if(theSubject == null)
         throw new IllegalArgumentException("theSubject is null");
      Set subjectGroups = theSubject.getPrincipals(Group.class);
      Iterator iter = subjectGroups.iterator();
      Group roleGrp = null;
      while (iter.hasNext())
      {
         Group grp = (Group) iter.next();
         String name = grp.getName();
         if (name.equals("Roles"))
            roleGrp = grp;
      }
      if( roleGrp == null )
         throw new ResourceException("Subject has not Roles");

      boolean isValid = false;
      iter = roles.iterator();
      while( iter.hasNext() && isValid == false )
      {
         String name = (String) iter.next();
         SimplePrincipal role = new SimplePrincipal(name);
         isValid = roleGrp.isMember(role);
      }
      if( isValid == false )
      {
         String msg = "Authorization failure, subjectRoles="+roleGrp
            + ", requiredRoles="+roles;
         throw new ResourceException(msg);
      }
   }
}
