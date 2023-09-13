package yasdpl

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import kotlinx.js.Void
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.js.Promise

external interface Console {
    fun dir(o: Any)
    fun error(vararg o: Any?)
    fun info(vararg o: Any?)
    fun log(vararg o: Any?)
    fun debug(vararg o: Any?)
    fun warn(vararg o: Any?)
}

external val console: Console

@DelicateCoroutinesApi
@ExperimentalJsExport
@JsExport
abstract class WebsocketClient(private val port: Short = 31338) {
    private val registry = HandlerRegistry().apply { initServicesInternal() }
    private val showDebug: Boolean = js("process.env.RELEASE_TYPE === 'development'") as Boolean

    lateinit var env: Env

    init {
        GlobalScope.promise {
            send("env", "get")
            {
                this@WebsocketClient.env = receiveDeserialized<Env>()
                return@send Result.success(Unit)
            }
            registry(port)
        }

    }

    @JsExport.Ignore
    abstract fun HandlerRegistry.initServices()

    private fun HandlerRegistry.initServicesInternal() {
        register("log")
        {
            method("info")
            {
                val pluginName = incoming.receive() as? Frame.Text
                val loggerName = incoming.receive() as? Frame.Text
                val message = incoming.receive() as? Frame.Text
                console.info(
                    "%c $pluginName Backend %c $loggerName %c",
                    "background: #16a085; color: black;",
                    "background: #1abc9c; color: black;",
                    "background: transparent;",
                    message?.readText()
                )
                Result.success(Unit)
            }
            method("debug")
            {
                val pluginName = incoming.receive() as? Frame.Text
                val loggerName = incoming.receive() as? Frame.Text
                val message = incoming.receive() as? Frame.Text
                if (showDebug) {
                    console.debug(
                        "%c $pluginName Backend %c $loggerName %c",
                        "background: #16a085; color: black;",
                        "background: #1abc9c; color: black;",
                        "color: blue;",
                        message?.readText()
                    )
                }
                Result.success(Unit)
            }
            method("error")
            {
                val pluginName = incoming.receive() as? Frame.Text
                val loggerName = incoming.receive() as? Frame.Text
                val message = incoming.receive() as? Frame.Text
                console.error(
                    "%c $pluginName Backend %c $loggerName %c",
                    "background: #16a085; color: black;",
                    "background: #FF0000;",
                    "background: transparent;",
                    message?.readText()
                )
                Result.success(Unit)
            }
            method("warn")
            {
                val pluginName = incoming.receive() as? Frame.Text
                val loggerName = incoming.receive() as? Frame.Text
                val message = incoming.receive() as? Frame.Text
                console.warn(
                    "%c $pluginName Backend %c $loggerName %c",
                    "background: #16a085; color: black;",
                    "background: #c4a000;",
                    "background: transparent;",
                    message?.readText()
                )
                Result.success(Unit)
            }
        }
        initServices()
    }

    @JsExport.Ignore
    fun send(
        descriptor: String,
        method: String,
        callback: suspend DefaultClientWebSocketSession.() -> Result<Unit>
    ): Promise<Unit> {
        return GlobalScope.promise {
            val client = HttpClient {
                install(ContentNegotiation)
                {
                    json()
                }
                install(WebSockets)
                {
                    contentConverter = KotlinxWebsocketSerializationConverter(Json)
                }
            }
            client.webSocket(
                method = HttpMethod.Get,
                host = "localhost",
                port = port.toInt(),
                path = "/$descriptor/$method"
            ) {
                callback()
                close()
            }
            client.close()
        }
    }

    fun close() {
        registry.close()
    }
}

@DelicateCoroutinesApi
@ExperimentalJsExport
@JsExport
class Logger(private val backendAPI: WebsocketClient, private val loggerName: String) {
    private val showDebug: Boolean = js("process.env.RELEASE_TYPE === 'development'") as Boolean
    fun info(vararg args: Any?) {
        backendAPI.send("log", "info") {
            send(loggerName)
            send(Json.encodeToString(args))
            return@send Result.success(Unit)
        }
        console.info(
            "%c ${backendAPI.env.pluginName} Backend %c $loggerName %c",
            "background: #16a085; color: black;",
            "background: #1abc9c; color: black;",
            "background: transparent;",
            *args
        )
    }

    fun debug(vararg args: Any?) {
        if (showDebug) {
            backendAPI.send("log", "debug") {
                send(loggerName)
                send(Json.encodeToString(args))
                return@send Result.success(Unit)
            }
            console.debug(
                "%c ${backendAPI.env.pluginName} Backend %c $loggerName %c",
                "background: #16a085; color: black;",
                "background: #1abc9c; color: black;",
                "color: blue;",
                *args
            )
        }
    }

    fun error(vararg args: Any?) {
        backendAPI.send("log", "error") {
            send(loggerName)
            send(Json.encodeToString(args))
            return@send Result.success(Unit)
        }
        console.error(
            "%c ${backendAPI.env.pluginName} Backend %c $loggerName %c",
            "background: #16a085; color: black;",
            "background: #FF0000;",
            "background: transparent;",
            *args
        )
    }

    fun warn(vararg args: Any?) {
        backendAPI.send("log", "warn") {
            send(loggerName)
            send(Json.encodeToString(args))
            return@send Result.success(Unit)
        }
        console.warn(
            "%c ${backendAPI.env.pluginName} Backend %c $loggerName %c",
            "background: #16a085; color: black;",
            "background: #c4a000;",
            "background: transparent;",
            *args
        )
    }
}

//class YasdplAPI(port: Short = 31338, private val serverAPI: ServerAPI) : YasdplInstance(port), ServerAPI by serverAPI
//{
//    suspend inline fun <reified P, reified R> yasdplCallBackendSuspend(name: String, parameters: P): Result<R?>
//    {
//        val params = parameters?.toJson()
//        val packet: Packet = Packet.Call(RemoteCall(name, params))
//        return sendCall(packet, port).map { it?.jsonObject?.fromJson() }
//    }
//}



