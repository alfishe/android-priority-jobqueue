package co.alfabits.android.jobqueue.test.jobqueue;

import co.alfabits.android.jobqueue.JobHolder;
import co.alfabits.android.jobqueue.JobManager;
import co.alfabits.android.jobqueue.JobQueue;
import co.alfabits.android.jobqueue.Params;
import co.alfabits.android.jobqueue.nonPersistentQueue.NonPersistentPriorityQueue;
import co.alfabits.android.jobqueue.test.util.JobQueueFactory;


import static org.hamcrest.CoreMatchers.*;
import org.hamcrest.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;

@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class NonPersistentJobQueueTest extends JobQueueTestBase {
    public NonPersistentJobQueueTest() {
        super(new JobQueueFactory() {
            @Override
            public JobQueue createNew(long sessionId, String id) {
                return new NonPersistentPriorityQueue(sessionId, id);
            }
        });
    }

    /**
     * issue #21 https://github.com/path/android-priority-jobqueue/issues/21
     */
    @Test
    public void testTooManyQueueChanges() throws InterruptedException {
        JobQueue jobQueue = createNewJobQueue();
        int limit = 10000;
        long delayMs = 2000;
        long then = System.nanoTime() + delayMs * JobManager.NS_PER_MS;
        for(int i = 0; i < limit; i++) {
            jobQueue.insert(createNewJobHolder(new Params(0).requireNetwork().delayInMs(delayMs)));
        }

        MatcherAssert.assertThat("all jobs require network, should return null", jobQueue.nextJobAndIncRunCount(false, null), nullValue());
        long sleep = then - System.nanoTime();
        sleep += JobManager.NS_PER_MS * 1000;
        if (sleep > 0) {
            Thread.sleep(sleep / JobManager.NS_PER_MS);
        }
        //should be able to get it w/o an overflow
        for(int i = 0; i < limit; i++) {
            JobHolder holder = jobQueue.nextJobAndIncRunCount(true, null);
            MatcherAssert.assertThat("should get a next job", holder, notNullValue());
            jobQueue.remove(holder);
        }

    }
}
