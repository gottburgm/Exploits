
package org.jboss.test.support.jar1.simple1;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * 
 * @author Adrian Brock
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public interface ISimpleBean
{
   // Constants -----------------------------------------------------

   // Public --------------------------------------------------------

   String getConstructorUsed();

   BigDecimal getABigDecimal();

   void setABigDecimal(BigDecimal bigDecimal);

   BigInteger getABigInteger();

   void setABigInteger(BigInteger bigInteger);

   boolean isAboolean();

   void setAboolean(boolean aboolean);

   Boolean getABoolean();

   void setABoolean(Boolean boolean1);

   byte getAbyte();

   void setAbyte(byte abyte);

   Byte getAByte();

   void setAByte(Byte byte1);

   char getAchar();

   void setAchar(char achar);

   Character getACharacter();

   void setACharacter(Character character);

   Date getADate();

   void setADate(Date date);

   double getAdouble();

   void setAdouble(double adouble);

   Double getADouble();

   void setADouble(Double double1);

   float getAfloat();

   void setAfloat(float afloat);

   Float getAFloat();

   void setAFloat(Float float1);

   long getAlong();

   void setAlong(long along);

   Long getALong();

   void setALong(Long long1);

   int getAnint();

   void setAnint(int anint);

   Integer getAnInt();

   void setAnInt(Integer anInt);

   short getAshort();

   void setAshort(short ashort);

   Short getAShort();

   void setAShort(Short short1);

   String getAString();

   void setAString(String string);

   ISimpleBean getOther();

   void setOther(ISimpleBean other);

}