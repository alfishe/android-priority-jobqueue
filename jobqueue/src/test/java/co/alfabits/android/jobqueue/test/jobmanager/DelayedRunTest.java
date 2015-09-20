package co.alfabits.android.jobqueue.test.jobmanager;

import co.alfabits.android.jobqueue.JobManager;
import co.alfabits.android.jobqueue.Params;
import co.alfabits.android.jobqueue.test.jobs.DummyJob;
import static org.hamcrest.CoreMatchers.*;
import org.hamcrest.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;

@Config(sdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class DelayedRunTest extends JobManagerTestBase {
    @Test
    public void testDelayedRun() throws Exception {
        testDelayedRun(false, false);
        testDelayedRun(true, false);
        testDelayedRun(false, true);
        testDelayedRun(true, true);
    }
    public void testDelayedRun(boolean persist, boolean tryToStop) throws Exception {
        JobManager jobManager = createJobManager();
        DummyJob delayedJob = new DummyJob(new Params(10).delayInMs(2000).setPersistent(persist));
        DummyJob nonDelayedJob = new DummyJob(new Params(0).setPersistent(persist));
        jobManager.addJob(delayedJob);
        jobManager.addJob(nonDelayedJob);
        Thread.sleep(500);
        MatcherAssert.assertThat("there should be 1 delayed job waiting to be run", jobManager.count(), equalTo(1));
        if(tryToStop) {//see issue #11
            jobManager.stop();
            Thread.sleep(3000);
            MatcherAssert.assertThat("there should still be 1 delayed job waiting to be run when job manager is stopped",
                    jobManager.count(), equalTo(1));
            jobManager.start();
        }
        Thread.sleep(3000);
        MatcherAssert.assertThat("all jobs should be completed", jobManager.count(), equalTo(0));

    }
}
