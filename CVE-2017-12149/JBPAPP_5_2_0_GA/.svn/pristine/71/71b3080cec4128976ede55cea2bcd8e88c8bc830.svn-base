<%@page contentType="text/html"%>
<%@page import="org.apache.commons.logging.Log"%>
<%@page import="org.apache.commons.logging.LogFactory"%>
<%
  org.jboss.test.classloader.leak.clstore.ClassLoaderStore.getInstance().storeClassLoader("JSP", getClass().getClassLoader());
  org.jboss.test.classloader.leak.clstore.ClassLoaderStore.getInstance().storeClassLoader("JSP_TCCL", Thread.currentThread().getContextClassLoader());
  
  Log log = LogFactory.getLog("WEBAPP");
  log.info("Logging from " + getClass().getName());
%>
WEBAPP