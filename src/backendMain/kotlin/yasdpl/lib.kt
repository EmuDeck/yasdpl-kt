package yasdpl

import io.github.oshai.kotlinlogging.ConsoleOutputAppender
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.cors.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation

fun CORSConfig.allowAnyHeader(includeUnsafe: Boolean = false) {
    HttpHeaders.safeHeader.forEach { allowHeader(it) }
    if(includeUnsafe)
        HttpHeaders.unsafeHeader.forEach { allowHeader(it) }
}

fun CORSConfig.exposeAnyHeader(includeUnsafe: Boolean = false) {
    HttpHeaders.safeHeader.forEach { exposeHeader(it) }
    if(includeUnsafe)
        HttpHeaders.unsafeHeader.forEach { exposeHeader(it) }
}

val HttpHeaders.safeHeader
    get() = allHeaders.filter { !isUnsafe(it) }

val HttpHeaders.unsafeHeader
    get() = allHeaders.filter { isUnsafe(it) }

val HttpHeaders.allHeaders
    get() = listOf(
        Accept, AcceptCharset, AcceptEncoding, AcceptLanguage, AcceptRanges,
        Age, Allow, ALPN, AuthenticationInfo, Authorization, CacheControl, Connection,
        ContentDisposition, ContentEncoding, ContentLanguage, ContentLength, ContentLocation,
        ContentRange, ContentType, Cookie, DASL, Date, DAV, Depth, Destination, ETag, Expect,
        Expires, From, Forwarded, Host, HTTP2Settings, If, IfMatch, IfModifiedSince,
        IfNoneMatch, IfRange, IfScheduleTagMatch, IfUnmodifiedSince, LastModified,
        Location, LockToken, Link, MaxForwards, MIMEVersion, OrderingType, Origin,
        Overwrite, Position, Pragma, Prefer, PreferenceApplied, ProxyAuthenticate,
        ProxyAuthenticationInfo, ProxyAuthorization, PublicKeyPins, PublicKeyPinsReportOnly,
        Range, Referrer, RetryAfter, ScheduleReply, ScheduleTag, SecWebSocketAccept,
        SecWebSocketExtensions, SecWebSocketKey, SecWebSocketProtocol, SecWebSocketVersion,
        Server, SetCookie, SLUG, StrictTransportSecurity, TE, Timeout, Trailer,
        TransferEncoding, Upgrade, UserAgent, Vary, Via, Warning, WWWAuthenticate,
        AccessControlAllowOrigin, AccessControlAllowMethods, AccessControlAllowCredentials,
        AccessControlAllowHeaders, AccessControlRequestMethod, AccessControlRequestHeaders,
        AccessControlExposeHeaders, AccessControlMaxAge, XHttpMethodOverride, XForwardedHost,
        XForwardedServer, XForwardedProto, XForwardedFor, XRequestId, XCorrelationId
    )

abstract class WebsocketServer(port: Short = 31338) {
    private val logger = KotlinLogging.logger("Backend").apply {
        KotlinLoggingConfiguration.appender = MultiAppender(listOf(ConsoleOutputAppender, FileAppender, WebsocketAppender(this@WebsocketServer)))
        KotlinLoggingConfiguration.formatter = BackendFormatter
    }

    private var closed = false

    private val services = ServiceRegistry().apply { initServicesInternal() }

    private val bindingQueue = mutableMapOf<String, Channel<QueueEntry>>()

    @Suppress("ExtractKtorModule")
    private val engine: ApplicationEngine = embeddedServer(CIO, port = port.toInt()) {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
        install(ServerContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
        install(CORS) {
            allowAnyHeader()
            exposeAnyHeader()
            allowCredentials = true
            allowHost("steamloopback.host", schemes = listOf("http", "https"))
        }
        routing {
            webSocket("/{service}/{method}") {
                val service = call.parameters["service"]
                val method = call.parameters["method"]
                if (service !== null && method !== null)
                    this@WebsocketServer.services(service, method, this)
                else
                    throw IllegalStateException("service and method must not be null")
            }
        }
    }

//    private val lastId = atomic(0)

//    private suspend fun PipelineContext<Unit, ApplicationCall>.processBody() {
//        val packet: Packet = call.receive()
//        val response = handleCall(packet)
//        call.respond(response)
//    }
//
//    private suspend fun PipelineContext<Unit, ApplicationCall>.handleCall(packet: Packet): Packet {
//        return when (packet) {
//            is Packet.Call -> {
////                val lastId = lastId.value
////                when {
////                    lastId == 0 -> {
////                        this@Yasdpl.lastId.value = packet.call.id
////                    }
////
////                    packet.call.id < MAX_ID_DIFFERENCE -> {
////                        this@Yasdpl.lastId.value = packet.call.id
////                    }
////
////                    packet.call.id > lastId && packet.call.id - lastId < MAX_ID_DIFFERENCE -> {
////                        this@Yasdpl.lastId.value = packet.call.id
////                    }
////
////                    packet.call.id < lastId && lastId - packet.call.id < MAX_ID_DIFFERENCE -> {
////
////                    }
////
////                    else -> {
////                        println("Got USDPL call with strange ID! got:${packet.call.id} last id:${lastId} (in release mode this packet will be rejected)")
////                    }
////                }
//                val result = services(packet.call.function, packet.call.parameters, this)
//                if (result.isSuccess) {
//                    return Packet.CallResponse(
//                        RemoteCallResponse(
//                            response = result.getOrThrow()
//                        )
//                    )
//                } else {
//                    return Packet.Invalid
//                }
//            }
//
//            is Packet.Many -> {
//                val result = arrayListOf<Packet>()
//                for (child in packet.children) {
//                    result.add(handleCall(child))
//                }
//                return Packet.Many(result)
//            }
//
////            is Packet.Cors -> {
////
////                return Packet.Invalid
////            }
//
//            else -> Packet.Invalid
//        }
//    }

    private fun ServiceRegistry.initServicesInternal()
    {
        initServices()
        register("bind") {
            val binds = buildList {
                initBindsInternal()
            }
            for (bind in binds) {
                method(bind)
                {
                    var result = Result.success(Unit)
                    while (!closed) {
                        val (method, callback) = bindingQueue.getOrPut(bind) { Channel() }.receive()
                        sendSerialized(BindingCall(method))
                        val ret = callback()
                        if (ret.isFailure) {
                            result = ret
                            break
                        }
                    }
                    return@method result
                }
            }
        }
        register("log") {
            method("info") {
                val name = incoming.receive() as? Frame.Text
                val args = Json.decodeFromString<Array<out Any?>>((incoming.receive() as? Frame.Text)?.readText() ?: "[]")
                val message = args.foldIndexed("") { i: Int, s: String, any: Any? ->
                    s + any.toString() + if (i < args.size - 1) ", " else ""
                }
                if (name!= null) {
                    KotlinLogging.logger(name.readText()).info { message }
                }
                return@method Result.success(Unit)
            }

            method("debug") {
                val name = incoming.receive() as? Frame.Text
                val args = Json.decodeFromString<Array<out Any?>>((incoming.receive() as? Frame.Text)?.readText() ?: "[]")
                val message = args.foldIndexed("") { i: Int, s: String, any: Any? ->
                    s + any.toString() + if (i < args.size - 1) ", " else ""
                }
                if (name!= null) {
                    KotlinLogging.logger(name.readText()).debug { message }
                }
                return@method Result.success(Unit)
            }

            method("error") {
                val name = incoming.receive() as? Frame.Text
                val args = Json.decodeFromString<Array<out Any?>>((incoming.receive() as? Frame.Text)?.readText() ?: "[]")
                val message = args.foldIndexed("") { i: Int, s: String, any: Any? ->
                    s + any.toString() + if (i < args.size - 1) ", " else ""
                }
                if (name!= null) {
                    KotlinLogging.logger(name.readText()).error { message }
                }
                return@method Result.success(Unit)
            }

            method("warn") {
                val name = incoming.receive() as? Frame.Text
                val args = Json.decodeFromString<Array<out Any?>>((incoming.receive() as? Frame.Text)?.readText() ?: "[]")
                val message = args.foldIndexed("") { i: Int, s: String, any: Any? ->
                    s + any.toString() + if (i < args.size - 1) ", " else ""
                }
                if (name!= null) {
                    KotlinLogging.logger(name.readText()).warn { message }
                }
                return@method Result.success(Unit)
            }
        }
        register("env") {
            method("get") {
                sendSerialized<Env>(DeckyEnv)
                return@method Result.success(Unit)
            }
        }
    }

    private fun MutableList<String>.initBindsInternal()
    {
        add("log")
        initBinds()
    }

    abstract fun ServiceRegistry.initServices()

    abstract fun MutableList<String>.initBinds()

    fun bind(service: String, method: String, callback: suspend DefaultWebSocketServerSession.() -> Result<Unit>) {
        runBlocking {
            bindingQueue.getOrPut(service) { Channel() }.send(QueueEntry(method, callback))
        }
    }

    operator fun set(descriptor: String, service: ServerService) {
        services[descriptor] = service
    }

    fun register(descriptor: String, service: Service.Companion.ServiceBuilder<DefaultWebSocketServerSession>.() -> Unit) {
        services.register(descriptor, service)
    }

    suspend operator fun invoke(): Result<Unit> = coroutineScope {
        val thread = launch {
            engine.start(true)
        }
        thread.invokeOnCompletion {
            closed = true
        }
        logger.info { "Backend Started" }
        thread.join()
        return@coroutineScope Result.success(Unit)
    }

//    fun info(message: String) {
//        logger.info { message }
//        bind("log", "info") {
//            send(message)
//            Result.success(Unit)
//        }
//    }
//
//    fun debug(message: String) {
//        logger.debug { message }
//        bind("log", "debug") {
//            send(message)
//            Result.success(Unit)
//        }
//    }
//
//    fun error(message: String, exception: Throwable) {
//        logger.error(exception) { message }
//        bind("log", "error") {
//            send(message)
//            Result.success(Unit)
//        }
//    }
//
//    fun warn(message: String) {
//        logger.warn { message }
//        bind("log", "warn") {
//            send(message)
//            Result.success(Unit)
//        }
//    }
}

data class QueueEntry(val method: String, val handler: suspend DefaultWebSocketServerSession.() -> Result<Unit>)



