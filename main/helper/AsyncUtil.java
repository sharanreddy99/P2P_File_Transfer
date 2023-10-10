package main.helper;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: Endstart
 * Date: 2020-11-30
 * Time: 21:35
 */
public class AsyncUtil {

    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    /**
     * add task run at fixed rate
     * 
     * @param task
     * @param intervalDelay
     * @return
     */
    public static ScheduledFuture<?> submit(Runnable task, int intervalDelay) {
        return scheduler.scheduleAtFixedRate(task, 10, intervalDelay, TimeUnit.SECONDS);
    }

    /**
     * add task run at fixed rate
     * 
     * @param task
     * @param intervalDelay
     * @return
     */
    public static ScheduledFuture<?> submit(Runnable task, int startDelay, int intervalDelay) {
        return scheduler.scheduleAtFixedRate(task, startDelay, intervalDelay, TimeUnit.SECONDS);
    }

}
