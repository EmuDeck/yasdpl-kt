package yasdpl

import io.ktor.server.application.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonElement

class ServiceRegistry
{
	private val services = mutableMapOf<String, MutexHolder<ServerService>>()

	suspend operator fun invoke(method: String, parameters: JsonElement?, context: PipelineContext<Unit, ApplicationCall>): Result<JsonElement?>
	{
		return services[method]?.use { it(parameters, context) } ?: Result.failure(IllegalArgumentException("No service registered for $method"))
	}

	operator fun set(method: String, service: ServerService)
	{
		services[method] = MutexHolder(service)
	}
}

class MutexHolder<T>(private val value: T)
{
	private val mutex = Mutex()

	suspend fun <R> use(block: suspend (T) -> R): R
	{
		return mutex.withLock { block(value) }
	}
}

interface ServerService
{
	suspend operator fun invoke(parameters: JsonElement?, context: PipelineContext<Unit, ApplicationCall>): Result<JsonElement?>

	companion object
	{
		inline fun create(crossinline handler: PipelineContext<Unit, ApplicationCall>.(parameters: JsonElement?) -> Result<JsonElement?>): ServerService
		{
			return object : ServerService
			{
				override suspend operator fun invoke(parameters: JsonElement?, context: PipelineContext<Unit, ApplicationCall>): Result<JsonElement?>
				{
					return context.handler(parameters)
				}
			}
		}
	}
}


