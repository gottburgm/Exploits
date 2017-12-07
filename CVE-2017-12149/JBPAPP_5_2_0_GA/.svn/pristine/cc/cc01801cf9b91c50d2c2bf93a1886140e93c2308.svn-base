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
package org.jboss.test.ws.jaxws.samples.exception.client;

import javax.xml.ws.WebFault;


@WebFault(name = "UserException", targetNamespace = "http://server.exception.samples.jaxws.ws.test.jboss.org/")
public class UserException_Exception
    extends Exception
{

    /**
     * Java type that goes as soapenv:Fault detail element.
     * 
     */
    private UserException faultInfo;

    /**
     * 
     * @param faultInfo
     * @param message
     */
    public UserException_Exception(String message, UserException faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @param faultInfo
     * @param message
     * @param cause
     */
    public UserException_Exception(String message, UserException faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @return
     *     returns fault bean: org.jboss.test.ws.jaxws.samples.exception.client.UserException
     */
    public UserException getFaultInfo() {
        return faultInfo;
    }

}
