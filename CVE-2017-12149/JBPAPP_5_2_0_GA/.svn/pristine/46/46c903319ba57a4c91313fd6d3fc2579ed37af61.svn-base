<%@page contentType="text/html"
   import="java.util.*"
   import="org.jboss.test.cluster.web.BindingListener"
%>

<html>
<%
   String resp = "FAILED";
   BindingListener listener = new BindingListener();
%>

<% 
   String bind = (String)request.getParameter("Binding");
   if( bind.equals("true") ) {
      session.setAttribute("binding", listener);
      try {
         Thread.sleep(1000);
      } catch (Exception ex) {}

      if( listener.getValueBound() ) {
         resp = "OK";
      }
   } else {
      listener = (BindingListener)session.getAttribute("binding");
      session.removeAttribute("binding");
      try {
         Thread.sleep(1000);
      } catch (Exception ex) {}

      if( listener.getValueUnBound() ) {
         resp = "OK";
      }
   }
%>

<%=resp%>
</html>
