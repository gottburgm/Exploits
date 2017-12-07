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
package org.jboss.test.security.test;

import java.security.BasicPermission;
import java.security.Permission;
import java.security.PermissionCollection;
import javax.naming.Name;

/** A path like heirarchical permission.

@author Scott.Stark@jboss.org
@version $Revision: 81036 $
*/
public class NamespacePermission extends BasicPermission
{
    private PermissionName fullName;
    private String actions;

    /** Creates new NamespacePermission */
    public NamespacePermission(String name, String actions)
    {
        super(name, actions);
        this.actions = actions;
        fullName = new PermissionName(name);
    }
    public NamespacePermission(Name name, String actions)
    {
        super(name.toString(), actions);
        this.actions = actions;
        fullName = new PermissionName(name);
    }

    public String getActions()
    {
        return actions;
    }

    public PermissionName getFullName()
    {
        return fullName;
    }

    public boolean implies(Permission p)
    {
        String pactions = p.getActions();
        boolean implied = true;
        for(int n = 0; n < actions.length(); n ++)
        {
            char a = actions.charAt(n);
            char pa = pactions.charAt(n);
            if( (a != '-' && pa != '-' && pa != a) )
            {
                implied = false;
                break;
            }
            else if( a == '-' && pa != '-' )
            {
                implied = false;
                break;
            }
        }
        return implied;
    }

    public PermissionCollection newPermissionCollection()
    {
    	return new NamespacePermissionCollection();
    }
}
