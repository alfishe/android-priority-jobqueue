package co.alfabits.android.jobqueue.log;

/**
 * Wrapper around {@link CustomLogger}. by default, logs to nowhere
 */
public class JqLog {
    private static CustomLogger customLogger = new CustomLogger() {
        @Override
        public boolean isDebugEnabled() {
            return false;
        }

        @Override
        public boolean isInfoEnabled() {
            return false;
        }

        @Override
        public boolean isWarnEnabled() {
            return false;
        }

        @Override
        public void d(String text, Object... args) {
            //void
        }

        @Override
        public void i(String text, Object... args) {
            //void
        }

        @Override
        public void w(String text, Object... args) {
            //void
        }

        @Override
        public void e(Throwable t, String text, Object... args) {
            //void
        }

        @Override
        public void e(String text, Object... args) {
            //void
        }
    };

    public static void setCustomLogger(CustomLogger customLogger) {
        JqLog.customLogger = customLogger;
    }

    public static boolean isDebugEnabled() {
        return customLogger.isDebugEnabled();
    }

    public static boolean isInfoEnabled() {
        return customLogger.isInfoEnabled();
    }

    public static boolean isWarnEnabled() {
        return customLogger.isWarnEnabled();
    }

    public static void d(String text, Object... args) {
        customLogger.d(text, args);
    }

    public static void i(String text, Object... args) {
        customLogger.i(text, args);
    }

    public static void w(String text, Object... args) {
        customLogger.w(text, args);
    }


    public static void e(Throwable t, String text, Object... args) {
        customLogger.e(t, text, args);
    }

    public static void e(String text, Object... args) {
        customLogger.e(text, args);
    }
}
