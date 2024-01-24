/*
 * Copyright (c) 2023 General Motors GTO LLC
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * SPDX-FileType: SOURCE
 * SPDX-FileCopyrightText: 2023 General Motors GTO LLC
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.uprotocol.rpc


/**
 * This class is used when making uRPC calls to pass additional options. Copied from Misha's class.
 * @property timeout a timeout in milliseconds
 * @property token An optional OAuth2 access token
 */
data class CallOptions internal constructor(
    val timeout: Int = TIMEOUT_DEFAULT,
    val token: String = TOKEN_DEFAULT
) {
    private constructor(builder: Builder) : this(builder.timeout, builder.token)

    /**
     * Builder for constructing `CallOptions`.
     * @property timeout a timeout in milliseconds
     * @property token an OAuth2 access token
     */
    class Builder @PublishedApi internal constructor() {
        var timeout = TIMEOUT_DEFAULT
            set(value) {
                field = value.takeIf { it >= 0 } ?: TIMEOUT_DEFAULT
            }
        var token = TOKEN_DEFAULT
            set(value) {
                field = value.ifBlank { TOKEN_DEFAULT }
            }

        /**
         * Construct a `CallOptions` from this builder.
         *
         * @return A constructed `CallOptions`.
         */
        @JvmSynthetic
        @PublishedApi
        internal fun build(): CallOptions {
            return CallOptions(this)
        }
    }

    companion object {
        /**
         * Default timeout of a call in milliseconds.
         */
        const val TIMEOUT_DEFAULT = 10000

        /**
         * Default token
         */
        const val TOKEN_DEFAULT = ""

        /**
         * Default instance.
         */
        val DEFAULT = CallOptions(TIMEOUT_DEFAULT, TOKEN_DEFAULT)

        @JvmName("-initializeCallOptions")
        inline fun callOptions(block: Builder.() -> Unit) =
            Builder().apply(block).build()
    }
}
