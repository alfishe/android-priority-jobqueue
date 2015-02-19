package co.alfabits.android.jobqueue.test;

import android.util.Log;

import org.junit.Before;
import org.robolectric.shadows.ShadowLog;

import co.alfabits.android.jobqueue.log.CustomLogger;
import co.alfabits.android.jobqueue.log.JqLog;

public class TestBase {
    protected static boolean ENABLE_DEBUG = false;

    @Before
    public void setUp() throws Exception {
        if (ENABLE_DEBUG) {
            enableDebug();
        }
    }

    private void enableDebug() {
        ShadowLog.stream = System.out;
        JqLog.setCustomLogger(new CustomLogger() {
            private String TAG = "test_logger";

            @Override
            public boolean isDebugEnabled() {
                return true;
            }

            @Override
            public boolean isInfoEnabled() {
                return true;
            }

            @Override
            public boolean isWarnEnabled() {
                return true;
            }

            @Override
            public void d(String text, Object... args) {
                Log.d(TAG, String.format(text, args));
            }

            @Override
            public void i(String text, Object... args) {
                Log.i(TAG, String.format(text, args));
            }

            @Override
            public void w(String text, Object... args) {
                Log.w(TAG, String.format(text, args));
            }

            @Override
            public void e(Throwable t, String text, Object... args) {
                Log.e(TAG, String.format(text, args), t);
            }

            @Override
            public void e(String text, Object... args) {
                Log.e(TAG, String.format(text, args));
            }
        });
    }
}
