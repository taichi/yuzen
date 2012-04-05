import static ch.qos.logback.classic.Level.*
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.status.OnConsoleStatusListener

statusListener(OnConsoleStatusListener)
context.getStatusManager().getCopyOfStatusListenerList().each {
	it.context = context
	it.start()
}

appender("CONSOLE", ConsoleAppender) {
	encoder(PatternLayoutEncoder) { pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n" }
}

logger 'org.thymeleaf', INFO, ['CONSOLE']
root INFO, ['CONSOLE']
