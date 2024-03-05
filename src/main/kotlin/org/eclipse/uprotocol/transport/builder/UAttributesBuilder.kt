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

package org.eclipse.uprotocol.transport.builder

import org.eclipse.uprotocol.uuid.factory.UUIDV8
import org.eclipse.uprotocol.v1.*

/**
 * Builder for easy construction of the UAttributes object.
 */
class UAttributesBuilder
/**
 * Construct the UAttributesBuilder with the configurations that are required for every payload transport.
 *
 * @param source   Source address of the message.
 * @param uuid       Unique identifier for the message.
 * @param messageType     Message type such as Publish a state change, RPC request or RPC response.
 * @param uPriority uProtocol Prioritization classifications.
 */ private constructor(
    private val source: UUri,
    private val uuid: UUID,
    private val messageType: UMessageType,
    private val uPriority: UPriority
) {
    private var timeToLive: Int? = null
    private var tokenAttr: String? = null
    private var sinkUUri: UUri? = null
    private var plevel: Int? = null
    private var commStatus: Int? = null
    private var reqId: UUID? = null
    private var traceparentAttr: String? = null

    /**
     * Add the time to live in milliseconds.
     *
     * @param ttl the time to live in milliseconds.
     * @return Returns the UAttributesBuilder with the configured ttl.
     */
    fun withTtl(ttl: Int?): UAttributesBuilder {
        timeToLive = ttl
        return this
    }

    /**
     * Add the authorization token used for TAP.
     *
     * @param token the authorization token used for TAP.
     * @return Returns the UAttributesBuilder with the configured token.
     */
    fun withToken(token: String?): UAttributesBuilder {
        tokenAttr = token
        return this
    }

    /**
     * Add the explicit destination URI.
     *
     * @param sink the explicit destination URI.
     * @return Returns the UAttributesBuilder with the configured sink.
     */
    fun withSink(sink: UUri?): UAttributesBuilder {
        sinkUUri = sink
        return this
    }

    /**
     * Add the permission level of the message.
     *
     * @param plevel the permission level of the message.
     * @return Returns the UAttributesBuilder with the configured plevel.
     */
    fun withPermissionLevel(plevel: Int?): UAttributesBuilder {
        this.plevel = plevel
        return this
    }

    /**
     * Add the communication status of the message.
     *
     * @param commstatus the communication status of the message.
     * @return Returns the UAttributesBuilder with the configured commstatus.
     */
    fun withCommStatus(commstatus: Int?): UAttributesBuilder {
        commStatus = commstatus
        return this
    }

    /**
     * Add the request ID.
     *
     * @param reqid the request ID.
     * @return Returns the UAttributesBuilder with the configured reqid.
     */
    fun withReqId(reqid: UUID?): UAttributesBuilder {
        reqId = reqid
        return this
    }

    /**
     * Add the traceparent.
     *
     * @param traceparent the trace parent.
     * @return Returns the UAttributesBuilder with the configured traceparent.
     */
    fun withTraceparent(traceparent: String): UAttributesBuilder {
        this.traceparentAttr = traceparent
        return this
    }

    /**
     * Construct the UAttributes from the builder.
     *
     * @return Returns a constructed
     */
    fun build(): UAttributes {
        return uAttributes {
            id = uuid
            type = messageType
            priority = uPriority
            sinkUUri?.let { sink = it }
            timeToLive?.let { ttl = it }
            plevel?.let { permissionLevel = it }
            commStatus?.let { commstatus = it }
            reqId?.let { reqid = it }
            tokenAttr?.let { token = it }
            traceparentAttr?.let { traceparent = it }
        }
    }

    companion object {
        /**
         * Construct a UAttributesBuilder for a publish message.
         * @param source   Source address of the message.
         * @param priority The priority of the message.
         * @return Returns the UAttributesBuilder with the configured priority.
         */
        fun publish(source: UUri, priority: UPriority): UAttributesBuilder {
            return UAttributesBuilder(
                source,
                UUIDV8(),
                UMessageType.UMESSAGE_TYPE_PUBLISH, priority
            )
        }

        /**
         * Construct a UAttributesBuilder for a notification message.
         * @param source   Source address of the message.
         * @param sink The destination URI.
         * @param priority The priority of the message.
         * @return Returns the UAttributesBuilder with the configured priority and sink.
         */
        fun notification(source: UUri, sink: UUri, priority: UPriority): UAttributesBuilder {
            return UAttributesBuilder(
                source,
                UUIDV8(),
                UMessageType.UMESSAGE_TYPE_PUBLISH, priority
            ).withSink(sink)
        }

        /**
         * Construct a UAttributesBuilder for a request message.
         * @param source   Source address of the message.
         * @param sink The destination URI.
         * @param priority The priority of the message.
         * @param ttl The time to live in milliseconds.
         * @return Returns the UAttributesBuilder with the configured priority, sink and ttl.
         */
        fun request(source: UUri, sink: UUri, priority: UPriority, ttl: Int): UAttributesBuilder {
            return UAttributesBuilder(
                source,
                UUIDV8(),
                UMessageType.UMESSAGE_TYPE_REQUEST, priority
            ).withTtl(ttl).withSink(sink)
        }

        /**
         * Construct a UAttributesBuilder for a response message.
         * @param source   Source address of the message.
         * @param sink The destination URI.
         * @param priority The priority of the message.
         * @param reqid The original request UUID used to correlate the response to the request.
         * @return Returns the UAttributesBuilder with the configured priority, sink and reqid.
         */
        fun response(source: UUri, sink: UUri, priority: UPriority, reqid: UUID): UAttributesBuilder {
            return UAttributesBuilder(
                source,
                UUIDV8(),
                UMessageType.UMESSAGE_TYPE_RESPONSE, priority
            ).withSink(sink).withReqId(reqid)
        }
    }
}
