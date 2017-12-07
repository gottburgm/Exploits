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
package org.jboss.verifier.strategy;

/*
 * Class org.jboss.verifier.strategy.VerificationStrategy
 * Copyright (C) 2000  Juha Lindfors
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * This package and its source code is available at www.jboss.org
 * $Id: VerificationStrategy.java 81030 2008-11-14 12:59:42Z dimitris@jboss.org $
 */


// standard imports
import java.util.Iterator;

// non-standard class dependencies

import org.jboss.metadata.EntityMetaData;
import org.jboss.metadata.MessageDrivenMetaData;
import org.jboss.metadata.SessionMetaData;

/**
 * @author  <a href="mailto:juha.lindfors@jboss.org">Juha Lindfors</a>
 * @version $Revision: 81030 $
 * @since   JDK 1.3
 */
public interface VerificationStrategy
{
   /**
    * Does the entity check
    */
   abstract void checkEntity( EntityMetaData entity );

   /**
    * Checks the session
    */
   abstract void checkSession( SessionMetaData session );

   /**
    * Checks the message driven beans (EJB 2.0 only).
    */
   abstract void checkMessageBean( MessageDrivenMetaData message );
}
/*
vim:ts=3:sw=3:et
*/
