package app.resources.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import java.util.LinkedList;
import java.util.List;

public class LogAppender extends AppenderBase<ILoggingEvent> {
    private static final int MAX_LOGS = 1000;
    private static final List<ILoggingEvent> logs = new LinkedList<>();

    @Override
    protected void append(ILoggingEvent logEvent) {
        synchronized (logs) {
            logs.add(logEvent);
            if (logs.size() > MAX_LOGS) {
                logs.remove(0);
            }
        }
    }

    public static List<ILoggingEvent> getLogs() {
        synchronized (logs) {
            return new LinkedList<>(logs); // return copy to avoid concurrency issues
        }
    }
}