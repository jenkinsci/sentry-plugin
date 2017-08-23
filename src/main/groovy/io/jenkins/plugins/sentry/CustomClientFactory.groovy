package io.jenkins.plugins.sentry

import io.sentry.DefaultSentryClientFactory
import io.sentry.dsn.Dsn
import io.sentry.context.ContextManager
import io.sentry.context.SingletonContextManager

class CustomClientFactory extends DefaultSentryClientFactory {
    protected ContextManager getContextManager(Dsn dsn) {
        return new SingletonContextManager();
    }
}
