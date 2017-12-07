/**
 * User: alesj
 * Date: 18.4.2006
 * Time: 12:42:33
 * 
 * (C) Genera Lynx d.o.o.
 */

package org.jboss.spring.kernel;

import java.util.Map;

/**
 * @author <a href="mailto:ales.justin@genera-lynx.com">Ales Justin</a>
 */
public class MapLocator implements Locator
{

   private Map map;

   public MapLocator(Map map)
   {
      this.map = map;
   }

   public Map getMap()
   {
      return map;
   }

   public Object locateBean(String beanName)
   {
      return getMap().get(beanName);
   }

}
