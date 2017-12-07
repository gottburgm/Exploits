<%@page contentType="text/html"%>
<%@page import="javax.naming.*"%>
<%@page import="org.jboss.test.classloader.leak.ejb.interfaces.*"%>
<%
  try
  {
      InitialContext ctx = new InitialContext();
      StatelessSessionHome home = (StatelessSessionHome) ctx.lookup("java:comp/env/ejb/ClassloaderLeakEJB");
      StatelessSession bean = home.create();
      bean.log("EJB");
  }
  catch (Exception e)
  {
      throw new javax.servlet.ServletException(e);
  }
%>
EJB