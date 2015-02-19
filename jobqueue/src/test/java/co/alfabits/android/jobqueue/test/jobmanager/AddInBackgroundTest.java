package co.alfabits.android.jobqueue.test.jobmanager;

import co.alfabits.android.jobqueue.AsyncAddCallback;
import co.alfabits.android.jobqueue.BaseJob;
import co.alfabits.android.jobqueue.Job;
import co.alfabits.android.jobqueue.JobHolder;
import co.alfabits.android.jobqueue.JobManager;
import co.alfabits.android.jobqueue.JobQueue;
import co.alfabits.android.jobqueue.Params;
import co.alfabits.android.jobqueue.test.jobs.DummyJob;
import org.fest.reflect.core.*;
import org.hamcrest.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

@RunWith(RobolectricTestRunner.class)
public class AddInBackgroundTest extends JobManagerTestBase {
    @Test
    public void testAddInBackground() throws InterruptedException {
        for(boolean delay : new boolean[]{true, false}) {
            for(boolean useCallback : new boolean[]{true, false}) {
                addInBackground(delay, useCallback);
            }
        }
    }

    public void addInBackground(boolean delayed, boolean useCallback) throws InterruptedException {
        long currentThreadId = Thread.currentThread().getId();
        final AtomicLong onAddedThreadId = new AtomicLong();
        final CountDownLatch addedLatch = new CountDownLatch(2);

        Job dummyJob = new DummyJob(new Params(1).setDelayMs(delayed ? 1000 : 0)) {
            @Override
            public void onAdded() {
                super.onAdded();
                onAddedThreadId.set(Thread.currentThread().getId());
                addedLatch.countDown();
            }
        };
        JobManager jobManager = createJobManager();
        jobManager.stop();
        final AtomicLong jobId = new AtomicLong(0);
        if(useCallback) {
            jobManager.addJobInBackground(dummyJob, new AsyncAddCallback() {
                @Override
                public void onAdded(long id) {
                    jobId.set(id);
                    addedLatch.countDown();
                }
            });
        } else {
            addedLatch.countDown();
            jobManager.addJobInBackground(dummyJob);
        }
        addedLatch.await();
        MatcherAssert.assertThat("thread ids should be different. delayed:" + delayed, currentThreadId, CoreMatchers.not(onAddedThreadId.get()));
        if(useCallback) {
            JobQueue queue = getNonPersistentQueue(jobManager);
            JobHolder holder = queue.findJobById(jobId.longValue());
            MatcherAssert.assertThat("there should be a job in the holder. id:" + jobId.longValue() +", delayed:" + delayed + ", use cb:" + useCallback
                    , holder, CoreMatchers.notNullValue());
            MatcherAssert.assertThat("id callback should have the proper id:", holder.getBaseJob(), CoreMatchers.is((BaseJob) dummyJob));
        }
    }

    protected JobQueue getNonPersistentQueue(JobManager jobManager) {
        return Reflection.field("nonPersistentJobQueue").ofType(JobQueue.class).in(jobManager).get();
    }
}
