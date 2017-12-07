package org.jboss.test.iiop.jbpapp6469;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.RemoteHome;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

import org.jboss.ejb3.annotation.IIOP;
import org.jboss.ejb3.annotation.SecurityDomain;

@Stateless
@RemoteHome(HelloRemoteHome.class)
@IIOP(interfaceRepositorySupported=false)
//@SecurityDomain("other")
public class HelloSessionBean
{
   @Resource SessionContext ctx;
   
   //@RolesAllowed({"allowed"})
   public String whoAmI()
   {
      return ctx.getCallerPrincipal().getName();
   }
   
   public String hello(String name)
   {
			System.out.println("hello called for name: " + name);
      return "Hello " + name;
   }
}
