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
 */
package org.eclipse.uprotocol.cloudevent.factory

import com.google.protobuf.Any
import com.google.protobuf.InvalidProtocolBufferException
import io.cloudevents.CloudEvent
import io.cloudevents.CloudEventData
import io.cloudevents.core.builder.CloudEventBuilder
import org.eclipse.uprotocol.cloudevent.datamodel.UCloudEventAttributes.Companion.uCloudEventAttributes
import org.eclipse.uprotocol.cloudevent.factory.UCloudEvent.getCePriority
import org.eclipse.uprotocol.cloudevent.factory.UCloudEvent.toMessage
import org.eclipse.uprotocol.uri.Uri
import org.eclipse.uprotocol.uri.serializer.LongUriSerializer
import org.eclipse.uprotocol.uri.toUri
import org.eclipse.uprotocol.uuid.factory.UUIDV8
import org.eclipse.uprotocol.uuid.serializer.LongUuidSerializer
import org.eclipse.uprotocol.v1.*
import org.eclipse.uprotocol.v1.UUID
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.Instant
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.*


internal class UCloudEventTest {
    @Test
    @DisplayName("Test extracting the source from a CloudEvent.")
    fun test_extract_source_from_cloudevent() {
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest()
        val cloudEvent: CloudEvent = builder.build()
        val source: String = UCloudEvent.getSource(cloudEvent)
        assertEquals("/body.access//door.front_left#Door", source)
    }

    @Test
    @DisplayName("Test extracting the sink from a CloudEvent when the sink exists.")
    fun test_extract_sink_from_cloudevent_when_sink_exists() {
        val sinkForTest = "//bo.cloud/petapp/1/rpc.response"
        val builder: CloudEventBuilder =
            buildBaseCloudEventBuilderForTest().withExtension("sink", URI.create(sinkForTest))
        val cloudEvent: CloudEvent = builder.build()
        val sink = UCloudEvent.getSink(cloudEvent)
        assertEquals(sinkForTest, sink?.get())
    }

    @Test
    @DisplayName("Test extracting the sink from a CloudEvent when the sink does not exist.")
    fun test_extract_sink_from_cloudevent_when_sink_does_not_exist() {
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest()
        val cloudEvent: CloudEvent = builder.build()
        val sink = UCloudEvent.getSink(cloudEvent)
        assertNull(sink)
    }

    @Test
    @DisplayName("Test extracting the request id from a CloudEvent when the request id exists.")
    fun test_extract_requestId_from_cloudevent_when_requestId_exists() {
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withExtension("reqid", "someRequestId")
        val cloudEvent: CloudEvent = builder.build()
        val requestId = UCloudEvent.getRequestId(cloudEvent)
        assertEquals("someRequestId", requestId)
    }

    @Test
    @DisplayName("Test extracting the request id from a CloudEvent when the request id does not exist.")
    fun test_extract_requestId_from_cloudevent_when_requestId_does_not_exist() {
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest()
        val cloudEvent: CloudEvent = builder.build()
        val requestId = UCloudEvent.getRequestId(cloudEvent)
        assertNull(requestId)
    }


    @Test
    @DisplayName("Test extracting the hash from a CloudEvent when the hash exists.")
    fun test_extract_hash_from_cloudevent_when_hash_exists() {
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest()
        val cloudEvent: CloudEvent = builder.build()
        val hash = UCloudEvent.getHash(cloudEvent)
        assertEquals("somehash", hash)
    }

    @Test
    @DisplayName("Test extracting the hash from a CloudEvent when the hash does not exist.")
    fun test_extract_hash_from_cloudevent_when_hash_does_not_exist() {
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withoutExtension("hash")
        val cloudEvent: CloudEvent = builder.build()
        val hash: String? = UCloudEvent.getHash(cloudEvent)
        assertNull(hash)
    }

    @Test
    @DisplayName("Test extracting the priority from a CloudEvent when the priority exists.")
    fun test_extract_priority_from_cloudevent_when_priority_exists() {
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest()
        val cloudEvent: CloudEvent = builder.build()
        val priority = UCloudEvent.getPriority(cloudEvent)
        assertEquals(getCePriority(UPriority.UPRIORITY_CS1), priority)
    }

    @Test
    @DisplayName("Test extracting the priority from a CloudEvent when the priority does not exist.")
    fun test_extract_priority_from_cloudevent_when_priority_does_not_exist() {
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withoutExtension("priority")
        val cloudEvent: CloudEvent = builder.build()
        val priority: String? = UCloudEvent.getPriority(cloudEvent)
        assertNull(priority)

        val message = toMessage(cloudEvent)
        assertEquals(message.attributes.priority, UPriority.UPRIORITY_UNSPECIFIED)
    }

    @Test
    @DisplayName("Test extracting the ttl from a CloudEvent when the ttl exists.")
    fun test_extract_ttl_from_cloudevent_when_ttl_exists() {
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest()
        val cloudEvent: CloudEvent = builder.build()
        val ttl = UCloudEvent.getTtl(cloudEvent)
        assertEquals(3, ttl)
    }

    @Test
    @DisplayName("Test extracting the ttl from a CloudEvent when the ttl does not exist.")
    fun test_extract_ttl_from_cloudevent_when_ttl_does_not_exist() {
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withoutExtension("ttl")
        val cloudEvent: CloudEvent = builder.build()
        val ttl: Int? = UCloudEvent.getTtl(cloudEvent)
        assertNull(ttl)
    }

    @Test
    @DisplayName("Test extracting the token from a CloudEvent when the token exists.")
    fun test_extract_token_from_cloudevent_when_token_exists() {
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest()
        val cloudEvent: CloudEvent = builder.build()
        val token: String? = UCloudEvent.getToken(cloudEvent)
        assertEquals("someOAuthToken", token)
    }

    @Test
    @DisplayName("Test extracting the token from a CloudEvent when the token does not exist.")
    fun test_extract_token_from_cloudevent_when_token_does_not_exist() {
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withoutExtension("token")
        val cloudEvent: CloudEvent = builder.build()
        val token: String? = UCloudEvent.getToken(cloudEvent)
        assertNull(token)
    }

    @Test
    @DisplayName("Test extracting the traceparent from a CloudEvent when the traceparent exists.")
    fun test_extract_traceparent_from_cloudevent_when_traceparent_exists() {
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest()
        val cloudEvent: CloudEvent = builder.build()
        val traceparent: String? = UCloudEvent.getTraceparent(cloudEvent)
        assertEquals("someTraceparent", traceparent)
    }

    @Test
    @DisplayName("Test extracting the traceparent from a CloudEvent when the traceparent does not exist.")
    fun test_extract_traceparent_from_cloudevent_when_traceparent_does_not_exist() {
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withoutExtension("traceparent")
        val cloudEvent: CloudEvent = builder.build()
        val traceparent: String? = UCloudEvent.getTraceparent(cloudEvent)
        assertNull(traceparent)
    }


    @Test
    @DisplayName("Test a CloudEvent has a platform communication error when the platform communication error exists.")
    fun test_cloudevent_has_platform_error_when_platform_error_exists() {
        val builder: CloudEventBuilder =
            buildBaseCloudEventBuilderForTest().withExtension("commstatus", UCode.ABORTED_VALUE)
        val cloudEvent: CloudEvent = builder.build()
        assertTrue(UCloudEvent.hasCommunicationStatusProblem(cloudEvent))
        assertEquals(UCode.ABORTED, UCloudEvent.getCommunicationStatus(cloudEvent))
    }

    @Test
    @DisplayName(
        "Test a CloudEvent has a platform communication error when the platform communication error does " + "not" + " exist."
    )
    fun test_cloudevent_has_platform_error_when_platform_error_does_not_exist() {
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest()
        val cloudEvent: CloudEvent = builder.build()
        assertFalse(UCloudEvent.hasCommunicationStatusProblem(cloudEvent))
        assertEquals(UCode.OK, UCloudEvent.getCommunicationStatus(cloudEvent))
    }

    @Test
    @DisplayName(
        "Test extracting the platform communication error from a CloudEvent when the platform communication " + "error exists but in the wrong format."
    )
    fun test_extract_platform_error_from_cloudevent_when_platform_error_exists_in_wrong_format() {
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withExtension("commstatus", "boom")
        val cloudEvent: CloudEvent = builder.build()
        assertFalse(UCloudEvent.hasCommunicationStatusProblem(cloudEvent))
        assertEquals(UCode.OK, UCloudEvent.getCommunicationStatus(cloudEvent))
    }

    @Test
    @DisplayName(
        "Test extracting the platform communication error from a CloudEvent when the platform communication " + "error exists."
    )
    fun test_extract_platform_error_from_cloudevent_when_platform_error_exists() {
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withExtension(
            "commstatus", UCode.INVALID_ARGUMENT_VALUE
        )
        val cloudEvent: CloudEvent = builder.build()
        val communicationStatus = UCloudEvent.getCommunicationStatus(cloudEvent)
        assertEquals(UCode.INVALID_ARGUMENT, communicationStatus)
    }

    @Test
    @DisplayName(
        "Test extracting the platform communication error from a CloudEvent when the platform communication " + "error does not exist."
    )
    fun test_extract_platform_error_from_cloudevent_when_platform_error_does_not_exist() {
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest()
        val cloudEvent: CloudEvent = builder.build()
        val communicationStatus = UCloudEvent.getCommunicationStatus(cloudEvent)
        assertEquals(UCode.OK, communicationStatus)
    }

    @Test
    @DisplayName("Test adding a platform communication error to an existing CloudEvent.")
    fun test_adding_platform_error_to_existing_cloudevent() {
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest()
        val cloudEvent: CloudEvent = builder.build()
        assertEquals(UCode.OK, UCloudEvent.getCommunicationStatus(cloudEvent))
        val cloudEvent1: CloudEvent = UCloudEvent.addCommunicationStatus(cloudEvent, UCode.DEADLINE_EXCEEDED_VALUE)
        assertEquals(UCode.DEADLINE_EXCEEDED, UCloudEvent.getCommunicationStatus(cloudEvent1))
        assertEquals(UCode.OK, UCloudEvent.getCommunicationStatus(cloudEvent))
    }

    @Test
    @DisplayName("Test adding an empty platform communication error to an existing CloudEvent, does nothing.")
    fun test_adding_empty_platform_error_to_existing_cloudevent() {
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest()
        val cloudEvent: CloudEvent = builder.build()
        assertEquals(UCode.OK, UCloudEvent.getCommunicationStatus(cloudEvent))
        val cloudEvent1: CloudEvent = UCloudEvent.addCommunicationStatus(cloudEvent, null)
        assertEquals(UCode.OK, UCloudEvent.getCommunicationStatus(cloudEvent))
        assertEquals(cloudEvent, cloudEvent1)
    }

    @Test
    @DisplayName("Test extracting creation timestamp from the CloudEvent UUID id when the id is not a UUIDV8.")
    fun test_extract_creation_timestamp_from_cloudevent_UUID_Id_when_not_a_UUIDV8_id() {
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest()
        val cloudEvent: CloudEvent = builder.build()
        val creationTimestamp = UCloudEvent.getCreationTimestamp(cloudEvent)
        assertNull(creationTimestamp)
    }

    @Test
    @DisplayName("Test extracting creation timestamp from the CloudEvent UUIDV8 id when the id is valid.")
    fun test_extract_creation_timestamp_from_cloudevent_UUIDV8_Id_when_UUIDV8_id_is_valid() {
        val uuid: UUID = UUIDV8()
        val strUuid = LongUuidSerializer.INSTANCE.serialize(uuid)

        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withId(strUuid)
        val cloudEvent: CloudEvent = builder.build()
        val creationTimestamp = UCloudEvent.getCreationTimestamp(cloudEvent) ?: fail()
        val now: OffsetDateTime = OffsetDateTime.now()
        val creationTimestampInstant: Instant = Instant.ofEpochMilli(creationTimestamp)
        val creationTimestampInstantEpochSecond: Long = creationTimestampInstant.epochSecond
        val nowTimeStampEpochSecond: Long = now.toEpochSecond()
        assertEquals(creationTimestampInstantEpochSecond, nowTimeStampEpochSecond)
    }

    @Test
    @DisplayName("Test if the CloudEvent is not expired using creation date when no ttl is configured.")
    fun test_cloudevent_is_not_expired_cd_when_no_ttl_configured() {
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withoutExtension("ttl")
        val cloudEvent: CloudEvent = builder.build()
        assertFalse(UCloudEvent.isExpiredByCloudEventCreationDate(cloudEvent))
    }

    @Test
    @DisplayName("Test if the CloudEvent is not expired using creation date when configured ttl is zero.")
    fun test_cloudevent_is_not_expired_cd_when_ttl_is_zero() {
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withExtension("ttl", 0)
        val cloudEvent: CloudEvent = builder.build()
        assertFalse(UCloudEvent.isExpiredByCloudEventCreationDate(cloudEvent))
    }

    @Test
    @DisplayName("Test if the CloudEvent is not expired using creation date when configured ttl is minus one.")
    fun test_cloudevent_is_not_expired_cd_when_ttl_is_minus_one() {
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withExtension("ttl", -1)
        val cloudEvent: CloudEvent = builder.build()
        assertFalse(UCloudEvent.isExpiredByCloudEventCreationDate(cloudEvent))
    }

    @Test
    @DisplayName("Test if the CloudEvent is not expired using creation date when configured ttl is 500 milliseconds " + "but no creation date.")
    fun test_cloudevent_is_not_expired_cd_when_ttl_3_mili_no_creation_date() {
        val protoPayload = buildProtoPayloadForTest()
        val builder: CloudEventBuilder = CloudEventBuilder.v1().withId("id").withType("pub.v1")
            .withSource(URI.create("/body.accss//door.front_left#Door")).withDataContentType(DATA_CONTENT_TYPE)
            .withDataSchema(URI.create(protoPayload.typeUrl)).withData(protoPayload.toByteArray())
            .withExtension("ttl", 500)
        val cloudEvent: CloudEvent = builder.build()
        assertFalse(UCloudEvent.isExpiredByCloudEventCreationDate(cloudEvent))
    }

    @Test
    @DisplayName("Test if the CloudEvent is not expired using creation date when configured ttl is 500 milliseconds " + "with creation date of now.")
    fun test_cloudevent_is_not_expired_cd_when_ttl_500_mili_with_creation_date_of_now() {
        val builder: CloudEventBuilder =
            buildBaseCloudEventBuilderForTest().withTime(OffsetDateTime.now()).withExtension("ttl", 500)
        val cloudEvent: CloudEvent = builder.build()
        assertFalse(UCloudEvent.isExpiredByCloudEventCreationDate(cloudEvent))
    }

    @Test
    @DisplayName(
        "Test if the CloudEvent is expired using creation date when configured ttl is 500 milliseconds with " + "creation date of yesterday."
    )
    fun test_cloudevent_is_expired_cd_when_ttl_500_mili_with_creation_date_of_yesterday() {
        val yesterday: OffsetDateTime = OffsetDateTime.now().minus(1, ChronoUnit.DAYS)
        val builder: CloudEventBuilder =
            buildBaseCloudEventBuilderForTest().withTime(yesterday).withExtension("ttl", 500)
        val cloudEvent: CloudEvent = builder.build()
        assertTrue(UCloudEvent.isExpiredByCloudEventCreationDate(cloudEvent))
    }

    @Test
    @DisplayName("Test if the CloudEvent is not expired using creation date when configured ttl is 500 milliseconds " + "with creation date of tomorrow.")
    fun test_cloudevent_is_not_expired_cd_when_ttl_500_mili_with_creation_date_of_tomorrow() {
        val tomorrow: OffsetDateTime = OffsetDateTime.now().plus(1, ChronoUnit.DAYS)
        val builder: CloudEventBuilder =
            buildBaseCloudEventBuilderForTest().withTime(tomorrow).withExtension("ttl", 500)
        val cloudEvent: CloudEvent = builder.build()
        assertFalse(UCloudEvent.isExpiredByCloudEventCreationDate(cloudEvent))
    }

    @Test
    @DisplayName("Test if the CloudEvent is not expired when no ttl is configured.")
    fun test_cloudevent_is_not_expired_when_no_ttl_configured() {
        val uuid: UUID = UUIDV8()
        val strUuid = LongUuidSerializer.INSTANCE.serialize(uuid)
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withoutExtension("ttl").withId(strUuid)
        val cloudEvent: CloudEvent = builder.build()
        assertFalse(UCloudEvent.isExpired(cloudEvent))
    }

    @Test
    @DisplayName("Test if the CloudEvent is not expired when configured ttl is zero.")
    fun test_cloudevent_is_not_expired_when_ttl_is_zero() {
        val uuid: UUID = UUIDV8()
        val strUuid = LongUuidSerializer.INSTANCE.serialize(uuid)
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withExtension("ttl", 0).withId(strUuid)
        val cloudEvent: CloudEvent = builder.build()
        assertFalse(UCloudEvent.isExpired(cloudEvent))
    }

    @Test
    @DisplayName("Test if the CloudEvent is not expired when configured ttl is minus one.")
    fun test_cloudevent_is_not_expired_when_ttl_is_minus_one() {
        val uuid: UUID = UUIDV8()
        val strUuid = LongUuidSerializer.INSTANCE.serialize(uuid)
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withExtension("ttl", -1).withId(strUuid)
        val cloudEvent: CloudEvent = builder.build()
        assertFalse(UCloudEvent.isExpired(cloudEvent))
    }

    @Test
    @DisplayName("Test if the CloudEvent is not expired when configured ttl is large number.")
    fun test_cloudevent_is_not_expired_when_ttl_is_large_number_mili() {
        val uuid: UUID = UUIDV8()
        val strUuid = LongUuidSerializer.INSTANCE.serialize(uuid)
        val builder: CloudEventBuilder =
            buildBaseCloudEventBuilderForTest().withExtension("ttl", Integer.MAX_VALUE).withId(strUuid)
        val cloudEvent: CloudEvent = builder.build()
        assertFalse(UCloudEvent.isExpired(cloudEvent))
    }

    @Test
    @DisplayName("Test if the CloudEvent is expired when configured ttl is 1 milliseconds.")
    @Throws(
        InterruptedException::class
    )
    fun test_cloudevent_is_expired_when_ttl_1_mili() {
        val uuid: UUID = UUIDV8()
        val strUuid = LongUuidSerializer.INSTANCE.serialize(uuid)
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withExtension("ttl", 1).withId(strUuid)
        val cloudEvent: CloudEvent = builder.build()
        Thread.sleep(800)
        assertTrue(UCloudEvent.isExpired(cloudEvent))
    }

    @Test
    @DisplayName("Test if the CloudEvent isExpired when passed invalid UUID")
    fun test_cloudevent_is_expired_for_invalid_uuid() {
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withExtension("ttl", 50000).withId("")
        val cloudEvent: CloudEvent = builder.build()
        assertFalse(UCloudEvent.isExpired(cloudEvent))
    }

    @Test
    @DisplayName("Test if the CloudEvent has a UUIDV8 id.")
    fun test_cloudevent_has_a_UUIDV8_id() {
        val uuid: UUID = UUIDV8()
        val strUuid = LongUuidSerializer.INSTANCE.serialize(uuid)
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withId(strUuid)
        val cloudEvent: CloudEvent = builder.build()
        assertTrue(UCloudEvent.isCloudEventId(cloudEvent))
    }

    @Test
    @DisplayName("Test if the CloudEvent does not have a UUIDV8 id.")
    fun test_cloudevent_does_not_have_a_UUIDV8_id() {
        val uuidJava: java.util.UUID = java.util.UUID.randomUUID()
        val uuid: UUID = uUID {
            msb = uuidJava.mostSignificantBits
            lsb = uuidJava.leastSignificantBits
        }
        val strUuid = LongUuidSerializer.INSTANCE.serialize(uuid)
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withExtension("ttl", 3).withId(strUuid)
        val cloudEvent: CloudEvent = builder.build()
        assertFalse(UCloudEvent.isCloudEventId(cloudEvent))
    }

    @Test
    @DisplayName("Test if the CloudEvent does not have a valid UUID id but some string")
    fun test_cloudevent_does_not_have_a_UUID_id_just_some_string() {
        val builder: CloudEventBuilder = buildBaseCloudEventBuilderForTest().withExtension("ttl", 3)
        val cloudEvent: CloudEvent = builder.build()
        assertFalse(UCloudEvent.isCloudEventId(cloudEvent))
    }

    @Test
    @DisplayName("Test extract payload from cloud event as Any protobuf object")
    fun test_extractPayload_from_cloud_event_as_any_proto_object() {
        val payloadForCloudEvent = buildProtoPayloadForTest()
        val cloudEventData: ByteArray = payloadForCloudEvent.toByteArray()
        val cloudEventBuilder: CloudEventBuilder = CloudEventBuilder.v1().withId("someId").withType("pub.v1")
            .withSource(URI.create("/body.access/1/door.front_left#Door")).withDataContentType(DATA_CONTENT_TYPE)
            .withDataSchema(URI.create(payloadForCloudEvent.typeUrl)).withData(cloudEventData)
        val cloudEvent: CloudEvent = cloudEventBuilder.build()
        val extracted: Any = UCloudEvent.getPayload(cloudEvent)
        assertEquals(payloadForCloudEvent, extracted)
    }

    @Test
    @DisplayName("Test extract payload from cloud event when payload is not an Any protobuf object")
    @Throws(
        InvalidProtocolBufferException::class
    )
    fun test_extractPayload_from_cloud_event_when_payload_is_not_an_any_proto_object() {
        val payloadForCloudEvent: io.cloudevents.v1.proto.CloudEvent = buildProtoPayloadForTest1()
        val cloudEventData: ByteArray = payloadForCloudEvent.toByteArray()
        val cloudEventBuilder: CloudEventBuilder = CloudEventBuilder.v1().withId("someId").withType("pub.v1")
            .withSource(URI.create("/body.access/1/door.front_left#Door")).withDataContentType(DATA_CONTENT_TYPE)
            .withDataSchema(URI.create("type.googleapis.com/io.cloudevents.v1.CloudEvent")).withData(cloudEventData)
        val cloudEvent: CloudEvent = cloudEventBuilder.build()
        val data: CloudEventData? = cloudEvent.data
        val dataAsAny: Any = Any.parseFrom(data?.toBytes())
        val extracted: Any = UCloudEvent.getPayload(cloudEvent)
        assertEquals(dataAsAny, extracted)
    }

    @Test
    @DisplayName("Test extract payload from cloud event when payload is a bad protobuf object")
    fun test_extractPayload_from_cloud_event_when_payload_is_bad_proto_object() {
        val cloudEventBuilder: CloudEventBuilder = CloudEventBuilder.v1().withId("someId").withType("pub.v1")
            .withSource(URI.create("/body.access/1/door.front_left#Door")).withDataContentType(DATA_CONTENT_TYPE)
            .withDataSchema(URI.create("type.googleapis.com/io.cloudevents.v1.CloudEvent"))
            .withData("<html><head></head><body><p>Hello</p></body></html>".toByteArray())
        val cloudEvent: CloudEvent = cloudEventBuilder.build()
        val extracted: Any = UCloudEvent.getPayload(cloudEvent)
        assertEquals(Any.getDefaultInstance(), extracted)
    }

    @Test
    @DisplayName("Test extract payload from cloud event as Any protobuf object when there is no data schema")
    fun test_extractPayload_from_cloud_event_as_any_proto_object_when_no_schema() {
        val payloadForCloudEvent = buildProtoPayloadForTest()
        val cloudEventData: ByteArray = payloadForCloudEvent.toByteArray()
        val cloudEventBuilder: CloudEventBuilder = CloudEventBuilder.v1().withId("someId").withType("pub.v1")
            .withSource(URI.create("/body.access/1/door.front_left#Door")).withDataContentType(DATA_CONTENT_TYPE)
            .withData(cloudEventData)
        val cloudEvent: CloudEvent = cloudEventBuilder.build()
        val extracted: Any = UCloudEvent.getPayload(cloudEvent)
        assertEquals(payloadForCloudEvent, extracted)
    }

    @Test
    @DisplayName("Test extract payload from cloud event as Any protobuf object when there is no data")
    fun test_extractPayload_from_cloud_event_as_any_proto_object_when_no_data() {
        val payloadForCloudEvent = buildProtoPayloadForTest()
        val cloudEventBuilder: CloudEventBuilder = CloudEventBuilder.v1().withId("someId").withType("pub.v1")
            .withSource(URI.create("/body.access/1/door.front_left#Door")).withDataContentType(DATA_CONTENT_TYPE)
            .withDataSchema(URI.create(payloadForCloudEvent.typeUrl))
        val cloudEvent: CloudEvent = cloudEventBuilder.build()
        val extracted: Any = UCloudEvent.getPayload(cloudEvent)
        assertEquals(Any.getDefaultInstance(), extracted)
    }

    @Test
    @DisplayName("Test unpack payload by class from cloud event as protobuf Message object")
    fun test_unpack_payload_by_class_from_cloud_event_proto_message_object() {
        val payloadForCloudEvent: Any = Any.pack(
            io.cloudevents.v1.proto.CloudEvent.newBuilder().setSpecVersion("1.0").setId("hello")
                .setSource("//VCU.MY_CAR_VIN/someService").setType("example.demo")
                .setProtoData(Any.newBuilder().build()).build()
        )
        val cloudEventData: ByteArray = payloadForCloudEvent.toByteArray()
        val cloudEventBuilder: CloudEventBuilder = CloudEventBuilder.v1().withId("someId").withType("pub.v1")
            .withSource(URI.create("/body.access/1/door.front_left#Door")).withDataContentType(DATA_CONTENT_TYPE)
            .withDataSchema(URI.create(payloadForCloudEvent.typeUrl)).withData(cloudEventData)
        val cloudEvent: CloudEvent = cloudEventBuilder.build()
        val unpackedCE: io.cloudevents.v1.proto.CloudEvent = UCloudEvent.unpack(
            cloudEvent, io.cloudevents.v1.proto.CloudEvent::class.java
        ) ?: fail()
        assertEquals("1.0", unpackedCE.specVersion)
        assertEquals("hello", unpackedCE.id)
        assertEquals("example.demo", unpackedCE.type)
        assertEquals("//VCU.MY_CAR_VIN/someService", unpackedCE.source)
    }

    @Test
    @DisplayName("Test unpack payload by class from cloud event when protobuf Message is not unpack-able")
    fun test_unpack_payload_by_class_from_cloud_event_proto_message_object_when_not_valid_getMessage() {
        val cloudEventBuilder: CloudEventBuilder = CloudEventBuilder.v1().withId("someId").withType("pub.v1")
            .withSource(URI.create("/body.access/1/door.front_left#Door")).withDataContentType(DATA_CONTENT_TYPE)
            .withDataSchema(URI.create("type.googleapis.com/io.cloudevents.v1.CloudEvent"))
            .withData("<html><head></head><body><p>Hello</p></body></html>".toByteArray())
        val cloudEvent: CloudEvent = cloudEventBuilder.build()
        val extracted: io.cloudevents.v1.proto.CloudEvent? = UCloudEvent.unpack(
            cloudEvent, io.cloudevents.v1.proto.CloudEvent::class.java
        )
        assertNull(extracted)
    }

    @Test
    @DisplayName("Test pretty printing a cloud event with a sink")
    fun test_pretty_printing_a_cloudevent_with_a_sink() {
        val sinkForTest = "//bo.cloud/petapp/1/rpc.response"
        val builder: CloudEventBuilder =
            buildBaseCloudEventBuilderForTest().withExtension("sink", URI.create(sinkForTest))
        val cloudEvent: CloudEvent = builder.build()
        val prettyPrint: String = UCloudEvent.toString(cloudEvent)
        val expected =
            ("CloudEvent{id='testme', source='/body.access//door.front_left#Door', " + "sink='//bo" + ".cloud/petapp/1/rpc.response', type='pub.v1'}")
        assertEquals(expected, prettyPrint)
    }

    @Test
    @DisplayName("Test pretty printing a cloud event without a sink")
    fun test_pretty_printing_a_cloudevent_without_a_sink() {
        val cloudEvent: CloudEvent = buildBaseCloudEventBuilderForTest().build()
        val prettyPrint: String = UCloudEvent.toString(cloudEvent)
        val expected = "CloudEvent{id='testme', source='/body.access//door.front_left#Door', type='pub.v1'}"
        assertEquals(expected, prettyPrint)
    }

    @Test
    @DisplayName("Test the type for a publish message type")
    fun test_type_for_publish() {
        val uCloudEventType = UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH)
        assertEquals("pub.v1", uCloudEventType)
    }


    @Test
    @DisplayName("Test the type for a request RPC message type")
    fun test_type_for_request() {
        val uCloudEventType = UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_REQUEST)
        assertEquals("req.v1", uCloudEventType)
    }

    @Test
    @DisplayName("Test the type for a response RPC message type")
    fun test_type_for_response() {
        val uCloudEventType = UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_RESPONSE)
        assertEquals("res.v1", uCloudEventType)
    }

    @Test
    @DisplayName("Test the type for a unspecified message type")
    fun test_parse_publish_event_type_from_string() {
        val uCloudEventType = UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_UNSPECIFIED)
        assertTrue(uCloudEventType.isBlank())
    }

    @Test
    fun test_to_message_with_valid_event() {
        // Additional attributes
        val uCloudEventAttributes = uCloudEventAttributes {
            priority = UPriority.UPRIORITY_CS2
            ttl = 3
        }

        // CloudEvent
        val cloudEvent = CloudEventFactory.publish(
            buildSourceForTest(), buildProtoPayloadForTest(), uCloudEventAttributes
        )

        val uMessage = toMessage(cloudEvent)
        assertNotNull(uMessage)
    }

    @Test
    fun test_from_message_with_valid_message() {
        // Additional attributes
        val uCloudEventAttributes = uCloudEventAttributes {
            priority = UPriority.UPRIORITY_CS2
            ttl = 3
        }

        // CloudEvent
        val cloudEvent = CloudEventFactory.publish(
            buildSourceForTest(), buildProtoPayloadForTest(), uCloudEventAttributes
        )
        val uMessage = toMessage(cloudEvent)
        assertNotNull(uMessage)
        val cloudEvent1 = UCloudEvent.fromMessage(uMessage)
        assertNotNull(cloudEvent1)
        assertEquals(cloudEvent, cloudEvent1)
    }

    @Test
    fun test_to_from_message_from_request_cloudevent() {
        // Additional attributes
        val uCloudEventAttributes = uCloudEventAttributes {
            priority = UPriority.UPRIORITY_CS2
            ttl = 3
            token = "someOAuthToken"
            traceparent = "someTraceparent"
        }

        // CloudEvent
        val cloudEvent = CloudEventFactory.request(
            buildSourceForTest(),
            ("//bo.cloud/petapp/1/rpc" + ".response").toUri(),
            buildProtoPayloadForTest(),
            uCloudEventAttributes
        )
        val result = toMessage(cloudEvent)
        assertNotNull(result)
        assertEquals(UCloudEvent.getTtl(cloudEvent), result.attributes.ttl)
        assertEquals(UCloudEvent.getToken(cloudEvent), result.attributes.getToken())
        assertEquals(
            UCloudEvent.getSink(cloudEvent)?.get(), LongUriSerializer.INSTANCE.serialize(result.attributes.sink)
        )
        assertEquals(UCloudEvent.getTraceparent(cloudEvent), result.attributes.traceparent)

        assertEquals(UCloudEvent.getPayload(cloudEvent).toByteString(), result.payload.getValue())
        assertEquals(
            UCloudEvent.getSource(cloudEvent), LongUriSerializer.INSTANCE.serialize(result.attributes.source)
        )
        assertEquals(UCloudEvent.getPriority(cloudEvent), getCePriority(result.attributes.priority))
        val cloudEvent1 = UCloudEvent.fromMessage(result)
        assertNotNull(cloudEvent1)
        assertEquals(cloudEvent, cloudEvent1)

    }

    @Test
    fun test_to_from_message_from_request_cloudevent_without_attributes() {
        // Additional attributes
        val uCloudEventAttributes = uCloudEventAttributes { }

        // CloudEvent
        val cloudEvent = CloudEventFactory.request(
            buildSourceForTest(), ("//bo.cloud/petapp/1/rpc.response").toUri(), buildProtoPayloadForTest(), uCloudEventAttributes
        )
        val result = toMessage(cloudEvent)
        assertNotNull(result)
        assertFalse(result.attributes.hasTtl())
        assertEquals(
            UCloudEvent.getSink(cloudEvent)?.get(), LongUriSerializer.INSTANCE.serialize(result.attributes.sink)
        )
        assertEquals(UCloudEvent.getPayload(cloudEvent).toByteString(), result.payload.getValue())
        assertEquals(
            UCloudEvent.getSource(cloudEvent), LongUriSerializer.INSTANCE.serialize(result.attributes.source)
        )
        assertEquals(result.attributes.getPriority().getNumber(), 0)
        assertEquals(cloudEvent, UCloudEvent.fromMessage(result))

    }

    @Test
    fun test_to_from_message_from_response_cloudevent() {
        // Additional attributes
        val uCloudEventAttributes = uCloudEventAttributes {
            priority = UPriority.UPRIORITY_CS2
            ttl = 3
        }

        // CloudEvent
        val cloudEvent = CloudEventFactory.response(
            buildSourceForTest(),
            ("//bo.cloud/petapp/1/rpc" + ".response").toUri(),
            LongUuidSerializer.INSTANCE.serialize(UUIDV8()),
            buildProtoPayloadForTest(),
            uCloudEventAttributes
        )
        val result = toMessage(cloudEvent)
        assertNotNull(result)
        assertEquals(
            UCloudEvent.getRequestId(cloudEvent), LongUuidSerializer.INSTANCE.serialize(result.attributes.reqid)
        )
        assertEquals(UCloudEvent.getTtl(cloudEvent), result.attributes.ttl)
        assertEquals(
            UCloudEvent.getSink(cloudEvent)?.get(), LongUriSerializer.INSTANCE.serialize(result.attributes.sink)
        )
        assertEquals(UCloudEvent.getPayload(cloudEvent).toByteString(), result.payload.getValue())
        assertEquals(
            UCloudEvent.getSource(cloudEvent), LongUriSerializer.INSTANCE.serialize(result.attributes.source)
        )
        assertEquals(UCloudEvent.getPriority(cloudEvent), getCePriority(result.attributes.priority))
        assertEquals(cloudEvent, UCloudEvent.fromMessage(result))

    }

    @Test
    fun test_umessage_has_platform_error_when_platform_error_exists() {
        // Additional attributes
        val uCloudEventAttributes = uCloudEventAttributes {
            priority = UPriority.UPRIORITY_CS2
            ttl = 3
        }

        val protoPayload = buildProtoPayloadForTest()
        val cloudEventBuilder = CloudEventFactory.buildBaseCloudEvent(
            LongUuidSerializer.INSTANCE.serialize(
                UUIDV8()
            ), buildSourceForTest(), protoPayload.toByteArray(), protoPayload.typeUrl, uCloudEventAttributes
        )
        cloudEventBuilder.withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH))
            .withExtension("commstatus", UCode.ABORTED_VALUE).withExtension("plevel", 2)
        val cloudEvent = cloudEventBuilder.build()
        val result = toMessage(cloudEvent)
        assertNotNull(result)
        assertEquals(UCode.ABORTED, UCloudEvent.getCommunicationStatus(cloudEvent))
        assertEquals(2, result.attributes.permissionLevel)

        val cloudEvent1 = UCloudEvent.fromMessage(result)
        assertEquals(cloudEvent, cloudEvent1)

    }

    @Test
    fun test_to_from_message_from_cloudevent_with_all_payload_formats() {
        // Additional attributes
        val uCloudEventAttributes = uCloudEventAttributes {
            priority = UPriority.UPRIORITY_CS2
            ttl = 3
        }
        val protoPayload = buildProtoPayloadForTest()
        val cloudEventBuilder = CloudEventFactory.buildBaseCloudEvent(
            LongUuidSerializer.INSTANCE.serialize(
                UUIDV8()
            ), buildSourceForTest(), protoPayload.toByteArray(), protoPayload.typeUrl, uCloudEventAttributes
        )
        cloudEventBuilder.withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH))

        val cloudEvent = cloudEventBuilder.build()
        var result = toMessage(cloudEvent)
        assertNotNull(result)
        assertEquals(UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY, result.payload.getFormat())

        val cloudEvent1 = UCloudEvent.fromMessage(result)
        assertEquals(cloudEvent, cloudEvent1)
        assertNull(cloudEvent1.dataContentType)

        val cloudEvent2 = cloudEventBuilder.withDataContentType("").build()
        result = toMessage(cloudEvent2)
        assertNotNull(result)
        assertEquals(UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY, result.payload.getFormat())
        val cloudEvent3 = UCloudEvent.fromMessage(result)
        assertNull(cloudEvent3.dataContentType)

        val cloudEvent4 = cloudEventBuilder.withDataContentType("application/json").build()
        result = toMessage(cloudEvent4)
        assertNotNull(result)
        assertEquals(UPayloadFormat.UPAYLOAD_FORMAT_JSON, result.payload.getFormat())
        val cloudEvent5 = UCloudEvent.fromMessage(result)
        assertEquals(cloudEvent4, cloudEvent5)
        assertEquals("application/json", cloudEvent5.dataContentType)

        val cloudEvent6 = cloudEventBuilder.withDataContentType("application/octet-stream").build()
        result = toMessage(cloudEvent6)
        assertNotNull(result)
        assertEquals(UPayloadFormat.UPAYLOAD_FORMAT_RAW, result.payload.getFormat())
        val cloudEvent7 = UCloudEvent.fromMessage(result)
        assertEquals(cloudEvent6, cloudEvent7)
        assertEquals("application/octet-stream", cloudEvent7.dataContentType)

        val cloudEvent8 = cloudEventBuilder.withDataContentType("text/plain").build()
        result = toMessage(cloudEvent8)
        assertNotNull(result)
        assertEquals(UPayloadFormat.UPAYLOAD_FORMAT_TEXT, result.payload.getFormat())
        val cloudEvent9 = UCloudEvent.fromMessage(result)
        assertEquals(cloudEvent8, cloudEvent9)
        assertEquals("text/plain", cloudEvent9.dataContentType)

        val cloudEvent10 = cloudEventBuilder.withDataContentType("application/x-someip").build()
        result = toMessage(cloudEvent10)
        assertNotNull(result)
        assertEquals(UPayloadFormat.UPAYLOAD_FORMAT_SOMEIP, result.payload.getFormat())
        val cloudEvent11 = UCloudEvent.fromMessage(result)
        assertEquals(cloudEvent10, cloudEvent11)
        assertEquals("application/x-someip", cloudEvent11.dataContentType)

        val cloudEvent12 = cloudEventBuilder.withDataContentType("application/x-someip_tlv").build()
        result = toMessage(cloudEvent12)
        assertNotNull(result)
        assertEquals(UPayloadFormat.UPAYLOAD_FORMAT_SOMEIP_TLV, result.payload.getFormat())
        val cloudEvent13 = UCloudEvent.fromMessage(result)
        assertEquals(cloudEvent12, cloudEvent13)
        assertEquals("application/x-someip_tlv", cloudEvent13.dataContentType)
    }

    @Test
    fun test_to_from_message_from_UCP_cloudevent() {
        // Additional attributes
        val uCloudEventAttributes = uCloudEventAttributes {
            ttl = 3
        }
        val protoPayload = buildProtoPayloadForTest()
        val cloudEventBuilder = CloudEventFactory.buildBaseCloudEvent(
            LongUuidSerializer.INSTANCE.serialize(
                UUIDV8()
            ), buildSourceForTest(), protoPayload.toByteArray(), protoPayload.typeUrl, uCloudEventAttributes
        )
        cloudEventBuilder.withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH))
        cloudEventBuilder.withExtension("priority", "CS4")
        val cloudEvent = cloudEventBuilder.build()

        val result = toMessage(cloudEvent)
        assertNotNull(result)
        assertEquals(getCePriority(UPriority.UPRIORITY_CS4), getCePriority(result.attributes.priority))
        val cloudEvent1 = UCloudEvent.fromMessage(result)
        assertEquals(getCePriority(UPriority.UPRIORITY_CS4), UCloudEvent.getPriority(cloudEvent1))
    }

    private fun buildSourceForTest(): Uri {
        // source
        val uUri: UUri = uUri {
            entity = uEntity { name = "body.access" }
            resource = uResource {
                name = "door"
                instance = "front_left"
                message = "Door"
            }
        }

        return LongUriSerializer.INSTANCE.serialize(uUri).toUri()
    }

    private fun buildBaseCloudEventBuilderForTest(): CloudEventBuilder {
        // source
        val source = buildSourceForTest()

        // fake payload
        val protoPayload = buildProtoPayloadForTest()

        // additional attributes
        val uCloudEventAttributes = uCloudEventAttributes {
            hash = "somehash"
            priority = UPriority.UPRIORITY_CS1
            ttl = 3
            token = "someOAuthToken"
            traceparent = "someTraceparent"
        }

        // build the cloud event
        val cloudEventBuilder: CloudEventBuilder = CloudEventFactory.buildBaseCloudEvent(
            "testme", source, protoPayload.toByteArray(), protoPayload.typeUrl, uCloudEventAttributes
        )
        cloudEventBuilder.withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH))
        return cloudEventBuilder
    }

    private fun buildProtoPayloadForTest(): Any {
        return Any.pack(buildProtoPayloadForTest1())
    }

    private fun buildProtoPayloadForTest1(): io.cloudevents.v1.proto.CloudEvent {
        return io.cloudevents.v1.proto.CloudEvent.newBuilder().setSpecVersion("1.0").setId("hello")
            .setSource("//VCU.MY_CAR_VIN/body.access//door.front_left#Door").setType("example.demo")
            .setProtoData(Any.newBuilder().build()).build()
    }

    companion object {
        private const val DATA_CONTENT_TYPE: String = CloudEventFactory.PROTOBUF_CONTENT_TYPE
    }
}