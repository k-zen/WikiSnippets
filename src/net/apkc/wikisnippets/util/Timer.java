package net.apkc.wikisnippets.util;

/**
 * Utility class for counting time.
 *
 * @author Andreas P. Koenzen
 */
public class Timer {

    public static enum Time {

        MILLISECOND, SECOND, MINUTE, HOUR
    }
    private long start = 0L;
    private long end = 0L;

    public void starTimer() {
        start = System.currentTimeMillis();
    }

    public void endTimer() {
        end = System.currentTimeMillis();
    }

    /**
     * Returns the running time in milliseconds.
     *
     * @return The running time.
     */
    public long getExecutionTime() {
        if (start > 0L) {
            return (System.currentTimeMillis() - start);
        }
        else {
            return 0L;
        }
    }

    /**
     * This method will compute the difference between two times in
     * milliseconds.
     *
     * @param timeUnit The time unit for the response.
     *
     * @return The time difference.
     */
    public double computeOperationTime(Timer.Time timeUnit) {
        long diff = (end - start);
        switch (timeUnit) {
            case MILLISECOND:
                return diff;
            case SECOND:
                return diff / 1000.0;
            case MINUTE:
                return diff / 1000.0 / 60;
            case HOUR:
                return diff / 1000.0 / 3600;
            default:
                return diff;
        }
    }

    /**
     * This method will compute the difference between two times, but the time
     * is passed as a parameter.
     *
     * @param timeUnit    The time unit for the response.
     * @param elapsedTime The time in milliseconds.
     *
     * @return The time difference.
     */
    public double customComputeOperationTime(Timer.Time timeUnit, long elapsedTime) {
        switch (timeUnit) {
            case MILLISECOND:
                return elapsedTime;
            case SECOND:
                return elapsedTime / 1000.0;
            case MINUTE:
                return elapsedTime / 1000.0 / 60;
            case HOUR:
                return elapsedTime / 1000.0 / 3600;
            default:
                return elapsedTime;
        }
    }
}
