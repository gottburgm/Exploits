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
package org.jboss.web.jsf.integration.injection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.xml.ws.WebServiceRef;

import org.jboss.logging.Logger;

import com.sun.faces.spi.InjectionProvider;
import com.sun.faces.spi.InjectionProviderException;

/**
 * Provides interface between JSF RI and Tomcat Catalina for injection of managed beans as
 * per JSF 1.2 Spec section 5.4.
 *
 * @author Stan Silvert
 * @author Fabien Carrion
 * @author Remy Maucherat
 */
public class JBossInjectionProvider implements InjectionProvider {
    private static final Logger LOG = Logger.getLogger(JBossInjectionProvider.class);
    private static final String NAMING_DISABLED = "Injection of naming resources into JSF managed beans disabled.";

    private Context namingContext;
    
    /**
     * Uses the default naming context for injection of resources into managed beans.
     */
    public JBossInjectionProvider() {
        try {
            this.namingContext = new InitialContext();
        } catch (Exception e) {
            LOG.warn(NAMING_DISABLED, e);
        }
    }
    
    /**
     * This constructor allows a subclass to override the default naming 
     * context.
     *
     * @param namingContext The naming context to use for injection of managed beans.
     *                      If this param is null then injection of resources will be
     *                      disabled and JBoss will only call @PostConstruct and
     *                      @PreDestroy methods.
     */
    protected JBossInjectionProvider(Context namingContext) {
        if (namingContext == null) {
            LOG.warn(NAMING_DISABLED);
        }
        
        this.namingContext = namingContext;
    }
    
    /**
     * Call methods on a managed bean that are annotated with @PreDestroy.
     */
    public void invokePreDestroy(Object managedBean) throws InjectionProviderException {
        try {
            Method[] methods = managedBean.getClass().getDeclaredMethods();
            Method preDestroy = null;
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].isAnnotationPresent(PreDestroy.class)) {
                    if ((preDestroy != null) 
                            || (methods[i].getParameterTypes().length != 0)
                            || (Modifier.isStatic(methods[i].getModifiers())) 
                            || (methods[i].getExceptionTypes().length > 0)
                            || (!methods[i].getReturnType().getName().equals("void"))) {
                        throw new IllegalArgumentException("Invalid PreDestroy annotation");
                    }
                    preDestroy = methods[i];
                }
            }

            // At the end the postconstruct annotated 
            // method is invoked
            if (preDestroy != null) {
                boolean accessibility = preDestroy.isAccessible();
                preDestroy.setAccessible(true);
                preDestroy.invoke(managedBean);
                preDestroy.setAccessible(accessibility);
            }
        } catch (Exception e) {
            LOG.error("PreDestroy failed on managed bean.", e);
        }
    }

    /**
     * Call methods on a managed bean that are annotated with @PostConstruct.
     */
    public void invokePostConstruct(Object managedBean) throws InjectionProviderException {
        try {
            Method[] methods = managedBean.getClass().getDeclaredMethods();
            Method postConstruct = null;
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].isAnnotationPresent(PostConstruct.class)) {
                    if ((postConstruct != null) 
                            || (methods[i].getParameterTypes().length != 0)
                            || (Modifier.isStatic(methods[i].getModifiers())) 
                            || (methods[i].getExceptionTypes().length > 0)
                            || (!methods[i].getReturnType().getName().equals("void"))) {
                        throw new IllegalArgumentException("Invalid PostConstruct annotation");
                    }
                    postConstruct = methods[i];
                }
            }

            // At the end the postconstruct annotated 
            // method is invoked
            if (postConstruct != null) {
                boolean accessibility = postConstruct.isAccessible();
                postConstruct.setAccessible(true);
                postConstruct.invoke(managedBean);
                postConstruct.setAccessible(accessibility);
            }
        } catch (Exception e) {
            LOG.error("PostConstruct failed on managed bean.", e);
        }
    }

    /**
     * Inject naming resources into a managed bean and then call methods
     * annotated with @PostConstruct.
     */
    public void inject(Object managedBean) throws InjectionProviderException {
        if (this.namingContext != null) {
            try {
                
                // Initialize fields annotations
                Field[] fields = managedBean.getClass().getDeclaredFields();
                for (int i = 0; i < fields.length; i++) {
                    if (fields[i].isAnnotationPresent(Resource.class)) {
                        Resource annotation = (Resource) fields[i].getAnnotation(Resource.class);
                        lookupFieldResource(namingContext, managedBean, fields[i], annotation.name());
                    }
                    if (fields[i].isAnnotationPresent(EJB.class)) {
                        EJB annotation = (EJB) fields[i].getAnnotation(EJB.class);
                        lookupFieldResource(namingContext, managedBean, fields[i], annotation.name());
                    }
                    if (fields[i].isAnnotationPresent(WebServiceRef.class)) {
                        WebServiceRef annotation = 
                            (WebServiceRef) fields[i].getAnnotation(WebServiceRef.class);
                        lookupFieldResource(namingContext, managedBean, fields[i], annotation.name());
                    }
                    if (fields[i].isAnnotationPresent(PersistenceContext.class)) {
                        PersistenceContext annotation = 
                            (PersistenceContext) fields[i].getAnnotation(PersistenceContext.class);
                        lookupFieldResource(namingContext, managedBean, fields[i], annotation.name());
                    }
                    if (fields[i].isAnnotationPresent(PersistenceUnit.class)) {
                        PersistenceUnit annotation = 
                            (PersistenceUnit) fields[i].getAnnotation(PersistenceUnit.class);
                        lookupFieldResource(namingContext, managedBean, fields[i], annotation.name());
                    }
                }
                
                // Initialize methods annotations
                Method[] methods = managedBean.getClass().getDeclaredMethods();
                for (int i = 0; i < methods.length; i++) {
                    if (methods[i].isAnnotationPresent(Resource.class)) {
                        Resource annotation = (Resource) methods[i].getAnnotation(Resource.class);
                        lookupMethodResource(namingContext, managedBean, methods[i], annotation.name());
                    }
                    if (methods[i].isAnnotationPresent(EJB.class)) {
                        EJB annotation = (EJB) methods[i].getAnnotation(EJB.class);
                        lookupMethodResource(namingContext, managedBean, methods[i], annotation.name());
                    }
                    if (methods[i].isAnnotationPresent(WebServiceRef.class)) {
                        WebServiceRef annotation = 
                            (WebServiceRef) methods[i].getAnnotation(WebServiceRef.class);
                        lookupMethodResource(namingContext, managedBean, methods[i], annotation.name());
                    }
                    if (methods[i].isAnnotationPresent(PersistenceContext.class)) {
                        PersistenceContext annotation = 
                            (PersistenceContext) methods[i].getAnnotation(PersistenceContext.class);
                        lookupMethodResource(namingContext, managedBean, methods[i], annotation.name());
                    }
                    if (methods[i].isAnnotationPresent(PersistenceUnit.class)) {
                        PersistenceUnit annotation = 
                            (PersistenceUnit) methods[i].getAnnotation(PersistenceUnit.class);
                        lookupMethodResource(namingContext, managedBean, methods[i], annotation.name());
                    }
                }
                
            } catch (Exception e) {
                LOG.error("Injection failed on managed bean.", e);
            }
        }
        
    }

    /**
     * Inject resources in specified field.
     */
    protected static void lookupFieldResource(javax.naming.Context context, 
            Object instance, Field field, String name)
        throws NamingException, IllegalAccessException {
    
        Object lookedupResource = null;
        boolean accessibility = false;
        
        if ((name != null) &&
                (name.length() > 0)) {
            lookedupResource = context.lookup(name);
        } else {
            lookedupResource = context.lookup(instance.getClass().getName() + "/" + field.getName());
        }
        
        accessibility = field.isAccessible();
        field.setAccessible(true);
        field.set(instance, lookedupResource);
        field.setAccessible(accessibility);
    }


    /**
     * Inject resources in specified method.
     */
    protected static void lookupMethodResource(javax.naming.Context context, 
            Object instance, Method method, String name)
        throws NamingException, IllegalAccessException, InvocationTargetException {
        
        if (!method.getName().startsWith("set") 
                || method.getParameterTypes().length != 1
                || !method.getReturnType().getName().equals("void")) {
            throw new IllegalArgumentException("Invalid method resource injection annotation");
        }
        
        Object lookedupResource = null;
        boolean accessibility = false;
        
        if ((name != null) &&
                (name.length() > 0)) {
            lookedupResource = context.lookup(name);
        } else {
            lookedupResource = 
                context.lookup(instance.getClass().getName() + "/" + method.getName().substring(3));
        }
        
        accessibility = method.isAccessible();
        method.setAccessible(true);
        method.invoke(instance, lookedupResource);
        method.setAccessible(accessibility);
    }
    
}
