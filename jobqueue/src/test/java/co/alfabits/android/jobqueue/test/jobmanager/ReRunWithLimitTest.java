package co.alfabits.android.jobqueue.test.jobmanager;

import co.alfabits.android.jobqueue.JobManager;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.equalTo;

@Config(sdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ReRunWithLimitTest extends JobManagerTestBase {
    @Test
    public void testReRunWithLimit() throws Exception {
        JobManager jobManager = createJobManager();
        testReRun(jobManager, false);
        testReRun(jobManager, true);
    }

    private void testReRun(JobManager jobManager, boolean persist) throws InterruptedException {
        DummyJobWithRunCount.runCount = 0;//reset
        DummyJobWithRunCount job = new DummyJobWithRunCount(persist);
        jobManager.addJob(0, job);
        int limit = 25;
        while (limit-- > 0 && DummyJobWithRunCount.runCount != 5) {
            Thread.sleep(100);
        }
        MatcherAssert.assertThat(DummyJobWithRunCount.runCount, equalTo(job.getRetryLimit()));
        MatcherAssert.assertThat((int) jobManager.count(), equalTo(0));
    }
}
