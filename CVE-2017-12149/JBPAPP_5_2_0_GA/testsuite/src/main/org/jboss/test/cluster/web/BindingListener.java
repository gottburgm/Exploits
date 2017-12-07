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
package org.jboss.test.cluster.web;

import java.io.Serializable;
import javax.servlet.http.HttpSessionBindingListener;

/**
 * Testing for clustered session binding event.
 * @author  Stan Silvert
 */
public class BindingListener implements HttpSessionBindingListener, Serializable {
    private boolean isValueBound = false;
    private boolean isValueUnBound = false;
 
    /** Creates a new instance of BindingListener */
    public BindingListener() {
    }
    
    public void valueBound(javax.servlet.http.HttpSessionBindingEvent httpSessionBindingEvent) {
       setValueBound();
    }
    
    public void valueUnbound(javax.servlet.http.HttpSessionBindingEvent httpSessionBindingEvent) {
       setValueUnBound();
    }
    
    public void setValueBound() { isValueBound = true; }
    public boolean getValueBound() { return isValueBound; }
    public void setValueUnBound() { isValueUnBound = true; }
    public boolean getValueUnBound() { return isValueUnBound; }
}
