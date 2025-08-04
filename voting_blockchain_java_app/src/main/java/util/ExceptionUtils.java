package util;

public class ExceptionUtils{
    // formats exceptions for logging
    public static String buildExceptionChain(Throwable e) {
        StringBuilder sb = new StringBuilder(":\n");
        while (e != null) {
            sb.append("\t")
                    .append(e.getClass().getSimpleName())
                    .append(": ")
                    .append(e.getMessage());


            StackTraceElement[] stackTrace = e.getStackTrace();
            if (stackTrace != null && stackTrace.length > 0) {
                StackTraceElement top = stackTrace[0];
                sb.append(" (")
                        .append(top.getFileName())
                        .append(":")
                        .append(top.getLineNumber())
                        .append(")");
            }
            sb.append("\n");
            e = e.getCause();
        }
        return sb.toString();
    }
}