package co.alfabits.android.jobqueue.test.util;

import co.alfabits.android.jobqueue.JobQueue;

public interface JobQueueFactory {
    public JobQueue createNew(long sessionId, String id);
}
