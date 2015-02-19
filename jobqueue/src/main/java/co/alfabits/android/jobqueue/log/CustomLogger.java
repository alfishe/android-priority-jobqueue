package co.alfabits.android.jobqueue.log;

/**
 * You can provide your own logger implementation to {@link co.alfabits.android.jobqueue.JobManager}
 * it is very similar to Roboguice's logger
 */
public interface CustomLogger {
    /**
     * JobManager may call this before logging sth that is (relatively) expensive to calculate
     * @return
     */
    public boolean isDebugEnabled();
    public boolean isInfoEnabled();
    public boolean isWarnEnabled();

    public void d(String text, Object... args);
    public void i(String text, Object... args);
    public void w(String text, Object... args);
    public void e(Throwable t, String text, Object... args);
    public void e(String text, Object... args);
}
