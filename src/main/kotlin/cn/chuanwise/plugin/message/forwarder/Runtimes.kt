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
import com.velocitypowered.api.event.connection.PluginMessageEvent
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ServerConnection
import com.velocitypowered.api.proxy.messages.ChannelMessageSink
import com.velocitypowered.api.proxy.messages.ChannelMessageSource
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier

object Runtimes {
    private val mapper = YAMLMapper
        .builder()
        .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
        .addModule(KotlinModule.Builder().build())
        .build()

    private var config: Config? = null
    private var channels: Set<MinecraftChannelIdentifier>? = null

    private fun flushConfig() {
        val configFile = Main.instance.dataDirectoryPath.resolve("config.yml").toFile()
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
    }

    private fun flushChannels() {
        closeChannels()

        val config = config ?: return
        if (!config.enable) {
            info { "Plugin Message Forwarder is disabled in config." }
            return
        }

        channels = config.channels
            .map { MinecraftChannelIdentifier.from(it) }
            .toSet()

        for (string in config.channels) {
            Main.instance.server.channelRegistrar.register(MinecraftChannelIdentifier.from(string))
        }
        info { "Register ${config.channels.size} channel(s): ${config.channels}." }
    }

    fun flush() {
        flushConfig()
        flushChannels()
    }

    fun close() {
        closeChannels()
    }

    private fun closeChannels() {
        val channels = channels ?: return

        Main.instance.server.channelRegistrar.unregister(*channels.toTypedArray())
        info { "Unregister ${channels.size} channel(s): ${channels}." }

        this.channels = null
    }

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