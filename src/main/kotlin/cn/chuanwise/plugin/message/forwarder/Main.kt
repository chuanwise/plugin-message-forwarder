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

import cn.chuanwise.plugin.message.forwarder.util.error
import cn.chuanwise.plugin.message.forwarder.util.info
import cn.chuanwise.plugin.message.forwarder.util.introduce
import cn.chuanwise.plugin.message.forwarder.util.warning
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PluginMessageEvent
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.ServerConnection
import com.velocitypowered.api.proxy.messages.ChannelMessageSink
import com.velocitypowered.api.proxy.messages.ChannelMessageSource
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import org.slf4j.Logger
import java.nio.file.Path

@Plugin(
    id = "plugin-message-forwarder",
    name = "Plugin Message Forwarder",
    version = "0.1.0-SNAPSHOT",
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

    private val mapper = YAMLMapper
        .builder()
        .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
        .addModule(KotlinModule.Builder().build())
        .build()

    private var config: Config? = null
    private var channels: Set<MinecraftChannelIdentifier>? = null

    @Subscribe
    fun onProxyInitialize(event: ProxyInitializeEvent) {
        instance = this

        val configFile = dataDirectoryPath.resolve("config.yml").toFile()
        if (configFile.isFile) {
            config = try {
                mapper.readValue<Config>(configFile)
            } catch (e: Throwable) {
                error { "Fail to load config from file: $configFile: $e" }
                e.printStackTrace()
                return
            }

            info { "Config file $configFile loaded" }
        } else {
            warning { "Config file $configFile doesn't exist, function disabled." }
            return
        }

        val config = config ?: return
        if (!config.enable) {
            info { "Plugin Message Forwarder is disabled in config." }
            return
        }

        channels = config.channels
            .map { MinecraftChannelIdentifier.from(it) }
            .toSet()

        for (string in config.channels) {
            server.channelRegistrar.register(MinecraftChannelIdentifier.from(string))
        }

        info { "Register ${config.channels.size} channel(s): ${config.channels}." }
        info { "Plugin enabled successfully!" }
    }

    @Subscribe
    fun onPluginMessage(event: PluginMessageEvent) {
        val channels = channels ?: return

        if (event.identifier !in channels) {
            return
        }

        event.result = PluginMessageEvent.ForwardResult.forward()

        val data = event.data
        val intro = data.introduce(prefix = 4, suffix = 4)
        info { "Forward message (${data.size} bytes, $intro) from ${event.source.introduce()} to ${event.target.introduce()}." }
    }

    private fun ChannelMessageSource.introduce(): String {
        return when (this) {
            is ServerConnection -> "backend server ${serverInfo.name} (${serverInfo.address})"
            is Player -> "player $username ($uniqueId)"
            else -> "source $this"
        }
    }

    private fun ChannelMessageSink.introduce(): String {
        return when (this) {
            is ServerConnection -> "backend server ${serverInfo.name} (${serverInfo.address})"
            is Player -> "player $username ($uniqueId)"
            else -> "source $this"
        }
    }
}