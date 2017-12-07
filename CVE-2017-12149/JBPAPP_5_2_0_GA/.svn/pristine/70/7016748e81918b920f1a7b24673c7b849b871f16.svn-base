/**
 * User: alesj
 * Date: 18.4.2006
 * Time: 12:42:33
 * 
 * (C) Genera Lynx d.o.o.
 */

package org.jboss.spring.kernel;

import org.jboss.kernel.Kernel;
import org.jboss.kernel.spi.dependency.KernelController;

/**
 * @author <a href="mailto:ales.justin@genera-lynx.com">Ales Justin</a>
 */
public class KernelLocator extends ControllerLocator
{

   private Kernel kernel;

   protected KernelLocator()
   {
   }

   public KernelLocator(Kernel kernel)
   {
      this.kernel = kernel;
   }

   public Kernel getKernel()
   {
      return kernel;
   }

   protected void setKernel(Kernel kernel)
   {
      this.kernel = kernel;
   }

   public KernelController getController()
   {
      return getKernel().getController();
   }

}
