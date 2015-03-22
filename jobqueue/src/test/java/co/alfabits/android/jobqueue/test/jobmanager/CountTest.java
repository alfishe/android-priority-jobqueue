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

@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class CountTest extends JobManagerTestBase {
    @Test
    public void testCount() throws Exception {
        JobManager jobManager = createJobManager();
        jobManager.stop();
        for (int i = 0; i < 10; i++) {
            jobManager.addJob(new DummyJob(new Params(0).persist()));
            MatcherAssert.assertThat((int) jobManager.count(), equalTo(i * 2 + 1));
            jobManager.addJob(new DummyJob(new Params(0).persist()));
            MatcherAssert.assertThat((int) jobManager.count(), equalTo(i * 2 + 2));
        }
        jobManager.start();
        Thread.sleep(2000);
        MatcherAssert.assertThat((int) jobManager.count(), equalTo(0));
    }
}
