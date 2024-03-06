/*
 * Copyright (c) 2024 General Motors GTO LLC
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

import java.util.Objects
import java.util.Optional

/**
 * This class is used when making uRPC calls to pass additional options. Copied from Misha's class.
 */
class CallOptions private constructor(private val mTimeout: Int, token: String?) {
    private val mToken: String

    private constructor(builder: Builder) : this(builder.mTimeout, builder.mToken)

    init {
        mToken = token ?: ""
    }

    /**
     * Get a timeout.
     *
     * @return A timeout in milliseconds.
     */
    fun timeout(): Int {
        return mTimeout
    }

    /**
     * Get an OAuth2 access token.
     *
     * @return An Optional OAuth2 access token.
     */
    fun token(): Optional<String> {
        return if (mToken.isBlank()) Optional.empty() else Optional.of(mToken)
    }

    /**
     * Builder for constructing `CallOptions`.
     */
    class Builder {
        var mTimeout = TIMEOUT_DEFAULT
        var mToken = ""

        /**
         * Add a timeout.
         *
         * @param timeout A timeout in milliseconds.
         * @return This builder.
         */
        fun withTimeout(timeout: Int): Builder {
            mTimeout = if (timeout <= 0) TIMEOUT_DEFAULT else timeout
            return this
        }

        /**
         * Add an OAuth2 access token.
         *
         * @param token An OAuth2 access token.
         * @return This builder.
         */
        fun withToken(token: String): Builder {
            mToken = token
            return this
        }

        /**
         * Construct a `CallOptions` from this builder.
         *
         * @return A constructed `CallOptions`.
         */
        fun build(): CallOptions {
            return CallOptions(this)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass !== other.javaClass) return false
        val that = other as CallOptions
        return mTimeout == that.mTimeout && Objects.equals(mToken, that.mToken)
    }

    override fun hashCode(): Int {
        return Objects.hash(mTimeout, mToken)
    }

    override fun toString(): String {
        return "CallOptions{" +
                "mTimeout=" + mTimeout +
                ", mToken='" + mToken + '\'' +
                '}'
    }

    companion object {
        /**
         * Default timeout of a call in milliseconds.
         */
        const val TIMEOUT_DEFAULT = 10000

        /**
         * Default instance.
         */
        val DEFAULT = CallOptions(TIMEOUT_DEFAULT, "")

        /**
         * Constructs a new builder.
         *
         * @return A builder.
         */
        fun newBuilder(): Builder {
            return Builder()
        }
    }
}
