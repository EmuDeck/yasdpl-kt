package yasdpl

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.core.*
import io.ktor.websocket.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class HandlerRegistry : Closeable {
    private val services = mutableMapOf<String, MutexHolder<ClientService>>()

    private var closed = false
    suspend operator fun invoke(port: Short): Result<Unit> = coroutineScope {
        for ((descriptor, service) in services.entries) {
            service.use {
                launch {
                    this@HandlerRegistry(descriptor, port)
                }
            }
        }
        return@coroutineScope Result.success(Unit)
    }

    suspend operator fun invoke(descriptor: String, port: Short): Result<Unit> {
        return services[descriptor]?.use {
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
                path = "/bind/$descriptor"
            ) {
                while (!closed) {
                    val (method) = receiveDeserialized<BindingCall>()
                    it(method, this)
                }
                close()
            }
            client.close()
            return@use Result.success(Unit)
        } ?: Result.failure(IllegalArgumentException("No service registered for $descriptor"))
    }

    operator fun set(descriptor: String, service: ClientService) {
        services[descriptor] = MutexHolder(service)
    }

    fun register(descriptor: String, service: Service.Companion.ServiceBuilder<DefaultClientWebSocketSession>.() -> Unit)
    {
        this[descriptor] = Service.builder(service)
    }

    override fun close() {
        closed = true
    }
}

typealias ClientService = Service<DefaultClientWebSocketSession>

