<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="org.jboss.test.jsf.webapp.*" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JBoss JSF Integration Test</title>
    </head>
    <body>

     <f:view>
         <font color="red"><h1><h:outputText value="#{injectionBean.name}: hit this page twice to complete the test."/></h1></font>
         <h3><h:outputText rendered="#{injectionBean.postConstructCalled}" value="@PostConstruct was called."/></h3>
         <h3><h:outputText rendered="#{mySessionBean.preDestroyCalled}" value="@PreDestroy was called."/></h3>
         <h3><h:outputText rendered="#{injectionBean.datasourceInjected}" value="Datasource was injected."/></h3>
         
         <%
               // I think that the fact I need to do this constitutes a bug in JSTL
               if (session.getAttribute("mySessionBean") == null) {
                    session.setAttribute("mySessionBean", new MySessionBean()); 
               }
          %>
          
         <font color="red"><h1>Classic test of JSTL 1.2/JSF 1.2 with deferred expressions:</h1></font>
         <c:forEach var="item" items="#{mySessionBean.numList}">
             <h3><h:outputText value="#{item}"/></h3>
         </c:forEach>

         <font color="red"><h1>ServletContext Minor Version Test (should return 5)</h1></font>
         <h3><h:outputText value="ServletContext.getMinorVersion() = #{application.minorVersion}"/></h3>

         <font color="red"><h1>Enum Test</h1></font>
         <h3><h:outputText rendered="#{mySessionBean.color == 'PURPLE'}" 
                           value="JBoss Color selection is #{mySessionBean.color}"
                           /></h3>
         <font color="red"><h1>Test using JDK class as a managed bean</h1></font>     
         <h3><h:outputText value="JButton value = #{myJButton}"/></h3>
     </f:view>
 
    </body>
</html>
