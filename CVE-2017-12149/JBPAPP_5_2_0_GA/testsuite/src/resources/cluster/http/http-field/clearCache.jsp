<%@page contentType="text/html"
   import="java.util.*"
   import="org.jboss.test.cluster.web.*"
%>

<html>
<center>
<% 
   String id=request.getSession().getId();
   CacheHelper.getCacheInstance().remove("/");
%>
<%=id%>

<h1><%=application.getServerInfo()%>:<%=request.getServerPort()%></h1>
</body>
</html>
