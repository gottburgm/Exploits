<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="java.net.URL"%>
<%@page import="javax.ejb.EJB"%>
<%@page import="org.jboss.test.web.ejb3.SimpleStateful"%>
<%@page import="org.jboss.test.web.ejb3.SimpleStateless"%>
<%@page import="javax.annotation.Resource"%>

<%!
   public static class Nested
   {
      
      @EJB
      SimpleStateful stateful;
   
      SimpleStateless stateless;
      
      @Resource(name = "url/myHome", mappedName = "http://www.jboss.org")
      URL url;
      
      public Nested()
      {
         super();
      }
      
      public URL getUrl()
      {
         return this.url;
      }
      
      public SimpleStateful getSimpleStateful()
      {
      	 return this.stateful;
      } 
      
      public SimpleStateless getSimpleStateless()
      {
         return this.stateless;
      }
      
      @EJB      
      public void setSimpleStateless(SimpleStateless stateless)
      {
         this.stateless = stateless;
      }
   }
%>
<%
 Nested nested = new Nested();
 
 assert nested.getSimpleStateful() != null : "Nested.stateful is null";
 assert nested.getSimpleStateful().doSomething() == true : "Nested.stateful returned false";
 assert nested.getSimpleStateless() != null : "Nested.stateless is null";
 assert nested.getSimpleStateless().doSomething() == true : "Nested.stateless returned false";
 assert nested.getUrl() != null : "Nested.url is not injected";

%>
tests passed.