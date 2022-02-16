package org.apache.logging.log4j.core.config;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.nr.agent.instrumentation.log4j2.AgentUtil;
import org.apache.logging.log4j.core.LogEvent;

import static com.nr.agent.instrumentation.log4j2.AgentUtil.*;

@Weave(originalName = "org.apache.logging.log4j.core.config.LoggerConfig", type = MatchType.ExactClass)
public class LoggerConfig_Instrumentation {

    protected void callAppenders(LogEvent event) {
        // Do nothing if application_logging.enabled: false
        if (isApplicationLoggingEnabled()) {
            if (isApplicationLoggingMetricsEnabled()) {
                // Generate log level metrics
                NewRelic.incrementCounter("Logging/lines");
                NewRelic.incrementCounter("Logging/lines/" + event.getLevel().toString());
            }

            if (isApplicationLoggingForwardingEnabled()) {
                // Record and send LogEvent to New Relic
                recordNewRelicLogEvent(event);
            }
        }
        Weaver.callOriginal();
    }
}