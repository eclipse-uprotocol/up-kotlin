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

package org.eclipse.uprotocol.cloudevent.datamodel

import org.eclipse.uprotocol.v1.UPriority
import java.util.*

/**
 * Specifies the properties that can configure the UCloudEvent.
 * @param hash     an HMAC generated on the data portion of the CloudEvent message using the device key.
 * @param priority uProtocol Prioritization classifications.
 * @param ttl      How long this event should live for after it was generated (in milliseconds).
 * Events without this attribute (or value is 0) MUST NOT timeout.
 * @param token    Oauth2 access token to perform the access request defined in the request message.
 */
data class UCloudEventAttributes internal constructor(
    private val hash: String? = null,
    private val priority: UPriority? = null,
    private val ttl: Int? = null,
    private val token: String? = null,
    private val traceparent: String? = null
) {
    private constructor(builder: UCloudEventAttributesBuilder) : this(
        builder.hash,
        builder.priority,
        builder.ttl,
        builder.token,
        builder.traceparent
    )

    val isEmpty: Boolean
        /**
         * Indicates that there are no added additional attributes to configure when building a CloudEvent.
         * @return Returns true if this attributes container is an empty container and has no valuable information in building a CloudEvent.
         */
        get() = hash().isEmpty && priority().isEmpty && ttl().isEmpty && token().isEmpty && traceparent().isEmpty

    /**
     * An HMAC generated on the data portion of the CloudEvent message using the device key.
     * @return Returns an Optional hash attribute.
     */
    fun hash(): Optional<String> {
        return if (hash.isNullOrBlank()) Optional.empty() else Optional.of(hash)
    }

    /**
     * uProtocol Prioritization classifications.
     * @return Returns an Optional priority attribute.
     */
    fun priority(): Optional<UPriority> {
        return if (priority == null) Optional.empty() else Optional.of(priority)
    }

    /**
     * How long this event should live for after it was generated (in milliseconds).
     * @return Returns an Optional time to live attribute.
     */
    fun ttl(): Optional<Int> {
        return if (ttl == null) Optional.empty() else Optional.of(ttl)
    }

    /**
     * Oauth2 access token to perform the access request defined in the request message.
     * @return Returns an Optional OAuth token attribute.
     */
    fun token(): Optional<String> {
        return if (token.isNullOrBlank()) Optional.empty() else Optional.of(token)
    }


    /**
     * An identifier used to correlate observability across related events.
     * @return Returns an Optional traceparent attribute.
     */
    fun traceparent(): Optional<String> {
        return if (traceparent.isNullOrBlank()) Optional.empty() else Optional.of(traceparent)
    }

    /**
     * Builder for constructing the UCloudEventAttributes.
     */
    class UCloudEventAttributesBuilder @PublishedApi internal constructor() {
        /**
         * add an HMAC generated on the data portion of the CloudEvent message using the device key.
         */
        var hash: String? = null

        /**
         * add a uProtocol Prioritization classifications.
         */
        var priority: UPriority? = null

        /**
         * add a time to live which is how long this event should live for after it was generated (in milliseconds).
         * Events without this attribute (or value is 0) MUST NOT timeout.
         */
        var ttl: Int? = null

        /**
         * Add an Oauth2 access token to perform the access request defined in the request message.
         */
        var token: String? = null

        /**
         * Add an identifier used to correlate observability across related events.
         */
        var traceparent: String? = null


        /**
         * Construct the UCloudEventAttributes from the builder.
         * @return Returns a constructed UProperty.
         */
        @JvmSynthetic
        @PublishedApi
        internal fun build(): UCloudEventAttributes {
            // validation if needed
            return UCloudEventAttributes(this)
        }
    }

    companion object {
        val EMPTY = UCloudEventAttributes()

        @JvmName("-initializeUCloudEventAttributes")
        inline fun uCloudEventAttributes(block: UCloudEventAttributesBuilder.() -> Unit) =
            UCloudEventAttributesBuilder().apply(block).build()
    }
}
