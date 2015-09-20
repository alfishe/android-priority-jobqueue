package co.alfabits.android.jobqueue.test.jobmanager;

import co.alfabits.android.jobqueue.BaseJob;
import co.alfabits.android.jobqueue.JobManager;
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
public class RunFailingJobTest extends JobManagerTestBase {
    @Test
    public void runFailingJob() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        JobManager jobManager = createJobManager();
        jobManager.addJob(0, new BaseJob(true) {
            @Override
            public void onAdded() {

            }

            @Override
            public void onRun() throws Throwable {
                throw new RuntimeException();
            }

            @Override
            protected void onCancel() {
                latch.countDown();
            }

            @Override
            protected boolean shouldReRunOnThrowable(Throwable throwable) {
                return false;
            }
        });
        latch.await(10, TimeUnit.SECONDS);
        MatcherAssert.assertThat((int) latch.getCount(), equalTo(0));
    }

}
