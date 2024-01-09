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

package org.eclipse.uprotocol.cloudevent.factory

import com.google.protobuf.Any
import com.google.protobuf.InvalidProtocolBufferException
import com.google.protobuf.Message
import io.cloudevents.CloudEvent
import io.cloudevents.CloudEventData
import io.cloudevents.core.builder.CloudEventBuilder
import org.eclipse.uprotocol.uri.serializer.LongUriSerializer
import org.eclipse.uprotocol.uuid.factory.UuidUtils
import org.eclipse.uprotocol.uuid.serializer.LongUuidSerializer
import org.eclipse.uprotocol.v1.*
import org.eclipse.uprotocol.v1.UUID
import java.net.URI
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.*


/**
 * Class to extract  information from a CloudEvent.
 */
interface UCloudEvent {
    companion object {
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
         * @return Returns an Optional String value of a CloudEvent sink attribute if it exists,
         * otherwise an Optional.empty() is returned.
         */
        fun getSink(cloudEvent: CloudEvent): Optional<String> {
            return extractStringValueFromExtension("sink", cloudEvent)
        }

        /**
         * Extract the request id from a cloud event that is a response RPC CloudEvent. The attribute is optional.
         * @param cloudEvent the response RPC CloudEvent with request id to be extracted.
         * @return Returns an Optional String value of a response RPC CloudEvent request id attribute if it exists,
         * otherwise an Optional.empty() is returned.
         */
        fun getRequestId(cloudEvent: CloudEvent): Optional<String> {
            return extractStringValueFromExtension("reqid", cloudEvent)
        }

        /**
         * Extract the hash attribute from a cloud event. The hash attribute is optional.
         * @param cloudEvent CloudEvent with hash to be extracted.
         * @return Returns an Optional String value of a CloudEvent hash attribute if it exists,
         * otherwise an Optional.empty() is returned.
         */
        fun getHash(cloudEvent: CloudEvent): Optional<String> {
            return extractStringValueFromExtension("hash", cloudEvent)
        }

        /**
         * Extract the string value of the priority attribute from a cloud event. The priority attribute is optional.
         * @param cloudEvent CloudEvent with priority to be extracted.
         * @return Returns an Optional String value of a CloudEvent priority attribute if it exists,
         * otherwise an Optional.empty() is returned.
         */
        fun getPriority(cloudEvent: CloudEvent): Optional<String> {
            return extractStringValueFromExtension("priority", cloudEvent)
        }

        /**
         * Extract the integer value of the ttl attribute from a cloud event. The ttl attribute is optional.
         * @param cloudEvent CloudEvent with ttl to be extracted.
         * @return Returns an Optional String value of a CloudEvent ttl attribute if it exists,
         * otherwise an Optional.empty() is returned.
         */
        fun getTtl(cloudEvent: CloudEvent): Optional<Int> {
            return extractStringValueFromExtension("ttl", cloudEvent).map(Integer::valueOf)
        }

        /**
         * Extract the string value of the token attribute from a cloud event. The token attribute is optional.
         * @param cloudEvent CloudEvent with token to be extracted.
         * @return Returns an Optional String value of a CloudEvent priority token if it exists,
         * otherwise an Optional.empty() is returned.
         */
        fun getToken(cloudEvent: CloudEvent): Optional<String> {
            return extractStringValueFromExtension("token", cloudEvent)
        }

        /**
         * Extract the integer value of the communication status attribute from a cloud event. The communication status attribute is optional.
         * If there was a platform communication error that occurred while delivering this cloudEvent, it will be indicated in this attribute.
         * If the attribute does not exist, it is assumed that everything was UCode.OK_VALUE.
         * @param cloudEvent CloudEvent with the platformError to be extracted.
         * @return Returns a UCode value that indicates of a platform communication error while delivering this CloudEvent or UCode.OK_VALUE.
         */
        fun getCommunicationStatus(cloudEvent: CloudEvent): Int {
            return try {
                extractIntegerValueFromExtension("commstatus", cloudEvent).orElse(UCode.OK_VALUE)
            } catch (e: Exception) {
                UCode.OK_VALUE
            }
        }

        /**
         * Indication of a platform communication error that occurred while trying to deliver the CloudEvent.
         * @param cloudEvent CloudEvent to be queried for a platform delivery error.
         * @return returns true if the provided CloudEvent is marked with having a platform delivery problem.
         */
        fun hasCommunicationStatusProblem(cloudEvent: CloudEvent): Boolean {
            return getCommunicationStatus(cloudEvent) != UCode.OK_VALUE
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
         * Extract the timestamp from the UUIDV8 CloudEvent Id, with Unix epoch as the
         * @param cloudEvent The CloudEvent with the timestamp to extract.
         * @return Return the timestamp from the UUIDV8 CloudEvent Id or an empty Optional if timestamp can't be extracted.
         */
        fun getCreationTimestamp(cloudEvent: CloudEvent): Optional<Long> {
            val cloudEventId = cloudEvent.id
            val uuid = LongUuidSerializer.instance().deserialize(cloudEventId)
            return UuidUtils.getTime(uuid)
        }

        /**
         * Calculate if a CloudEvent configured with a creation time and a ttl attribute is expired.<br></br>
         * The ttl attribute is a configuration of how long this event should live for after it was generated (in milliseconds)
         * @param cloudEvent The CloudEvent to inspect for being expired.
         * @return Returns true if the CloudEvent was configured with a ttl &gt; 0 and a creation time to compare for expiration.
         */
        fun isExpiredByCloudEventCreationDate(cloudEvent: CloudEvent): Boolean {
            val maybeTtl: Optional<Int> = getTtl(cloudEvent)
            if (maybeTtl.isEmpty) {
                return false
            }
            val ttl: Int = maybeTtl.get()
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
            val maybeTtl: Optional<Int> = getTtl(cloudEvent)
            if (maybeTtl.isEmpty) {
                return false
            }
            val ttl: Int = maybeTtl.get()
            if (ttl <= 0) {
                return false
            }
            val cloudEventId: String = cloudEvent.id
            val uuid = LongUuidSerializer.instance().deserialize(cloudEventId)
            if (uuid == UUID.getDefaultInstance()) {
                return false
            }
            val delta: Long = System.currentTimeMillis() - UuidUtils.getTime(uuid).orElse(0L)
            return delta >= ttl
        }

        /**
         * Check if a CloudEvent is a valid UUIDv6 or v8 .
         * @param cloudEvent The CloudEvent with the id to inspect.
         * @return Returns true if the CloudEvent is valid.
         */
        fun isCloudEventId(cloudEvent: CloudEvent): Boolean {
            val cloudEventId: String = cloudEvent.id
            val uuid = LongUuidSerializer.instance().deserialize(cloudEventId)
            return UuidUtils.isUuid(uuid)
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
         * An all or nothing error handling strategy is implemented. If anything goes wrong, an empty optional will be returned. <br></br>
         * Example: <br></br>
         * <pre>Optional&lt;SomeMessage&gt; unpacked = UCloudEvent.unpack(cloudEvent, SomeMessage.class);</pre>
         * @param cloudEvent CloudEvent containing the payload to extract.
         * @param clazz The class that extends [Message] that the payload is extracted into.
         * @return Returns a [Message] payload of the class type that is provided.
         * @param <T> The class type of the Message to be unpacked.
        </T> */
        fun <T : Message> unpack(cloudEvent: CloudEvent, clazz: Class<T>): Optional<T> {
            return try {
                Optional.of(getPayload(cloudEvent).unpack(clazz))
            } catch (e: InvalidProtocolBufferException) {
                // All or nothing error handling strategy. If something goes wrong, you just get an empty.
                Optional.empty()
            }
        }

        /**
         * Function used to pretty print a CloudEvent containing only the id, source, type and maybe a sink. Used mainly for logging.
         * @param cloudEvent The CloudEvent we want to pretty print.
         * @return returns the String representation of the CloudEvent containing only the id, source, type and maybe a sink.
         */
        fun toString(cloudEvent: CloudEvent?): String {
            return if (cloudEvent != null) (((("CloudEvent{id='" + cloudEvent.id) + "', source='" + cloudEvent.source) + "'" + getSink(
                cloudEvent
            ).map { sink -> String.format(", sink='%s'", sink) }
                .orElse("")) + ", type='" + cloudEvent.type) + "'}" else "null"
        }

        /**
         * Utility for extracting the String value of an extension.
         * @param extensionName The name of the CloudEvent extension.
         * @param cloudEvent The CloudEvent containing the data.
         * @return returns the Optional String value of the extension matching the extension name,
         * or an Optional.empty() is the value does not exist.
         */
        private fun extractStringValueFromExtension(extensionName: String, cloudEvent: CloudEvent): Optional<String> {
            val extensionNames: MutableSet<String>? = cloudEvent.extensionNames
            if (extensionNames != null) {
                if (extensionNames.contains(extensionName)) {
                    val extension: kotlin.Any? = cloudEvent.getExtension(extensionName)
                    return if (extension == null) Optional.empty() else Optional.of(extension.toString())
                }
            }
            return Optional.empty()
        }

        /**
         * Utility for extracting the Integer value of an extension.
         * @param extensionName The name of the CloudEvent extension.
         * @param cloudEvent The CloudEvent containing the data.
         * @return returns the Optional Integer value of the extension matching the extension name,
         * or an Optional.empty() is the value does not exist.
         */
        private fun extractIntegerValueFromExtension(extensionName: String, cloudEvent: CloudEvent): Optional<Int> {
            return extractStringValueFromExtension(extensionName, cloudEvent).map(Integer::valueOf)
        }

        fun getEventType(type: UMessageType?): String {
            return when (type) {
                UMessageType.UMESSAGE_TYPE_PUBLISH -> "pub.v1"
                UMessageType.UMESSAGE_TYPE_REQUEST -> "req.v1"
                UMessageType.UMESSAGE_TYPE_RESPONSE -> "res.v1"
                else -> ""
            }
        }

        fun getMessageType(ce_type: String?): UMessageType {
            return when (ce_type) {
                "pub.v1" -> UMessageType.UMESSAGE_TYPE_PUBLISH
                "req.v1" -> UMessageType.UMESSAGE_TYPE_REQUEST
                "res.v1" -> UMessageType.UMESSAGE_TYPE_RESPONSE
                else -> UMessageType.UMESSAGE_TYPE_UNSPECIFIED
            }
        }

        /**
         * Get the UMessage from the cloud event
         * @param event The CloudEvent containing the data.
         * @return returns the UMessage
         */
        fun toMessage(event: CloudEvent): UMessage {
            val sourceUUri = LongUriSerializer.instance().deserialize(getSource(event))

            val msgPayload = uPayload {
                format = getUPayloadFormatFromContentType(event.dataContentType)
                value = getPayload(event).toByteString()
            }

            val msgAttributes = uAttributes {
                id = LongUuidSerializer.instance().deserialize(event.id)
                type = getMessageType(event.type)
                if (hasCommunicationStatusProblem(event)) {
                    commstatus = getCommunicationStatus(event)
                }


                getPriority(event).ifPresent {
                    val adjustedPriority = if (it.startsWith("UPRIORITY_")) it else "UPRIORITY_$it"
                    priority = UPriority.valueOf(adjustedPriority)
                }

                getSink(event).ifPresent { sink = LongUriSerializer.instance().deserialize(it) }

                getRequestId(event).ifPresent { reqid = LongUuidSerializer.instance().deserialize(it) }

                getTtl(event).ifPresent { ttl = it }

                getToken(event).ifPresent { token = it }

                extractIntegerValueFromExtension("plevel", event).ifPresent { permissionLevel = it }

            }
            return uMessage {
                attributes = msgAttributes
                payload = msgPayload
                source = sourceUUri
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
            val attributes: UAttributes = message.attributes
            val builder: CloudEventBuilder =
                CloudEventBuilder.v1().withId(LongUuidSerializer.instance().serialize(attributes.id))
            builder.withType(getEventType(attributes.type))
            builder.withSource(URI.create(LongUriSerializer.instance().serialize(message.source)))
            val contentType = getContentTypeFromUPayloadFormat(message.payload.format)
            if (contentType.isNotEmpty()) {
                builder.withDataContentType(contentType)
            }
            // IMPORTANT: Currently, ONLY the VALUE format is supported in the SDK!
            if (message.payload.hasValue()) {
                builder.withData(message.payload.value.toByteArray())
            }

            if (attributes.hasTtl()) {
                builder.withExtension("ttl", attributes.ttl)
            }
            if (attributes.hasToken()) {
                builder.withExtension("token", attributes.token)
            }

            if (attributes.priorityValue > 0) {
                builder.withExtension("priority", attributes.priority.name)
            }

            if (attributes.hasSink()) {
                builder.withExtension("sink", URI.create(LongUriSerializer.instance().serialize(attributes.sink)))
            }

            if (attributes.hasCommstatus()) {
                builder.withExtension("commstatus", attributes.commstatus)
            }

            if (attributes.hasReqid()) {
                builder.withExtension("reqid", LongUuidSerializer.instance().serialize(attributes.reqid))
            }

            if (attributes.hasPermissionLevel()) {
                builder.withExtension("plevel", attributes.permissionLevel)
            }

            return builder.build()

        }

        /**
         * Retrieves the payload format enumeration based on the provided content type.
         *
         * @param contentType The content type string representing the format of the payload.
         * @return The corresponding UPayloadFormat enumeration based on the content type.
         */
        private fun getUPayloadFormatFromContentType(contentType: String?): UPayloadFormat {
            return when (contentType) {
                "application/json" -> UPayloadFormat.UPAYLOAD_FORMAT_JSON
                "application/octet-stream" -> UPayloadFormat.UPAYLOAD_FORMAT_RAW
                "text/plain" -> UPayloadFormat.UPAYLOAD_FORMAT_TEXT
                "application/x-someip" -> UPayloadFormat.UPAYLOAD_FORMAT_SOMEIP
                "application/x-someip_tlv" -> UPayloadFormat.UPAYLOAD_FORMAT_SOMEIP_TLV
                else -> UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF
            }
        }

        /**
         * Retrieves the content type string based on the provided UPayloadFormat enumeration.
         *
         * @param format The UPayloadFormat enumeration representing the payload format.
         * @return The corresponding content type string based on the payload format.
         */
        private fun getContentTypeFromUPayloadFormat(format: UPayloadFormat): String = when (format) {
            UPayloadFormat.UPAYLOAD_FORMAT_JSON -> "application/json"
            UPayloadFormat.UPAYLOAD_FORMAT_RAW -> "application/octet-stream"
            UPayloadFormat.UPAYLOAD_FORMAT_TEXT -> "text/plain"
            UPayloadFormat.UPAYLOAD_FORMAT_SOMEIP -> "application/x-someip"
            UPayloadFormat.UPAYLOAD_FORMAT_SOMEIP_TLV -> "application/x-someip_tlv"
            else -> ""
        }
    }
}
