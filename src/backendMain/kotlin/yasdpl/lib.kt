package yasdpl

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.cors.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.util.pipeline.*
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
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

class Yasdpl(port: Int) {
    private val services = ServiceRegistry()

    @Suppress("ExtractKtorModule")
    private val engine: ApplicationEngine = embeddedServer(CIO, port = port) {
        install(WebSockets)
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
            post("/yasdpl/call") {
                processBody()
            }
        }
    }

//    private val lastId = atomic(0)

    private suspend fun PipelineContext<Unit, ApplicationCall>.processBody() {
        val packet: Packet = call.receive()
        val response = handleCall(packet)
        call.respond(response)
    }

    private suspend fun PipelineContext<Unit, ApplicationCall>.handleCall(packet: Packet): Packet {
        return when (packet) {
            is Packet.Call -> {
//                val lastId = lastId.value
//                when {
//                    lastId == 0 -> {
//                        this@Yasdpl.lastId.value = packet.call.id
//                    }
//
//                    packet.call.id < MAX_ID_DIFFERENCE -> {
//                        this@Yasdpl.lastId.value = packet.call.id
//                    }
//
//                    packet.call.id > lastId && packet.call.id - lastId < MAX_ID_DIFFERENCE -> {
//                        this@Yasdpl.lastId.value = packet.call.id
//                    }
//
//                    packet.call.id < lastId && lastId - packet.call.id < MAX_ID_DIFFERENCE -> {
//
//                    }
//
//                    else -> {
//                        println("Got USDPL call with strange ID! got:${packet.call.id} last id:${lastId} (in release mode this packet will be rejected)")
//                    }
//                }
                val result = services(packet.call.function, packet.call.parameters, this)
                if (result.isSuccess) {
                    return Packet.CallResponse(
                        RemoteCallResponse(
                            response = result.getOrThrow()
                        )
                    )
                } else {
                    return Packet.Invalid
                }
            }

            is Packet.Many -> {
                val result = arrayListOf<Packet>()
                for (child in packet.children) {
                    result.add(handleCall(child))
                }
                return Packet.Many(result)
            }

//            is Packet.Cors -> {
//
//                return Packet.Invalid
//            }

            else -> Packet.Invalid
        }
    }

    operator fun set(function: String, service: ServerService) {
        services[function] = service
    }

    @Deprecated(
        "Use the operators, the old functions are only still here for backwards compatibility",
        ReplaceWith("operator fun set(function: String, service: ServerService)")
    )
    fun register(function: String, service: ServerService) {
        this[function] = service
    }

    suspend operator fun invoke(): Result<Unit> = coroutineScope {
        launch {
            engine.start(true)
        }.join()
        return@coroutineScope Result.success(Unit)
    }

    @Deprecated(
        "Use the operators, the old functions are only still here for backwards compatibility",
        ReplaceWith("suspend operator fun invoke()")
    )
    suspend fun run(): Result<Unit> = coroutineScope {
        invoke()
    }

    companion object {
        const val MAX_ID_DIFFERENCE = 32
    }

}

