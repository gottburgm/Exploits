<%
   org.apache.catalina.connector.Request catrequest =
      org.jboss.web.tomcat.service.request.ActiveRequestResponseCacheValve.activeRequest.get();
   if(catrequest == null)
      response.setStatus(500);
   else
      response.setStatus(200);
%>
