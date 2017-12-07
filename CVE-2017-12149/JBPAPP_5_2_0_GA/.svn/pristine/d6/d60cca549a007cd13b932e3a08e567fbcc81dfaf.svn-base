<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="org.jboss.test.web.jbas8318.*" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JBAS-8318 JSF injection test</title>
    </head>
    <body>
    <f:view>

        <h:outputText rendered="#{simpleManagedBean.simpleEnvEntryInjected}" value="Success: Injection of simple env-entry string is ok" />
    	<br/>
    	<h:outputText rendered="#{simpleManagedBean.queueInjected}" value="Success: Injection of Queue is ok" />
    	<br/>
    	<h:outputText rendered="#{simpleManagedBean.userTransactionInjected}" value="Success: Injection of UserTransaction is ok" />
    	<br/>
    	<h:outputText rendered="#{simpleManagedBean.baseClassResourcesInjected}" value="Success: Injection of resources in base class is ok" />
    	<br/>

    	<h:outputText rendered="#{jsfManagedBeanInMetaInf.queueInjected}" value="Success: Injection of Queue in JSF managed bean configured in .war/META-INF/faces-config.xml is ok" />
    	<br/>

    	<h:outputText rendered="#{libJarJSFManagedBean.queueInjected}" value="Success: Injection of Queue in JSF managed bean configured in .war/lib/*.jar/META-INF/faces-config.xml is ok" />
    	<br/>

        <h:outputText rendered="#{managedBeanWithAnnotationsOnlyInBaseClass.baseClassResourcesInjected}" value="Success: Injection of resources in a managed bean with annotations only in the base class, is ok" />
        <br/>

    </f:view>
    </body>
</html>
