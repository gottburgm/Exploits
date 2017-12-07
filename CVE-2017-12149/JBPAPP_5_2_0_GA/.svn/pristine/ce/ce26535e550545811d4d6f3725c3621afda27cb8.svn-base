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
package org.jboss.test.jsf.webapp;

/**
 * Session bean used to tell if @PreDestroy was called on the InjectionBean.
 *
 * Also allows you to get the numList for testing JSTL/JSF integration.
 *
 * @author Stan Silvert
 */
public class MySessionBean {
    
    private String[] numList = {"number one", "number two", "number three"};
    
    private boolean preDestroyCalled = false;

    private JBossColor color = JBossColor.PURPLE;
    
    public boolean isPreDestroyCalled() {
        return this.preDestroyCalled;
    }
    
    public void setPreDestroyCalled(boolean preDestroyCalled) {
        this.preDestroyCalled = preDestroyCalled;
    }

    public JBossColor getColor() {
        return this.color;
    }

    public void setColor(JBossColor color) {
        this.color = color;
    }
    
    public String[] getNumList() {
        return this.numList;
    }
    
    public void setNumList(String[] numList) {
        this.numList = numList;
    }
}
