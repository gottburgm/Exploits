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
package test.asynchronous;
import junit.framework.TestCase;
import org.jboss.aspects.asynchronous.aspects.AsynchronousFacade;
import org.jboss.aspects.asynchronous.aspects.AsynchronousFacadeImpl;
import org.jboss.aspects.asynchronous.AsynchronousResponse;
import org.jboss.aspects.asynchronous.AsynchronousConstants;
import org.jboss.aspects.asynchronous.AsynchronousTask;
import org.jboss.aspects.asynchronous.ThreadManagerResponse;
import org.jboss.aspects.asynchronous.concurrent.ThreadManagerFactory;
import org.jboss.aspects.asynchronous.ThreadManager;
/**
 *
 * @version <tt>$Revision: 80997 $</tt>
 * @author  <a href="mailto:chussenet@yahoo.com">{Claude Hussenet Independent Consultant}</a>.
 */
public class JUnitTestAsynchronousAspects
	extends TestCase
	implements AsynchronousConstants {
	public JUnitTestAsynchronousAspects(String arg0) {
		super(arg0);
		BusinessModel bm1 = new BusinessModel();

		bm1.processBusinessModel();
		AsynchronousTask aT = ((AsynchronousFacade)bm1).getAsynchronousTask();
		ThreadManagerResponse response = aT.getResponse();

	}

	public static void testAsynchronousCall() {
		BusinessModel bm1 = new BusinessModel(200);
		long t0 = System.currentTimeMillis();
		bm1.processBusinessModel();
		long t1 = System.currentTimeMillis();
		assertTrue(
			"Not an asynchronous call:" + (t1 - t0) + " ms.",
			(t1 - t0) < 100);
	}

	public static void testMixinAsynchronousFacadeInterface() {
		BusinessModel bm1 = new BusinessModel();
		assertTrue(
			"not an instance of AsynchronousFacade",
			bm1 instanceof AsynchronousFacade);
	}
	public static void testMixinAsynchronousFacadeInterface2() {
		BusinessModel bm1 = new BusinessModel();
		BusinessModel bm2 = new BusinessModel();
		assertTrue(
			"bm1 is not an instance of AsynchronousFacade",
			bm1 instanceof AsynchronousFacade);
		assertTrue(
			"bm2 is not an instance of AsynchronousFacade",
			bm2 instanceof AsynchronousFacade);
		AsynchronousFacade asynchronousFacade1 = (AsynchronousFacade)bm1;
		AsynchronousFacade asynchronousFacade2 = (AsynchronousFacade)bm2;
		asynchronousFacade1.setId("OK");
		asynchronousFacade2.setId("KO");
		assertNotSame(
			"same instances(ID) of AsynchronousFacade",
			asynchronousFacade1.getId(),
			asynchronousFacade2.getId());
	}
	public static void test2AsynchronousCallOnSameInstance() {
		BusinessModel bm1 = new BusinessModel();
		long res1 = bm1.processBusinessModel2(15);
		AsynchronousFacade asynchronousFacade = (AsynchronousFacade)bm1;
		AsynchronousTask asynchronousTask1 =
			asynchronousFacade.getAsynchronousTask();
		long res2 = bm1.processBusinessModel2(10);
		AsynchronousTask asynchronousTask2 =
			asynchronousFacade.getAsynchronousTask();
		assertEquals(
			"Method is not succesfull !",
			OK,
			asynchronousFacade.getResponseCode(asynchronousTask1));
		assertTrue(
			"value returned is not an instance of Long",
			asynchronousFacade.getReturnValue(asynchronousTask1)
				instanceof Long);
		assertEquals(
			"Method does not return the right value !"
				+ ((Long)asynchronousFacade.getReturnValue(asynchronousTask1))
					.longValue(),
			((Long)asynchronousFacade.getReturnValue(asynchronousTask1))
				.longValue(),
			15);
		assertEquals(
			"Method is not succesfull !",
			asynchronousFacade.getResponseCode(asynchronousTask2),
			OK);
		assertTrue(
			"value returned is not an instance of Long",
			asynchronousFacade.getReturnValue(asynchronousTask2)
				instanceof Long);
		assertEquals(
			"Method does not return the right value !"
				+ ((Long)asynchronousFacade.getReturnValue(asynchronousTask2))
					.longValue(),
			((Long)asynchronousFacade.getReturnValue(asynchronousTask2))
				.longValue(),
			10);
	}
	public static void test2AsynchronousCallOnSameInstance2() {
		BusinessModel bm1 = new BusinessModel();
		AsynchronousFacade asynchronousFacade = (AsynchronousFacade)bm1;
		asynchronousFacade.setTimeout(50);
		long res1 = bm1.processBusinessModel2(1000);
		AsynchronousTask asynchronousTask1 =
			asynchronousFacade.getAsynchronousTask();
		asynchronousFacade.setTimeout(200);
		long res2 = bm1.processBusinessModel2(10);
		AsynchronousTask asynchronousTask2 =
			asynchronousFacade.getAsynchronousTask();
		assertEquals(
			"Method did not timeout !",
			TIMEOUT,
			asynchronousFacade.getResponseCode(asynchronousTask1));
		assertEquals(
			"Method is not succesfull !",
			asynchronousFacade.getResponseCode(asynchronousTask2),
			OK);
		assertTrue(
			"value returned is not an instance of Long",
			asynchronousFacade.getReturnValue(asynchronousTask2)
				instanceof Long);
		assertEquals(
			"Method does not return the right value !"
				+ ((Long)asynchronousFacade.getReturnValue(asynchronousTask2))
					.longValue(),
			((Long)asynchronousFacade.getReturnValue(asynchronousTask2))
				.longValue(),
			10);
	}

	public static void testTimeout() {
		BusinessModel bm1 = new BusinessModel(200);
		AsynchronousFacade asynchronousFacade = (AsynchronousFacade)bm1;
		asynchronousFacade.setTimeout(100);
		bm1.processBusinessModel();
		long t0 = System.currentTimeMillis();
		//System.out.println(
		//	asynchronousFacade.getAsynchronousTask().getResponse());
		assertEquals(
			"Method did not timeout !",
			TIMEOUT,
			asynchronousFacade.getResponseCode());
		long t1 = System.currentTimeMillis();
		assertTrue("Method time out in " + (t1 - t0) + " ms.", (t1 - t0) < 120);
		assertTrue("Method did not run " + (t1 - t0) + " ms.", (t1 - t0) > 80);
	}
	public static void testReturnValue() {
		BusinessModel bm1 = new BusinessModel();
		long value = bm1.processBusinessModel2(10);
		AsynchronousFacade asynchronousFacade = (AsynchronousFacade)bm1;
		assertEquals(
			"Method is not succesfull !",
			OK,
			asynchronousFacade.getResponseCode());
		assertTrue(
			"value returned is not an instance of Long",
			asynchronousFacade.getReturnValue() instanceof Long);
		assertEquals(
			"Method does not return the right value !"
				+ ((Long)asynchronousFacade.getReturnValue()).longValue(),
			((Long)asynchronousFacade.getReturnValue()).longValue(),
			10);
	}
	public static void testCleanupCallWhenTimeout() {
		BusinessModelWithCleanup bm1 = new BusinessModelWithCleanup();
		BusinessModelWithCleanup bm2 = new BusinessModelWithCleanup();
		AsynchronousFacade asynchronousFacade1 = (AsynchronousFacade)bm1;
		AsynchronousFacade asynchronousFacade2 = (AsynchronousFacade)bm2;
		asynchronousFacade1.setTimeout(100);
		long value1 = bm1.processBusinessModel2(200);
		long value2 = bm2.processBusinessModel2(200);
		assertEquals(
			"Method did not timeout !",
			TIMEOUT,
			asynchronousFacade1.getResponseCode());
		assertEquals(
			"Method is not successfull !",
			OK,
			asynchronousFacade2.getResponseCode());
		assertEquals("Cleanup method not called", true, bm1.bCleanupCalled);
		assertEquals("Cleanup method called", false, bm2.bCleanupCalled);
	}
	public static void testIsDone() {
		BusinessModel bm1 = new BusinessModel(200);
		bm1.processBusinessModel();
		AsynchronousFacade asynchronousFacade = (AsynchronousFacade)bm1;
		long t0 = System.currentTimeMillis();
		assertFalse("isDone returns TRUE  !", asynchronousFacade.isDone());
		long t1 = System.currentTimeMillis();
		assertTrue(
			"isDone is a blocking call " + (t1 - t0) + " ms.",
			(t1 - t0) < 20);
		assertEquals(
			"Method is not succesfull !",
			OK,
			asynchronousFacade.getResponseCode());
		assertTrue("isDone returns FALSE  !", asynchronousFacade.isDone());
	}
	public static void testExceptionRaisedInMethodCall() {
		BusinessModel bm1 = new BusinessModel();
		long value = bm1.processBusinessModel2(-1);
		AsynchronousFacade asynchronousFacade = (AsynchronousFacade)bm1;
		assertEquals(
			"EXCEPTIONCAUGHT error not returned !",
			EXCEPTIONCAUGHT,
			asynchronousFacade.getResponseCode());
	}

	public static void testPoolSizeFull() {
		ThreadManagerFactory.getThreadManager().setMaximumPoolSize(10);
		for (int i = 0; i < 10; i++) {
			BusinessModel bm1 = new BusinessModel(200);
			bm1.processBusinessModel();

		}
		BusinessModel bm1 = new BusinessModel(200);
		bm1.processBusinessModel();
		AsynchronousFacade asynchronousFacade = (AsynchronousFacade)bm1;
		assertEquals(
			"Pool size not full !",
			CAN_NOT_PROCESS,
			asynchronousFacade.getResponseCode());
	}

	public static void testPerformance() {
		ThreadManagerFactory.getThreadManager().setMaximumPoolSize(5000);
		long tt0 = System.currentTimeMillis();
		float nbt = 100000;
		for (int i = 0; i < nbt; i++) {
			BusinessModel bm = new BusinessModel(200);
		}
		long tt1 = System.currentTimeMillis();
		float time = (tt1 - tt0) / nbt;
		System.out.println(
			(int)nbt
				+ " advised instances created in "
				+ (tt1 - tt0)
				+ " (ms).Average time "
				+ time
				+ " (ms).");
		BusinessModel bm = new BusinessModel(10);
		AsynchronousFacade Fbm = (AsynchronousFacade)bm;
		long total = 0;
		int iOk = 0;
		int nb1 = 200;
		int nb2 = 5;
		AsynchronousTask[] Tbm = new AsynchronousTask[nb2];
		for (int j = 0; j < nb1; j++) {
			long t0 = System.currentTimeMillis();
			for (int i = 0; i < nb2; i++) {
				bm.processBusinessModel();
				Tbm[i] = Fbm.getAsynchronousTask();
			}
			long t1 = System.currentTimeMillis();
			total += (t1 - t0);
			for (int i = 0; i < nb2; i++) {
				int ok = Fbm.getResponseCode(Tbm[i]);
				if (ok == OK)
					iOk++;
			}
		}
		System.out.println(
			nb1 * nb2
				+ " asynchronous method invocations in "
				+ total
				+ " (ms).Average time "
				+ (total / (float) (nb1 * nb2))
				+ " (ms).");
		assertEquals("Some errors:", nb1 * nb2, iOk);
	}
	public static void testResponseTimeReturned() {
		BusinessModel bm1 = new BusinessModel(200);
		int ERROR = 20;
		long t0 = System.currentTimeMillis();
		bm1.processBusinessModel();
		AsynchronousFacade asynchronousFacade1 = (AsynchronousFacade)bm1;
		assertEquals(
			"Method is not succesfull !",
			OK,
			asynchronousFacade1.getResponseCode());
		long t1 = System.currentTimeMillis();
		long startingTime =
			asynchronousFacade1.getThreadManagerResponse().getStartingTime();
		long endingTime =
			asynchronousFacade1.getThreadManagerResponse().getEndingTime();
		assertTrue(
			"starting time issue ? " + (startingTime - t0),
			(startingTime - t0) < ERROR);
		assertTrue(
			"ending time issue ? " + (t1 - endingTime),
			(t1 - endingTime) < ERROR);
	}
	public static void testAsynchronousCallOnPrivateMethod() {
		BusinessModel bm1 = new BusinessModel(200);
		long t0 = System.currentTimeMillis();
		bm1.callPrivateMethod();
		long t1 = System.currentTimeMillis();
		assertTrue(
			"private method not an asynchronous call:" + (t1 - t0) + " ms.",
			(t1 - t0) < 100);
	}
	public static void test2AsynchronousCallsOnSameInstanceFrom2DifferentThreads() {
		BusinessModel bm1 = new BusinessModel(100);
		BusinessThread businessThread1 =
			new BusinessThread(bm1, 100, 100, "First Call");
		BusinessThread businessThread2 =
			new BusinessThread(bm1, 200, 200, "Second Call");
		Thread th1 = new Thread(businessThread1);
		th1.start();
		Thread th2 = new Thread(businessThread2);
		th2.start();
		BusinessModel.sleep(500);
		assertNotSame(
			"The result from 2 differents threads are the same !",
			businessThread1.result,
			businessThread2.result);
	}
	public static void testAsynchronousCallOnStaticMethod() {
		Parameter object = new Parameter();
		long t0 = System.currentTimeMillis();
		BusinessModel.processBusinessModel4(200, object);
		long t1 = System.currentTimeMillis();
		assertTrue(
			"not an instance of AsynchronousFacade",
			object instanceof AsynchronousFacade);
		assertTrue(
			"static method not an asynchronous call:" + (t1 - t0) + " ms.",
			(t1 - t0) < 100);
		AsynchronousFacade asynchronousFacade = (AsynchronousFacade)object;
		AsynchronousTask asynchronousTask1 =
			asynchronousFacade.getAsynchronousTask();
		assertEquals(
			"Method does not return the right value !",
			200,
			((Long)asynchronousFacade.getReturnValue(asynchronousTask1))
				.longValue());
	}
	public static void testAsynchronousCallOnStaticMethodWithTimeout() {
		int nb = 1;
		for (int i = 0; i < nb; i++) {
			 	AsynchronousCallOnStaticMethodWithTimeout();
		}
	}
	public static void AsynchronousCallOnStaticMethodWithTimeout() {
		Parameter object1 = new Parameter();
		Parameter object2 = new Parameter();
		Parameter object3 = new Parameter();
		Parameter object4 = new Parameter();
		assertTrue(
			"not an instance of AsynchronousFacade",
			object1 instanceof AsynchronousFacade
				|| object2 instanceof AsynchronousFacade);
		((AsynchronousFacade)object1).setTimeout(100);
		((AsynchronousFacade)object3).setTimeout(100);
		((AsynchronousFacade)object4).setTimeout(100);
		long t0 = System.currentTimeMillis();
		BusinessModel.processBusinessModel4(200, object1);
		BusinessModel.processBusinessModel4(150, object2);
		BusinessModelWithStaticCleanup.processBusinessModel4(200, object3);
		BusinessModelWithStaticCleanupWithParameters.processBusinessModel4(
			200,
			object4);
		System.out.println(System.currentTimeMillis()-t0);
		long t1 = System.currentTimeMillis();
		assertTrue(
			"static method not an asynchronous call:" + (t1 - t0) + " ms.",
			(t1 - t0) < 160);
		AsynchronousFacade asynchronousFacade1 = (AsynchronousFacade)object1;
		assertEquals(
			"Method did not timeout !",
			TIMEOUT,
			asynchronousFacade1.getResponseCode());
		AsynchronousFacade asynchronousFacade = (AsynchronousFacade)object2;
		AsynchronousTask asynchronousTask2 =
			asynchronousFacade.getAsynchronousTask();
		assertEquals(
			"Method does not return the right value !",
			150,
			((Long)asynchronousFacade.getReturnValue(asynchronousTask2))
				.longValue());
	}
}
