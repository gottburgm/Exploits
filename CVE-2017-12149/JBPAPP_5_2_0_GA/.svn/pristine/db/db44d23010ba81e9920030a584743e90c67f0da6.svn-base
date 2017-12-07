/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.test.ejb3.jbpapp8035;

import org.jboss.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sample Timer sample bean used by the unit test.
 * @author nicolasleroux
 */
@Stateless
public class TimerSampleBean implements TimerSample {
    @Resource
    private SessionContext ctx;

    private static AtomicInteger count = new AtomicInteger(0);

    public static final Logger log = Logger
            .getLogger(TimerSampleBean.class);

    public int getCount() {
        return count.intValue();
    }

    public void resetCount() {
        count.set(0);
    }

    public void scheduleTimer(long milliseconds) {
        log.info("scheduleTimer with initial duration " + milliseconds);
        ctx.getTimerService().createTimer(
                milliseconds, "Hello World");
    }

    public void scheduleTimer(long milliseconds, long period) {
        log.info("scheduleTimer with initial duration " + milliseconds + " and period " + period);

        ctx.getTimerService().createTimer(
                milliseconds, period, "Hello World repeat");
    }

    public void scheduleTimer(Date date) {
        log.info("scheduleTimer with date " + date);

        ctx.getTimerService().createTimer(
                date, "Hello World ");
    }

    public void scheduleTimerTxTimeout(long milliseconds) {
        log.info("scheduleTimer with initial duration " + milliseconds);
        ctx.getTimerService().createTimer(
                milliseconds, "Hello World ");
        try {
            Thread.sleep(300200);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void cancelAllTimers() {
        // Cancel the timers
        for (Object t : ctx.getTimerService().getTimers()) {
            ((Timer)t).cancel();
        }
    }

    @Timeout
    public void timeoutHandler(Timer timer) {
        log.info("---------------------");
        log.info("* Received Timer event: " + timer.getInfo() + " in " + Thread.currentThread().getName());
        log.info("---------------------");
        sleep(100);
        count.incrementAndGet();
    }

    public void scheduleTimers(long number, long milliseconds) {
        for (int i = 0; i < number; i++) {
            log.info("scheduleTimer with initial duration " + milliseconds);
            ctx.getTimerService().createTimer(
                    milliseconds, "Hello World");
        }
    }

    private static void sleep(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // ignore
        }
    }
}
