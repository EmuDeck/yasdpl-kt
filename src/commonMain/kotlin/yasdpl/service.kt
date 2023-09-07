package yasdpl

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable

interface Service<C>
{
    val services: Map<String, suspend C.() -> Result<Unit>>

    suspend operator fun invoke(method: String, context: C): Result<Unit>

    companion object
    {
        private fun <C> create(services: Map<String, suspend C.() -> Result<Unit>>, handler: suspend C.(method: String) -> Result<Unit>): Service<C>
        {
            return object : Service<C>
            {
                override suspend operator fun invoke(method: String, context: C): Result<Unit>
                {
                    return context.handler(method)
                }

                override val services: Map<String, suspend C.() -> Result<Unit>> = services
            }
        }

        fun <C> create(handler: suspend C.(method: String) -> Result<Unit>): Service<C> = create(emptyMap(), handler)

        fun <C> builder(func: ServiceBuilder<C>.() -> Unit): Service<C>
        {
            val builder = ServiceBuilder<C>()
            builder.func()
            return builder.build()
        }

        class ServiceBuilder<C>
        {
            private val services = mutableMapOf<String, suspend C.() -> Result<Unit>>()

            fun method(method: String, handler: suspend C.() -> Result<Unit>)
            {
                services[method] = handler
            }

            fun build(): Service<C> {
                return create(services) { method ->
                    val service = services[method]?: return@create Result.failure(IllegalArgumentException("No service registered for $method"))
                    return@create service()
                }
            }
        }
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

@Serializable
data class BindingCall(val method: String)