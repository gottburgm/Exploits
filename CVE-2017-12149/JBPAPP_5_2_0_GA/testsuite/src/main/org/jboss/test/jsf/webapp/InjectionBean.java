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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;
import javax.naming.InitialContext;
import javax.sql.DataSource;

/**
 * Tests resource injection and lifecycle annotations for JSF managed bean.
 *
 * @author Stan Silvert
 */
public class InjectionBean {
    
    private DataSource defaultDS;
    
    private boolean postConstructCalled = false;
    private boolean datasourceInjected = false;
    
    // This bean lives in the HttpSession.  Save a reference here.
    private MySessionBean mySessionBean = null;
    
    /** Creates a new instance of InjectionBean */
    public InjectionBean() {
    }
    
    public String getName() {
        return "InjectionBean";
    }
    
    public boolean getPostConstructCalled() {
        return this.postConstructCalled;
    }
    
    public boolean getDatasourceInjected() {
        return this.datasourceInjected;
    }

    @PostConstruct
    private void testPostConstruct() {
        this.postConstructCalled = true;
        
        Object dataSourceFromLookup = null;
        try {
            dataSourceFromLookup = new InitialContext().lookup("java:/DefaultDS");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (defaultDS == dataSourceFromLookup) {
            this.datasourceInjected = true;
        } 
        
        // doing this puts an instance of MySesisonBean into the session
        ValueExpression preDestroyVe = expressionFactory().createValueExpression(elContext(), "#{mySessionBean}", MySessionBean.class);
        this.mySessionBean = (MySessionBean)preDestroyVe.getValue(elContext());
        
    }
    
    private ELContext elContext() {
        return FacesContext.getCurrentInstance().getELContext();
    }
    
    private ExpressionFactory expressionFactory() {
        return FacesContext.getCurrentInstance().getApplication().getExpressionFactory();
    }
    
    @PreDestroy
    public void testPreDestroy() {
        this.mySessionBean.setPreDestroyCalled(true);
    }
    
    @Resource(name="defaultDS", mappedName="java:/DefaultDS")
    public void setDefaultDS(DataSource dataSource) {
        this.defaultDS = dataSource;
    }
    
}
