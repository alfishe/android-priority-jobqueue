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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Config(sdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class PersistentJobTest extends JobManagerTestBase {
    //TEST parallel running
    public static CountDownLatch persistentRunLatch = new CountDownLatch(1);

    @Test
    public void testPersistentJob() throws Exception {
        JobManager jobManager = createJobManager();
        jobManager.addJob(0, new DummyPersistentLatchJob());
        persistentRunLatch.await(5, TimeUnit.SECONDS);
        MatcherAssert.assertThat((int) persistentRunLatch.getCount(), equalTo(0));
    }

    protected static class DummyPersistentLatchJob extends DummyJob {

        public DummyPersistentLatchJob() {
            super(new Params(0).persist());
        }

        @Override
        public void onRun() throws Throwable {
            PersistentJobTest.persistentRunLatch.countDown();
        }
    }
}
