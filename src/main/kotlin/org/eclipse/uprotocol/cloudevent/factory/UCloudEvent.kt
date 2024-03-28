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
import com.google.protobuf.Descriptors.EnumValueDescriptor
import com.google.protobuf.InvalidProtocolBufferException
import com.google.protobuf.Message
import io.cloudevents.CloudEvent
import io.cloudevents.CloudEventData
import io.cloudevents.core.builder.CloudEventBuilder
import org.eclipse.uprotocol.UprotocolOptions
import org.eclipse.uprotocol.uri.Uri
import org.eclipse.uprotocol.uri.serializer.LongUriSerializer
import org.eclipse.uprotocol.uuid.factory.getTime
import org.eclipse.uprotocol.uuid.factory.isUuid
import org.eclipse.uprotocol.uuid.serializer.LongUuidSerializer
import org.eclipse.uprotocol.v1.*
import java.net.URI
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit


/**
 * Class to extract  information from a CloudEvent.
 */
object UCloudEvent {
    /**
     * Extract the source from a cloud event. The source is a mandatory attribute.
     * The CloudEvent constructor does not allow creating a cloud event without a source.
     * @param cloudEvent CloudEvent with source to be extracted.
     * @return Returns the String value of a CloudEvent source attribute.
     */
    fun getSource(cloudEvent: CloudEvent): String {
        return cloudEvent.source.toString()
    }

    /**
     * Extract the sink from a cloud event. The sink attribute is optional.
     * @param cloudEvent CloudEvent with sink to be extracted.
     * @return Returns a Uri String value of a CloudEvent sink attribute if it exists,
     * otherwise Null is returned.
     */
    fun getSink(cloudEvent: CloudEvent): Uri? {
        return extractStringValueFromExtension("sink", cloudEvent)?.let { Uri(it) }
    }

    /**
     * Extract the request id from a cloud event that is a response RPC CloudEvent. The attribute is optional.
     * @param cloudEvent the response RPC CloudEvent with request id to be extracted.
     * @return Returns a String value of a response RPC CloudEvent request id attribute if it exists,
     * otherwise Null is returned.
     */
    fun getRequestId(cloudEvent: CloudEvent): String? {
        return extractStringValueFromExtension("reqid", cloudEvent)
    }

    /**
     * Extract the hash attribute from a cloud event. The hash attribute is optional.
     * @param cloudEvent CloudEvent with hash to be extracted.
     * @return Returns a String value of a CloudEvent hash attribute if it exists,
     * otherwise Null is returned.
     */
    fun getHash(cloudEvent: CloudEvent): String? {
        return extractStringValueFromExtension("hash", cloudEvent)
    }

    /**
     * Extract the string value of the priority attribute from a cloud event. The priority attribute is optional.
     * @param cloudEvent CloudEvent with priority to be extracted.
     * @return Returns a String value of a CloudEvent priority attribute if it exists,
     * otherwise Null is returned.
     */
    fun getPriority(cloudEvent: CloudEvent): String? {
        return extractStringValueFromExtension("priority", cloudEvent)
    }

    /**
     * Extract the integer value of the ttl attribute from a cloud event. The ttl attribute is optional.
     * @param cloudEvent CloudEvent with ttl to be extracted.
     * @return Returns an Int value of a CloudEvent ttl attribute if it exists,
     * otherwise Null is returned.
     */
    fun getTtl(cloudEvent: CloudEvent): Int? {
        return extractStringValueFromExtension("ttl", cloudEvent)?.toInt()
    }

    /**
     * Extract the string value of the token attribute from a cloud event. The token attribute is optional.
     * @param cloudEvent CloudEvent with token to be extracted.
     * @return Returns a String value of a CloudEvent priority token if it exists,
     * otherwise Null is returned.
     */
    fun getToken(cloudEvent: CloudEvent): String? {
        return extractStringValueFromExtension("token", cloudEvent)
    }


    /**
     * Extract the string value of the traceparent attribute from a cloud event. The traceparent attribute is optional.
     * @param cloudEvent CloudEvent with traceparent to be extracted.
     * @return Returns a String value of a CloudEvent traceparent if it exists,
     * otherwise Null is returned.
     */
    fun getTraceparent(cloudEvent: CloudEvent): String? {
        return extractStringValueFromExtension("traceparent", cloudEvent)
    }

    /**
     * Fetch the UCode from the CloudEvent commstatus integer value. The communication status attribute is optional.
     * If there was a platform communication error that occurred while delivering this cloudEvent, it will be indicated in this attribute.
     * If the attribute does not exist, it is assumed that everything was UCode.OK_VALUE.
     * @param cloudEvent CloudEvent with the platformError to be extracted.
     * @return Returns a UCode that indicates of a platform communication error while delivering this CloudEvent or UCode.OK.
     */
    fun getCommunicationStatus(cloudEvent: CloudEvent): UCode {
        return try {
            UCode.forNumber(extractIntegerValueFromExtension("commstatus", cloudEvent) ?: UCode.OK_VALUE)
        } catch (e: Exception) {
            UCode.OK
        }
    }

    /**
     * Indication of a platform communication error that occurred while trying to deliver the CloudEvent.
     * @param cloudEvent CloudEvent to be queried for a platform delivery error.
     * @return returns true if the provided CloudEvent is marked with having a platform delivery problem.
     */
    fun hasCommunicationStatusProblem(cloudEvent: CloudEvent): Boolean {
        return getCommunicationStatus(cloudEvent) != UCode.OK
    }

    /**
     * Returns a new CloudEvent from the supplied CloudEvent, with the platform communication added.
     * @param cloudEvent CloudEvent that the platform delivery error will be added.
     * @param communicationStatus the platform delivery error UCode to add to the CloudEvent.
     * @return Returns a new CloudEvent from the supplied CloudEvent, with the platform communication added.
     */
    fun addCommunicationStatus(cloudEvent: CloudEvent, communicationStatus: Int?): CloudEvent {
        if (communicationStatus == null) {
            return cloudEvent
        }
        val builder: CloudEventBuilder = CloudEventBuilder.v1(cloudEvent)
        builder.withExtension("commstatus", communicationStatus)
        return builder.build()
    }

    /**
     * Extract the timestamp from the UUIDV8 CloudEvent ID, with Unix epoch as the
     * @param cloudEvent The CloudEvent with the timestamp to extract.
     * @return Return the timestamp from the UUIDV8 CloudEvent ID or a Null if timestamp can't be extracted.
     */
    fun getCreationTimestamp(cloudEvent: CloudEvent): Long? {
        val cloudEventId = cloudEvent.id
        val uuid = LongUuidSerializer.INSTANCE.deserialize(cloudEventId)
        return uuid.getTime()
    }

    /**
     * Calculate if a CloudEvent configured with a creation time and a ttl attribute is expired.<br></br>
     * The ttl attribute is a configuration of how long this event should live for after it was generated (in milliseconds)
     * @param cloudEvent The CloudEvent to inspect for being expired.
     * @return Returns true if the CloudEvent was configured with a ttl &gt; 0 and a creation time to compare for expiration.
     */
    fun isExpiredByCloudEventCreationDate(cloudEvent: CloudEvent): Boolean {
        val ttl = getTtl(cloudEvent) ?: return false
        if (ttl <= 0) {
            return false
        }
        val cloudEventCreationTime: OffsetDateTime = cloudEvent.time ?: return false
        val now: OffsetDateTime = OffsetDateTime.now()
        val creationTimePlusTtl: OffsetDateTime = cloudEventCreationTime.plus(ttl.toLong(), ChronoUnit.MILLIS)
        return now.isAfter(creationTimePlusTtl)
    }

    /**
     * Calculate if a CloudEvent configured with UUIDv8 id and a ttl attribute is expired.<br></br>
     * The ttl attribute is a configuration of how long this event should live for after it was generated (in milliseconds)
     * @param cloudEvent The CloudEvent to inspect for being expired.
     * @return Returns true if the CloudEvent was configured with a ttl &gt; 0 and UUIDv8 id to compare for expiration.
     */
    fun isExpired(cloudEvent: CloudEvent): Boolean {
        val ttl = getTtl(cloudEvent) ?: return false
        if (ttl <= 0) {
            return false
        }
        val cloudEventId: String = cloudEvent.id
        val uuid = LongUuidSerializer.INSTANCE.deserialize(cloudEventId)
        if (uuid == UUID.getDefaultInstance()) {
            return false
        }
        val delta: Long = System.currentTimeMillis() - (uuid.getTime() ?: 0L)
        return delta >= ttl
    }

    /**
     * Check if a CloudEvent is a valid UUIDv6 or v8 .
     * @param cloudEvent The CloudEvent with the id to inspect.
     * @return Returns true if the CloudEvent is valid.
     */
    fun isCloudEventId(cloudEvent: CloudEvent): Boolean {
        val cloudEventId: String = cloudEvent.id
        val uuid = LongUuidSerializer.INSTANCE.deserialize(cloudEventId)
        return uuid.isUuid()
    }

    /**
     * Extract the payload from the CloudEvent as a protobuf Any object. <br></br>
     * An all or nothing error handling strategy is implemented. If anything goes wrong, an Any.getDefaultInstance() will be returned.
     * @param cloudEvent CloudEvent containing the payload to extract.
     * @return Extracts the payload from a CloudEvent as a Protobuf Any object.
     */
    fun getPayload(cloudEvent: CloudEvent): Any {
        val data: CloudEventData = cloudEvent.data ?: return Any.getDefaultInstance()
        return try {
            Any.parseFrom(data.toBytes())
        } catch (e: InvalidProtocolBufferException) {
            Any.getDefaultInstance()
        }
    }

    /**
     * Extract the payload from the CloudEvent as a protobuf Message of the provided class. The protobuf of this message
     * class must be loaded on the client for this to work. <br></br>
     * An all or nothing error handling strategy is implemented. If anything goes wrong, a Null will be returned. <br></br>
     * Example: <br></br>
     * <pre>SomeMessage; unpacked = UCloudEvent.unpack(cloudEvent, SomeMessage::class.java);</pre>
     * @param cloudEvent CloudEvent containing the payload to extract.
     * @param clazz The class that extends [Message] that the payload is extracted into.
     * @return Returns a [Message] payload of the class type that is provided.
     * @param <T> The class type of the Message to be unpacked.
    </T> */
    fun <T : Message> unpack(cloudEvent: CloudEvent, clazz: Class<T>): T? {
        return try {
            getPayload(cloudEvent).unpack(clazz)
        } catch (e: InvalidProtocolBufferException) {
            // All or nothing error handling strategy. If something goes wrong, you just get an empty.
            null
        }
    }

    /**
     * Function used to pretty print a CloudEvent containing only the id, source, type and maybe a sink. Used mainly for logging.
     * @param cloudEvent The CloudEvent we want to pretty print.
     * @return returns the String representation of the CloudEvent containing only the id, source, type and maybe a sink.
     */
    fun toString(cloudEvent: CloudEvent): String {
        val sink = getSink(cloudEvent)?.let { ", sink='$it'" } ?: ""
        return "CloudEvent{id='${cloudEvent.id}', source='${cloudEvent.source}'${sink}, type='${cloudEvent.type}'}"

    }

    /**
     * Utility for extracting the String value of an extension.
     * @param extensionName The name of the CloudEvent extension.
     * @param cloudEvent The CloudEvent containing the data.
     * @return returns the String value of the extension matching the extension name,
     * or a Null is the value does not exist.
     */
    private fun extractStringValueFromExtension(extensionName: String, cloudEvent: CloudEvent): String? {
        return cloudEvent.extensionNames?.let {
            if (it.contains(extensionName)) {
                cloudEvent.getExtension(extensionName)?.toString()
            } else {
                null
            }
        }
    }

    /**
     * Utility for extracting the Integer value of an extension.
     * @param extensionName The name of the CloudEvent extension.
     * @param cloudEvent The CloudEvent containing the data.
     * @return returns the Integer value of the extension matching the extension name,
     * or a Null is the value does not exist.
     */
    private fun extractIntegerValueFromExtension(extensionName: String, cloudEvent: CloudEvent): Int? {
        return extractStringValueFromExtension(extensionName, cloudEvent)?.toInt()
    }

    /**
     * Get the string representation of the UMessageType.
     *
     * Note: The UMessageType is determined by the type of the CloudEvent. If
     * the UMessageType is UMESSAGE_TYPE_NOTIFICATION, we assume the CloudEvent type
     * is "pub.v1" and the sink is present.
     * @param type The UMessageType
     * @return returns the string representation of the UMessageType
     *
     */
    fun getEventType(type: UMessageType): String {
        return getCeName(type.valueDescriptor)
    }

    /**
     * Get the string representation of the UPriority
     * @param priority
     * @return returns the string representation of the UPriority
     */
    fun getCePriority(priority: UPriority): String {
        return getCeName(priority.valueDescriptor)
    }

    /**
     * Get the UPriority from the string name
     * @param priority
     * @return returns the UPriority
     */
    fun getUPriority(priority: String, default : UPriority = UPriority.UNRECOGNIZED): UPriority {
        return UPriority.getDescriptor().values
            .filter { value ->
                value.options.hasExtension(UprotocolOptions.ceName) && value.options
                    .getExtension(UprotocolOptions.ceName) == priority
            }.map { value ->
            UPriority.forNumber(value.number)
        }.firstOrNull() ?: default
    }

    /**
     * Get the UMessageType from the string representation.
     *
     * Note: The UMessageType is determined by the type of the CloudEvent.
     * If the CloudEvent type is "pub.v1" and the sink is present, the UMessageType is assumed to be
     * UMESSAGE_TYPE_NOTIFICATION, this is because uProtocol CloudEvent definition did not have an explicit
     * notification type.
     *
     * @return returns the UMessageType
     */
    fun getMessageType(type: String): UMessageType {
        return UMessageType.getDescriptor().values
            .filter { value ->
                value.options.hasExtension(UprotocolOptions.ceName) && value.options
                    .getExtension(UprotocolOptions.ceName) == type
            }.map { value ->
                UMessageType.forNumber(value.number)
            }.firstOrNull() ?: UMessageType.UNRECOGNIZED
    }

    /**
     * Get the UMessage from the cloud event
     * @param event The CloudEvent containing the data.
     * @return returns the UMessage
     */
    fun toMessage(event: CloudEvent): UMessage {

        val msgPayload = uPayload {
            format = getUPayloadFormatFromContentType(event.dataContentType)
            value = getPayload(event).toByteString()
        }

        val msgAttributes = uAttributes {
            id = LongUuidSerializer.INSTANCE.deserialize(event.id)
            type = getMessageType(event.type)
            source = LongUriSerializer.INSTANCE.deserialize(event.source.toString())
            if (hasCommunicationStatusProblem(event)) {
                commstatus = getCommunicationStatus(event)
            }
            getPriority(event)?.let { p ->
                priority = getUPriority(p, UPriority.UPRIORITY_UNSPECIFIED)
            }

            getSink(event)?.let { sink = LongUriSerializer.INSTANCE.deserialize(it.get()) }

            getRequestId(event)?.let { reqid = LongUuidSerializer.INSTANCE.deserialize(it) }

            getTtl(event)?.let { ttl = it }

            getToken(event)?.let { token = it }

            getTraceparent(event)?.let { traceparent = it }

            extractIntegerValueFromExtension("plevel", event)?.let { permissionLevel = it }

        }
        return uMessage {
            attributes = msgAttributes
            payload = msgPayload
        }
    }

    /**
     * Get the Cloudevent from the UMessage<br>
     * <b>Note: For now, only the value format of UPayload is supported in the SDK.If the UPayload has a reference, it
     * needs to be copied to CloudEvent.</b>
     * @param message The UMessage protobuf containing the data
     * @return returns the cloud event
     */
    fun fromMessage(message: UMessage): CloudEvent {
        val attributes: UAttributes = message.attributes ?: UAttributes.getDefaultInstance()
        val payload = message.payload ?: UPayload.getDefaultInstance()
        val builder: CloudEventBuilder =
            CloudEventBuilder.v1().withId(LongUuidSerializer.INSTANCE.serialize(attributes.id))
        builder.withType(getEventType(attributes.type))
        builder.withSource(URI.create(LongUriSerializer.INSTANCE.serialize(attributes.source)))
        val contentType = getContentTypeFromUPayloadFormat(payload.format)
        if (contentType.isNotEmpty()) {
            builder.withDataContentType(contentType)
        }
        // IMPORTANT: Currently, ONLY the VALUE format is supported in the SDK!
        if (payload.hasValue()) {
            builder.withData(payload.value.toByteArray())
        }

        if (attributes.hasTtl()) {
            builder.withExtension("ttl", attributes.ttl)
        }
        if (attributes.hasToken()) {
            builder.withExtension("token", attributes.token)
        }

        if (attributes.priorityValue > 0) {
            builder.withExtension("priority", getCePriority(attributes.priority))
        }

        if (attributes.hasSink()) {
            builder.withExtension("sink", URI.create(LongUriSerializer.INSTANCE.serialize(attributes.sink)))
        }

        if (attributes.hasCommstatus()) {
            builder.withExtension("commstatus", attributes.commstatus.number)
        }

        if (attributes.hasReqid()) {
            builder.withExtension("reqid", LongUuidSerializer.INSTANCE.serialize(attributes.reqid))
        }

        if (attributes.hasPermissionLevel()) {
            builder.withExtension("plevel", attributes.permissionLevel)
        }

        if (attributes.hasTraceparent()) {
            builder.withExtension("traceparent", attributes.traceparent)
        }

        return builder.build()

    }

    /**
     * Retrieves the payload format enumeration based on the provided string representation of the data content type <br>
     * This method uses the uProtocol mimeType custom options declared in upayload.proto.
     *
     * @param contentType The content type string representing the format of the payload.
     * @return The corresponding UPayloadFormat enumeration based on the content type.
     */
    private fun getUPayloadFormatFromContentType(contentType: String?): UPayloadFormat {
        if (contentType == null) {
            return UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY
        }
        return UPayloadFormat.getDescriptor().values.filter { v: EnumValueDescriptor ->
            v.options.hasExtension(UprotocolOptions.mimeType) && v.options
                .getExtension(UprotocolOptions.mimeType) == contentType
        }.map { v: EnumValueDescriptor ->
            UPayloadFormat.forNumber(v.number)
        }.firstOrNull() ?: UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY
    }

    /**
     * Retrieves the string representation of the data content type based on the provided UPayloadFormat. <BR></BR>
     * This method uses the uProtocol mimeType custom options declared in upayload.proto.
     *
     * @param format The UPayloadFormat enumeration representing the payload format.
     * @return The corresponding content type string based on the payload format.
     */
    fun getContentTypeFromUPayloadFormat(format: UPayloadFormat): String {
        // Since the default value is UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY, we return an empty string.
        if (format == UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY) {
            return ""
        }
        return format.valueDescriptor.options.getExtension(UprotocolOptions.mimeType)
    }

    /**
     * Retrieves the string representation of the data content type based on the provided Enum value descriptor. <BR></BR>
     *
     * @param descriptor The EnumDescriptor enumeration representing the payload format.
     * @return The corresponding string name for the value.
     */
    fun getCeName(descriptor: EnumValueDescriptor): String {
        return descriptor.options.getExtension(UprotocolOptions.ceName)
    }
}

