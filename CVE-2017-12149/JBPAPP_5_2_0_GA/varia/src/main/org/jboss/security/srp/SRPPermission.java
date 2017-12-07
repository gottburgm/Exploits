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
package org.jboss.security.srp;

import java.security.BasicPermission;

/** A custom permission class for protecting access to sensitive SRP information
like the private session key and private key.

The following table lists all the possible SRPPermission target names,
and for each provides a description of what the permission allows
and a discussion of the risks of granting code the permission.
<table border=1 cellpadding=5>
    <tr>
        <th>Permission Target Name</th>
        <th>What the Permission Allows</th>
        <th>Risks of Allowing this Permission</th>
    </tr>

    <tr>
        <td>getSessionKey</td>
        <td>Access the private SRP session key</td>
        <td>This provides access the the private session key that results from
the SRP negiotation. Access to this key will allow one to encrypt/decrypt msgs
that have been encrypted with the session key.
        </td>
    </tr>

</table>

@author Scott.Stark@jboss.org
@version $Revision: 81038 $
*/
public class SRPPermission extends BasicPermission
{

    /** Creates new SRPPermission */
    public SRPPermission(String name)
    {
        super(name);
    }
    public SRPPermission(String name, String actions)
    {
        super(name, actions);
    }

}
