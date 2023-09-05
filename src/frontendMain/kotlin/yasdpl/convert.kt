package yasdpl

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.*
import kotlinx.serialization.serializer

fun <T> toJson(serializer: KSerializer<T>, value: T): JsonObject = Json.encodeToJsonElement(serializer, value).jsonObject

inline fun <reified T> T.toJson(): JsonObject
{
	return toJson(serializer(), this)
}

@OptIn(ExperimentalSerializationApi::class)
fun Any.toJsonDynamic(): JsonObject
{
	return Json.decodeFromDynamic(this)
}

fun <T> fromJson(serializer: KSerializer<T>, json: JsonObject): T = Json.decodeFromJsonElement(serializer, json)

inline fun <reified T> JsonObject.fromJson(): T
{
	return fromJson(serializer(), this)
}

@OptIn(ExperimentalSerializationApi::class)
fun JsonObject.fromJsonDynamic(): Any
{
    return Json.encodeToDynamic(this) as Any
}

@ExperimentalJsExport
@Suppress("NON_EXPORTABLE_TYPE")
@JsExport
fun fromJson(json: JsonObject): Any
{
    return json.fromJsonDynamic()
}

@ExperimentalJsExport
@Suppress("NON_EXPORTABLE_TYPE")
@JsExport
fun toJson(value: Any): JsonObject
{
    return value.toJsonDynamic()
}