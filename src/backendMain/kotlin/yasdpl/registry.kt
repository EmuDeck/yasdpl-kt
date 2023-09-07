package yasdpl

import io.ktor.server.websocket.*

class ServiceRegistry
{
    val services = mutableMapOf<String, MutexHolder<ServerService>>()

	suspend operator fun invoke(descriptor: String, method: String, context: DefaultWebSocketServerSession): Result<Unit>
	{
		return services[descriptor]?.use { it(method, context) } ?: Result.failure(IllegalArgumentException("No service registered for $method"))
	}

	operator fun set(descriptor: String, service: ServerService)
	{
		services[descriptor] = MutexHolder(service)
	}

    fun register(descriptor: String, service: Service.Companion.ServiceBuilder<DefaultWebSocketServerSession>.() -> Unit)
    {
        this[descriptor] = Service.builder(service)
    }
}

typealias ServerService = Service<DefaultWebSocketServerSession>

