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
package org.jboss.verifier.event;

/*
 * Class org.jboss.verifier.event.VerificationEventGeneratorSupport
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
 * $Id: VerificationEventGeneratorSupport.java 81030 2008-11-14 12:59:42Z dimitris@jboss.org $
 *
 * You can reach the author by sending email to jplindfo@helsinki.fi.
 */


// standard imports
import java.util.Enumeration;


// non-standard class dependencies
import org.jboss.verifier.event.EventGeneratorSupport;
/*
 * import org.gjt.lindfors.util.EventGeneratorSupport; to org.jboss.verifier.event.EventGeneratorSupport;
 * Trying to replace gjt-util, hence adding EventGeneratorSupport.java to this package
 * which is taken from Juha Lindfors implementation found at the following reference:
 * Refer org.gjt.lindfors.util.EventGeneratorSupport at www.gjt.org  
 */
import org.jboss.verifier.strategy.VerificationContext;


/**
 * << DESCRIBE THE CLASS HERE >>
 *
 * For more detailed documentation, refer to the
 * <a href="" << INSERT DOC LINK HERE >> </a>
 *
 * @see     << OTHER RELATED CLASSES >>
 *
 * @author 	Juha Lindfors
 * @version $Revision: 81030 $
 * @since  	JDK 1.3
 */
public class VerificationEventGeneratorSupport extends EventGeneratorSupport {

    /*
     * Default constructor
     */
    public VerificationEventGeneratorSupport() {

        super();

    }
    
    public void addVerificationListener(VerificationListener listener) {

        super.addListener(listener);

    }
    
    public void removeVerificationListener(VerificationListener listener) {

        super.removeListener(listener);

    }    
    
    
    /*
     * Fires the event to all VerificationListeners. Listeners implements the
     * beanChecked method and can pull the information from the event object
     * and decide how to handle the situation by themselves
     */
    public void fireBeanChecked(VerificationEvent event) {    
            
        Enumeration e = super.getListeners();

        while (e.hasMoreElements()) {
            VerificationListener listener = (VerificationListener) e.nextElement();
            listener.beanChecked(event);
        }
    }
    
    public void fireSpecViolation(VerificationEvent event) {
        
        Enumeration e = super.getListeners();
        
        while (e.hasMoreElements()) {
            VerificationListener listener = (VerificationListener) e.nextElement();
            listener.specViolation(event);
        }
    }

}

