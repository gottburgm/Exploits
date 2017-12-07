<%@page contentType="text/html"
   import="java.util.*"
   import="org.jboss.test.cluster.web.aop.AopBindingListener"
%>

<html>
<%
   String resp = "FAILED";
   AopBindingListener listener = new AopBindingListener();
%>

<% 
   String bind = (String)request.getParameter("Binding");
   if( "new".equals(bind) ) {
      session.setAttribute("binding", listener);
      try {
         Thread.sleep(1000);
      } catch (Exception ex) {}

      if( listener.getValueBound() ) {
         resp = "OK";
      }
   } 
   else if ( "rebind".equals(bind) ) {
      // JBAS-2381 -- if we rebind the same object 
      // we should not get a valueUnbound()
      listener = (AopBindingListener)session.getAttribute("binding");
      session.setAttribute("binding", new AopBindingListener());
      try {
         Thread.sleep(1000);
      } catch (Exception ex) {}

      if( listener.getValueUnBound() ) {
         resp = "OK";
      }
   } 
   else if ( "replace".equals(bind) ){
      listener = (AopBindingListener)session.getAttribute("binding");
      session.setAttribute("binding", listener);
      try {
         Thread.sleep(1000);
      } catch (Exception ex) {}

      if( listener.getValueUnBound() == false ) {
         resp = "OK";
      }
   } 
   else if ( "remove".equals(bind) ) {
      listener = (AopBindingListener)session.getAttribute("binding");
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
