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
 */

package org.eclipse.uprotocol.cloudevent.factory

import org.eclipse.uprotocol.uuid.factory.UUIDUtils
import com.google.protobuf.Any
import com.google.protobuf.InvalidProtocolBufferException
import com.google.protobuf.Message
import com.google.rpc.Code
import io.cloudevents.CloudEvent
import io.cloudevents.CloudEventData
import io.cloudevents.core.builder.CloudEventBuilder
import org.eclipse.uprotocol.v1.UUID
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.Optional

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
            return extractStringValueFromExtension("ttl", cloudEvent)
                .map(Integer::valueOf)
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
         * If the attribute does not exist, it is assumed that everything was Code.OK_VALUE.
         * @param cloudEvent CloudEvent with the platformError to be extracted.
         * @return Returns a [Code] value that indicates of a platform communication error while delivering this CloudEvent or Code.OK_VALUE.
         */
        fun getCommunicationStatus(cloudEvent: CloudEvent): Int {
            return try {
                extractIntegerValueFromExtension("commstatus", cloudEvent)
                    .orElse(Code.OK_VALUE)
            } catch (e: Exception) {
                Code.OK_VALUE
            }
        }

        /**
         * Indication of a platform communication error that occurred while trying to deliver the CloudEvent.
         * @param cloudEvent CloudEvent to be queried for a platform delivery error.
         * @return returns true if the provided CloudEvent is marked with having a platform delivery problem.
         */
        fun hasCommunicationStatusProblem(cloudEvent: CloudEvent): Boolean {
            return getCommunicationStatus(cloudEvent) != Code.OK_VALUE
        }

        /**
         * Returns a new CloudEvent from the supplied CloudEvent, with the platform communication added.
         * @param cloudEvent CloudEvent that the platform delivery error will be added.
         * @param communicationStatus the platform delivery error Code to add to the CloudEvent.
         * @return Returns a new CloudEvent from the supplied CloudEvent, with the platform communication added.
         */
        fun addCommunicationStatus(cloudEvent: CloudEvent, communicationStatus: Int?): CloudEvent? {
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
            val uuid = UUIDUtils.fromString(cloudEventId)

            return if (uuid.isEmpty) Optional.empty() else UUIDUtils.getTime(uuid.get())
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
            if (maybeTtl.isEmpty()) {
                return false
            }
            val ttl: Int = maybeTtl.get()
            if (ttl <= 0) {
                return false
            }
            val cloudEventId: String = cloudEvent.getId()
            val uuid: Optional<UUID> = UUIDUtils.fromString(cloudEventId)
            if (uuid.isEmpty()) {
                return false
            }
            val delta: Long = System.currentTimeMillis() - UUIDUtils.getTime(uuid.get()).orElse(0L)
            return delta >= ttl
        }

        /**
         * Check if a CloudEvent is a valid UUIDv6 or v8 .
         * @param cloudEvent The CloudEvent with the id to inspect.
         * @return Returns true if the CloudEvent is valid.
         */
        fun isCloudEventId(cloudEvent: CloudEvent): Boolean {
            val cloudEventId: String = cloudEvent.id
            val uuid: Optional<UUID> = UUIDUtils.fromString(cloudEventId)
            return if (uuid.isEmpty) false else UUIDUtils.isUuid(uuid.get())
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
            return if (cloudEvent != null) (((("CloudEvent{id='" + cloudEvent.id).toString() +
                    "', source='" + cloudEvent.getSource()) + "'" +
                    getSink(cloudEvent).map { sink -> String.format(", sink='%s'", sink) }
                        .orElse("")) +
                    ", type='" + cloudEvent.getType()) + "'}" else "null"
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
            return extractStringValueFromExtension(extensionName, cloudEvent)
                .map(Integer::valueOf)
        }
    }
}
