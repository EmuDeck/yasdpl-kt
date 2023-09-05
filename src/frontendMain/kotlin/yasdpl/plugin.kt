//package yasdpl
//
//import dfl.DefinePluginFn
//import dfl.Plugin
//import dfl.ServerAPI
//import react.*
//
//
//abstract class DeckyPlugin
//{
//
//    protected abstract fun ChildrenBuilder.title()
//    protected abstract fun ChildrenBuilder.icon()
//    protected abstract fun ChildrenBuilder.content()
//    protected abstract val alwaysRender: Boolean
//    protected abstract val yasdplPort: Short
//    protected abstract fun load(serverAPI: YasdplAPI)
//
//    protected abstract fun unload(serverAPI: YasdplAPI)
//
////    fun define(): DefinePluginFn {
////        return dfl.definePlugin { serverAPI ->
////            val yasdplAPI = YasdplAPI(yasdplPort, serverAPI)
////            this.load(yasdplAPI)
////            val plugin = object : Plugin {
////                override val title = JSX { title() }
////                override val icon = JSX { icon() }
////                override val content = JSX { content() }
////                override val alwaysRender = this@DeckyPlugin.alwaysRender
////                override val onDismount = {
////                    this@DeckyPlugin.unload(yasdplAPI)
////                }
////            }
////            return@definePlugin plugin
////        }
////    }
//}