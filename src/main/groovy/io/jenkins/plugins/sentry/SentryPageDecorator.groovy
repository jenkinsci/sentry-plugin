package io.jenkins.plugins.sentry

import java.util.logging.*

import hudson.Extension
import hudson.model.PageDecorator
import net.sf.json.JSONObject
import org.kohsuke.stapler.StaplerRequest

@Extension
class SentryPageDecorator extends PageDecorator {
    String getSentryDsn() {
        return System.env.get('SENTRY_PUBLIC_DSN')
    }
}
