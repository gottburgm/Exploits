<%@page import="javax.naming.InitialContext,javax.rmi.PortableRemoteObject,org.jboss.test.security.interfaces.CustomPrincipalHome,org.jboss.test.security.interfaces.CustomPrincipal,org.jboss.test.security.ejb.CustomPrincipalImpl"
%>

<%
   boolean isCustomType = false;
   try
   {
     InitialContext ic = new InitialContext();
     Object obj = ic.lookup("jaas.CustomPrincipalPropagation");
     obj = PortableRemoteObject.narrow(obj, CustomPrincipalHome.class);
     CustomPrincipalHome home = (CustomPrincipalHome) obj;
     CustomPrincipal bean = home.create();
     isCustomType = bean.validateCallerPrincipal(CustomPrincipalImpl.class);
     bean.remove();
   }
   catch(Exception e)
   {
     throw new IllegalStateException(e.getMessage());
   }
   if(!isCustomType)
       throw new IllegalStateException("Custom Principal not seen");
   out.println("Propagation Success");
%>
