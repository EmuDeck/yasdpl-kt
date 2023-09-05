package yasdpl

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.errors.*
import kotlinx.serialization.json.JsonElement

suspend fun sendRecvPacket(
	packet: Packet,
	port: Short
): Result<Packet> {
	val client = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }
	val response = client.post("http://localhost:$port/yasdpl/call") {
		contentType(ContentType.Application.Json)
        header(HttpHeaders.AccessControlAllowOrigin, "*")
		setBody(packet)
	}
    if (response.status != HttpStatusCode.OK) {
        return Result.failure(IOException("Error: ${response.status}"))
    }
	return Result.success(response.body())
}


suspend fun sendCall(
	packet: Packet,
	port: Short
): Result<JsonElement?>
{
    return sendRecvPacket(packet, port).mapCatching { pack ->
        when (pack)
        {
            is Packet.CallResponse -> pack.response.response
            else -> throw IOException("Error: $pack")
        }
    }
}