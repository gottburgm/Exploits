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
package org.jboss.test.jmx.compliance.server.support;

/**
 * <description> 
 *
 * @see <related>
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 81019 $
 *   
 */
public interface TestMBean
{
   public String getThisWillScream() throws MyScreamingException;
   public void setThisWillScream(String str) throws MyScreamingException;
   
   public String getThrowUncheckedException();
   public void setThrowUncheckedException(String str);
   
   public String getError();
   public void setError(String str);
   
   public void setAStringAttribute(String str);
   
   public void operationWithException() throws MyScreamingException;
   
   public boolean opWithPrimBooleanReturn();
   
   public long opWithPrimLongReturn();
   
   public long[] opWithPrimLongArrayReturn();
   
   public Long[] opWithLongArrayReturn();
   
   public double opWithPrimDoubleReturn();
   
   public void opWithLongSignature(int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, 
                                   int i11, int i12, int i13, int i14, int i15, int i16, int i17, int i18, int i19, int i20);

   public void opWithMixedSignature(int i1, double i2, long i3, boolean i4, byte i5,
                                    short i6, long[] i7, Long[] i8, Short i9, Byte i10, 
                                    Long i11, Double i12, int i13, int i14, int i15,
                                    int i16, int i17, int i18, int i19, int i20);
                                   
   public void counterWithException();

   public int getCounter();                                  
}
      



