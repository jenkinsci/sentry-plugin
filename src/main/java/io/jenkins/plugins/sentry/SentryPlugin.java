package io.jenkins.plugins.sentry;

import hudson.Plugin;
import io.sentry.Sentry;

public class SentryPlugin extends Plugin {
    public void postInitialize() {
        Sentry.init();
    }
}
