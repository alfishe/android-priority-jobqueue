package co.alfabits.android.jobqueue.test.jobqueue;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import co.alfabits.android.jobqueue.JobQueue;
import co.alfabits.android.jobqueue.cachedQueue.CachedJobQueue;
import co.alfabits.android.jobqueue.nonPersistentQueue.NonPersistentPriorityQueue;
import co.alfabits.android.jobqueue.test.util.JobQueueFactory;

@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class CachedNonPersistentJobQueueTest extends JobQueueTestBase {
    public CachedNonPersistentJobQueueTest() {
        super(new JobQueueFactory() {
            @Override
            public JobQueue createNew(long sessionId, String id) {
                return new CachedJobQueue(new NonPersistentPriorityQueue(sessionId, id));
            }
        });
    }
}
