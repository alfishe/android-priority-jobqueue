package co.alfabits.android.jobqueue.test.jobqueue;

import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import co.alfabits.android.jobqueue.JobQueue;
import co.alfabits.android.jobqueue.persistentQueue.sqlite.SqliteJobQueue;
import co.alfabits.android.jobqueue.test.util.JobQueueFactory;

@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class CachedPersistentJobQueueTest extends JobQueueTestBase {
    public CachedPersistentJobQueueTest() {
        super(new JobQueueFactory() {
            @Override
            public JobQueue createNew(long sessionId, String id) {
                return new SqliteJobQueue(Robolectric.application, sessionId, id, new SqliteJobQueue.JavaSerializer());
            }
        });
    }
}
