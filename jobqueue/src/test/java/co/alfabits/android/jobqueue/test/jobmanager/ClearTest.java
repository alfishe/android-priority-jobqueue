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
public class ClearTest extends JobManagerTestBase {
    @Test
    public void testClear() throws Exception {
        JobManager jobManager = createJobManager();
        final int LIMIT = 20;
        for(int i = 0; i < LIMIT; i++) {
            jobManager.addJob(new DummyJob(new Params(0).setPersistent(i % 2 == 1)));
        }
        jobManager.clear();
        MatcherAssert.assertThat("after clear, count should be 0", jobManager.count(), equalTo(0));
    }
}
