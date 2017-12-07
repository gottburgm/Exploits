<%@page contentType="text/html"
   import="org.jboss.mx.util.*"
   import="javax.management.*"
   import="org.jboss.test.cluster.web.CacheHelper"
%>
<%
   MBeanServer server = MBeanServerLocator.locateJBoss();
   Object version = server.invoke(CacheHelper.OBJECT_NAME, "getSessionVersion", 
                                  new Object[]{session.getId()}, 
                                  new String[] {"java.lang.String"});
%>
<%= version %>
