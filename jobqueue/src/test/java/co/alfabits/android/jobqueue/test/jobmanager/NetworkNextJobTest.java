package co.alfabits.android.jobqueue.test.jobmanager;

import co.alfabits.android.jobqueue.JobHolder;
import co.alfabits.android.jobqueue.JobManager;
import co.alfabits.android.jobqueue.Params;
import co.alfabits.android.jobqueue.config.Configuration;
import co.alfabits.android.jobqueue.test.jobs.DummyJob;
import org.fest.reflect.method.Invoker;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;

@Config(sdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class NetworkNextJobTest extends JobManagerTestBase {
    @Test
    public void testNetworkNextJob() throws Exception {
        DummyNetworkUtil dummyNetworkUtil = new DummyNetworkUtil();
        JobManager jobManager = createJobManager(new Configuration.Builder(RuntimeEnvironment.application).networkUtil(dummyNetworkUtil));
        jobManager.stop();
        DummyJob dummyJob = new DummyJob(new Params(0).requireNetwork());
        long dummyJobId = jobManager.addJob(dummyJob);
        dummyNetworkUtil.setHasNetwork(false);
        Invoker<JobHolder> nextJobMethod = getNextJobMethod(jobManager);
        MatcherAssert.assertThat("when there isn't any network, next job should return null", nextJobMethod.invoke(), nullValue());
        MatcherAssert.assertThat("even if there is network, job manager should return correct count", jobManager.count(), equalTo(1));
        dummyNetworkUtil.setHasNetwork(true);
        JobHolder retrieved = nextJobMethod.invoke();
        MatcherAssert.assertThat("when network is recovered, next job should be retrieved", retrieved, notNullValue());
    }
}
