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

package org.jboss.verifier.event;

/*
 * Class org.jboss.verifier.event.Library (refer Class org.gjt.lindfors.util.Library at www.gjt.org)
 * Copyright (C) 1999  Juha Lindfors
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 * This package and its source code is available at www.jboss.org
 * $Id: Library.java 85945 2009-03-16 19:45:12Z dimitris@jboss.org $
 *
 * You can reach the author by sending email to jpl@gjt.org or
 * directly to jplindfo@helsinki.fi.
 */


/**
 * Collection of miscellaneous utility methods.
 * <p>
 * For more detailed documentation, refer to the
 * <a href="http://www.gjt.org/%7ejpl/org_gjt_lindfors_util/index.html">
 * Util Library Developer's Guide</a>
 *
 * @author     Juha Lindfors
 * @version    $Revision: 85945 $
 * @since      JDK1.1
 */
public class Library {

    private Library() {}

    /**
     * Returns the class name of an object. This method returns only the
     * name of the class, not a fully qualified class name with package
     * information. For fully qualified class name, use
     * <code>getClass().getName()</code>.
     *
     * @param    obj     object whose class name is wanted
     *
     * @return   object's class name, without package information
     *
     * @see    java.lang.Object#getClass()
     */
    public static String getClassName(Object obj) {
        String str = obj.getClass().getName();
        int index = str.lastIndexOf('.');
        return str.substring(index+1);
    }
}

