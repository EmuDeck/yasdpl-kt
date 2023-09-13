package yasdpl

import io.github.oshai.kotlinlogging.Formatter
import io.github.oshai.kotlinlogging.FormattingAppender
import io.github.oshai.kotlinlogging.KLoggingEvent
import io.github.oshai.kotlinlogging.Level
import io.ktor.websocket.*
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer
import okio.use

object BackendFormatter : Formatter
{
    override fun formatMessage(loggingEvent: KLoggingEvent): String {
            with(loggingEvent) {
                return buildString {
                    append("${level.name} ")
                    append("[${DeckyEnv.pluginName}] ")
                    append("[${loggingEvent.loggerName}] ")
                    append(message)
                    append("\n")
                }
            }
    }
}

object FileAppender : FormattingAppender()
{
    init {
            FileSystem.SYSTEM.delete(DeckyEnv.logDir.toPath()/"plugin.log")
    }
    override fun logFormattedMessage(loggingEvent: KLoggingEvent, formattedMessage: Any?) {
        FileSystem.SYSTEM.appendingSink(DeckyEnv.logDir.toPath()/"plugin.log").use { sink ->
            sink.buffer().use { buffer ->
                buffer.writeUtf8(formattedMessage.toString())
            }
        }
    }
}

class MultiAppender(private val appenders: List<FormattingAppender>) : FormattingAppender()
{
    override fun logFormattedMessage(loggingEvent: KLoggingEvent, formattedMessage: Any?) {
        appenders.forEach { it.logFormattedMessage(loggingEvent, formattedMessage) }
    }
}

class WebsocketAppender(private val websocketServer: WebsocketServer) : FormattingAppender()
{
    override fun logFormattedMessage(loggingEvent: KLoggingEvent, formattedMessage: Any?) {
            when (loggingEvent.level) {
                Level.INFO -> websocketServer.bind("log", "info")
                {
                    send(loggingEvent.loggerName)
                    send(formattedMessage.toString())
                    return@bind Result.success(Unit)
                }

                Level.DEBUG -> websocketServer.bind("log", "debug")
                {
                    send(loggingEvent.loggerName)
                    send(formattedMessage.toString())
                    return@bind Result.success(Unit)
                }

                Level.ERROR -> websocketServer.bind("log", "error")
                {
                    send(loggingEvent.loggerName)
                    send(formattedMessage.toString())
                    return@bind Result.success(Unit)
                }

                Level.WARN -> websocketServer.bind("log", "warn")
                {
                    send(loggingEvent.loggerName)
                    send(formattedMessage.toString())
                    return@bind Result.success(Unit)
                }

                else -> Unit
            }
    }
}