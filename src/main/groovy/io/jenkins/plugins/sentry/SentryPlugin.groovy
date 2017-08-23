package io.jenkins.plugins.sentry

import hudson.Plugin
import java.util.logging.*

import io.sentry.Sentry
import io.sentry.jul.SentryHandler

class SentryPlugin extends Plugin {
    Logger LOG = Logger.getLogger(SentryPlugin.name)

    void postInitialize() {
        if (!System.env.get('SENTRY_DSN')) {
            LOG.error('The `SENTRY_DSN` environment variable is not defined, not configuring Sentry')
            return
        }

        Sentry.init()

        System.env.each { key, value ->
            key.eachMatch('SENTRY_TAG_(\\w+)') { match ->
                LOG.info("Adding a tag for `${match[1]}` with `${value}`")
                Sentry.context.addTag(match[1], value)
            }
        }

        if (!System.env.get('SENTRY_NO_LOGWATCHER')) {
            addLogWatcher()
        }
    }

    void addLogWatcher() {
        SentryHandler sentry = new SentryHandler()
        /* Default everything to the warning level, no need for INFO */
        sentry.level = Level.WARNING

        Thread.start {
            LOG.info('Waiting for 2 minutes for Jenkins to bootstrap before configuring Sentry')
            sleep 120
            while (true) {
                LogManager.logManager.loggerNames.toList().each { loggerName ->
                    /* Avoid excessive warnings */
                    /* https://issues.jenkins-ci.org/browse/JENKINS-46404 */
                    if (loggerName == 'org.jenkinsci.plugins.durabletask.ProcessLiveness') {
                        continue
                    }

                    Logger manager = LogManager.logManager.getLogger(loggerName)

                    boolean found = false
                    manager.handlers.toList().each { handler ->
                        if (handler.class == SentryHandler) {
                            found = true
                        }
                    }

                    if (!found) {
                        LOG.info("Adding Sentry to ${loggerName}")
                        manager.addHandler(sentry)
                    }
                }
                /* Sleep for five minutes in this thread, to make sure we are always
                * adding Sentry to new loggers should they appear
                */
                sleep 500
            }
        }

    }
}
