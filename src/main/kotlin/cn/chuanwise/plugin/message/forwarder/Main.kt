/*
 * Copyright 2026 Chuanwise.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.chuanwise.plugin.message.forwarder

import cn.chuanwise.plugin.message.forwarder.util.info
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PluginMessageEvent
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import org.slf4j.Logger
import java.nio.file.Path

@Plugin(
    id = "plugin-message-forwarder",
    name = "Plugin Message Forwarder",
    version = "0.1.0",
    authors = ["Chuanwise"]
)
class Main @Inject constructor(
    val server: ProxyServer,
    val logger: Logger,
    @param:DataDirectory val dataDirectoryPath: Path
) {
    companion object {
        lateinit var instance: Main
    }

    @Subscribe
    fun onProxyInit(event: ProxyInitializeEvent) {
        instance = this

        Runtimes.flush()
        info { "Runtime flushed!" }

        server.commandManager.apply {
            val meta = metaBuilder("plugin-message-forwarder")
                .aliases("pmf")
                .plugin(this@Main)
                .build()

            register(meta, Command)
        }
        info { "Command /plugin-message-forwarder registered!" }

        info { "Plugin enabled successfully!" }
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        Runtimes.close()
        info { "Plugin disabled successfully!" }
    }

    @Subscribe
    fun onPluginMessage(event: PluginMessageEvent) {
        Runtimes.onPluginMessage(event)
    }
}