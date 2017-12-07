/**
 * User: alesj
 * Date: 18.4.2006
 * Time: 12:42:33
 * 
 * (C) Genera Lynx d.o.o.
 */

package org.jboss.spring.kernel;

/**
 * @author <a href="mailto:ales.justin@genera-lynx.com">Ales Justin</a>
 */
public class MicrocontainerLocatorSupport
{

   private Locator locator = new NullLocator();

   public MicrocontainerLocatorSupport()
   {
   }

   public MicrocontainerLocatorSupport(Locator locator)
   {
      this.locator = locator;
   }

   public Locator getLocator()
   {
      return locator;
   }

   public void setLocator(Locator locator)
   {
      this.locator = locator;
   }

   public Object locateBean(String beanName)
   {
      return getLocator().locateBean(beanName);
   }

}
