package usdpl.rpc

import io.ktor.server.application.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ServiceRegistry
{
	private val services = mutableMapOf<String, MutexHolder<ServerService>>()

	suspend operator fun invoke(descriptor: String, method: String, context: PipelineContext<Unit, ApplicationCall>)
	{
		services[descriptor]?.use { it(method, context) }
	}

	operator fun set(descriptor: String, service: ServerService)
	{
		services[descriptor] = MutexHolder(service)
	}
}

class MutexHolder<T>(private val value: T)
{
	private val mutex = Mutex()

	suspend fun use(block: suspend (T) -> Unit)
	{
		mutex.withLock { block(value) }
	}
}

interface ServerService
{
	suspend operator fun invoke(method: String, context: PipelineContext<Unit, ApplicationCall>)

	companion object
	{
		inline fun create(crossinline handler: PipelineContext<Unit, ApplicationCall>.(method: String) -> Unit): ServerService
		{
			return object : ServerService
			{
				override suspend operator fun invoke(method: String, context: PipelineContext<Unit, ApplicationCall>)
				{
					context.handler(method)
				}
			}
		}
	}
}

fun test()
{
	ServerService.create { method ->

	}
}


