/**
 * User: alesj
 * Date: 18.4.2006
 * Time: 12:42:33
 * 
 * (C) Genera Lynx d.o.o.
 */

package org.jboss.spring.kernel;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.config.*;
import org.springframework.core.Ordered;

/**
 * @author <a href="mailto:ales.justin@genera-lynx.com">Ales Justin</a>
 */
public class MicrocontainerConfigurer extends MicrocontainerLocatorSupport
      implements BeanFactoryPostProcessor, BeanNameAware, BeanFactoryAware, Ordered
{

   private String beanName;
   private BeanFactory beanFactory;
   private int order = Integer.MAX_VALUE;

   private String prefix = "mc${";

   public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactoryToProcess) throws BeansException
   {
      BeanDefinitionVisitor visitor = new MicrocontainerBeanDefinitionVisitor();
      String[] beanNames = beanFactoryToProcess.getBeanDefinitionNames();
      //noinspection ForLoopReplaceableByForEach
      for (int i = 0; i < beanNames.length; i++)
      {
         // Check that we're not parsing our own bean definition,
         // to avoid failing on unresolvable placeholders in properties file locations.
         if (!(beanNames[i].equals(this.beanName) && beanFactoryToProcess.equals(this.beanFactory)))
         {
            BeanDefinition bd = beanFactoryToProcess.getBeanDefinition(beanNames[i]);
            try
            {
               visitor.visitBeanDefinition(bd);
            }
            catch (BeanDefinitionStoreException ex)
            {
               throw new BeanDefinitionStoreException(bd.getResourceDescription(), beanNames[i], ex.getMessage());
            }
         }
      }
   }

   protected boolean isMicrocontainerRef(String value)
   {
      return value != null && value.startsWith(prefix);
   }

   protected String parseBeansReference(String value)
   {
      int endIndex = value.lastIndexOf("$");
      endIndex = endIndex >= 0 ? endIndex : value.length();
      return value.substring(prefix.length(), endIndex);
   }

   private class MicrocontainerBeanDefinitionVisitor extends BeanDefinitionVisitor
   {

      protected String resolveStringValue(String strVal)
      {
         return strVal;
      }

      protected Object resolveValue(Object value)
      {
         value = super.resolveValue(value);
         if (value instanceof TypedStringValue)
         {
            TypedStringValue typedStringValue = (TypedStringValue) value;
            String beansRef = typedStringValue.getValue();
            if (isMicrocontainerRef(beansRef))
            {
               return locateBean(parseBeansReference(beansRef));
            }
         }
         if (value instanceof String)
         {
            String beansRef = (String) value;
            if (isMicrocontainerRef(beansRef))
            {
               return locateBean(parseBeansReference(beansRef));
            }
         }
         return value;
      }

   }

   /**
    * Only necessary to check that we're not parsing our own bean definition,
    * to avoid failing on unresolvable placeholders in properties file locations.
    * The latter case can happen with placeholders for system properties in
    * resource locations.
    *
    * @see #setLocations
    * @see org.springframework.core.io.ResourceEditor
    */
   public void setBeanName(String beanName)
   {
      this.beanName = beanName;
   }

   /**
    * Only necessary to check that we're not parsing our own bean definition,
    * to avoid failing on unresolvable placeholders in properties file locations.
    * The latter case can happen with placeholders for system properties in
    * resource locations.
    *
    * @see #setLocations
    * @see org.springframework.core.io.ResourceEditor
    */
   public void setBeanFactory(BeanFactory beanFactory)
   {
      this.beanFactory = beanFactory;
   }

   public int getOrder()
   {
      return order;
   }

   public void setOrder(int order)
   {
      this.order = order;
   }

   public void setPrefix(String prefix)
   {
      this.prefix = prefix;
   }

}
