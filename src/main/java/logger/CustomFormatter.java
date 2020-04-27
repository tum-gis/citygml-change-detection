package logger;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class CustomFormatter extends Formatter {
    private SimpleDateFormat df;

    public CustomFormatter(SimpleDateFormat df) {
        super();
        this.df = df;
    }

    public String format(LogRecord record) {
        StringBuilder builder = new StringBuilder(1000);

        if (this.df != null) {
            builder.append("[" + this.df.format(new Date()) + " ");
            builder.append(String.format("%8s", Thread.currentThread().getName().replace("pool-", "p").replace("thread-", "t") + "]") + " ");
            // builder.append("[").append(record.getSourceClassName()).append(".");
            // builder.append(record.getSourceMethodName()).append("] ");
            // builder.append("[").append(record.getLevel()).append("] ");
        }

        builder.append(formatMessage(record));
        builder.append("\n");

        // Print on the console
        System.out.print(builder.toString());

        return builder.toString();
    }

    @Override
    public String getHead(Handler h) {
        return super.getHead(h);
    }

    @Override
    public String getTail(Handler h) {
        return super.getTail(h);
    }
}
