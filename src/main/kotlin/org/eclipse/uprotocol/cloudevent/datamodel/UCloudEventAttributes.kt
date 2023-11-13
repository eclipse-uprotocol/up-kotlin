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

package org.eclipse.uprotocol.cloudevent.datamodel

import java.util.*

/**
 * Specifies the properties that can configure the UCloudEvent.
 */
class UCloudEventAttributes {
    private val hash: String?
    private val priority: Priority?
    private val ttl: Int?
    private val token: String?

    /**
     * Construct the properties object.
     *
     * @param hash     an HMAC generated on the data portion of the CloudEvent message using the device key.
     * @param priority uProtocol Prioritization classifications defined at QoS in SDV-202.
     * @param ttl      How long this event should live for after it was generated (in milliseconds).
     * Events without this attribute (or value is 0) MUST NOT timeout.
     * @param token    Oauth2 access token to perform the access request defined in the request message.
     */
    private constructor(hash: String?, priority: Priority?, ttl: Int?, token: String?) {
        this.hash = hash
        this.priority = priority
        this.ttl = ttl
        this.token = token
    }

    private constructor(builder: UCloudEventAttributesBuilder) {
        hash = builder.hash
        priority = builder.priority
        ttl = builder.ttl
        token = builder.token
    }

    val isEmpty: Boolean
        /**
         * Indicates that there are no added additional attributes to configure when building a CloudEvent.
         * @return Returns true if this attributes container is an empty container and has no valuable information in building a CloudEvent.
         */
        get() = hash().isEmpty && priority().isEmpty && ttl().isEmpty && token().isEmpty

    /**
     * An HMAC generated on the data portion of the CloudEvent message using the device key.
     * @return Returns an Optional hash attribute.
     */
    fun hash(): Optional<String> {
        return if (hash.isNullOrBlank()) Optional.empty() else Optional.of(hash)
    }

    /**
     * uProtocol Prioritization classifications defined at QoS in SDV-202.
     * @return Returns an Optional priority attribute.
     */
    fun priority(): Optional<Priority> {
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
     * Builder for constructing the UCloudEventAttributes.
     */
    class UCloudEventAttributesBuilder {
        var hash: String? = null
        var priority: Priority? = null
        var ttl: Int? = null
        var token: String? = null

        /**
         * add an HMAC generated on the data portion of the CloudEvent message using the device key.
         * @param hash an HMAC generated on the data portion of the CloudEvent message using the device key.
         * @return Returns the UCloudEventAttributesBuilder with the configured hash.
         */
        fun withHash(hash: String?): UCloudEventAttributesBuilder {
            this.hash = hash
            return this
        }

        /**
         * add a uProtocol Prioritization classifications defined at QoS in SDV-202.
         * @param priority uProtocol Prioritization classifications defined at QoS in SDV-202.
         * @return Returns the UCloudEventAttributesBuilder with the configured priority.
         */
        fun withPriority(priority: Priority?): UCloudEventAttributesBuilder {
            this.priority = priority
            return this
        }

        /**
         * add a time to live which is how long this event should live for after it was generated (in milliseconds).
         * Events without this attribute (or value is 0) MUST NOT timeout.
         * @param ttl How long this event should live for after it was generated (in milliseconds).
         * Events without this attribute (or value is 0) MUST NOT timeout.
         * @return Returns the UCloudEventAttributesBuilder with the configured time to live.
         */
        fun withTtl(ttl: Int?): UCloudEventAttributesBuilder {
            this.ttl = ttl
            return this
        }

        /**
         * Add an Oauth2 access token to perform the access request defined in the request message.
         * @param token An Oauth2 access token to perform the access request defined in the request message.
         * @return Returns the UCloudEventAttributesBuilder with the configured OAuth token.
         */
        fun withToken(token: String?): UCloudEventAttributesBuilder {
            this.token = token
            return this
        }

        /**
         * Construct the UCloudEventAttributes from the builder.
         * @return Returns a constructed UProperty.
         */
        fun build(): UCloudEventAttributes {
            // validation if needed
            return UCloudEventAttributes(this)
        }
    }

    /**
     * Priority according to SDV 202 Quality of Service (QoS) and Prioritization.
     */
    enum class Priority(private val qosString: String) {
        // Low Priority. No bandwidth assurance such as File Transfer.
        LOW("CS0"),

        // Standard, undifferentiated application such as General (unclassified).
        STANDARD("CS1"),

        // Operations, Administration, and Management such as Streamer messages (sub, connect, etc…)
        OPERATIONS("CS2"),

        // Multimedia streaming such as Video Streaming
        MULTIMEDIA_STREAMING("CS3"),

        // Real-time interactive such as High priority (rpc events)
        REALTIME_INTERACTIVE("CS4"),

        // Signaling such as Important
        SIGNALING("CS5"),

        // Network control such as Safety Critical
        NETWORK_CONTROL("CS6");

        fun qosString(): String {
            return qosString
        }
    }

    @Override
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass !== other.javaClass) return false
        val that = other as UCloudEventAttributes
        return Objects.equals(hash, that.hash) && priority == that.priority && Objects.equals(
            ttl,
            that.ttl
        ) && Objects.equals(token, that.token)
    }

    @Override
    override fun hashCode(): Int {
        return Objects.hash(hash, priority, ttl, token)
    }

    @Override
    override fun toString(): String {
        return "UCloudEventAttributes{" +
                "hash='" + hash + '\'' +
                ", priority=" + priority +
                ", ttl=" + ttl +
                ", token='" + token + '\'' +
                '}'
    }

    companion object {
        private val EMPTY = UCloudEventAttributes(null, null, null, null)

        /**
         * Static factory method for creating an empty  cloud event attributes object, to avoid working with null<br></br>
         * @return Returns an empty  cloud event attributes that indicates
         * that there are no added additional attributes to configure.
         */
        fun empty(): UCloudEventAttributes {
            return EMPTY
        }
    }
}
