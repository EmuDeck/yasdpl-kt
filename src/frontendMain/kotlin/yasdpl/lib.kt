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

@Suppress("NON_EXPORTABLE_TYPE")
@DelicateCoroutinesApi
@ExperimentalJsExport
@JsExport
abstract class WebsocketClient(private val port: Short = 31338) {
    private val registry = HandlerRegistry().apply { initServicesInternal() }

    init {
        GlobalScope.promise {
            registry(port)
        }
    }

    @JsExport.Ignore
    abstract fun HandlerRegistry.initServices()

    private fun HandlerRegistry.initServicesInternal()
    {
        register("log")
        {
            method("info")
            {
                val message = incoming.receive() as? Frame.Text
                console.info(
                    "%c MetaDeck %c Backend %c",
                    "background: #16a085; color: black;",
                    "background: #1abc9c; color: black;",
                    "background: transparent;",
                    message?.readText())
                Result.success(Unit)
            }
            method("debug")
            {
                val message = incoming.receive() as? Frame.Text
                console.debug(
                    "%c MetaDeck %c Backend %c",
                    "background: #16a085; color: black;",
                    "background: #1abc9c; color: black;",
                    "color: blue;",
                    message?.readText())
                Result.success(Unit)
            }
            method("error")
            {
                val message = incoming.receive() as? Frame.Text
                console.error(
                    "%c MetaDeck %c Backend %c",
                    "background: #16a085; color: black;",
                    "background: #FF0000;",
                    "background: transparent;",
                    message?.readText())
                Result.success(Unit)
            }
            method("warn")
            {
                val message = incoming.receive() as? Frame.Text
                console.warn(
                    "%c MetaDeck %c Backend %c",
                    "background: #16a085; color: black;",
                    "background: #c4a000;",
                    "background: transparent;",
                    message?.readText())
                Result.success(Unit)
            }
        }
        initServices()
    }

    @JsExport.Ignore
    protected fun send(descriptor: String, method: String, callback: suspend DefaultClientWebSocketSession.() -> Result<Unit>): Promise<Unit>
    {
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

    fun test(): Promise<Unit> = send("test", "test")
    {
        send("test")
        Result.success(Unit)
    }

    fun close()
    {
        registry.close()
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



