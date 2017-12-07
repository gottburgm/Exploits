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
package org.jboss.web.tomcat.security;

/**
 * An implementation of the catelinz Realm and Valve interfaces. The Realm
 * implementation handles authentication and authorization using the JBossSX
 * security framework. It relieas on the JNDI ENC namespace setup by the
 * AbstractWebContainer. In particular, it uses the java:comp/env/security
 * subcontext to access the security manager interfaces for authorization and
 * authenticaton. <p/> The Valve interface is used to associated the
 * authenticated user with the SecurityAssociation class when a request begins
 * so that web components may call EJBs and have the principal propagated. The
 * security association is removed when the request completes.
 *
 * @deprecated User JBossWebRealm
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81037 $
 * @see org.jboss.security.AuthenticationManager
 * @see org.jboss.security.CertificatePrincipal
 * @see org.jboss.security.RealmMapping
 * @see org.jboss.security.SimplePrincipal
 * @see org.jboss.security.SecurityAssociation
 * @see org.jboss.security.SubjectSecurityManager
 */
public class JBossSecurityMgrRealm extends JBossWebRealm
{ 
}
