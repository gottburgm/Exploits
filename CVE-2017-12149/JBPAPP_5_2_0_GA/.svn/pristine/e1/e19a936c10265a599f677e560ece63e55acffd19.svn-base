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
package org.jboss.test.jmx.compliance.relation;

import java.util.ArrayList;

import javax.management.ObjectName;
import javax.management.relation.RelationNotification;
import javax.management.relation.RelationService;

import junit.framework.TestCase;

/**
 * Relation Notification Tests
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 */
public class RelationNotificationTestCase
  extends TestCase
{

  // Constants -----------------------------------------------------------------

  static String[] types = new String[]
  {
    RelationNotification.RELATION_BASIC_CREATION,
    RelationNotification.RELATION_MBEAN_CREATION,
    RelationNotification.RELATION_BASIC_UPDATE,
    RelationNotification.RELATION_MBEAN_UPDATE,
    RelationNotification.RELATION_BASIC_REMOVAL,
    RelationNotification.RELATION_MBEAN_REMOVAL
  };

  // Attributes ----------------------------------------------------------------

  // Constructor ---------------------------------------------------------------

  /**
   * Construct the test
   */
  public RelationNotificationTestCase(String s)
  {
    super(s);
  }

  // Tests ---------------------------------------------------------------------

  /**
   * Make sure all the constants are different
   */
  public void testDifferent()
  {
    for (int i = 0; i < (types.length - 1); i++)
    {
      for (int j = i + 1; j < types.length; j++)
        if (types[i].equals(types[j]))
          fail("Relation Notifications types not unique");
    }
  }

  /**
   * Test Basic Creation
   */
  public void testBasicCreation()
  {
    RelationNotification rn = null;
    try
    {
      rn = new RelationNotification(RelationNotification.RELATION_BASIC_CREATION,
             new RelationService(true), 21, 23, "message", "relationId", 
             "relationTypeName", null, null);
    }
    catch (Exception e)
    {
      fail(e.toString());
    }
    assertEquals(RelationNotification.RELATION_BASIC_CREATION, rn.getType());
    assertEquals(21, rn.getSequenceNumber());
    assertEquals(23, rn.getTimeStamp());
    assertEquals("message", rn.getMessage());
    assertEquals("relationId", rn.getRelationId());
    assertEquals("relationTypeName", rn.getRelationTypeName());
    assertEquals(null, rn.getObjectName());
    assertEquals(0, rn.getMBeansToUnregister().size());
  }

  /**
   * Test Basic Removal
   */
  public void testBasicRemoval()
  {
    RelationNotification rn = null;
    ArrayList unregs = new ArrayList();
    try
    {
      rn = new RelationNotification(RelationNotification.RELATION_BASIC_REMOVAL,
             new RelationService(true), 21, 23, "message", "relationId", 
             "relationTypeName", null, unregs);
    }
    catch (Exception e)
    {
      fail(e.toString());
    }
    assertEquals(RelationNotification.RELATION_BASIC_REMOVAL, rn.getType());
    assertEquals(21, rn.getSequenceNumber());
    assertEquals(23, rn.getTimeStamp());
    assertEquals("message", rn.getMessage());
    assertEquals("relationId", rn.getRelationId());
    assertEquals("relationTypeName", rn.getRelationTypeName());
    assertEquals(null, rn.getObjectName());
    assertEquals(unregs, rn.getMBeansToUnregister());
  }

  /**
   * Test MBean Creation
   */
  public void testMBeanCreation()
  {
    RelationNotification rn = null;
    ObjectName objectName = null;
    try
    {
      objectName = new ObjectName(":a=a");
      rn = new RelationNotification(RelationNotification.RELATION_MBEAN_CREATION,
             new RelationService(true), 21, 23, "message", "relationId", 
             "relationTypeName", objectName, null);
    }
    catch (Exception e)
    {
      fail(e.toString());
    }
    assertEquals(RelationNotification.RELATION_MBEAN_CREATION, rn.getType());
    assertEquals(21, rn.getSequenceNumber());
    assertEquals(23, rn.getTimeStamp());
    assertEquals("message", rn.getMessage());
    assertEquals("relationId", rn.getRelationId());
    assertEquals("relationTypeName", rn.getRelationTypeName());
    assertEquals(objectName, rn.getObjectName());
    assertEquals(0, rn.getMBeansToUnregister().size());
  }

  /**
   * Test MBean Removal
   */
  public void testMBeanRemoval()
  {
    RelationNotification rn = null;
    ObjectName objectName = null;
    ArrayList unregs = new ArrayList();
    try
    {
      objectName = new ObjectName(":a=a");
      rn = new RelationNotification(RelationNotification.RELATION_MBEAN_REMOVAL,
             new RelationService(true), 21, 23, "message", "relationId", 
             "relationTypeName", objectName, unregs);
    }
    catch (Exception e)
    {
      fail(e.toString());
    }
    assertEquals(RelationNotification.RELATION_MBEAN_REMOVAL, rn.getType());
    assertEquals(21, rn.getSequenceNumber());
    assertEquals(23, rn.getTimeStamp());
    assertEquals("message", rn.getMessage());
    assertEquals("relationId", rn.getRelationId());
    assertEquals("relationTypeName", rn.getRelationTypeName());
    assertEquals(objectName, rn.getObjectName());
    assertEquals(unregs, rn.getMBeansToUnregister());
  }

  /**
   * Test Basic Update
   */
  public void testBasicUpdate()
  {
    RelationNotification rn = null;
    ArrayList newRoles = new ArrayList();
    ArrayList oldRoles = new ArrayList();
    try
    {
      rn = new RelationNotification(RelationNotification.RELATION_BASIC_UPDATE,
             new RelationService(true), 21, 23, "message", "relationId", 
             "relationTypeName", null, "roleName", newRoles, oldRoles);
    }
    catch (Exception e)
    {
      fail(e.toString());
    }
    assertEquals(RelationNotification.RELATION_BASIC_UPDATE, rn.getType());
    assertEquals(21, rn.getSequenceNumber());
    assertEquals(23, rn.getTimeStamp());
    assertEquals("message", rn.getMessage());
    assertEquals("relationId", rn.getRelationId());
    assertEquals("relationTypeName", rn.getRelationTypeName());
    assertEquals(null, rn.getObjectName());
    assertEquals("roleName", rn.getRoleName());
    assertEquals(0, rn.getNewRoleValue().size());
    assertEquals(0, rn.getOldRoleValue().size());
  }

  /**
   * Test MBean Update
   */
  public void testMBeanUpdate()
  {
    RelationNotification rn = null;
    ObjectName objectName = null;
    ArrayList newRoles = new ArrayList();
    ArrayList oldRoles = new ArrayList();
    try
    {
      objectName = new ObjectName(":a=a");
      rn = new RelationNotification(RelationNotification.RELATION_MBEAN_UPDATE,
             new RelationService(true), 21, 23, "message", "relationId", 
             "relationTypeName", objectName, "roleName", newRoles, oldRoles);
    }
    catch (Exception e)
    {
      fail(e.toString());
    }
    assertEquals(RelationNotification.RELATION_MBEAN_UPDATE, rn.getType());
    assertEquals(21, rn.getSequenceNumber());
    assertEquals(23, rn.getTimeStamp());
    assertEquals("message", rn.getMessage());
    assertEquals("relationId", rn.getRelationId());
    assertEquals("relationTypeName", rn.getRelationTypeName());
    assertEquals(objectName, rn.getObjectName());
    assertEquals("roleName", rn.getRoleName());
    assertEquals(0, rn.getNewRoleValue().size());
    assertEquals(0, rn.getOldRoleValue().size());
  }

  /**
   * Test Creation/Removal Error Handling
   */
  public void testCreationRemovalErrors()
  {
    ObjectName objectName = null;
    try
    {
      objectName = new ObjectName(":a=a");
    }
    catch (Exception e)
    {
      fail(e.toString());
    }
    ArrayList unregs = new ArrayList();

    boolean caught = false;
    try
    {
      new RelationNotification("blah",
             new RelationService(true), 21, 23, "message", "relationId", 
             "relationTypeName", objectName, unregs);
    }
    catch (IllegalArgumentException e)
    {
      caught = true;
    }
    catch (Exception e)
    {
      fail(e.toString());
    }
    if (caught == false)
      fail("Creation/Removal accepts an invalid type");

    caught = false;
    try
    {
      new RelationNotification(RelationNotification.RELATION_BASIC_UPDATE,
             new RelationService(true), 21, 23, "message", "relationId", 
             "relationTypeName", objectName, unregs);
    }
    catch (IllegalArgumentException e)
    {
      caught = true;
    }
    catch (Exception e)
    {
      fail(e.toString());
    }
    if (caught == false)
      fail("Creation/Removal accepts basic update");

    caught = false;
    try
    {
      new RelationNotification(RelationNotification.RELATION_MBEAN_UPDATE,
             new RelationService(true), 21, 23, "message", "relationId", 
             "relationTypeName", objectName, unregs);
    }
    catch (IllegalArgumentException e)
    {
      caught = true;
    }
    catch (Exception e)
    {
      fail(e.toString());
    }
    if (caught == false)
      fail("Creation/Removal accepts mean update");

    caught = false;
    try
    {
      new RelationNotification(RelationNotification.RELATION_BASIC_CREATION,
             null, 21, 23, "message", "relationId", 
             "relationTypeName", objectName, unregs);
    }
    catch (IllegalArgumentException e)
    {
      caught = true;
    }
    catch (Exception e)
    {
      fail(e.toString());
    }
    if (caught == false)
      fail("Creation/Removal accepts null source");

    caught = false;
    try
    {
      new RelationNotification(RelationNotification.RELATION_BASIC_CREATION,
             new RelationService(true), 21, 23, "message", null, 
             "relationTypeName", objectName, unregs);
    }
    catch (IllegalArgumentException e)
    {
      caught = true;
    }
    catch (Exception e)
    {
      fail(e.toString());
    }
    if (caught == false)
      fail("Creation/Removal accepts null relation id");

    caught = false;
    try
    {
      new RelationNotification(RelationNotification.RELATION_BASIC_CREATION,
             new RelationService(true), 21, 23, "message", "relation id", 
             null, objectName, unregs);
    }
    catch (IllegalArgumentException e)
    {
      caught = true;
    }
    catch (Exception e)
    {
      fail(e.toString());
    }
    if (caught == false)
      fail("Creation/Removal accepts null relation type name");
  }

  /**
   * Test Creation/Removal Error Handling
   */
  public void testCreationRemovalErrors2()
  {
    ObjectName objectName = null;
    try
    {
      objectName = new ObjectName(":a=a");
    }
    catch (Exception e)
    {
      fail(e.toString());
    }
    ArrayList unregs = new ArrayList();

    boolean caught = false;
    try
    {
      new RelationNotification(null,
             new RelationService(true), 21, 23, "message", "relationId", 
             "relationTypeName", objectName, unregs);
    }
    catch (NullPointerException e)
    {
      fail("FAILS IN RI: Throws the wrong exception type");
    }
    catch (IllegalArgumentException e)
    {
      caught = true;
    }
    catch (Exception e)
    {
      fail(e.toString());
    }
    if (caught == false)
      fail("Creation/Removal accepts an a null type");
  }

  /**
   * Test Update Error Handling
   */
  public void testUpdateErrors()
  {
    ObjectName objectName = null;
    try
    {
      objectName = new ObjectName(":a=a");
    }
    catch (Exception e)
    {
      fail(e.toString());
    }
    ArrayList newRoles = new ArrayList();
    ArrayList oldRoles = new ArrayList();

    boolean caught = false;
    try
    {
      new RelationNotification("blah",
             new RelationService(true), 21, 23, "message", "relationId", 
             "relationTypeName", objectName, "roleInfo", newRoles, oldRoles);
    }
    catch (IllegalArgumentException e)
    {
      caught = true;
    }
    catch (Exception e)
    {
      fail(e.toString());
    }
    if (caught == false)
      fail("Update accepts an invalid type");

    caught = false;
    try
    {
      new RelationNotification(RelationNotification.RELATION_BASIC_CREATION,
             new RelationService(true), 21, 23, "message", "relationId", 
             "relationTypeName", objectName, "roleInfo", newRoles, oldRoles);
    }
    catch (IllegalArgumentException e)
    {
      caught = true;
    }
    catch (Exception e)
    {
      fail(e.toString());
    }
    if (caught == false)
      fail("Update accepts basic create");

    caught = false;
    try
    {
      new RelationNotification(RelationNotification.RELATION_MBEAN_CREATION,
             new RelationService(true), 21, 23, "message", "relationId", 
             "relationTypeName", objectName, "roleInfo", newRoles, oldRoles);
    }
    catch (IllegalArgumentException e)
    {
      caught = true;
    }
    catch (Exception e)
    {
      fail(e.toString());
    }
    if (caught == false)
      fail("Creation/Removal accepts mean create");

    caught = false;
    try
    {
      new RelationNotification(RelationNotification.RELATION_BASIC_REMOVAL,
             new RelationService(true), 21, 23, "message", "relationId", 
             "relationTypeName", objectName, "roleInfo", newRoles, oldRoles);
    }
    catch (IllegalArgumentException e)
    {
      caught = true;
    }
    catch (Exception e)
    {
      fail(e.toString());
    }
    if (caught == false)
      fail("Update accepts basic remove");

    caught = false;
    try
    {
      new RelationNotification(RelationNotification.RELATION_MBEAN_REMOVAL,
             new RelationService(true), 21, 23, "message", "relationId", 
             "relationTypeName", objectName, "roleInfo", newRoles, oldRoles);
    }
    catch (IllegalArgumentException e)
    {
      caught = true;
    }
    catch (Exception e)
    {
      fail(e.toString());
    }
    if (caught == false)
      fail("Update accepts mean remove");

    caught = false;
    try
    {
      new RelationNotification(RelationNotification.RELATION_BASIC_UPDATE,
             null, 21, 23, "message", "relationId", 
             "relationTypeName", objectName, "roleInfo", newRoles, oldRoles);
    }
    catch (IllegalArgumentException e)
    {
      caught = true;
    }
    catch (Exception e)
    {
      fail(e.toString());
    }
    if (caught == false)
      fail("Update accepts null source");

    caught = false;
    try
    {
      new RelationNotification(RelationNotification.RELATION_BASIC_UPDATE,
             new RelationService(true), 21, 23, "message", null, 
             "relationTypeName", objectName, "roleInfo", newRoles, oldRoles);
    }
    catch (IllegalArgumentException e)
    {
      caught = true;
    }
    catch (Exception e)
    {
      fail(e.toString());
    }
    if (caught == false)
      fail("Update accepts null relation id");

    caught = false;
    try
    {
      new RelationNotification(RelationNotification.RELATION_BASIC_UPDATE,
             new RelationService(true), 21, 23, "message", "relation id", 
             null, objectName, "roleInfo", newRoles, oldRoles);
    }
    catch (IllegalArgumentException e)
    {
      caught = true;
    }
    catch (Exception e)
    {
      fail(e.toString());
    }
    if (caught == false)
      fail("Update accepts null relation type name");

    caught = false;
    try
    {
      new RelationNotification(RelationNotification.RELATION_BASIC_UPDATE,
             new RelationService(true), 21, 23, "message", "relation id", 
             null, objectName, null, newRoles, oldRoles);
    }
    catch (IllegalArgumentException e)
    {
      caught = true;
    }
    catch (Exception e)
    {
      fail(e.toString());
    }
    if (caught == false)
      fail("Update accepts null role info");

    caught = false;
    try
    {
      new RelationNotification(RelationNotification.RELATION_BASIC_UPDATE,
             new RelationService(true), 21, 23, "message", "relation id", 
             "relationTypeName", objectName, "roleInfo", null, oldRoles);
    }
    catch (IllegalArgumentException e)
    {
      caught = true;
    }
    catch (Exception e)
    {
      fail(e.toString());
    }
    if (caught == false)
      fail("Creation/Removal accepts null new role value");

    caught = false;
    try
    {
      new RelationNotification(RelationNotification.RELATION_BASIC_UPDATE,
             new RelationService(true), 21, 23, "message", "relation id", 
             "relationTypeName", objectName, "roleInfo", newRoles, null);
    }
    catch (IllegalArgumentException e)
    {
      caught = true;
    }
    catch (Exception e)
    {
      fail(e.toString());
    }
    if (caught == false)
      fail("Update accepts null old role value");
  }

  /**
   * Test Update Error Handling
   */
  public void testUpdateErrors2()
  {
    ObjectName objectName = null;
    try
    {
      objectName = new ObjectName(":a=a");
    }
    catch (Exception e)
    {
      fail(e.toString());
    }
    ArrayList newRoles = new ArrayList();
    ArrayList oldRoles = new ArrayList();

    boolean caught = false;
    try
    {
      new RelationNotification(null,
             new RelationService(true), 21, 23, "message", "relationId", 
             "relationTypeName", objectName, "roleInfo", newRoles, oldRoles);
    }
    catch (NullPointerException e)
    {
      fail("FAILS IN RI: Throws the wrong exception type");
    }
    catch (IllegalArgumentException e)
    {
      caught = true;
    }
    catch (Exception e)
    {
      fail(e.toString());
    }
    if (caught == false)
      fail("Update accepts an a null type");
  }

  /**
   * Test serialization.
   */
/*  public void testSerializationBasicCreation()
  {
    RelationNotification orig = null;
    RelationNotification rn = null;
    try
    {
      orig = new RelationNotification(RelationNotification.RELATION_BASIC_CREATION,
             new RelationService(true), 21, 23, "message", "relationId", 
             "relationTypeName", null, null);

      // Serialize it
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(orig);
    
      // Deserialize it
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      ObjectInputStream ois = new ObjectInputStream(bais);
      rn = (RelationNotification) ois.readObject();
    }
    catch (Exception e)
    {
      fail(e.toString());
    }
    assertEquals(RelationNotification.RELATION_BASIC_CREATION, rn.getType());
    assertEquals(21, rn.getSequenceNumber());
    assertEquals(23, rn.getTimeStamp());
    assertEquals("message", rn.getMessage());
    assertEquals("relationId", rn.getRelationId());
    assertEquals("relationTypeName", rn.getRelationTypeName());
    assertEquals(null, rn.getObjectName());
    assertEquals(0, rn.getMBeansToUnregister().size());
  }
*/
  /**
   * Test serialization.
   */
/*  public void testSerializationBasicRemoval()
  {
    RelationNotification orig = null;
    RelationNotification rn = null;
    ObjectName objectName = null;
    ArrayList unregs = new ArrayList();
    try
    {
      orig = new RelationNotification(RelationNotification.RELATION_BASIC_REMOVAL,
             new RelationService(true), 21, 23, "message", "relationId", 
             "relationTypeName", null, unregs);

      // Serialize it
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(orig);
    
      // Deserialize it
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      ObjectInputStream ois = new ObjectInputStream(bais);
      rn = (RelationNotification) ois.readObject();
    }
    catch (Exception e)
    {
      fail(e.toString());
    }
    assertEquals(RelationNotification.RELATION_BASIC_REMOVAL, rn.getType());
    assertEquals(21, rn.getSequenceNumber());
    assertEquals(23, rn.getTimeStamp());
    assertEquals("message", rn.getMessage());
    assertEquals("relationId", rn.getRelationId());
    assertEquals("relationTypeName", rn.getRelationTypeName());
    assertEquals(null, rn.getObjectName());
    assertEquals(unregs, rn.getMBeansToUnregister());
  }
*/

  /**
   * Test serialization.
   */
/*  public void testSerializationMBeanCreation()
  {
    RelationNotification orig = null;
    RelationNotification rn = null;
    ObjectName objectName = null;
    try
    {
      objectName = new ObjectName(":a=a");
      orig = new RelationNotification(RelationNotification.RELATION_MBEAN_CREATION,
             new RelationService(true), 21, 23, "message", "relationId", 
             "relationTypeName", objectName, null);

      // Serialize it
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(orig);
    
      // Deserialize it
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      ObjectInputStream ois = new ObjectInputStream(bais);
      rn = (RelationNotification) ois.readObject();
    }
    catch (Exception e)
    {
      fail(e.toString());
    }
    assertEquals(RelationNotification.RELATION_MBEAN_CREATION, rn.getType());
    assertEquals(21, rn.getSequenceNumber());
    assertEquals(23, rn.getTimeStamp());
    assertEquals("message", rn.getMessage());
    assertEquals("relationId", rn.getRelationId());
    assertEquals("relationTypeName", rn.getRelationTypeName());
    assertEquals(objectName, rn.getObjectName());
    assertEquals(0, rn.getMBeansToUnregister().size());
  }
*/

  /**
   * Test serialization.
   */
/*  public void testSerializationMBeanRemoval()
  {
    RelationNotification orig = null;
    RelationNotification rn = null;
    ObjectName objectName = null;
    ArrayList unregs = new ArrayList();
    try
    {
      objectName = new ObjectName(":a=a");
      orig = new RelationNotification(RelationNotification.RELATION_MBEAN_REMOVAL,
             new RelationService(true), 21, 23, "message", "relationId", 
             "relationTypeName", objectName, unregs);

      // Serialize it
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(orig);
    
      // Deserialize it
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      ObjectInputStream ois = new ObjectInputStream(bais);
      rn = (RelationNotification) ois.readObject();
    }
    catch (Exception e)
    {
      fail(e.toString());
    }
    assertEquals(RelationNotification.RELATION_MBEAN_REMOVAL, rn.getType());
    assertEquals(21, rn.getSequenceNumber());
    assertEquals(23, rn.getTimeStamp());
    assertEquals("message", rn.getMessage());
    assertEquals("relationId", rn.getRelationId());
    assertEquals("relationTypeName", rn.getRelationTypeName());
    assertEquals(objectName, rn.getObjectName());
    assertEquals(unregs, rn.getMBeansToUnregister());
  }
*/

  /**
   * Test serialization.
   */
/*  public void testSerializationBasicUpdate()
  {
    RelationNotification orig = null;
    RelationNotification rn = null;
    ArrayList newRoles = new ArrayList();
    ArrayList oldRoles = new ArrayList();
    try
    {
      orig = new RelationNotification(RelationNotification.RELATION_BASIC_UPDATE,
             new RelationService(true), 21, 23, "message", "relationId", 
             "relationTypeName", null, "roleName", newRoles, oldRoles);

      // Serialize it
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(orig);
    
      // Deserialize it
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      ObjectInputStream ois = new ObjectInputStream(bais);
      rn = (RelationNotification) ois.readObject();
    }
    catch (Exception e)
    {
      fail(e.toString());
    }
    assertEquals(RelationNotification.RELATION_BASIC_UPDATE, rn.getType());
    assertEquals(21, rn.getSequenceNumber());
    assertEquals(23, rn.getTimeStamp());
    assertEquals("message", rn.getMessage());
    assertEquals("relationId", rn.getRelationId());
    assertEquals("relationTypeName", rn.getRelationTypeName());
    assertEquals(null, rn.getObjectName());
    assertEquals("roleName", rn.getRoleName());
    assertEquals(0, rn.getNewRoleValue().size());
    assertEquals(0, rn.getOldRoleValue().size());
  }
*/

  /**
   * Test serialization.
   */
/*  public void testSerializationMBeanUpdate()
  {
    RelationNotification orig = null;
    RelationNotification rn = null;
    ObjectName objectName = null;
    ArrayList newRoles = new ArrayList();
    ArrayList oldRoles = new ArrayList();
    try
    {
      objectName = new ObjectName(":a=a");
      orig = new RelationNotification(RelationNotification.RELATION_MBEAN_UPDATE,
             new RelationService(true), 21, 23, "message", "relationId", 
             "relationTypeName", objectName, "roleName", newRoles, oldRoles);

      // Serialize it
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(orig);
    
      // Deserialize it
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      ObjectInputStream ois = new ObjectInputStream(bais);
      rn = (RelationNotification) ois.readObject();
    }
    catch (Exception e)
    {
      fail(e.toString());
    }
    assertEquals(RelationNotification.RELATION_MBEAN_UPDATE, rn.getType());
    assertEquals(21, rn.getSequenceNumber());
    assertEquals(23, rn.getTimeStamp());
    assertEquals("message", rn.getMessage());
    assertEquals("relationId", rn.getRelationId());
    assertEquals("relationTypeName", rn.getRelationTypeName());
    assertEquals(objectName, rn.getObjectName());
    assertEquals("roleName", rn.getRoleName());
    assertEquals(0, rn.getNewRoleValue().size());
    assertEquals(0, rn.getOldRoleValue().size());
  }
*/
}
