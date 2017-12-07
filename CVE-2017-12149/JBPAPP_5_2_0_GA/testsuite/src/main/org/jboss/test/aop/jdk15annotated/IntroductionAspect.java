/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.test.aop.jdk15annotated;

import org.jboss.aop.Mixin;
import org.jboss.aop.Introduction;
import org.jboss.aop.Aspect;
/**
 *
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision: 85945 $
 */
@Aspect (scope=org.jboss.aop.advice.Scope.PER_VM)
public class IntroductionAspect
{
   @Mixin (target=org.jboss.test.aop.jdk15annotated.NoInterfacesPOJO.class, interfaces={java.io.Externalizable.class})
   public static ExternalizableMixin createExternalizableMixin(NoInterfacesPOJO pojo) {
       return new ExternalizableMixin(pojo);
   }
   
   @Mixin (target=org.jboss.test.aop.jdk15annotated.NoInterfacesPOJO2.class, interfaces={Comparable.class})
   public static ComparableMixin createComparableMixin() {
       return new ComparableMixin();
   }
   
   @Introduction (target=org.jboss.test.aop.jdk15annotated.NoInterfacesPOJO.class, interfaces={org.jboss.test.aop.jdk15annotated.EmptyInterface.class})
   public static Object noInterfacesPOJOIntro;

   @Mixin (typeExpression="has(* *->pojoInterfaces2Method(..))", interfaces={java.io.Externalizable.class}, isTransient=false)
   public static ExternalizableMixin createExternalizableMixin2(NoInterfacesPOJO pojo) {
       return new ExternalizableMixin(pojo);
   }
   
   @Introduction (typeExpression="has(* *->pojoInterfaces2Method(..))", interfaces={org.jboss.test.aop.jdk15annotated.EmptyInterface.class})
   public static Object noInterfacesPOJO2Intro;

}