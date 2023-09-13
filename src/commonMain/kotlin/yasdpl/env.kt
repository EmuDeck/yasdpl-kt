package yasdpl

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
interface Env
{
    val version: String
    val user: String
    val home: String
    val settingsDir: String
    val runtimeDir: String
    val logDir: String
    val pluginDir: String
    val pluginName: String
    val pluginVersion: String
    val pluginAuthor: String
}