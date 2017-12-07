/**
 * User: alesj
 * Date: 18.4.2006
 * Time: 12:42:33
 * 
 * (C) Genera Lynx d.o.o.
 */

package org.jboss.spring.kernel;

import org.jboss.dependency.spi.ControllerContext;
import org.jboss.kernel.spi.dependency.KernelController;

/**
 * @author <a href="mailto:ales.justin@genera-lynx.com">Ales Justin</a>
 */
public class ControllerLocator implements Locator
{

   private KernelController controller;

   protected ControllerLocator()
   {
   }

   public ControllerLocator(KernelController controller)
   {
      this.controller = controller;
   }

   public KernelController getController()
   {
      return controller;
   }

   public Object locateBean(String beanName)
   {
      ControllerContext context = getController().getInstalledContext(beanName);
      if (context == null)
      {
         return null;
      }
      return context.getTarget();
   }

}
