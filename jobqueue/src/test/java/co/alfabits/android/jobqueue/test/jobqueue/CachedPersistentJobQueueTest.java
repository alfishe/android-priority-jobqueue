package co.alfabits.android.jobqueue.test.jobqueue;

import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import co.alfabits.android.jobqueue.JobQueue;
import co.alfabits.android.jobqueue.persistentQueue.sqlite.SqliteJobQueue;
import co.alfabits.android.jobqueue.test.util.JobQueueFactory;

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
