@file:OptIn(ExperimentalJsExport::class)

package yasdpl

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonObject
import kotlin.js.Promise

@JsExport
open class YasdplInstance(val port: Short = 31338) {
    @OptIn(DelicateCoroutinesApi::class)
    fun <P,R> yasdplCallBackendPromise(name: String, parameters: P): Promise<R?>
    {
        val params = parameters?.toJsonDynamic()
        val packet: Packet = Packet.Call(RemoteCall(name, params))
        return Promise { resolve, reject ->
            GlobalScope.launch {
                sendCall(packet, port).map { it?.jsonObject?.fromJsonDynamic()?.asDynamic() }.fold(resolve, reject)
            }
        }
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



