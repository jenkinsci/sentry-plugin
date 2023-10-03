package io.jenkins.plugins.sentry;

import hudson.Plugin;
import io.sentry.Sentry;
import io.sentry.jul.SentryHandler;

import java.util.Enumeration;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class SentryPlugin extends Plugin {
    private static final Logger LOG = Logger.getLogger(SentryPlugin.class.getName());

    public void postInitialize() {
        if (System.getenv("SENTRY_DSN") == null) {
            LOG.warning("The `SENTRY_DSN` environment variable is not defined, not configuring Sentry");
            return;
        }

        Sentry.init();
        LOG.info("Sentry initialized");

        if (System.getenv("SENTRY_NO_LOGWATCHER") == null) {
            LOG.info("Running Sentry addLogWatcher");
            addLogWatcher();
        }
    }

    public void addLogWatcher() {
        final SentryHandler sentry = new SentryHandler();
        /* Default everything to the warning level, no need for INFO */
        sentry.setMinimumEventLevel(Level.WARNING);

        new Thread(() -> {
            LOG.info("Waiting for 2 minutes for Jenkins to bootstrap before configuring Sentry");
            try {
                Thread.sleep(120_000); // Converted seconds to milliseconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            while (true) {
                Enumeration<String> enum_ = LogManager.getLogManager().getLoggerNames();
                LOG.fine("Iterating over loggers");
                while (enum_.hasMoreElements()) {
                    String loggerName = enum_.nextElement();
                    /* Avoid excessive warnings */
                    /* https://issues.jenkins-ci.org/browse/JENKINS-46404 */
                    if (!loggerName.equals("org.jenkinsci.plugins.durabletask.ProcessLiveness")) {
                        LOG.fine("Checking logger: " + loggerName);
                        Logger manager = LogManager.getLogManager().getLogger(loggerName);
                        if (manager != null) {
                            boolean found = false;
                            for (Handler handler : manager.getHandlers()) {
                                if (handler instanceof SentryHandler) {
                                    found = true;
                                    break;
                                }
                            }

                            if (!found) {
                                LOG.info("Adding Sentry to " + loggerName);
                                manager.addHandler((Handler) sentry);
                            }
                        }
                    }
                }

                // Sleep for five minutes in this thread, to make sure we are always
                // adding Sentry to new loggers should they appear
                try {
                    Thread.sleep(300_000); // Converted 5 minutes to milliseconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
