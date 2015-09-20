package co.alfabits.android.jobqueue.test.jobmanager;

import co.alfabits.android.jobqueue.JobManager;
import co.alfabits.android.jobqueue.JobQueue;
import co.alfabits.android.jobqueue.Params;
import co.alfabits.android.jobqueue.test.jobs.DummyJob;

import org.fest.reflect.core.Reflection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@Config(sdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class CancelJobTest extends JobManagerTestBase {
    @Test
    public void testCancelJob() throws Exception {
        testCancelJob(false);
        testCancelJob(true);
    }

    private void testCancelJob(boolean isPersistent) {
        DummyJob dummyJob = new DummyJob(new Params(0).setPersistent(isPersistent));
        JobManager jobManager = createJobManager();
        jobManager.stop();
        long jobId = jobManager.addJob(dummyJob);
        JobQueue queue = getQueue(jobManager, isPersistent);
        assertNotNull("there should be a job in the holder. id:" + jobId + ", isPersistent:" + isPersistent
                , queue.findJobById(jobId));
        boolean isCanceled = jobManager.cancelJob(jobId, isPersistent);
        assertTrue("cancelJob() should return true. id: " + jobId + ", isPersistent:" + isPersistent
                , isCanceled);
        assertNull("there should not be a job exists in queue. id:" + jobId + ", isPersistent:" + isPersistent
                , queue.findJobById(jobId));
        // the job handled in manager and the one here are different instance if persistent.
        if (!isPersistent) {
            assertEquals("job.onCancel() should be called once. id:" + jobId + ", isPersistent: false"
                    , 1, dummyJob.getOnCancelCnt());
        }
    }

    @Test
    public void testCancelRunningJob() throws Exception {
        final CountDownLatch runWaitLatch = new CountDownLatch(1);
        final CountDownLatch runEndLatch = new CountDownLatch(1);
        DummyJob dummyJob = new DummyJob(new Params(0)) {
            @Override
            public void onRun() throws Throwable {
                super.onRun();
                runWaitLatch.countDown();
                runEndLatch.await();
            }
        };
        JobManager jobManager = createJobManager();
        jobManager.stop();
        long jobId = jobManager.addJob(dummyJob);
        JobQueue queue = getQueue(jobManager, false);
        assertNotNull("there should be a job in the holder. id:" + jobId
                , queue.findJobById(jobId));
        jobManager.start();
        runWaitLatch.await();
        boolean isCanceled = jobManager.cancelJob(jobId, false);
        assertFalse("cancelJob() should return false. id: " + jobId, isCanceled);
        runEndLatch.countDown();
        jobManager.stop();
    }

    @Test
    public void testCancelNotExistJob() throws Exception {
        JobManager jobManager = createJobManager();
        jobManager.stop();
        long jobId = 0xDEADBEEF;
        boolean isCanceled = jobManager.cancelJob(jobId, false);
        assertFalse("cancelJob() should return false. id: " + jobId, isCanceled);
    }

    private JobQueue getQueue(JobManager jobManager, boolean isPersistent) {
        if (isPersistent) {
            return Reflection.field("persistentJobQueue").ofType(JobQueue.class).in(jobManager).get();
        } else {
            return Reflection.field("nonPersistentJobQueue").ofType(JobQueue.class).in(jobManager).get();
        }
    }
}
