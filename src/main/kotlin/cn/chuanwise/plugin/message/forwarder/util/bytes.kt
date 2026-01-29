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

package cn.chuanwise.plugin.message.forwarder.util

fun ByteArray.prefix(length: Int): ByteArray {
    if (this.size <= length) {
        return this
    }
    return this.copyOf(length)
}

fun ByteArray.suffix(length: Int): ByteArray {
    if (this.size <= length) {
        return this
    }
    return this.copyOfRange(this.size - length, this.size)
}

fun ByteArray.introduce(prefix: Int, suffix: Int): String {
    if (this.size <= prefix + suffix) {
        return "content: ${toHexString()}"
    }
    return "prefix ($prefix bytes): ${prefix(prefix).toHexString()} ... suffix ($suffix bytes): ${suffix(suffix).toHexString()}"
}