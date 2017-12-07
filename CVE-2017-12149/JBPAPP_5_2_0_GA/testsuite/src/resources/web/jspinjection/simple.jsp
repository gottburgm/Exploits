<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="javax.ejb.EJB"%>
<%@page import="org.jboss.test.web.ejb3.SimpleStateful"%>
<%@page import="org.jboss.test.web.ejb3.SimpleStateless"%>
<%!
@EJB(mappedName = "simpleStatefulMappedName")
private SimpleStateful simpleStateful;
   
@EJB
private SimpleStateless simpleStateless;
%>
<%
assert simpleStateful != null : "simpleStateful is null";
assert simpleStateless != null : "simpleStateless is null";

assert simpleStateful.doSomething() == true : "simpleStateful returned false";
assert simpleStateless.doSomething() == true : "simpleStateless returned false";

%>
tests passed.