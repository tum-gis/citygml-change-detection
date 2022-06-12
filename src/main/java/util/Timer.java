package util;

public class Timer {

    private long startTime;
    private long endTime;

    public Timer() {
        // Set to 0 to force calling start() and end()
        startTime = 0;
        endTime = 0;
    }

    private long peek() {
        return System.currentTimeMillis();
    }

    public long start() {
        startTime = peek();
        return startTime;
    }

    // Return time in seconds
    public long end() throws TimerMissingStartException {
        if (startTime == 0) {
            throw new TimerMissingStartException();
        }
        long endTime = peek();
        long totalTime = endTime - startTime;
        return totalTime / 1000; // in seconds
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public class TimerMissingStartException extends Exception {
        @Override
        public String getMessage() {
            return "Developer error: Must call start() first before end()";
        }
    }
}
