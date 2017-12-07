<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="org.jboss.test.jsf.webapp.*" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JBoss JSF Injection Test</title>
    </head>
    <body>
    <f:view>
        <h:outputText rendered="#{simpleManagedBean.testStatefulBean}" value="testStatefulBean is ok" />
    	<br/>
    	<h:outputText rendered="#{simpleManagedBean.testStatelessBean}" value="testStatelessBean is ok" />
    	<br/>
    	<h:outputText rendered="#{simpleManagedBean.testURL}" value="testURL is ok" />
    	<br/>
    	<h:outputText rendered="#{simpleManagedBean.testSimpleLocal}" value="testSimpleLocal is ok" />
    </f:view>
    </body>
</html>