<%@page contentType="text/html"%>
<%@page import="javax.naming.*"%>
<%@page import="org.jboss.test.classloader.leak.ejb.interfaces.*"%>
<%
  try
  {
      InitialContext ctx = new InitialContext();
      StatelessSessionHome slsbhome = (StatelessSessionHome) ctx.lookup("java:comp/env/ejb/ClassloaderLeakEJB2SLSB");
      StatelessSession slsbbean = slsbhome.create();
      slsbbean.log("EJB");
      StatefulSessionHome sfsbhome = (StatefulSessionHome) ctx.lookup("java:comp/env/ejb/ClassloaderLeakEJB2SFSB");
      StatefulSession sfsbbean = sfsbhome.create();
      sfsbbean.log("EJB");
  }
  catch (Exception e)
  {
      throw new javax.servlet.ServletException(e);
  }
%>
EJB