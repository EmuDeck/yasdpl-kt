package usdpl

import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import usdpl.rpc.ServerService
import usdpl.rpc.ServiceRegistry

class WebsocketServer(port: Int) {
	private val services = ServiceRegistry()
	private val engine: ApplicationEngine
	init
	{
		@Suppress("ExtractKtorModule")
		engine = embeddedServer(CIO, port = port) {
			install(WebSockets)
			routing {
				webSocket {
					get("/{service}/{method}") {
						val service = call.parameters["service"]
						val method = call.parameters["method"]
						if (service !== null && method !== null)
							this@WebsocketServer.services(service, method, this)
						else
							throw IllegalStateException("service and method must not be null")
					}
				}
			}
		}
	}

	operator fun set(descriptor: String, service: ServerService)
	{
		services[descriptor] = service
	}

	suspend operator fun invoke() = coroutineScope {
		launch {
			engine.start(true)
		}.join()
	}

}

