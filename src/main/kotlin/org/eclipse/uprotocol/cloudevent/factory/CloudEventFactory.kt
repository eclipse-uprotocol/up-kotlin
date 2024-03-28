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

package org.eclipse.uprotocol.cloudevent.factory

import com.google.protobuf.Any
import com.google.protobuf.Empty
import io.cloudevents.CloudEvent
import io.cloudevents.core.builder.CloudEventBuilder
import org.eclipse.uprotocol.cloudevent.datamodel.UCloudEventAttributes
import org.eclipse.uprotocol.uri.Uri
import org.eclipse.uprotocol.uuid.factory.UUIDV8
import org.eclipse.uprotocol.uuid.serializer.LongUuidSerializer
import org.eclipse.uprotocol.v1.UMessageType
import org.eclipse.uprotocol.v1.UUID
import org.eclipse.uprotocol.v1.UUri
import java.net.URI

/**
 * A factory is a part of the software has methods to generate concrete objects, usually of the same type or interface.<br></br>
 * CloudEvents is a specification for describing events in a common way. We will use CloudEvents
 * to formulate all kinds of  events (messages) that will be sent to and from devices.<br></br>
 * The CloudEvent factory knows how to generate CloudEvents of the 4 core types: req.v1, res.v1, pub.v1, and file.v1<br></br>
 */
object CloudEventFactory {
    /**
     * Create a CloudEvent for an event for the use case of: RPC Request message.
     *
     * @param applicationUriForRPC   The [Uri] for the application requesting the RPC.
     * @param serviceMethodUri       The [Uri] for the method to be called on the service Ex.: :/body.access/1/rpc.UpdateDoor
     * @param protoPayload           Protobuf Any object with the Message command to be executed on the sink service.
     * @param attributes             Additional attributes such as ttl, hash, priority and token.
     * @return Returns an  request CloudEvent.
     */
    fun request(
        applicationUriForRPC: Uri,
        serviceMethodUri: Uri,
        protoPayload: Any,
        attributes: UCloudEventAttributes
    ): CloudEvent {
        val id = generateCloudEventId()
        return buildBaseCloudEvent(
            id,
            applicationUriForRPC,
            protoPayload.toByteArray(),
            protoPayload.typeUrl,
            attributes
        )
            .withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_REQUEST))
            .withExtension("sink", URI.create(serviceMethodUri.get()))
            .build()
    }

    /**
     * Create a CloudEvent for an event for the use case of: RPC Response message.
     *
     * @param applicationUriForRPC  The destination of the response. The [Uri] for the original application that requested the RPC and this response is for.
     * @param serviceMethodUri      The [Uri] for the method that was called on the service Ex.: :/body.access/1/rpc.UpdateDoor
     * @param requestId             The cloud event id from the original request cloud event that this response if for.
     * @param protoPayload          The protobuf serialized response message as defined by the application interface or the
     * google.rpc.Status message containing the details of an error.
     * @param attributes            Additional attributes such as ttl, hash and priority.
     * @return Returns an  response CloudEvent.
     */
    fun response(
        applicationUriForRPC: Uri,
        serviceMethodUri: Uri,
        requestId: String,
        protoPayload: Any,
        attributes: UCloudEventAttributes
    ): CloudEvent {
        val id = generateCloudEventId()
        return buildBaseCloudEvent(
            id,
            serviceMethodUri,
            protoPayload.toByteArray(),
            protoPayload.typeUrl,
            attributes
        )
            .withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_RESPONSE))
            .withExtension("sink", URI.create(applicationUriForRPC.get()))
            .withExtension("reqid", requestId)
            .build()
    }

    /**
     * Create a CloudEvent for an event for the use case of: RPC Response message that failed.
     *
     * @param applicationUriForRPC  The destination of the response. The [Uri] for the original application that requested the RPC and this response is for.
     * @param serviceMethodUri      The [Uri] for the method that was called on the service Ex.: :/body.access/1/rpc.UpdateDoor
     * @param requestId             The cloud event id from the original request cloud event that this response if for.
     * @param communicationStatus   A UCode value that indicates of a platform communication error while delivering this CloudEvent.
     * @param attributes            Additional attributes such as ttl, hash and priority.
     * @return Returns a response CloudEvent Response for the use case of RPC Response message that failed.
     */
    fun failedResponse(
        applicationUriForRPC: Uri,
        serviceMethodUri: Uri,
        requestId: String,
        communicationStatus: Int,
        attributes: UCloudEventAttributes
    ): CloudEvent {
        val id = generateCloudEventId()
        val protoPayload: Any = Any.pack(Empty.getDefaultInstance())
        return buildBaseCloudEvent(
            id,
            serviceMethodUri,
            protoPayload.toByteArray(),
            protoPayload.typeUrl,
            attributes
        )
            .withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_RESPONSE))
            .withExtension("sink", URI.create(applicationUriForRPC.get()))
            .withExtension("reqid", requestId)
            .withExtension("commstatus", communicationStatus)
            .build()
    }

    /**
     * Create a CloudEvent for an event for the use case of: Publish generic message.
     *
     * @param source The [Uri] of the topic being published.
     * @param protoPayload protobuf Any object with the Message to be published.
     * @param attributes Additional attributes such as ttl, hash and priority.
     * @return Returns a publish CloudEvent.
     */
    fun publish(source: Uri, protoPayload: Any, attributes: UCloudEventAttributes): CloudEvent {
        val id = generateCloudEventId()
        return buildBaseCloudEvent(id, source, protoPayload.toByteArray(), protoPayload.typeUrl, attributes)
            .withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH))
            .build()
    }

    /**
     * Create a CloudEvent for an event for the use case of: Publish a notification message.<br></br>
     * A published event containing the sink (destination) is often referred to as a notification, it is an event sent to a specific consumer.
     *
     * @param source        The [Uri] of the topic being published.
     * @param sink          The [Uri] of the destination of this notification.
     * @param protoPayload  protobuf Any object with the Message to be published.
     * @param attributes    Additional attributes such as ttl, hash and priority.
     * @return Returns a publish CloudEvent.
     */
    fun notification(
        source: Uri,
        sink: Uri,
        protoPayload: Any,
        attributes: UCloudEventAttributes
    ): CloudEvent {
        val id = generateCloudEventId()
        return buildBaseCloudEvent(id, source, protoPayload.toByteArray(), protoPayload.typeUrl, attributes)
            .withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH))
            .withExtension("sink", URI.create(sink.get()))
            .build()
    }

    /**
     * @return Returns a UUIDv8 id.
     */
    private fun generateCloudEventId(): String {
        val uuid: UUID = UUIDV8()
        return LongUuidSerializer.INSTANCE.serialize(uuid)
    }

    /**
     * Base CloudEvent builder that is the same for all CloudEvent types.
     *
     * @param id                 Event unique identifier.
     * @param source             Identifies who is sending this event in the format of a uProtocol URI that
     * can be built from a [UUri] object.
     * @param protoPayloadBytes  The serialized Event data with the content type of "application/x-protobuf".
     * @param protoPayloadSchema The schema of the proto payload bytes, for example you can use `protoPayload.typeUrl` on your service/app object.
     * @param attributes        Additional cloud event attributes that can be passed in. All attributes are optional and will be added only if they
     * were configured.
     * @return Returns a CloudEventBuilder that can be additionally configured and then by calling .build() construct a CloudEvent
     * ready to be serialized and sent to the transport layer.
     */
    fun buildBaseCloudEvent(
        id: String?,
        source: Uri,
        protoPayloadBytes: ByteArray,
        protoPayloadSchema: String?,
        attributes: UCloudEventAttributes
    ): CloudEventBuilder {
        val cloudEventBuilder: CloudEventBuilder = CloudEventBuilder.v1()
            .withId(id)
            .withSource(URI.create(source.get())) /* Not needed:
                .withDataContentType(PROTOBUF_CONTENT_TYPE)
                .withDataSchema(URI.create(protoPayloadSchema))
                */
            .withData(protoPayloadBytes)
        attributes.ttl?.let { ttl -> cloudEventBuilder.withExtension("ttl", ttl) }
        attributes.priority?.let { priority ->
            cloudEventBuilder.withExtension(
                "priority",
                UCloudEvent.getCeName(priority.valueDescriptor)
            )
        }
        attributes.hash?.let { hash -> cloudEventBuilder.withExtension("hash", hash) }
        attributes.token?.let { token -> cloudEventBuilder.withExtension("token", token) }
        attributes.traceparent?.let { traceparent: String ->
            cloudEventBuilder.withExtension("traceparent", traceparent)
        }

        return cloudEventBuilder
    }

    const val PROTOBUF_CONTENT_TYPE = "application/x-protobuf"
}

