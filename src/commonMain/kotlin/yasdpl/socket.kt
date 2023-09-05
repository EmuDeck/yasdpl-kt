package yasdpl

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator


/// Accepted Packet types and the data they contain
@Serializable
sealed class Packet
{
	/// A remote call
	@Serializable
	data class Call(val call: RemoteCall) : Packet()

	/// A response to a remote call
	@Serializable
	data class CallResponse(val response: RemoteCallResponse) : Packet()

//    @Serializable
//    data class Cors() : Packet()

	/// Unused
	@Serializable
	data object KeepAlive : Packet()

	/// Invalid
	@Serializable
	data object Invalid : Packet()

	/// General message
	@Serializable
	data class Message(val message: String) : Packet()

	/// Response to an unsupported packet
	@Serializable
	data object Unsupported : Packet()

	/// Broken packet type, useful for testing
	@Serializable
	data object Bad : Packet()

	/// Many packets merged into one
	@Serializable
	data class Many(val children: List<Packet>) : Packet()

	/// Translation data dump
	@Serializable
	data class Translations(val data: Map<String, List<String>>) : Packet()

	/// Request translations for language
	@Serializable
	data class Language(val language: String) : Packet()
}