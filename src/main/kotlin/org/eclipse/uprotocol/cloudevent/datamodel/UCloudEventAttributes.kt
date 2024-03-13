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

/**
 * Specifies the properties that can configure the UCloudEvent.
 * @param hash An HMAC generated on the data portion of the CloudEvent message using the device key.
 * @param priority uProtocol Prioritization classifications.
 * @param ttl      How long this event should live for after it was generated (in milliseconds).
 * Events without this attribute (or value is 0) MUST NOT timeout.
 * @param token    Oauth2 access token to perform the access request defined in the request message.
 * @param traceparent An identifier used to correlate observability across related events.
 */
data class UCloudEventAttributes internal constructor(
    val hash: String? = null,
    val priority: UPriority? = null,
    val ttl: Int? = null,
    val token: String? = null,
    val traceparent: String? = null
) {
    private constructor(builder: UCloudEventAttributesBuilder) : this(
        if (builder.hash.isNullOrBlank()) null else builder.hash,
        builder.priority,
        builder.ttl,
        if (builder.token.isNullOrBlank()) null else builder.token,
        builder.traceparent
    )

    /**
     * An HMAC generated on the data portion of the CloudEvent message using the device key.
     */
    val isEmpty: Boolean
        /**
         * Indicates that there are no added additional attributes to configure when building a CloudEvent.
         * @return Returns true if this attributes container is an empty container and has no valuable information in building a CloudEvent.
         */
        get() = hash == null && priority == null && ttl == null && token == null && traceparent == null

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
